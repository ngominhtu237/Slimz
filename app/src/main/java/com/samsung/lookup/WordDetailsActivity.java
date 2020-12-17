package com.samsung.lookup;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.samsung.lookup.adapter.ViewPagerAdapter;
import com.samsung.lookup.data.secondDB.SaveDB;
import com.samsung.lookup.fragment.EngEngFragment;
import com.samsung.lookup.fragment.EngVietFragment;
import com.samsung.lookup.fragment.NoteFragment;
import com.samsung.lookup.fragment.SynonymFragment;
import com.samsung.lookup.fragment.TechnicalFragment;
import com.samsung.lookup.fragment.stack.WordStack;

import java.util.Arrays;

import static com.samsung.lookup.fragment.stack.WordStack.addToStack;

public class WordDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private ImageButton btBack;
    private ImageView btStar, btStarYellow, btStarBlue, btStarPink;
    private TextView tvWordName;
    private String receivedWordName;

    private SaveDB mSaveDB;
    private static final String[] arrSelectStarColor= {"yellow", "blue", "pink"};

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_details);
        Iconics.init(getApplicationContext());
        Iconics.registerFont(new GoogleMaterial());
        Toolbar toolbar = findViewById(R.id.toolbar);
        tvWordName = findViewById(R.id.tvWordName);
        btBack = findViewById(R.id.btBack);
        btStar = findViewById(R.id.btStar);
        btStarYellow = findViewById(R.id.btStarYellow);
        btStarBlue = findViewById(R.id.btStarBlue);
        btStarPink = findViewById(R.id.btStarPink);
        btBack.setOnClickListener(this);
        btStar.setOnClickListener(this);
        btStarYellow.setOnClickListener(this);
        btStarBlue.setOnClickListener(this);
        btStarPink.setOnClickListener(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""); // hide title

        mSaveDB = new SaveDB(this);
        mSaveDB.open();

        Intent intent = getIntent();
        // From MainActivity
        receivedWordName = intent.getStringExtra("wordFromActivity");

        // From EngVietFragment
        if(receivedWordName == null) {
            receivedWordName = intent.getStringExtra(("wordFromFragment"));
            if (receivedWordName == null) {
                receivedWordName = intent.getStringExtra(("wordFromStack"));
            } else {
                addToStack(receivedWordName);
            }
            Intent i = getIntent();
            i.putExtra("resendWord", receivedWordName);
        }
        if(receivedWordName != null) {
            mSaveDB.addHistoryWord(this, receivedWordName);
            addToStack(receivedWordName);
        }
        Log.v("wordstack", String.valueOf(WordStack.stackOfWords));

        checkWordFavorite(receivedWordName);

        tvWordName.setText(receivedWordName);

        mViewPager = findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void checkWordFavorite(String receivedWordName) {
        String isFavoriteWordColor = mSaveDB.getColorFavoriteWordByName(this, receivedWordName);
        switch (isFavoriteWordColor) {
            case "noColor":
                btStar.setVisibility(View.VISIBLE);
                break;
            case "yellow":
                btStarYellow.setVisibility(View.VISIBLE);
                break;
            case "blue":
                btStarBlue.setVisibility(View.VISIBLE);
                break;
            case "pink":
                btStarPink.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFrag(new EngVietFragment(), "ENG - VIET");
        mViewPagerAdapter.addFrag(new TechnicalFragment(), "TECHNICAL");
        mViewPagerAdapter.addFrag(new SynonymFragment(), "SYNONYM");
        mViewPagerAdapter.addFrag(new EngEngFragment(), "ENG - ENG");
        mViewPagerAdapter.addFrag(new NoteFragment(), "NOTE");
        viewPager.setAdapter(mViewPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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
        int stackSize = WordStack.stackOfWords.size();
        if(stackSize > 1) {
            Intent intent = new Intent(this, WordDetailsActivity.class);
            WordStack.stackOfWords.pop();
            intent.putExtra("wordFromStack", WordStack.stackOfWords.peek());
            startActivity(intent);
        } else if (stackSize == 1) {
            WordStack.stackOfWords.pop();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btBack:
                onBackPressed();
                break;
            case R.id.btStar:
                openDialogColor();
                break;
            case R.id.btStarYellow:
                btStar.setVisibility(View.VISIBLE);
                btStarYellow.setVisibility(View.GONE);
                mSaveDB.removeFavoriteWord(this, receivedWordName);
                break;
            case R.id.btStarBlue:
                btStar.setVisibility(View.VISIBLE);
                btStarBlue.setVisibility(View.GONE);

                break;
            case R.id.btStarPink:
                btStar.setVisibility(View.VISIBLE);
                btStarPink.setVisibility(View.GONE);
                break;
        }
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
                btStar.setVisibility(View.GONE);
                btStarBlue.setVisibility(View.GONE);
                btStarPink.setVisibility(View.GONE);
                btStarYellow.setVisibility(View.VISIBLE);
                mSaveDB.addFavoriteWord(WordDetailsActivity.this, receivedWordName, arrSelectStarColor[0]);
                dialog.dismiss();
            }
        });
        btStarBlueDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btStar.setVisibility(View.GONE);
                btStarYellow.setVisibility(View.GONE);
                btStarPink.setVisibility(View.GONE);
                btStarBlue.setVisibility(View.VISIBLE);
                mSaveDB.addFavoriteWord(WordDetailsActivity.this, receivedWordName, arrSelectStarColor[1]);
                dialog.dismiss();
            }
        });
        btStarPinkDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btStar.setVisibility(View.GONE);
                btStarYellow.setVisibility(View.GONE);
                btStarBlue.setVisibility(View.GONE);
                btStarPink.setVisibility(View.VISIBLE);
                mSaveDB.addFavoriteWord(WordDetailsActivity.this, receivedWordName, arrSelectStarColor[2]);
                dialog.dismiss();
            }
        });
    }
}

