import pandas as pd
import requests
from urllib.parse import urlencode
import psycopg2
from pymongo import MongoClient
import urllib.parse
import datetime
from bson import ObjectId
import datetime
import json
import os
import google.generativeai as genai


# 결과 저장용 리스트
emog_dataset = []

# 지역 코드
region = [11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28, 29]

for r in region:
    request_url = f'https://mediboard.nemc.or.kr/api/v1/search/handy?searchCondition=regional&emogloca={r}'
    response = requests.get(request_url)

    if response.status_code == 200:
        data = response.json()
        emog_dataset.extend(data['result']['data'])
    else:
        print(f"지역 코드 {r} 요청 실패: {response.status_code}")

emog_dictionary = {item['emogCode']: item for item in emog_dataset}

# 인코딩이 필요한 사용자 이름과 비밀번호
raw_username = "lxFinal"
raw_password = "Child119@"

#  urllib.parse.quote_plus를 사용하여 인코딩
encoded_username = urllib.parse.quote_plus(raw_username)
encoded_password = urllib.parse.quote_plus(raw_password)

MONGO_URI = f"mongodb+srv://{encoded_username}:{encoded_password}@emergencyhospital.5qwiuzq.mongodb.net/"

# 3. 인코딩된 URI로 MongoClient 연결 시도
from pymongo import MongoClient

try:
    client = MongoClient(MONGO_URI)
    # 데이터베이스와 컬렉션 선택
    db = client['emergencyhospital']
    collection = db['emogs_collection']

    ## 🚨 1단계: 기존 데이터 모두 삭제 (초기화)
    print("⏳ 기존 데이터를 모두 삭제합니다...")
    # 필터 {}는 컬렉션의 모든 도큐먼트를 의미합니다.
    delete_result = collection.delete_many({})
    print(f"✅ 기존 데이터 {delete_result.deleted_count}개가 성공적으로 삭제되었습니다.")
    
    # insert_many()는 리스트 형태의 데이터를 여러 도큐먼트로 삽입합니다.
    result = collection.insert_many(emog_dataset)
    
    print(f"✅ {len(result.inserted_ids)}개의 도큐먼트가 성공적으로 삽입되었습니다.")
    print("✅ MongoDB 연결 성공!")
    
except Exception as e:
    print(f"❌ 연결 실패: {e}")



try:
    client = MongoClient(MONGO_URI)
    # 데이터베이스와 컬렉션 선택
    db = client['emergencyhospital']
    collection = db['emogs_collection']

    query_result = collection.find({})
    
    # find() 결과(Cursor 객체)를 리스트로 변환하여 개수를 확인하고 출력합니다.
    found_documents = list(query_result)
    
    print(f"{len(found_documents)}개의 도큐먼트가 검색되었습니다.")
        
    print("MongoDB 연결 성공 및 조회 완료!")
    
except Exception as e:
    print(f"연결 실패: {e}")

finally:
    # 클라이언트 연결은 반드시 닫아주는 것이 좋습니다.
    if 'client' in locals() and client:
        client.close()
        print("MongoDB 클라이언트 연결이 닫혔습니다.")


#현재 시간 (KST, 한국 표준시) 가져오기
# AI 판별 로직이 한국 시간을 기준으로 해야 하므로 KST(UTC+9)
kst_timezone = datetime.timezone(datetime.timedelta(hours=9))
current_time_kst = datetime.datetime.now(kst_timezone)

# API 전송을 위해 ISO 8601 표준 문자열 형식으로 변환
current_time_str = current_time_kst.isoformat()


# --- 1. 프롬프트 엔지니어링 함수 ---

def create_gemini_prompt(payload):
    """
    Gemini API에 전송할 프롬프트 문자열을 생성합니다.
    [소아 진료] 가능 여부에 초점을 맞춰 데이터를 분석하고 분류합니다.
    """
    
    payload_str = json.dumps(payload, indent=2, ensure_ascii=False)
    
    prompt = f"""
당신은 한국 응급실(ER) 데이터를 **'소아 환자(어린이, 아기)'의 진료 가능 여부**를 판단하는 AI입니다.
모든 분석은 **소아를 둔 부모의 관점**에서 이루어져야 합니다.
주어진 데이터를 분석하여 'state'를 0, 1, 2 중 하나로 분류하고, 'message'를 생성해야 합니다.

[분류 기준]
* state: 0 (소아 진료불가)
    * 'erMessages'나 'unavailableMessages'에 **'소아 진료 불가', '소아 응급실 중단/폐쇄', '응급실 폐쇄', '전반적 환자 수용 불가'** 등 소아 환자가 명백히 진료받을 수 없는 내용이 있을 경우.
* state: 1 (소아 조건부 진료)
    * **'소아 진료'에 구체적인 조건이나 제한**이 붙는 경우.
    * (예: '소아 야간 진료 불가', '○세 이상만 가능', '소아 정형외과/내과 불가', '소아는 사전 문의 필요' 등)
* state: 2 (소아 진료가능)
    * 'erMessages'와 'unavailableMessages'에 **소아 진료와 관련된 명시적인 제한 사항이 전혀 없는 경우.**
    * **[중요]** 성인 대상 진료(예: '성인 뇌수술', '사지접합')만 제한된 경우, 소아 진료와 무관하므로 'state: 2'로 분류합니다.
    * **[중요]** '119 사전 문의' 등 **소아를 특정하지 않는** 단순 안내 메시지만 있는 경우에도 'state: 2'로 분류합니다.

[출력 포맷]
* 반드시 다음의 JSON 형식을 따라야 합니다.
* JSON 외에 어떠한 설명도 추가하지 마세요.

{{
  "emogCode": "입력된 emogCode",
  "state": 0, 1, 또는 2,
  "message": "분류 결과에 따른 요약 메시지"
}}

[메시지 작성 가이드]
* state가 0: "소아 진료불가"
* state가 1: **'소아 진료'와 관련된 제한 사항**만 쉼표(,)로 구분합니다.
    * 불가능/제한 항목은 **'항목(X)'** (예: 소아 야간(X), 10세 미만(X))
    * 문의 필요 항목은 **'항목(문의)'** (예: 소아과(문의))
* state가 2: "소아 진료가능"

---
[분석할 데이터]
{payload_str}
---

[출력 JSON]
"""
    return prompt


def get_er_status_from_gemini(api_key, payload):
    """
    (예시) 실제로 Gemini API를 호출하고 결과를 파싱하는 함수
    """
    # 1. API 키 설정
    genai.configure(api_key=api_key) 
    # 참고: 실제 환경에서는 os.environ.get("GEMINI_API_KEY") 사용 권장
    
    # 2. 프롬프트 생성
    prompt = create_gemini_prompt(payload)
    model = genai.GenerativeModel('models/gemini-2.5-flash')
    
    try:
        #API 호출
        response = model.generate_content(
            prompt,
            # JSON 모드 강제
            generation_config=genai.types.GenerationConfig(
                response_mime_type="application/json" 
            ))
        result_json = json.loads(response.text)
        return result_json
    except (json.JSONDecodeError, AttributeError) as e:
        print(f"Error: Gemini API 응답 파싱 실패 - {e}")
        return None

# --- postgres db ---
DB_HOST = "34.50.41.93"
DB_NAME = "hospital"       # 데이터베이스 이름
DB_USER = "postgres"       # pgAdmin 로그인할 때 쓰는 사용자 이름
DB_PASS = "Child119@"  # pgAdmin 로그인할 때 쓰는 비밀번호
# ------------------------------------
conn = None
cur = None


for i in range(len(found_documents)):
    emog_code = found_documents[i].get('emogCode')
    er_messages = found_documents[i].get('erMessages', [])
    unavailable_messages = found_documents[i].get('unavailableMessages', [])
    
    api_request_payload = {
        "emogCode": emog_code,
        "erMessages": er_messages,
        "unavailableMessages": unavailable_messages,
        "currentTime": current_time_str
    }
    print(api_request_payload)
    result_emergency = get_er_status_from_gemini(api_key="AIzaSyA0r-m0evmY02vkptSA6qYm8JpT4afDa4c",payload=api_request_payload)
    try:
        # 데이터베이스 연결
        conn = psycopg2.connect(host=DB_HOST, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
        cur = conn.cursor()
        
        # 실행할 SQL 쿼리 정의
        # hpid를 기준으로 state message 업데이트합니다.
        sql = """
        UPDATE public.emergency
        SET state = %s, message = %s
        WHERE hpid = %s
        """
        update_param = []
        update_param.append((result_emergency['state'],result_emergency['message'],result_emergency['emogCode']))
        # 쿼리 일괄 실행
        # update_param 리스트에 있는 모든 튜플에 대해 UPDATE 쿼리를 실행합니다.
        cur.executemany(sql, update_param)
        
        # 트랜잭션 커밋 - 모든 변경 사항을 데이터베이스에 영구 저장
        conn.commit()
        # cur.rowcount는 executemany로 영향을 받은 총 행의 수를 반환합니다.
        print(f"총 {cur.rowcount}개 행이 성공적으로 업데이트되었습니다.")
    except (Exception, psycopg2.Error) as error:
        print("PostgreSQL 작업 중 오류 발생:", error)
        # 오류 발생 시, 이번 트랜잭션에서 실행된 모든 변경 사항을 취소합니다.
        if conn:
            conn.rollback()
            print("트랜잭션이 롤백되었습니다.")
    finally:
        # 리소스 정리
        if cur is not None:
            cur.close()
        if conn is not None:
            conn.close()
        print("PostgreSQL 연결이 닫혔습니다.")
    
    
    
