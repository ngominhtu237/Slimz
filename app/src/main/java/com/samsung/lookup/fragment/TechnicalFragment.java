package com.samsung.lookup.fragment;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.lookup.R;

/**
 * A quick_translate {@link Fragment} subclass.
 */
public class TechnicalFragment extends Fragment {


    public TechnicalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_technical, container, false);
    }

}
