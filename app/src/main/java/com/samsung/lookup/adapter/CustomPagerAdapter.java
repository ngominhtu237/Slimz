package com.samsung.lookup.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.samsung.lookup.IClickWordCallback;
import com.samsung.lookup.R;
import com.samsung.lookup.utils.WebViewUtils;

import static com.samsung.lookup.utils.HtmlUtils.createStyleIntro;

public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private String enViDetails = createStyleIntro("Eng - Viet");
    private String enEnDetails = createStyleIntro("Eng - Eng");
    private String synonymDetails = createStyleIntro("Synonym");
    private CustomACQuickAdapter.WordDetailsInterface mWordDetailsInterface;

    public CustomPagerAdapter(Context context, CustomACQuickAdapter.WordDetailsInterface wordDetailsInterface) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mWordDetailsInterface = wordDetailsInterface;
    }

    @Override
    public int getCount() {
        return CustomPagerEnum.values().length;
    }

    public void setEnViDetails(String enViDetails) {
        this.enViDetails = enViDetails;
    }

    public void setEnEnDetails(String enEnDetails) {
        this.enEnDetails = enEnDetails;
    }

    public void setSynonymDetails(String synonymDetails) {
        this.synonymDetails = synonymDetails;
    }

    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
        ViewGroup view = (ViewGroup) inflater.inflate(customPagerEnum.getLayoutResId(), collection, false);
        collection.addView(view);
        WebView mWebViewDetails = view.findViewById(R.id.wordDetailsTV);
        WebViewUtils.addScript(mContext, mWebViewDetails, callback);
        switch (position) {
            case 0:
                mWebViewDetails.loadData(enViDetails, "text/html; charset=utf-8", "UTF-8");
                break;
            case 1:
                mWebViewDetails.loadData(enEnDetails, "text/html; charset=utf-8", "UTF-8");
                break;
            case 2:
                mWebViewDetails.loadData(synonymDetails, "text/html; charset=utf-8", "UTF-8");
                break;
        }

        return view;
    }

    private final IClickWordCallback callback = new IClickWordCallback() {
        @Override
        public void open(String word) {
            mWordDetailsInterface.openWord(word);
        }
    };

    @Override
    public void destroyItem(ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        CustomPagerEnum customPagerEnum = CustomPagerEnum.values()[position];
        return mContext.getString(customPagerEnum.getTitleResId());
    }
}
