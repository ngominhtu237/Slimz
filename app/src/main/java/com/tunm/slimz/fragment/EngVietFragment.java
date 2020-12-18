package com.tunm.slimz.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.tunm.slimz.IClickWordCallback;
import com.tunm.slimz.R;
import com.tunm.slimz.activity.WordDetailsActivity;
import com.tunm.slimz.fragment.base.BaseFragment;
import com.tunm.slimz.model.Word;
import com.tunm.slimz.utils.HtmlUtils;
import com.tunm.slimz.utils.WebViewUtils;
import com.tunm.slimz.MyApp;

import org.jsoup.nodes.Document;

/**
 * A quick_translate {@link Fragment} subclass.
 */
public class EngVietFragment extends BaseFragment {


    public EngVietFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_eng_viet, container, false);
        WebView mWebViewDetails = v.findViewById(R.id.tvWordDetails);
        mWebViewDetails.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        Intent intent = mActivity.getIntent();
        String receivedWordName = intent.getStringExtra("wordFromActivity");
        if(receivedWordName == null) {
            receivedWordName = intent.getStringExtra("resendWord");
        }
        Word word = MyApp.getEngVietDbAccess().getWord(receivedWordName);
        Document document = HtmlUtils.format(word);
        WebViewUtils.addScript(mActivity, mWebViewDetails, callback);

        mWebViewDetails.loadData(document.toString(), "text/html; charset=utf-8", "utf-8");
        return v;
    }

    private final IClickWordCallback callback = new IClickWordCallback() {
        @Override
        public void open(String word) {
            mActivity.finish();
            Intent intent = new Intent(mActivity, WordDetailsActivity.class);
            intent.putExtra("wordFromFragment", word);
            startActivity(intent);
        }
    };
}

