package com.samsung.lookup.service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.samsung.lookup.MainActivity;
import com.samsung.lookup.R;
import com.samsung.lookup.adapter.CustomACQuickAdapter;
import com.samsung.lookup.adapter.CustomPagerAdapter;
import com.samsung.lookup.utils.Measure;
import com.samsung.lookup.view.CustomAutoCompleteTextView;

import java.util.ArrayList;

import static com.samsung.lookup.MyApp.getDictionaryDB;
import static com.samsung.lookup.MyApp.getEngVietDbAccess;

public class QuickTranslateService extends Service implements View.OnTouchListener, View.OnClickListener, CustomACQuickAdapter.WordDetailsInterface {
    public static final int WIDTH_FLOAT_WINDOW = 300; // dp?
    public static final int HEIGHT_FLOAT_WINDOW = 220;
    private WindowManager mWindowManager;
    private MyGroupView mFloatingIcon, mFloatingView;
    private WindowManager.LayoutParams mFloatingIconViewParams, mFloatingWindowViewParams;
    private int prevX, prevY;
    private float mStartX, mStartY;
    private int lastPosX, lastPosY; // save last position
    private GestureDetector gestureDetector;
    private int state;
    private final static int STATE_FLOATING_WINDOW = 0;
    private final static int STATE_FLOATING_ICON = 1;
    int widthScreen, heightScreen;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private CustomPagerAdapter mCustomPagerAdapter;

    private ArrayList<String> wordNameArr = new ArrayList<>();
    private CustomACQuickAdapter customACQuickAdapter;
    private GetDataTask getDataTask;
    private ArrayList<String> mListHistoryWord;
    private CustomAutoCompleteTextView mAutoCompleteTextView;

    public QuickTranslateService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initView();
        return START_STICKY;
    }

    private void initView() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        heightScreen = displayMetrics.heightPixels;
        widthScreen = displayMetrics.widthPixels;

        createFloatingWindow();
        createFloatingIcon();
        openFloatingWindow();
    }

    private void setupViewPager(ViewPager viewPager) {
//        mCustomPagerAdapter = new CustomPagerAdapter(getApplicationContext());
//        viewPager.setAdapter(mCustomPagerAdapter);

    }

    private void createFloatingWindow() {
        mFloatingView = new MyGroupView(this);
        ContextThemeWrapper ctx = new ContextThemeWrapper(this, R.style.AppTheme);
        View view = View.inflate(ctx, R.layout.floating_window, mFloatingView);
        view.findViewById(R.id.triangle).setOnTouchListener(this);
        view.findViewById(R.id.moveView).setOnTouchListener(this);
        mFloatingView.setOnTouchListener(this);
//        view.findViewById(R.id.btMinimize).setOnClickListener(this);
//        view.findViewById(R.id.btRestore).setOnClickListener(this);
//        view.findViewById(R.id.btClose).setOnClickListener(this);
        mViewPager = view.findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mAutoCompleteTextView = view.findViewById(R.id.searchview);
        customACQuickAdapter = new CustomACQuickAdapter(getApplicationContext(), this, R.layout.item_layout_quick, wordNameArr);
        mAutoCompleteTextView.setAdapter(customACQuickAdapter);
        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {

            long lastPress = 0l;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                Log.v("tunm", "fire!!");
                if (System.currentTimeMillis() - lastPress > 10) {
                    lastPress = System.currentTimeMillis();
                    if (!mAutoCompleteTextView.isPerformingCompletion() && charSequence.length() > 0) {
                        runSearch(charSequence.toString());
//                        Log.v("tunm", charSequence.toString());
                    }
                }

                // update icon mic and del
                if (mAutoCompleteTextView.getText().length() > 0) {
                    customACQuickAdapter.isNeedToChange = false;
                } else {
                    mListHistoryWord = getDictionaryDB().getHistoryWord(50);
                    if (mListHistoryWord.size() > 0) {
                        customACQuickAdapter.setNewData(mListHistoryWord);
                        customACQuickAdapter.isNeedToChange = true;
                        Log.v("tunm", "length < 0 => get history");
                    }
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        });
        mAutoCompleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAutoCompleteTextView.getText().toString().matches("")) {
                    mListHistoryWord = getDictionaryDB().getHistoryWord(50);
                    if (mListHistoryWord.size() > 0) {
                        customACQuickAdapter.setNewData(mListHistoryWord);
                        customACQuickAdapter.isNeedToChange = true;
                        mAutoCompleteTextView.showDropDown();
                    }
                } else {
                    getDataTask = new GetDataTask();
                    getDataTask.execute(mAutoCompleteTextView.getText().toString());
                    Log.v("tunm1", "query-click");
                }
            }
        });

        mFloatingWindowViewParams = new WindowManager.LayoutParams();
        mFloatingWindowViewParams.width = Measure.pxToDp(WIDTH_FLOAT_WINDOW, getApplicationContext());
        mFloatingWindowViewParams.height = Measure.pxToDp(HEIGHT_FLOAT_WINDOW, getApplicationContext());
        mFloatingWindowViewParams.gravity = Gravity.START | Gravity.CLIP_VERTICAL;
        mFloatingWindowViewParams.format = PixelFormat.TRANSLUCENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFloatingWindowViewParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mFloatingWindowViewParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mFloatingWindowViewParams.windowAnimations = android.R.style.Animation_InputMethod;
        mFloatingWindowViewParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mFloatingWindowViewParams.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
    }

    public void runSearch(String wordName) {
        getDataTask = new GetDataTask();
        getDataTask.execute(wordName);
        Log.v("tunm1", "query-click");
        mAutoCompleteTextView.setSelection(wordName.length());
    }

//    public void runShowDropDown() {
//        if (!mAutoCompleteTextView.isPopupShowing()) {
//            mAutoCompleteTextView.showDropDown();
//            Log.v("tunm", "showDropdown!!");
//        }
//    }

    private void createFloatingIcon() {
        mFloatingIcon = new MyGroupView(this);
        View view = View.inflate(this, R.layout.view_icon, mFloatingIcon);
        TextView tvFloatingIcon = view.findViewById(R.id.tvFloatingIcon);
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/Alleyster.ttf");
        tvFloatingIcon.setTypeface(type);
        tvFloatingIcon.setOnTouchListener(this);

        mFloatingIconViewParams = new WindowManager.LayoutParams();
        mFloatingIconViewParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mFloatingIconViewParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mFloatingIconViewParams.gravity = Gravity.CENTER;
        mFloatingIconViewParams.format = PixelFormat.TRANSLUCENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mFloatingIconViewParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mFloatingIconViewParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mFloatingWindowViewParams.windowAnimations = android.R.style.Animation_InputMethod;
        mFloatingIconViewParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.tvFloatingIcon && gestureDetector.onTouchEvent(event)) {
            // single tap
            openFloatingWindow();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v.getId() == R.id.moveView) {
                    prevX = mFloatingWindowViewParams.x;
                    prevY = mFloatingWindowViewParams.y;
                    Log.e(">>", "prevX:" + prevX + "  prevY:" + prevY);
                }
                if (v.getId() == R.id.tvFloatingIcon) {
                    prevX = mFloatingIconViewParams.x;
                    prevY = mFloatingIconViewParams.y;
                }
                if (v.getId() == R.id.triangle) {
                    Log.v(">>", "press triangle");
                    Log.e(">>", "prevX:" + mFloatingIconViewParams.x + "  prevY:" + mFloatingIconViewParams.y);
                }
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                // Log.v(">>", "action move detect");
                double deltaX = event.getRawX() - mStartX;
                double deltaY = event.getRawY() - mStartY;
                if (v.getId() == R.id.moveView) {
                    mFloatingWindowViewParams.x = prevX + (int) deltaX;
                    mFloatingWindowViewParams.y = prevY + (int) deltaY;
                    lastPosX = mFloatingWindowViewParams.x;
                    lastPosY = mFloatingWindowViewParams.y;
                    mWindowManager.updateViewLayout(mFloatingView, mFloatingWindowViewParams);
                }
                if (v.getId() == R.id.tvFloatingIcon) {
                    mFloatingIconViewParams.x = prevX + (int) deltaX;
                    mFloatingIconViewParams.y = prevY + (int) deltaY;
                    lastPosX = mFloatingIconViewParams.x;
                    lastPosY = mFloatingIconViewParams.y;
                    mWindowManager.updateViewLayout(mFloatingIcon, mFloatingIconViewParams);
                }
                if (v.getId() == R.id.triangle) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    int width = mFloatingWindowViewParams.width;
                    int height = mFloatingWindowViewParams.height;

                    // Log.e(">>", "width:" + width + " height:" + height + " x:" + x + " y:" + y);
                    int tmpX = width + x;
                    int tmpY = height + y;
                    if (tmpX >= widthScreen / 2 && tmpX < widthScreen) {
                        mFloatingWindowViewParams.width = tmpX;
                    }
                    if (tmpY >= heightScreen / 3 && tmpY < heightScreen) {
                        mFloatingWindowViewParams.height = tmpY;
                    }

                    mWindowManager.updateViewLayout(mFloatingView, mFloatingWindowViewParams);
                }
                break;
            case MotionEvent.ACTION_OUTSIDE:
                // ở đây ta ko thể biết đc đang ở view nào khi chỉ dựa vào id => cần biến state
                if (state == STATE_FLOATING_WINDOW) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    minimizeToFloatIcon();
                }
                break;
        }
        return true;
    }


    @Override
    public void onDestroy() {
        Log.v("tunm", "Service destroy!!!!");
        removeAllView();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btMinimize:
//                minimizeToFloatIcon();
//                break;
//            case R.id.btRestore:
//                openFullDictionary();
//                break;
//            case R.id.btClose:
//                onDestroy();
//                break;
            case R.id.searchview:
//                Toast.makeText(getApplicationContext(), "click", Toast.LENGTH_SHORT).show();
//                break;
        }
    }

    private void openFullDictionary() {
        try {
            removeAllView();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void minimizeToFloatIcon() {
        mFloatingIconViewParams.x = lastPosX;
        mFloatingIconViewParams.y = lastPosY;
        try {
            mWindowManager.removeView(mFloatingView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mWindowManager.addView(mFloatingIcon, mFloatingIconViewParams);
        state = STATE_FLOATING_ICON;
    }

    private void openFloatingWindow() {
        mFloatingWindowViewParams.x = lastPosX;
        mFloatingWindowViewParams.y = lastPosY;
        try {
            mWindowManager.removeView(mFloatingIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mWindowManager.addView(mFloatingView, mFloatingWindowViewParams);
        state = STATE_FLOATING_WINDOW;
    }

    @Override
    public void openWord(String name) {

    }

    @Override
    public void generateWord(String word) {

    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    }

    public void removeAllView() {
        if (mFloatingView.getWindowToken() != null) {
            mWindowManager.removeView(mFloatingView);
        }
        if (mFloatingIcon.getWindowToken() != null) {
            mWindowManager.removeView(mFloatingIcon);
        }
    }

    private class GetDataTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            wordNameArr = getEngVietDbAccess().getWordNames(strings[0], 10);
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            customACQuickAdapter.setNewData(wordNameArr);
            mAutoCompleteTextView.showDropDown();
            super.onPostExecute(integer);
        }
    }

}
