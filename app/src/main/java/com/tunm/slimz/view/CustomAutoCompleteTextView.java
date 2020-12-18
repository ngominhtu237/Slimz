package com.tunm.slimz.view;

import android.content.Context;
import android.graphics.Rect;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;

public class CustomAutoCompleteTextView extends AppCompatAutoCompleteTextView {
    public CustomAutoCompleteTextView(Context context) {
        super(context);
    }

    public CustomAutoCompleteTextView(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public CustomAutoCompleteTextView(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null ) {
            performFiltering(getText(), 0);
        }
    }
}
