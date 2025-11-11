# 1. 베이스 이미지 선택 (Python 3.9 슬림 버전)
FROM python:3.9-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. requirements.txt 파일 먼저 복사
COPY requirements.txt .

# 4. pip 패키지 설치
RUN pip install --no-cache-dir -r requirements.txt

# 5. app 폴더의 모든 코드를 /app 디렉토리로 복사
#    (로컬의 app/main.py -> 컨테이너의 /app/main.py)
#    (로컬의 app/update_message.py -> 컨테이너의 /app/update_message.py)
COPY ./app/ .

# 6. (중요) FastAPI 서버 실행
#    Cloud Run이 8080 포트를 기본으로 사용합니다.
#    main:app -> main.py 파일의 app 객체를 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]