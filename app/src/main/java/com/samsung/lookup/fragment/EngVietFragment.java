package com.samsung.lookup.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.samsung.lookup.R;
import com.samsung.lookup.WordDetailsActivity;
import com.samsung.lookup.data.DatabaseAccess;
import com.samsung.lookup.model.Word;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;

/**
 * A quick_translate {@link Fragment} subclass.
 */
public class EngVietFragment extends Fragment {

    private WebView mWebViewDetails;
    private DatabaseAccess databaseAccess;
    private String receivedWordName;
    private Word word;

    public EngVietFragment() {
        databaseAccess = DatabaseAccess.getInstance(getActivity());
        databaseAccess.open();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("JavascriptInterface")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_eng_viet, container, false);
        mWebViewDetails = v.findViewById(R.id.tvWordDetails);
        mWebViewDetails.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        Intent intent = getActivity().getIntent();

        // FRAGMENT
//        if(word == null && getArguments() != null) {
//            word = (Word) getArguments().getSerializable("wordSendSelf");
//        } else {
//            word = (Word) intent.getSerializableExtra("word_detail");
//        }
        receivedWordName = intent.getStringExtra("wordNameSend");
        if(receivedWordName == null) {
            receivedWordName = intent.getStringExtra("resendWord");
        }
        word = databaseAccess.getWord(receivedWordName);

        String result = word.getDetails().replaceAll("_", "'");
        result = result.replaceAll("\\\\r\\\\n", "");
        Document document = Jsoup.parse(result);
        Elements remove_b_tag = document.select("b");
        remove_b_tag.tagName("p");
        Elements add_arrow = document.select("ul > li > ul > li > font > p");
        add_arrow.attr("class", "arrow");

        Elements tmpE1 = document.select("body > i");
        tmpE1.attr("class", "title");

        Elements tmpE2 = document.select("ul > li > ul > li");
        tmpE2.attr("class", "delColon");

        Elements tmpE3 = document.select("ul > li > ul > li > ul > li > p");
        tmpE3.attr("class", "underscore");


        Log.v("tux", document.toString());


        // Enable Javascript
        WebSettings ws = mWebViewDetails.getSettings();
        ws.setJavaScriptEnabled(true);
        mWebViewDetails.setBackgroundColor(Color.TRANSPARENT);
        mWebViewDetails.addJavascriptInterface(new Object()
        {
            @JavascriptInterface           // For API 17+
            public void performClick(String openedWord) {
                // Activity
                Intent intent = new Intent(getActivity(), WordDetailsActivity.class);
                intent.putExtra("wordSendFromFragment", openedWord);
                startActivity(intent);

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

        mWebViewDetails.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                injectCSS();
                injectScriptFile(mWebViewDetails, "script.js");

//                tvWordDetails.loadUrl(
//                        "javascript:(function() { "
//                                + "var element = document.getElementById('arrow');"
//                                + "element.innerHTML = 'New text!';"
//                                + "})()");
                mWebViewDetails.loadUrl("javascript:addArrow()");
                mWebViewDetails.loadUrl("javascript:formatTitle()");
                mWebViewDetails.loadUrl("javascript:deleteColon()");
                mWebViewDetails.loadUrl("javascript:underScore()");
                super.onPageFinished(view, url);
            }
        });

        // Load a webpage
        mWebViewDetails.loadData(document.toString(), "text/html; charset=utf-8", "utf-8");
        return v;
    }

    // Inject CSS method: read style.css from assets folder
    // Append stylesheet to document head
    private void injectCSS() {
        try {
            InputStream inputStream = getActivity().getAssets().open("style.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            mWebViewDetails.loadUrl("javascript:(function() {" +
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

    private void injectScriptFile(WebView view, String scriptFile) {
        InputStream input;
        try {
            input = getActivity().getAssets().open(scriptFile);
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

