# emergency_python: 데이터 수집·가공·갱신

아이119의 병원·응급실 데이터를 만드는 Python 쪽 코드입니다. 크게 **① 병원 마스터 데이터 구축(1회성)** 과 **② 운영 자동화(주기 실행)** 로 나뉩니다.

## ① 병원 마스터 데이터 (노트북, 번호 순서대로 실행)

| 단계 | 파일 | 하는 일 |
|---|---|---|
| 01 | `icaretok_childhospital/01_collect_children_hospital.ipynb` | 국립중앙의료원 아이케어톡에서 소아청소년과 병원 목록·상세 수집 |
| 02 | `moonlightData/02_collect_moonlight.ipynb` | 달빛어린이병원 목록·상세 수집 |
| 03 | `03_geocode_vworld.ipynb` | 병원 엑셀(`data_hospital_original/`)의 주소를 VWorld API로 좌표 변환 (`VWORLD_API_KEY` 환경변수) |
| 03b | `03b_extract_geocode_failures.ipynb` | 좌표 변환에 실패한 행을 추출 (`data_hospital_geo_na/`) |
| 04 | `04_integrate_children_hospital.ipynb` | 정제된 4개 CSV를 병합하고 SHP로 저장 (`childhospitalShape/`) |
| 참고 | `moondata.ipynb` | 달빛어린이병원 1차 CSV → GeoJSON (`moonlight_gdf.geojson`) |
| 참고 | `../data/moonlight_dump_puppeteer.cjs` | e-gen 달빛어린이병원 정보 크롤링 (`cd data && npm install`) |

- 결과물은 PostgreSQL(PostGIS)에 **수동으로 적재**하고, GeoServer 작업공간 `hospital`에서 레이어(`moonlight_hospital`, `children_hospital`, `hospital`, `emergency`, `pharmacy`, `aed` 등)로 발행해 앱이 WFS로 읽습니다. 적재용 스크립트는 저장소에 없습니다.
- `data_hospital_geo_clear/`는 지오코딩 결과에서 결측을 정리한 파일이며, 정리 과정은 기록이 없습니다.
- 소아과 엑셀 4종(`child_c, child_gh, child_h, child_tgh`)은 같은 노트북 03을 파일명만 바꿔 실행한 것으로 보입니다.
- `emergency.geojson`은 응급실 초기 수집 결과 스냅샷이며, 생성 코드는 남아 있지 않습니다.
- 각 폴더의 `*.txt`는 테이블 생성 SQL 메모입니다.

## ② 운영 자동화 (`automation/`)

| 파일 | 하는 일 |
|---|---|
| `update_bed.py` | 국립중앙의료원 API에서 응급실 병상 수를 받아 PostgreSQL `emergency` 테이블 갱신 (과거 VM cron 5분 주기, `crontab.example` 참고) |
| `automation_service/` | FastAPI 서비스(Cloud Run용 Dockerfile 포함). `POST /run`으로 작업을 실행하고 `GET /logs/{job_id}`로 로그를 스트리밍. 관리자 화면의 "자동화 실행" 버튼이 호출 |
| `automation_service/app/update_message.py` | 응급실 정보를 MongoDB에 저장 → Gemini로 진료 상태(0 불가/1 조건부/2 가능)와 안내 메시지 생성 → PostgreSQL 갱신 |

## 설정

접속 정보는 환경변수로 주입합니다. `automation/.env.example`을 복사해 `.env`로 쓰세요. 비밀 값은 커밋하지 않습니다.
