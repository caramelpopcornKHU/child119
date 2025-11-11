# Dockerfile
FROM python:3.12-slim

# 시스템 패키지 (psycopg2 사용 시 빌드 도구 필요할 수 있음)
RUN apt-get update && apt-get install -y \
    build-essential \
    libpq-dev \
  && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리
WORKDIR /app

# 파이썬 의존성 먼저 복사 + 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY app ./app

# (선택) 환경변수로 FastAPI 포트 지정
ENV PORT=8000

# 컨테이너에서 노출할 포트
EXPOSE 8000

# uvicorn으로 FastAPI 실행
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
