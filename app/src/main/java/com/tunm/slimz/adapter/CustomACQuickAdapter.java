package com.tunm.slimz.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tunm.slimz.R;

import java.util.ArrayList;
import java.util.List;

public class CustomACQuickAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private ArrayList<String> mWordArrayList;
    private ArrayList<String> mWordArrayListAll;
    private int mLayoutResourceId;
    public boolean isNeedToChange;
    private WordDetailsInterface mWordDetailsInterface;

    public CustomACQuickAdapter(@NonNull Context context, WordDetailsInterface wordDetailsInterface, int resource, @NonNull ArrayList<String> wordArrayList) {
        super(context, resource, wordArrayList);
        this.mContext = context;
        this.mWordDetailsInterface = wordDetailsInterface;
        this.mLayoutResourceId = resource;
        this.mWordArrayList = new ArrayList<>(wordArrayList);
        this.mWordArrayListAll = new ArrayList<>(wordArrayList);
    }

    public void setNewData(ArrayList<String> newData) {
        mWordArrayList.clear();
        mWordArrayListAll.clear();
        mWordArrayList = newData;
        mWordArrayListAll = newData;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mWordArrayList.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return mWordArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return wordFilter;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            v = inflater.inflate(mLayoutResourceId, parent, false);
        }
        ImageView iconSearchSuggest = v.findViewById(R.id.iconSearchSuggest);
        ImageView iconHistorySuggest = v.findViewById(R.id.iconHistorySuggest);
        TextView tvSuggestWord = v.findViewById(R.id.tvSuggestWord);
        v.findViewById(R.id.iconArrowContainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWordDetailsInterface.generateWord(mWordArrayList.get(position));
            }
        });
        tvSuggestWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWordDetailsInterface.openWord(mWordArrayList.get(position));
            }
        });
        iconSearchSuggest.setVisibility(isNeedToChange ? View.GONE : View.VISIBLE);
        iconHistorySuggest.setVisibility(isNeedToChange ? View.VISIBLE : View.GONE);
        tvSuggestWord.setText(mWordArrayList.get(position));
        return v;
    }
    public interface WordDetailsInterface {
        void openWord(String word);
        void generateWord(String word);
    }

    private Filter wordFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            ArrayList<String> wordsSuggestion = new ArrayList<>();
            if (constraint != null) {
                for (String wordName : mWordArrayListAll) {
                    if (wordName.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        wordsSuggestion.add(wordName);
                    }
                }
                filterResults.values = wordsSuggestion;
                filterResults.count = wordsSuggestion.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mWordArrayList.clear();
            if (results != null && results.count > 0) {
                // avoids unchecked cast warning when using mDepartments.addAll((ArrayList<Department>) results.values);
                for (Object object : (List<?>) results.values) {
                    if (object instanceof String) {
                        mWordArrayList.add(object.toString());
                    }
                }
                notifyDataSetChanged();
            } else if (constraint == null) {
                // no filter, add entire original list back in
                mWordArrayList.addAll(mWordArrayListAll);
                notifyDataSetInvalidated();
            }
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return resultValue.toString();
        }
    };
}
