import json
import psycopg2 

# --- 1. 본인 환경에 맞게 수정하세요 ---
# 호스트 이름은 아까 오류 메시지에서 가져왔습니다.
DB_HOST = "database-child119.cz8448ogme18.ap-southeast-2.rds.amazonaws.com"
DB_NAME = "postgres"       # 이전에 "postgres"로 확인했습니다.
DB_USER = "postgres"       
DB_PASS = "Child119"  # 여기에 본인 RDS 비밀번호를 입력하세요.
# ------------------------------------

def convert_to_int(value):
    """
    JSON 안의 값이 문자열("3")이거나 null일 수 있으므로,
    정수(int)로 안전하게 변환합니다.
    """
    if value is None or value == "":
        return None
    try:
        return int(value)
    except ValueError:
        return None # 숫자로 변환 못하는 경우

try:
    # 2. GeoJSON 파일 열기
    with open('emergency.geojson', 'r', encoding='utf-8') as f:
        data = json.load(f)

    # 3. 데이터베이스 연결
    conn = psycopg2.connect(host=DB_HOST, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
    cur = conn.cursor()

    # 4. 데이터를 삽입할 SQL 쿼리
    insert_query = """
    INSERT INTO test.emergency_rooms 
        (emogCode, emergencyRoomNickname, emergencyRoomName, address, latitude, longitude, 
         childEmergencyAvailable, childEmergencyTotal, availabilityStatus)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    ON CONFLICT (emogCode) DO NOTHING; -- 기본 키(emogCode) 중복 시 무시
    """

    # 5. GeoJSON의 'features'를 하나씩 돌면서 데이터 추출 및 삽입
    count = 0
    for feature in data['features']:
        props = feature['properties']
        
        # 'child...' 필드는 문자열이거나 null이므로 convert_to_int 함수로 처리
        child_avail = convert_to_int(props.get('childEmergencyAvailable'))
        child_total = convert_to_int(props.get('childEmergencyTotal'))

        cur.execute(insert_query, (
            props.get('emogCode'),
            props.get('emergencyRoomNickname'),
            props.get('emergencyRoomName'),
            props.get('address'),
            props.get('latitude'),  # 이 필드들은 이미 숫자입니다.
            props.get('longitude'), # 이 필드들은 이미 숫자입니다.
            child_avail,
            child_total,
            props.get('availabilityStatus') # 이 필드는 이미 숫자입니다.
        ))
        count += 1

    # 6. 변경사항 저장(커밋) 및 연결 종료
    conn.commit()
    cur.close()
    conn.close()

    print(f"성공! 총 {count}개의 응급실 데이터를 'emergency_rooms' 테이블에 삽입했습니다.")

except FileNotFoundError:
    print("오류: 'emergency.geojson' 파일을 찾을 수 없습니다.")
except Exception as e:
    print(f"오류가 발생했습니다: {e}")