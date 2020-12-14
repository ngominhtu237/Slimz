package com.samsung.lookup.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.samsung.lookup.R;
import com.samsung.lookup.WordDetailsActivity;
import com.samsung.lookup.adapter.HistoryRecycleViewAdapter;
import com.samsung.lookup.data.DatabaseAccess;
import com.samsung.lookup.data.secondDB.SaveDB;
import com.samsung.lookup.event.RecyclerClick_Listener;
import com.samsung.lookup.event.RecyclerTouchListener;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

public class HistoryWordActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private HistoryRecycleViewAdapter mHistoryRecycleViewAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<String> mListHistoryWord;
    private EditText etWordSearch;

    private DatabaseAccess databaseAccess;
    private SaveDB mSaveDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_word);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        etWordSearch = findViewById(R.id.etWordSearch);
        mRecyclerView = findViewById(R.id.rvWordHistory);
        implementEditTextWordSearchListener();
        implementRecyclerViewClickListeners();

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();
        mSaveDB = new SaveDB(this);
        mSaveDB.open();
        mListHistoryWord = mSaveDB.getHistoryWord(200);

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
        mListHistoryWord = mSaveDB.getHistoryWord(200);
        mHistoryRecycleViewAdapter.swap(mListHistoryWord);
        super.onResume();
    }

    public void removeItem(int position) {
        mSaveDB.deleteHistoryWord(HistoryWordActivity.this, mListHistoryWord.get(position)); //1
        // sau khi delete thì phải cập nhật lại mListHistoryWord trong class này vì nếu không update thì lần 2 thực hiện dòng 1
        // vẫn là mListHistoryWord ban đầu
        mListHistoryWord = mSaveDB.getHistoryWord(200);
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
        intent.putExtra("wordNameSend", wordName);
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
