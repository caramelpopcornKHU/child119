import json
import psycopg2 # 방금 설치한 라이브러리

# --- 1. 본인 환경에 맞게 수정하세요 ---
DB_HOST = "database-child119.cz8448ogme18.ap-southeast-2.rds.amazonaws.com"
DB_NAME = "postgres"       # 아까 확인한 데이터베이스 이름
DB_USER = "postgres"       # pgAdmin 로그인할 때 쓰는 사용자 이름
DB_PASS = "Child119"  # pgAdmin 로그인할 때 쓰는 비밀번호
# ------------------------------------

try:
    # 2. GeoJSON 파일 열기
    with open('moonlight_gdf.geojson', 'r', encoding='utf-8') as f:
        data = json.load(f)

    # 3. 데이터베이스 연결
    conn = psycopg2.connect(host=DB_HOST, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
    cur = conn.cursor()

    # 4. 데이터를 삽입할 SQL 쿼리
    insert_query = """
    INSERT INTO test.moonlight_hospitals 
        (emogcode, regionCode, name, address, tel, gc_type, clean_address, latitude, longitude)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
    ON CONFLICT (emogcode) DO NOTHING; -- 혹시 스크립트를 여러 번 실행해도 중복되지 않도록 함
    """

    # 5. GeoJSON의 'features'를 하나씩 돌면서 데이터 추출 및 삽입
    count = 0
    for feature in data['features']:
        props = feature['properties']
        
        # 'properties' 안에 있는 위도, 경도를 사용 [cite: 1]
        lat = props.get('latitude')
        lon = props.get('longitude')

        cur.execute(insert_query, (
            props.get('emogcode'),
            props.get('regionCode'),
            props.get('name'),
            props.get('address'),
            props.get('tel'),
            props.get('_GC_TYPE'),
            props.get('_CLEANADDR'),
            lat,
            lon
        ))
        count += 1

    # 6. 변경사항 저장(커밋) 및 연결 종료
    conn.commit()
    cur.close()
    conn.close()

    print(f"성공! 총 {count}개의 병원 데이터를 'moonlight_hospitals' 테이블에 삽입했습니다.")

except FileNotFoundError:
    print("오류: 'moonlight_gdf.geojson' 파일을 찾을 수 없습니다.")
except Exception as e:
    print(f"오류가 발생했습니다: {e}")