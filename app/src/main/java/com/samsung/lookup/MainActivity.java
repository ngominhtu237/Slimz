package com.samsung.lookup;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.samsung.lookup.activity.HistoryWordActivity;
import com.samsung.lookup.activity.MarkWordActivity;
import com.samsung.lookup.adapter.CustomAutoCompWordAdapter;
import com.samsung.lookup.adapter.CustomRecycleViewAdapter;
import com.samsung.lookup.data.DatabaseAccess;
import com.samsung.lookup.data.secondDB.SaveDB;
import com.samsung.lookup.event.RecyclerClick_Listener;
import com.samsung.lookup.event.RecyclerTouchListener;
import com.samsung.lookup.service.QuickTranslate;
import com.samsung.lookup.service.QuickTranslateService;
import com.samsung.lookup.view.CustomAutoCompleteTextView;

import java.util.ArrayList;
import java.util.Locale;

import wei.mark.standout.StandOutWindow;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    public final static int REQUEST_CODE = 113;
    private CustomAutoCompleteTextView completeTextView;
    private ArrayList<String> wordNameArr = new ArrayList<>();
    private CustomAutoCompWordAdapter customAutoCompWordAdapter;
    private CustomRecycleViewAdapter recycleViewAdapter;
    private DatabaseAccess databaseAccess;
    private SaveDB mSaveDB;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private String[] mTitle;
    private ImageView ivIconMicro, ivIconDelLetter;
    private ArrayList<String> mListHistoryWord;
    private GetDataTask getDataTask;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isMyServiceRunning(QuickTranslateService.class)) {
            // disable floating icon + kill service
            Intent intent = new Intent(MainActivity.this, QuickTranslateService.class);
            stopService(intent);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        mRecyclerView = findViewById(R.id.recycleView);
        ivIconMicro = findViewById(R.id.ivIconMicro);
        ivIconDelLetter = findViewById(R.id.ivIconDeleteLetter);
        mTitle = getResources().getStringArray(R.array.title_list);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""); // hide title

        completeTextView = findViewById(R.id.textView);
        databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
        databaseAccess.open();
        mSaveDB = new SaveDB(this);
        mSaveDB.open();
        customAutoCompWordAdapter = new CustomAutoCompWordAdapter(MainActivity.this, R.layout.item_autocomplete_layout, wordNameArr);
        completeTextView.setThreshold(0);
        completeTextView.setAdapter(customAutoCompWordAdapter);

        implementMicroAndDeleteListeners();

        completeTextView.addTextChangedListener(new TextWatcher() {

            long lastPress = 0L;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                if(System.currentTimeMillis() - lastPress > 10){
                    lastPress = System.currentTimeMillis();
                    if (!completeTextView.isPerformingCompletion() && charSequence.length() > 0) {
                        runSearch(charSequence.toString());
                        Log.v("Dropdown", "isshow: " + completeTextView.isPopupShowing());
                    }
                }

                // update icon mic and del
                if(completeTextView.getText().length() > 0) {
                    ivIconMicro.setVisibility(View.GONE);
                    ivIconDelLetter.setVisibility(View.VISIBLE);
                    customAutoCompWordAdapter.isNeedToChange = false;
                } else {
                    Log.v("onTextChanged", "length < 0 - getHistory");
                    ivIconMicro.setVisibility(View.VISIBLE);
                    ivIconDelLetter.setVisibility(View.GONE);
                    mListHistoryWord = mSaveDB.getHistoryWord(50);
                    if(mListHistoryWord.size() > 0) {
                        customAutoCompWordAdapter.setNewData(mListHistoryWord);
                        customAutoCompWordAdapter.isNeedToChange = true;
                        runShowDropDown();
                    }
                }
            }

            @Override
            public void afterTextChanged(final Editable s) { }
        });
        completeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(completeTextView.getText().toString().matches("")) {
                    mListHistoryWord = mSaveDB.getHistoryWord(50);
                    if(mListHistoryWord.size() > 0) {
                        customAutoCompWordAdapter.setNewData(mListHistoryWord);
                        customAutoCompWordAdapter.isNeedToChange = true;
                        completeTextView.showDropDown();
                    }
                } else {
                    getDataTask = new GetDataTask();
                    getDataTask.execute(completeTextView.getText().toString());
                    completeTextView.showDropDown();
                }
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        recycleViewAdapter = new CustomRecycleViewAdapter(this, mTitle);
        mRecyclerView.setAdapter(recycleViewAdapter);
        implementRecyclerViewClickListeners();

        checkDrawOverlayPermission();
    }

    private void implementMicroAndDeleteListeners() {

        findViewById(R.id.iconDeleteContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                completeTextView.setText("");
            }
        });

        findViewById(R.id.iconMicroContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                promptSpeechInput();
            }
        });
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void startWordDetails(String wordName) {
        completeTextView.setText(wordName);
        Intent intent = new Intent(MainActivity.this, WordDetailsActivity.class);
        intent.putExtra("wordFromActivity", wordName);
        startActivity(intent);
    }

    private void implementRecyclerViewClickListeners() {
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerClick_Listener() {
            @Override
            public void onClick(View view, int position) {
                if(position == 0) {
                    //startService(new Intent(MainActivity.this, QuickTranslateService.class));
                    //finish();
                    StandOutWindow
                            .show(MainActivity.this, QuickTranslate.class, StandOutWindow.DEFAULT_ID);
                }
                if(position == 1) {
                    startActivity(new Intent(MainActivity.this, HistoryWordActivity.class));
                }
                if(position == 2) {
                    startActivity(new Intent(MainActivity.this, MarkWordActivity.class));
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private class GetDataTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Log.v("onTextChanged", "word search: " + strings[0]);
            wordNameArr = databaseAccess.getWordNames(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            customAutoCompWordAdapter.setNewData(wordNameArr);
            super.onPostExecute(integer);
        }
    }

    @Override
    public void onResume() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onResume();
    }

    public CustomAutoCompleteTextView getCompleteTextView() {
        return completeTextView;
    }

    public void runSearch(String wordName) {
        getDataTask = new GetDataTask();
        getDataTask.execute(wordName);
        runShowDropDown();
        completeTextView.setSelection(wordName.length());
    }

    public void runShowDropDown() {
        if(!completeTextView.isPopupShowing()){
            completeTextView.showDropDown();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    completeTextView.setText(result.get(0));
                }
                break;
            }
            case REQUEST_CODE: {
                if (Settings.canDrawOverlays(this)) {
                    // continue here - permission was granted
                } else {
                    finish();
                }
            }

        }
    }

    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            /** if not construct intent to request permission */
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            /** request permission via start activity for result */
            startActivityForResult(intent, REQUEST_CODE);
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}




