package com.samsung.lookup.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.samsung.lookup.R;
import com.samsung.lookup.activity.base.BaseActivity;
import com.samsung.lookup.adapter.HistoryRecycleViewAdapter;
import com.samsung.lookup.event.RecyclerClick_Listener;
import com.samsung.lookup.event.RecyclerTouchListener;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static com.samsung.lookup.MyApp.getDictionaryDB;

public class HistoryWordActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private HistoryRecycleViewAdapter mHistoryRecycleViewAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<String> mListHistoryWord;
    private EditText etWordSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etWordSearch = findViewById(R.id.etWordSearch);
        mRecyclerView = findViewById(R.id.rvWordHistory);
        implementEditTextWordSearchListener();
        implementRecyclerViewClickListeners();

        mListHistoryWord = getDictionaryDB().getHistoryWord(200);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new LandingAnimator());
        mLinearLayoutManager = new  LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mHistoryRecycleViewAdapter = new HistoryRecycleViewAdapter(this, mListHistoryWord);
        mRecyclerView.setAdapter(mHistoryRecycleViewAdapter);
    }

    @Override
    protected void onResume() {
        mListHistoryWord = getDictionaryDB().getHistoryWord(200);
        mHistoryRecycleViewAdapter.swap(mListHistoryWord);
        super.onResume();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_history_word;
    }

    @Override
    protected void initToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            Log.v(TAG, "getSupportActionBar null");
        }
    }

    @Override
    protected void loadAd() {
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void removeItem(int position) {
        getDictionaryDB().deleteHistoryWord(HistoryWordActivity.this, mListHistoryWord.get(position)); //1
        // sau khi delete thì phải cập nhật lại mListHistoryWord trong class này vì nếu không update thì lần 2 thực hiện dòng 1
        // vẫn là mListHistoryWord ban đầu
        mListHistoryWord = getDictionaryDB().getHistoryWord(200);
        mHistoryRecycleViewAdapter.removeItemAnimation(position);
    }

    public void showDeletePopup(final int position) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_delete_history_word);
        dialog.show();
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnDelete = dialog.findViewById(R.id.btn_delete);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position);
                dialog.dismiss();
            }
        });
    }

    public void openWord(String wordName) {
        Intent intent = new Intent(this, WordDetailsActivity.class);
        intent.putExtra("wordFromActivity", wordName);
        startActivity(intent);
    }

    private void implementRecyclerViewClickListeners() {
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerClick_Listener() {
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void implementEditTextWordSearchListener() {
        etWordSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mHistoryRecycleViewAdapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
