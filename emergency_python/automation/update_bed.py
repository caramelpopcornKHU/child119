import requests
from urllib.parse import urlencode
import psycopg2

# 결과 저장용 리스트
rows = []

region = [11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28, 29]

for r in region:
    request_url = f'https://mediboard.nemc.or.kr/api/v1/search/handy?searchCondition=regional&emogloca={r}'
    response = requests.get(request_url)

    if response.status_code == 200:
        data = response.json()
        for item in data['result']['data']:
            rows.append({
                'emogCode': item['emogCode'],
                'childEmergencyAvailable': item['childEmergencyAvailable'],
                'childEmergencyTotal': item['childEmergencyTotal'],
            })
    else:
        print(f"지역 코드 {r} 요청 실패: {response.status_code}")

# ------------------------------------
# 하드코딩된 값 대신, os.environ.get()을 사용해 환경 변수에서 값을 가져옴
DB_HOST = os.environ.get('DB_HOST')
DB_NAME = os.environ.get('DB_NAME')
DB_USER = os.environ.get('DB_USER')
DB_PASS = os.environ.get('DB_PASS')
# ------------------------------------

conn = None
cur = None

try:
    # 3. 데이터베이스 연결
    conn = psycopg2.connect(host=DB_HOST, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
    cur = conn.cursor()

    # 4. 실행할 SQL 쿼리 정의
    # hpid를 기준으로 bedavail과 bedtotal을 업데이트합니다.
    sql = """
    UPDATE public.emergency
    SET bed_avail = %s, bed_total = %s
    WHERE hpid = %s
    """

    # 5. executemany에 맞게 데이터 리스트 준비 (튜플의 리스트)
    data_to_update = []
    for row in rows:
        #
        # 데이터 정제:
        # 예시 데이터 {'emogCode': 'A1100006', 'childEmergencyAvailable': ' '} 처럼
        # 비어 있거나 공백만 있는 문자열이 DB의 숫자 컬럼에 들어가면 오류가 발생합니다.
        # 이런 경우 파이썬의 None으로 변경하여 DB에 NULL 값이 들어가도록 합니다.
        #
        
        # 'childEmergencyAvailable' 값 정제
        avail = row['childEmergencyAvailable']
        if isinstance(avail, str) and not avail.strip():
            avail = None  # 비어있는 문자열이면 None (NULL)으로 처리
        
        # 'childEmergencyTotal' 값 정제
        total = row['childEmergencyTotal']
        if isinstance(total, str) and not total.strip():
            total = None  # 비어있는 문자열이면 None (NULL)으로 처리

        # (SET bedavail, SET bedtotal, WHERE hpid) 순서로 튜플 생성
        data_to_update.append((avail, total, row['emogCode']))

    # 6. 쿼리 일괄 실행
    # data_to_update 리스트에 있는 모든 튜플에 대해 UPDATE 쿼리를 실행합니다.
    cur.executemany(sql, data_to_update)

    # 7. 트랜잭션 커밋 (중요!)
    # 모든 변경 사항을 데이터베이스에 영구 저장합니다.
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
    # 8. 리소스 정리
    if cur is not None:
        cur.close()
    if conn is not None:
        conn.close()
    print("PostgreSQL 연결이 닫혔습니다.")