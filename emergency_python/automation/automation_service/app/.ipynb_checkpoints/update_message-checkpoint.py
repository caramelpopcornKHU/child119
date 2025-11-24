import requests
import psycopg2
import psycopg2.extras # Batch 처리를 위해 추가
from pymongo import MongoClient
import urllib.parse
import datetime
import json
import os
import google.generativeai as genai
import logging # 로깅 모듈 추가
from concurrent.futures import ThreadPoolExecutor, as_completed # 병렬 처리를 위해 추가

# --- 1. 로깅 설정 (표준 로깅 시스템) ---
# [시간] - [로그 레벨] - [메시지] 형식으로 로그를 출력합니다.
logging.basicConfig(
    level=logging.INFO,
    format='[%(asctime)s] - %(levelname)s - %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)


# --- 2. 설정 및 민감 정보 관리 (환경 변수 사용) ---
# 서버 환경 변수로 설정하여 불러옵니다.

# MongoDB
RAW_USERNAME = os.environ.get("MONGO_USER", "xxxxxxxx")
RAW_PASSWORD = os.environ.get("MONGO_PASS", "xxxxxxxx@")
ENCODED_USERNAME = urllib.parse.quote_plus(RAW_USERNAME)
ENCODED_PASSWORD = urllib.parse.quote_plus(RAW_PASSWORD)
MONGO_URI = f"mongodb+srv://{ENCODED_USERNAME}:{ENCODED_PASSWORD}@emergencyhospital.5qwiuzq.mongodb.net/"

# PostgreSQL
DB_HOST = os.environ.get("PG_HOST", "xxxxxxxx")
DB_NAME = os.environ.get("PG_DBNAME", "xxxxxxxx")
DB_USER = os.environ.get("PG_USER", "xxxxxxxx")
DB_PASS = os.environ.get("PG_PASS", "xxxxxxxx")

# Gemini API
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "xxxxxxx")

# API 설정
NEMC_REGION_CODES = [11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28, 29]
MAX_API_WORKERS = 10 # 동시에 호출할 API 스레드 수 (조절 가능)


# --- 3. 프롬프트 및 API 호출 함수  ---
def create_gemini_prompt(payload):
    
    payload_str = json.dumps(payload, indent=2, ensure_ascii=False)
    
    prompt = f"""
당신은 한국 응급실(ER) 데이터를 분석하여 **'소아 환자(어린이, 아기)'를 둔 부모**에게 진료 가능 여부를 알려주는 AI입니다.
주어진 데이터를 분석하여 'state'를 분류하고, **의학 용어를 모르는 보호자가 즉시 이해할 수 있는 쉬운 말**로 'message'를 생성해야 합니다.

[분류 기준]
* state: 0 (소아 진료불가)
    * 데이터에 '소아 진료 불가', '응급실 폐쇄' 등 소아 진료가 명백히 불가능한 내용이 포함된 경우.
* state: 1 (소아 조건부 진료)
    * '소아 진료'가 가능하지만 구체적인 조건(시간, 연령, 증상 등)이나 제한이 있는 경우.
* state: 2 (소아 진료가능)
    * 소아 진료에 대한 제한 사항이 없는 경우.
    * **성인 대상**의 제한 사항(예: 성인 DI 불가)이나 단순 안내(예: 119 사전 문의)만 있는 경우 '진료가능'으로 봅니다.

[메시지 작성 및 용어 순화 가이드] (매우 중요)
메시지 작성 시 원문의 의학 약어/영어를 **반드시** 아래와 같이 쉬운 한국어로 번역하여 요약하세요.
* **DI** -> 약물중독
* **f/u (follow up)** -> 재진(이전 진료 환자)
* **Pulmo** -> 호흡기내과
* **Lac (Laceration)** -> 열상(찢어진 상처)
* **Fx (Fracture)** -> 골절
* **TA (Traffic Accident)** -> 교통사고
* **Suture** -> 봉합(꿰매는 처치)
* **Foreign body** -> 이물질 삼킴/박힘
* **Abd pain** -> 복통
* **Fever** -> 발열
* **Infect** -> 감염/격리

[출력 포맷]
반드시 다음 JSON 형식을 따르며, JSON 외의 텍스트는 출력하지 마세요.

{{
  "emogCode": "입력된 emogCode",
  "state": 0, 1, 또는 2,
  "message": "순화된 요약 메시지"
}}

[메시지 작성 상세 규칙]
* state가 0일 때: "소아 진료불가"
* state가 1일 때: 
    * **제한/불가 항목**: '항목(X)' (예: 소아 약물중독(X), 호흡기내과 재진만(O))
    * **문의 필요 항목**: '항목(문의)' (예: 소아과(문의), 열상(문의))
    * **[핵심]** 'DI 수용불가'라고 쓰지 말고 -> **'소아 약물중독(X)'**로 변환.
    * **[핵심]** 'pulmo f/u 환자 외 불가'라고 쓰지 말고 -> **'소아 호흡기내과 재진만(O)'**로 변환.
* state가 2일 때: "소아 진료가능"

---
[분석할 데이터]
{payload_str}
---

[출력 JSON]
"""
    return prompt

def get_er_status_from_gemini(api_key, payload):
    # 1. API 키 설정 (함수 호출 시마다 설정)
    genai.configure(api_key=api_key) 
    
    # 2. 프롬프트 생성 및 모델 초기화
    prompt = create_gemini_prompt(payload)
    model = genai.GenerativeModel('models/gemini-2.5-flash') # 최신 모델명 확인 권장
    
    try:
        # 3. API 호출
        response = model.generate_content(
            prompt,
            generation_config=genai.types.GenerationConfig(
                response_mime_type="application/json" 
            ))
        result_json = json.loads(response.text)
        return result_json
    except Exception as e:
        # API 호출 실패 시 에러 로그 기록
        logging.error(f"Gemini API 오류 (emogCode: {payload.get('emogCode')}): {e}", exc_info=True)
        # 실패 시 None 반환하여 후속 처리에서 제외
        return None

# --- 4. 로직 수행 함수 (기능별로 분리) ---

def fetch_nemc_data():
    """중앙응급의료센터 API에서 최신 병원 데이터를 가져옵니다."""
    logging.info("NEMC API로부터 데이터 수집을 시작합니다...")
    emog_dataset = []
    for r in NEMC_REGION_CODES:
        request_url = f'https://mediboard.nemc.or.kr/api/v1/search/handy?searchCondition=regional&emogloca={r}'
        try:
            response = requests.get(request_url, timeout=10) # 타임아웃 설정
            
            if response.status_code == 200:
                data = response.json()
                emog_dataset.extend(data['result']['data'])
            else:
                logging.warning(f"NEMC 지역 코드 {r} 요청 실패: {response.status_code}")
        except requests.RequestException as e:
            logging.error(f"NEMC 지역 코드 {r} 요청 중 예외 발생: {e}")
            
    logging.info(f"NEMC API로부터 총 {len(emog_dataset)}개의 병원 데이터를 수집했습니다.")
    return emog_dataset

def update_mongodb(emog_dataset):
    """MongoDB에 연결하여 기존 데이터를 삭제하고 새 데이터로 교체합니다."""
    if not emog_dataset:
        logging.warning("MongoDB에 저장할 데이터가 없습니다. 업데이트를 건너뜁니다.")
        return
        
    logging.info("MongoDB에 연결하여 데이터 업데이트를 시작합니다...")
    try:
        client = MongoClient(MONGO_URI)
        db = client['emergencyhospital']
        collection = db['emogs_collection']

        # 1단계: 기존 데이터 모두 삭제
        delete_result = collection.delete_many({})
        logging.info(f"MongoDB: 기존 데이터 {delete_result.deleted_count}개 삭제 완료.")
        
        # 2단계: 새 데이터 삽입
        result = collection.insert_many(emog_dataset)
        logging.info(f"MongoDB: 새 데이터 {len(result.inserted_ids)}개 삽입 완료.")
        
    except Exception as e:
        logging.error(f"MongoDB 작업 중 오류 발생: {e}", exc_info=True)
    finally:
        if 'client' in locals() and client:
            client.close()
            logging.info("MongoDB 연결이 닫혔습니다.")

def fetch_data_from_mongodb():
    """MongoDB에서 AI 분석에 사용할 데이터를 조회합니다."""
    logging.info("MongoDB에서 AI 분석 대상 데이터를 조회합니다...")
    found_documents = []
    try:
        client = MongoClient(MONGO_URI)
        db = client['emergencyhospital']
        collection = db['emogs_collection']
        
        # 필요한 필드만 가져오도록 프로젝션 설정 (최적화)
        query_result = collection.find({}, {"emogCode": 1, "erMessages": 1, "unavailableMessages": 1, "_id": 0})
        found_documents = list(query_result)
        logging.info(f"MongoDB에서 총 {len(found_documents)}개의 문서를 조회했습니다.")
        
    except Exception as e:
        logging.error(f"MongoDB 조회 중 오류 발생: {e}", exc_info=True)
    finally:
        if 'client' in locals() and client:
            client.close()
            logging.info("MongoDB 조회 연결이 닫혔습니다.")
    return found_documents

def process_single_document(doc, current_time_str):
    """단일 문서를 받아 Gemini API에 전송하고 결과를 반환합니다 (병렬 처리용 함수)."""
    emog_code = doc.get('emogCode')
    if not emog_code:
        return None

    api_request_payload = {
        "emogCode": emog_code,
        "erMessages": doc.get('erMessages', []),
        "unavailableMessages": doc.get('unavailableMessages', []),
        "currentTime": current_time_str
    }
    
    # Gemini API 호출
    result_emergency = get_er_status_from_gemini(GEMINI_API_KEY, api_request_payload)
    
    if result_emergency and 'state' in result_emergency:
        # PostgreSQL에 업데이트할 튜플 형식으로 반환
        return (
            result_emergency.get('state'),
            result_emergency.get('message'),
            result_emergency.get('emogCode') # emogCode는 hpid와 동일
        )
    else:
        logging.warning(f"emogCode {emog_code}에 대한 AI 분석 결과를 받지 못했습니다.")
        return None

def update_postgresql_batch(update_data_list):
    """PostgreSQL에 AI 분석 결과를 Batch로 일괄 업데이트합니다."""
    if not update_data_list:
        logging.warning("PostgreSQL에 업데이트할 데이터가 없습니다.")
        return

    logging.info(f"PostgreSQL에 총 {len(update_data_list)}개의 데이터 업데이트를 시작합니다...")
    
    conn = None
    cur = None
    
    # 실행할 SQL (hpid = emogCode)
    sql = """
    UPDATE public.emergency
    SET state = %s, message = %s
    WHERE hpid = %s
    """
    
    try:
        # DB에 한 번만 연결
        conn = psycopg2.connect(host=DB_HOST, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
        cur = conn.cursor()
        
        # execute_batch를 사용하여 대량의 데이터를 효율적으로 업데이트
        psycopg2.extras.execute_batch(cur, sql, update_data_list)
        
        # 모든 작업 완료 후 한 번만 커밋
        conn.commit()
        
        logging.info(f"PostgreSQL: 총 {cur.rowcount}개 행이 성공적으로 업데이트되었습니다.")
        
    except (Exception, psycopg2.Error) as error:
        logging.error(f"PostgreSQL Batch 작업 중 오류 발생: {error}", exc_info=True)
        if conn:
            conn.rollback()
            logging.warning("트랜잭션이 롤백되었습니다.")
    finally:
        # 모든 작업 완료 후 한 번만 연결 해제
        if cur:
            cur.close()
        if conn:
            conn.close()
        logging.info("PostgreSQL 연결이 닫혔습니다.")


# --- 5. 메인 실행 로직 ---
def main():
    logging.info("=== 응급실 데이터 처리 배치 작업을 시작합니다 ===")
    
    # 1. NEMC API에서 원본 데이터 수집
    emog_dataset = fetch_nemc_data()
    
    # 2. MongoDB에 최신 데이터로 업데이트 (초기화 및 삽입)
    update_mongodb(emog_dataset)
    
    # 3. AI 분석을 위해 MongoDB에서 데이터 다시 조회
    documents_to_process = fetch_data_from_mongodb()
    
    if not documents_to_process:
        logging.info("처리할 데이터가 없습니다. 작업을 종료합니다.")
        return

    # 4. Gemini API 병렬 처리
    logging.info(f"총 {len(documents_to_process)}개의 문서에 대해 Gemini API 병렬 분석을 시작합니다 (Workers: {MAX_API_WORKERS})...")
    
    # KST 시간 설정
    kst_timezone = datetime.timezone(datetime.timedelta(hours=9))
    current_time_str = datetime.datetime.now(kst_timezone).isoformat()
    
    # PostgreSQL에 일괄 업데이트할 데이터를 담을 리스트
    postgres_update_list = []
    
    # ThreadPoolExecutor를 사용하여 API 호출 병렬 수행
    with ThreadPoolExecutor(max_workers=MAX_API_WORKERS) as executor:
        # 퓨처(Future) 객체 딕셔너리 생성 (어떤 emogCode가 실패했는지 추적하기 위함)
        future_to_doc = {
            executor.submit(process_single_document, doc, current_time_str): doc
            for doc in documents_to_process
        }
        
        completed_count = 0
        for future in as_completed(future_to_doc):
            doc = future_to_doc[future]
            emog_code = doc.get('emogCode')
            
            try:
                # API 호출 결과 (성공 시 튜플, 실패 시 None)
                result_tuple = future.result() 
                
                if result_tuple:
                    postgres_update_list.append(result_tuple)
                
                completed_count += 1
                if completed_count % 50 == 0: # 50개 처리마다 로그 출력
                    logging.info(f"AI 분석 진행 중... ({completed_count}/{len(documents_to_process)})")

            except Exception as e:
                logging.error(f"emogCode {emog_code} 처리 중 예외 발생: {e}", exc_info=True)

    logging.info(f"Gemini API 분석 완료. (총 {len(postgres_update_list)}개 결과 수집)")

    # 5. PostgreSQL에 AI 분석 결과 일괄 업데이트
    update_postgresql_batch(postgres_update_list)
    
    logging.info("=== 모든 배치 작업이 성공적으로 완료되었습니다 ===")


if __name__ == "__main__":
    
    main()