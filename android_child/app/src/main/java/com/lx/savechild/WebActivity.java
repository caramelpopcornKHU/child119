package com.lx.savechild;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface; // 1. 임포트
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        WebView webview = findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true); // 2. JS는 필수!

        // 3. 웹뷰에 "AndroidBridge"라는 이름으로 인터페이스 주입
        webview.addJavascriptInterface(new WebViewBridge(this), "AndroidBridge");

        //webview.loadUrl("http://172.168.10.30:5173/");
        webview.loadUrl("http://172.168.10.12:5173/");
    }

    /**
     * 4. 웹에서 호출할 함수들을 정의하는 브릿지 클래스
     */
    public class WebViewBridge {
        private Context context;

        WebViewBridge(Context context) {
            this.context = context;
        }

        /**
         * 5. @JavascriptInterface 어노테이션이 있어야만
         * 웹에서 이 함수를 호출할 수 있습니다.
         */
        @JavascriptInterface
        public void startNavigation() {
            // 이 코드는 안드로이드에서 실행됩니다.
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }

        // 여기에 필요한 다른 함수들(ex: showToast(String msg))을 추가할 수 있습니다.
    }
}