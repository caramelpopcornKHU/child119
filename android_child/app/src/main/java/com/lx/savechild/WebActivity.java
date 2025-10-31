package com.lx.savechild;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest; // 1. 임포트 추가
import android.webkit.WebView;
import android.webkit.WebViewClient; // 1. 임포트 추가

import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity {

    // 2. 멤버 변수로 선언 (생명주기 관리를 위해)
    private WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        // 3. (필수) WebViewClient 설정: 외부 브라우저로 열리는 현상 방지
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true; // true를 반환하면 WebView가 URL을 직접 처리
            }
        });

        // 4. 리팩토링된 브릿지 주입 (ApplicationContext 전달)
        webview.addJavascriptInterface(new WebViewBridge(getApplicationContext()), "AndroidBridge");

        // 5. 프로토콜을 명시한 URL 로드

        //webview.loadUrl("api.child119.com");
        webview.loadUrl("https://child119-5fe1f.web.app/");

    }

    // 6. WebView 생명주기 관리 (메모리 누수 방지)
    @Override
    protected void onPause() {
        super.onPause();
        webview.onPause(); // WebView 일시정지
    }

    @Override
    protected void onResume() {
        super.onResume();
        webview.onResume(); // WebView 재개
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // onDestroy에서 WebView를 제거해야 메모리 누수가 발생하지 않습니다.
        if (webview != null) {
            webview.destroy();
            webview = null; // 참조 해제
        }
    }

    /**
     * 7. 'static' 내부 클래스로 변경 (WebActivity에 대한 암시적 참조 방지)
     */
    private static class WebViewBridge {
        // 8. ApplicationContext 사용
        private Context appContext;

        WebViewBridge(Context context) {
            // Activity Context 대신 Application Context를 저장
            this.appContext = context.getApplicationContext();
        }

        @JavascriptInterface
        public void startNavigation() {
            // 9. ApplicationContext로 Activity 시작
            Intent intent = new Intent(appContext, MainActivity.class);

            // 10. ApplicationContext에서 Activity를 시작할 때는 FLAG_ACTIVITY_NEW_TASK 필수
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            appContext.startActivity(intent);
        }
    }
}