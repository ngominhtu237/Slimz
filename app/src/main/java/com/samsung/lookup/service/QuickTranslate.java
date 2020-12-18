package com.samsung.lookup.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.samsung.lookup.MainActivity;
import com.samsung.lookup.R;
import com.samsung.lookup.adapter.CustomACQuickAdapter;
import com.samsung.lookup.adapter.CustomPagerAdapter;
import com.samsung.lookup.model.Word;
import com.samsung.lookup.utils.HtmlUtils;
import com.samsung.lookup.view.CustomAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

import static com.samsung.lookup.MyApp.getDictionaryDB;
import static com.samsung.lookup.MyApp.getEngVietDbAccess;
import static com.samsung.lookup.fragment.stack.WordStack.addToStack;
import static wei.mark.standout.ui.Window.WindowDataKeys.HEIGHT_BEFORE_MAXIMIZE;
import static wei.mark.standout.ui.Window.WindowDataKeys.IS_MAXIMIZED;
import static wei.mark.standout.ui.Window.WindowDataKeys.WIDTH_BEFORE_MAXIMIZE;
import static wei.mark.standout.ui.Window.WindowDataKeys.X_BEFORE_MAXIMIZE;
import static wei.mark.standout.ui.Window.WindowDataKeys.Y_BEFORE_MAXIMIZE;

public class QuickTranslate extends StandOutWindow implements View.OnTouchListener, CustomACQuickAdapter.WordDetailsInterface {

    private static final String TAG = "QuickTranslate";

    private static final int OPENED_WIDTH = 1000;
    private static final int OPENED_HEIGHT = 850;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private CustomPagerAdapter mCustomPagerAdapter;

    private ArrayList<String> wordNameArr = new ArrayList<>();
    private CustomACQuickAdapter customACQuickAdapter;
    private GetDataTask getDataTask;
    private ArrayList<String> mListHistoryWord;
    private CustomAutoCompleteTextView mAutoCompleteTextView;

    private MyGroupView mFloatingIcon;
    private WindowManager.LayoutParams mFloatingIconViewParams;
    private int currentId;
    private boolean isFloatingWindowHidden;
    private boolean isFloatingWindowClose;

    private GestureDetector gestureDetector;
    private int prevX, prevY;
    private float mStartX, mStartY;
    private int lastPosX, lastPosY;
    @Override
    public String getAppName() {
        return getResources().getString(R.string.app_name);
    }

    @Override
    public int getAppIcon() {
        return R.mipmap.icons_more_option_white;
    }

    @Override
    public void createAndAttachView(final int id, FrameLayout frame) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.quick_translate, frame, true);

        createFloatingIcon();
        currentId = id;
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());

        view.findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFloatingWindowClose = true;
                close(id);
                removeAllView();
            }
        });
        final ImageButton optionBtn = view.findViewById(R.id.optionBtn);
        optionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupWindow dropDown = getDropDown(id);
                if (dropDown != null) {
                    dropDown.showAsDropDown(optionBtn);
                }
            }
        });
        view.findViewById(R.id.resizeIv).setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return onTouchHandleResize(id, getWindow(id), view, motionEvent);
            }
        });
        view.findViewById(R.id.maximizeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StandOutLayoutParams params = getWindow(id).getLayoutParams();
                Bundle data = getWindow(id).getData();
                boolean isMaximized = data.getBoolean(IS_MAXIMIZED);
                if (isMaximized && params.width == getWindow(id).getDisplayWidth() && params.height == getWindow(id).getDisplayHeight() && params.x == 0 && params.y == 0) {
                    data.putBoolean(IS_MAXIMIZED, false);
                    int oldWidth = data.getInt(WIDTH_BEFORE_MAXIMIZE, -1);
                    int oldHeight = data.getInt(HEIGHT_BEFORE_MAXIMIZE, -1);
                    int oldX = data.getInt(X_BEFORE_MAXIMIZE, -1);
                    int oldY = data.getInt(Y_BEFORE_MAXIMIZE, -1);
                    getWindow(id).edit().setSize(oldWidth, oldHeight).setPosition(oldX, oldY).commit();
                    ((ImageButton)view).setImageResource(R.drawable.ic_max_window_24);
                } else {
                    data.putBoolean(IS_MAXIMIZED, true);
                    data.putInt(WIDTH_BEFORE_MAXIMIZE, params.width);
                    data.putInt(HEIGHT_BEFORE_MAXIMIZE, params.height);
                    data.putInt(X_BEFORE_MAXIMIZE, params.x);
                    data.putInt(Y_BEFORE_MAXIMIZE, params.y);
                    getWindow(id).edit().setSize(1f, 1f).setPosition(0, 0).commit();
                    ((ImageButton)view).setImageResource(R.drawable.ic_min_window_24);
                }
            }
        });

        mViewPager = view.findViewById(R.id.viewpager);
        setupViewPager(mViewPager);
        mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mAutoCompleteTextView = view.findViewById(R.id.searchview);
        customACQuickAdapter = new CustomACQuickAdapter(getApplicationContext(), this, R.layout.item_layout_quick, wordNameArr);
        mAutoCompleteTextView.setAdapter(customACQuickAdapter);
        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {

            long lastPress = 0L;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                if (System.currentTimeMillis() - lastPress > 10) {
                    lastPress = System.currentTimeMillis();
                    if (!mAutoCompleteTextView.isPerformingCompletion() && charSequence.length() > 0) {
                        runSearch(charSequence.toString());
                    }
                }

                // update icon mic and del
                if (mAutoCompleteTextView.getText().length() > 0) {
                    customACQuickAdapter.isNeedToChange = false;
                } else {
                    mListHistoryWord = getDictionaryDB().getHistoryWord(10);
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
                    mListHistoryWord = getDictionaryDB().getHistoryWord(10);
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
    }

    @Override
    public void openWord(String wordName) {
        mAutoCompleteTextView.setText(wordName);
        Word word = getEngVietDbAccess().getWord(wordName);
        mCustomPagerAdapter.setEnViDetails(HtmlUtils.format(word).toString());
        mCustomPagerAdapter.notifyDataSetChanged();
        getDictionaryDB().addHistoryWord(this, wordName);
        addToStack(wordName);
        new Handler().post(new Runnable() {
            public void run() {
                mAutoCompleteTextView.dismissDropDown();
            }});
    }

    @Override
    public void generateWord(String wordName) {
        mAutoCompleteTextView.setText(wordName);
        runSearch(wordName);
    }

    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void runSearch(String wordName) {
        getDataTask = new GetDataTask();
        getDataTask.execute(wordName);
        mAutoCompleteTextView.setSelection(wordName.length());
    }

    private void setupViewPager(ViewPager viewPager) {
        mCustomPagerAdapter = new CustomPagerAdapter(getApplicationContext(), this);
        viewPager.setAdapter(mCustomPagerAdapter);
    }

    @Override
    public StandOutLayoutParams getParams(int id, wei.mark.standout.ui.Window window) {
        return getParams(id);
    }

    public StandOutLayoutParams getParams(int id) {
        return getOpenedParams(id);
    }

    private StandOutLayoutParams getOpenedParams(int id) {
        StandOutLayoutParams openedParams = new StandOutLayoutParams(id, OPENED_WIDTH, OPENED_HEIGHT);
        openedParams.minWidth = 400;
        openedParams.minHeight = 400;
        openedParams.x = 150;
        openedParams.y = 250;
        return openedParams;
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return "Click to close the QuickTranslate";
    }

    @Override
    public Intent getPersistentNotificationIntent(int id) {
        return StandOutWindow.getCloseIntent(this, QuickTranslate.class, id);
    }

    @Override
    public int getFlags(int id) {
        return StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
                | StandOutFlags.FLAG_BODY_MOVE_ENABLE;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.tvFloatingIcon && isFloatingWindowHidden && gestureDetector.onTouchEvent(event)) {
            try {
                mWindowManager.removeView(mFloatingIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
            show(currentId);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (v.getId() == R.id.tvFloatingIcon) {
                    prevX = mFloatingIconViewParams.x;
                    prevY = mFloatingIconViewParams.y;
                }
                mStartX = event.getRawX();
                mStartY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                double deltaX = event.getRawX() - mStartX;
                double deltaY = event.getRawY() - mStartY;
                if (v.getId() == R.id.tvFloatingIcon) {
                    mFloatingIconViewParams.x = prevX + (int) deltaX;
                    mFloatingIconViewParams.y = prevY + (int) deltaY;
                    lastPosX = mFloatingIconViewParams.x;
                    lastPosY = mFloatingIconViewParams.y;
                    mWindowManager.updateViewLayout(mFloatingIcon, mFloatingIconViewParams);
                }
                break;
        }
        return true;
    }

    private static class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    }

    @Override
    public int getThemeStyle() {
        return R.style.FloatingWindowTheme;
    }

    @Override
    public boolean onFocusChange(int id, Window window, boolean focus) {
        if (!focus && !isFloatingWindowHidden) {
            hide(id);
            isFloatingWindowHidden = true;
        }
        return super.onFocusChange(id, window, focus);
    }

    @Override
    public boolean onHide(int id, Window window) {
        Log.v(TAG, "onHide");
        if(!isFloatingWindowClose) {
            mFloatingIconViewParams.x = lastPosX;
            mFloatingIconViewParams.y = lastPosY;
            mWindowManager.addView(mFloatingIcon, mFloatingIconViewParams);
        }
        return super.onHide(id, window);
    }

    @Override
    public boolean onShow(int id, Window window) {
        isFloatingWindowHidden = false;
        return super.onShow(id, window);
    }

    @Override
    public void onDestroy() {
        removeAllView();
        super.onDestroy();
    }

    public void removeAllView() {
        try {
            ((WindowManager) getApplicationContext().getSystemService(Service.WINDOW_SERVICE)).removeView(mFloatingIcon);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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
        mFloatingIconViewParams.windowAnimations = android.R.style.Animation_InputMethod;
        mFloatingIconViewParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }

    @SuppressLint("StaticFieldLeak")
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

    @Override
    public List<DropDownListItem> getDropDownItems(int id) {
        List<DropDownListItem> items = new ArrayList<>();
        items.add(new DropDownListItem(
                R.drawable.ic_open_dictionary_24, "Open Dictionary"
                , new Runnable() {

            @Override
            public void run() {
                isFloatingWindowClose = true;
                close(currentId);
                removeAllView();
                startMainActivity();
            }
        }));
        return items;
    }
}
