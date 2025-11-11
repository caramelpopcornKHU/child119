import google.generativeai as genai
import os

# --- [중요] ---
# 사용 중인 API 키로 설정해야 합니다.
# (환경 변수 또는 직접 입력)
#my_api_key = os.environ.get("GEMINI_API_KEY") 
my_api_key = "AIzaSyA0r-m0evmY02vkptSA6qYm8JpT4afDa4c"

if not my_api_key:
    print("Error: GEMINI_API_KEY를 설정해주세요.")
else:
    genai.configure(api_key=my_api_key)

    print("--- 현재 API 키로 사용 가능한 모델 목록 ---")
    
    try:
        # generateContent (채팅/프롬프트)를 지원하는 모델만 필터링
        for m in genai.list_models():
          if 'generateContent' in m.supported_generation_methods:
            print(f"- {m.name}")
            
    except Exception as e:
        print(f"모델 목록을 가져오는 중 오류 발생: {e}")