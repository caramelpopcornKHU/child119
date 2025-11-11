# app/main.py
import uuid
import threading
import subprocess
import time
from typing import Dict, List

from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import StreamingResponse, JSONResponse

app = FastAPI(
    title="Child119 Automation Service",
    description="아이119 관리자용 크롤링 자동화 + 로그 스트리밍 서비스",
)

# 작업 상태/로그 저장용 in-memory 구조
jobs: Dict[str, str] = {}         # job_id -> 'running' | 'done' | 'error'
log_buffers: Dict[str, List[str]] = {}  # job_id -> [log lines]


def run_automation_job(job_id: str):
    """
    별도 쓰레드에서 update_message.py를 서브프로세스로 실행하고,
    stdout 로그를 한 줄씩 읽어 log_buffers[job_id]에 쌓는다.
    """
    jobs[job_id] = "running"
    log_buffers[job_id] = []

    # python -u : 버퍼링 없이 바로바로 로그가 나오도록
    cmd = ["python", "-u", "update_message.py"]

    try:
        process = subprocess.Popen(
            cmd,
            cwd="app",  # 컨테이너 내부에서 update_message.py가 있는 디렉토리
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
        )

        # stdout 한 줄씩 읽어서 버퍼에 추가
        for line in process.stdout:
            line = line.rstrip("\n")
            if line:
                log_buffers[job_id].append(line)

        process.wait()

        if process.returncode == 0:
            jobs[job_id] = "done"
            log_buffers[job_id].append("✅ 작업이 정상 종료되었습니다.")
        else:
            jobs[job_id] = "error"
            log_buffers[job_id].append(
                f"❌ 프로세스가 오류 코드 {process.returncode}로 종료되었습니다."
            )

    except Exception as e:
        jobs[job_id] = "error"
        log_buffers[job_id].append(f"❌ 예외 발생: {e}")


@app.post("/run")
def start_run():
    """
    자동화 작업 실행 요청.
    - 새 job_id 생성
    - 백그라운드 쓰레드에서 update_message.py 실행 시작
    - jobId를 Vue 관리자 화면에 반환
    """
    job_id = str(uuid.uuid4())

    # 쓰레드로 작업 실행 (FastAPI 메인 스레드를 막지 않게)
    t = threading.Thread(target=run_automation_job, args=(job_id,), daemon=True)
    t.start()

    return JSONResponse({"jobId": job_id})


@app.get("/logs/{job_id}")
async def stream_logs(job_id: str, request: Request):
    """
    SSE 형식으로 로그 스트리밍.
    Vue에서는 EventSource로 이 엔드포인트를 구독하면 됨.
    """

    if job_id not in jobs:
        raise HTTPException(status_code=404, detail="Unknown job_id")

    async def event_generator():
        last_index = 0

        while True:
            # 클라이언트가 연결을 끊었는지 체크
            if await request.is_disconnected():
                break

            logs = log_buffers.get(job_id, [])
            if last_index < len(logs):
                # 새로 추가된 로그만 보내기
                for line in logs[last_index:]:
                    yield f"data: {line}\n\n"
                last_index = len(logs)

            # 작업이 완료/에러 상태면 한 텀 더 보내고 종료
            if jobs.get(job_id) in ("done", "error"):
                await asyncio.sleep(0.3)
                break

            await asyncio.sleep(0.5)

    import asyncio
    return StreamingResponse(event_generator(), media_type="text/event-stream")
