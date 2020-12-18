package com.tunm.slimz.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.tunm.slimz.R;
import com.tunm.slimz.activity.base.BaseActivity;
import com.tunm.slimz.adapter.FavoriteRecycleViewAdapter;
import com.tunm.slimz.event.RecyclerClick_Listener;
import com.tunm.slimz.event.RecyclerTouchListener;
import com.tunm.slimz.model.WorkMark;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.LandingAnimator;

import static com.tunm.slimz.MyApp.getDictionaryDB;

public class MarkWordActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private FavoriteRecycleViewAdapter mFavoriteRecycleViewAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<WorkMark> mArrWorkMark;
    private EditText etWordMarkSearch;

    private Menu mOptionMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etWordMarkSearch = findViewById(R.id.etWordMarkSearch);
        mRecyclerView = findViewById(R.id.rvWordMark);

        implementEditTextWordSearchListener();
        implementRecyclerViewClickListeners();

        mArrWorkMark = getDictionaryDB().getFavoriteWord(200);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new LandingAnimator());
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mFavoriteRecycleViewAdapter = new FavoriteRecycleViewAdapter(this, mArrWorkMark);
        mRecyclerView.setAdapter(mFavoriteRecycleViewAdapter);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mark_word;
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

    @Override
    protected void onResume() {
        mArrWorkMark = getDictionaryDB().getFavoriteWord(200);
        mFavoriteRecycleViewAdapter.swap(mArrWorkMark);
        super.onResume();
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
        etWordMarkSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mFavoriteRecycleViewAdapter.getFilter().filter(charSequence.toString());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionMenu = menu;
        getMenuInflater().inflate(R.menu.menu_mark_work, menu);
        MenuItem filter = menu.findItem(R.id.action_filter_word_mark).setIcon(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_filter_list).color(Color.WHITE).sizeDp(22));
        MenuItem clear_filter = menu.findItem(R.id.action_clear_filter_word_mark).setIcon(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_clear_all).color(Color.WHITE).sizeDp(22));
        clear_filter.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter_word_mark:
                openDialogColor();
                break;
            case R.id.action_clear_filter_word_mark:
                mArrWorkMark = getDictionaryDB().getFavoriteWord(200);
                mFavoriteRecycleViewAdapter.swap(mArrWorkMark);
                mOptionMenu.findItem(R.id.action_filter_word_mark).setVisible(true);
                mOptionMenu.findItem(R.id.action_clear_filter_word_mark).setVisible(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openDialogColor() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_select_favorite);
        dialog.show();
        final ImageButton btStarYellowDialog = dialog.findViewById(R.id.btStarYellow);
        final ImageButton btStarBlueDialog = dialog.findViewById(R.id.btStarBlue);
        final ImageButton btStarPinkDialog = dialog.findViewById(R.id.btStarPink);
        btStarYellowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArrWorkMark = getDictionaryDB().getFavoriteWordByColor(MarkWordActivity.this, "yellow");
                mFavoriteRecycleViewAdapter.swap(mArrWorkMark);
                mOptionMenu.findItem(R.id.action_filter_word_mark).setVisible(false);
                mOptionMenu.findItem(R.id.action_clear_filter_word_mark).setVisible(true);
                dialog.dismiss();
            }
        });
        btStarBlueDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArrWorkMark = getDictionaryDB().getFavoriteWordByColor(MarkWordActivity.this, "blue");
                mFavoriteRecycleViewAdapter.swap(mArrWorkMark);
                mOptionMenu.findItem(R.id.action_filter_word_mark).setVisible(false);
                mOptionMenu.findItem(R.id.action_clear_filter_word_mark).setVisible(true);
                dialog.dismiss();
            }
        });
        btStarPinkDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArrWorkMark = getDictionaryDB().getFavoriteWordByColor(MarkWordActivity.this, "pink");
                mFavoriteRecycleViewAdapter.swap(mArrWorkMark);
                mOptionMenu.findItem(R.id.action_filter_word_mark).setVisible(false);
                mOptionMenu.findItem(R.id.action_clear_filter_word_mark).setVisible(true);
                dialog.dismiss();
            }
        });
    }
}
