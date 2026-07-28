package com.taesik.household;

import android.os.Bundle;
import android.webkit.WebSettings;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 시스템 글자 크기 설정에 영향받지 않도록 WebView 폰트 스케일 고정
        WebSettings settings = this.bridge.getWebView().getSettings();
        settings.setTextZoom(100);
    }
}
