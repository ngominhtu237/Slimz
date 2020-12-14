package com.samsung.lookup.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.samsung.lookup.R;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

import static wei.mark.standout.ui.Window.WindowDataKeys.HEIGHT_BEFORE_MAXIMIZE;
import static wei.mark.standout.ui.Window.WindowDataKeys.IS_MAXIMIZED;
import static wei.mark.standout.ui.Window.WindowDataKeys.WIDTH_BEFORE_MAXIMIZE;
import static wei.mark.standout.ui.Window.WindowDataKeys.X_BEFORE_MAXIMIZE;
import static wei.mark.standout.ui.Window.WindowDataKeys.Y_BEFORE_MAXIMIZE;

public class QuickTranslate extends StandOutWindow implements View.OnTouchListener {

    private static final String TAG = "QuickTranslate";
    private static StandOutLayoutParams openedParams;

    private static final int OPENED_WIDTH = 800;
    private static final int OPENED_HEIGHT = 650;

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
        // create a new layout from body.xml
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.quick_translate, frame, true);

        view.findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close(id);
            }
        });
        final Button optionBtn = view.findViewById(R.id.optionBtn);
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
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // handle dragging to move
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
                } else {
                    data.putBoolean(IS_MAXIMIZED, true);
                    data.putInt(WIDTH_BEFORE_MAXIMIZE, params.width);
                    data.putInt(HEIGHT_BEFORE_MAXIMIZE, params.height);
                    data.putInt(X_BEFORE_MAXIMIZE, params.x);
                    data.putInt(Y_BEFORE_MAXIMIZE, params.y);
                    getWindow(id).edit().setSize(1f, 1f).setPosition(0, 0).commit();
                }
            }
        });
    }

    @Override
    public StandOutLayoutParams getParams(int id, wei.mark.standout.ui.Window window) {
        return getParams(id);
    }

    public StandOutLayoutParams getParams(int id) {
        return getOpenedParams(id);
    }

    private StandOutLayoutParams getOpenedParams(int id) {
        openedParams = new StandOutLayoutParams(id, OPENED_WIDTH, OPENED_HEIGHT);
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
    public boolean onClose(int id, Window window) {
        Log.d(TAG, "window closing");
        stopSelf();
        return super.onClose(id, window);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public int getThemeStyle() {
        return android.R.style.Theme_Holo_Light_NoActionBar;
    }
}
