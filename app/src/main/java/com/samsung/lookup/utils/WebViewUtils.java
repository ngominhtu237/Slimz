package com.samsung.lookup.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.samsung.lookup.IClickWordCallback;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tu.nm1 on 16,December,2020
 */
public class WebViewUtils {
    @SuppressLint("SetJavaScriptEnabled")
    public static WebView addScript(final Context activity, final WebView webView, final IClickWordCallback cb) {
        // Enable Javascript
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.addJavascriptInterface(new Object()
        {
            @JavascriptInterface           // For API 17+
            public void performClick(String openedWord) {
                // Activity
                cb.open(openedWord);

                // FRAGMENT
//                databaseAccess = DatabaseAccess.getInstance(getActivity());
//                databaseAccess.open();
//                Word wordSend = databaseAccess.getWord(openedWord);
//                EngVietFragment mEngVietFragment = new EngVietFragment();
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("wordSendSelf",wordSend);
//                mEngVietFragment.setArguments(bundle);
//                getChildFragmentManager()
//                        .beginTransaction()
//                        .add(R.id.fragment_container, mEngVietFragment)
//                        .addToBackStack("B")
//                        .commit();
            }
        }, "ok");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                injectCSS(activity, webView);
                injectScriptFile(activity, webView);

//                tvWordDetails.loadUrl(
//                        "javascript:(function() { "
//                                + "var element = document.getElementById('arrow');"
//                                + "element.innerHTML = 'New text!';"
//                                + "})()");
                webView.loadUrl("javascript:addArrow()");
                webView.loadUrl("javascript:formatTitle()");
                webView.loadUrl("javascript:deleteColon()");
                webView.loadUrl("javascript:underScore()");
                super.onPageFinished(view, url);
            }
        });
        return webView;
    }

    // Inject CSS method: read style.css from assets folder
    // Append stylesheet to document head
    private static void injectCSS(Context activity, WebView webView) {
        try {
            InputStream inputStream = activity.getAssets().open("style.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            webView.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void injectScriptFile(Context activity, WebView view) {
        InputStream input;
        try {
            input = activity.getAssets().open("script.js");
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            // String-ify the script byte-array using BASE64 encoding !!!
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "script.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(script)" +
                    "})()");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
