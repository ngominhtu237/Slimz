package com.tunm.slimz.fragment.base;

import android.app.Activity;
import android.content.Context;
import androidx.fragment.app.Fragment;

/**
 * Created by tu.nm1 on 14,December,2020
 */
public class BaseFragment extends Fragment {
    protected Activity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }
}
