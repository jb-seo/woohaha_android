package com.wooshin.woohaha;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    private NfcAdapter nfcAdapter;
    private WebView webview;
    private WebViewClient webviewClient;

    private class AndroidBridge {
        @SuppressLint("JavascriptInterface")
        public void setMessage(final String arg) {

        }
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview = (WebView) findViewById(R.id.webview);
        webviewClient = new WebViewClient();
        webview.setWebViewClient(webviewClient);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webview.loadUrl("http://3.34.91.181:8080/");
        webview.addJavascriptInterface(new AndroidBridge(), "android");
        webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        int flags = NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        flags |= NfcAdapter.FLAG_READER_NFC_A;

        Bundle opts = new Bundle();
        opts.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        nfcAdapter.enableReaderMode(this, new NfcAdapter.ReaderCallback() {
            private String serial;
            @Override
            public void onTagDiscovered(Tag tag) {
                serial = "";
                byte []tagId = tag.getId();
                for (int i = 0; i < tagId.length; i++) {
                    String x = Integer.toHexString(((int) tagId[i] & 0xff));
                    if (x.length() == 1) {
                        x = '0' + x;
                    }
                    serial += x;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("Tag discovered", serial);
                        webview.loadUrl("javascript:setSerial('"+serial+"')");
                    }
                });
            }
        }, flags, opts);
    }
}
