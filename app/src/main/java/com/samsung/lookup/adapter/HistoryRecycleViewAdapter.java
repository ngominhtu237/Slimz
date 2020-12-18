package com.samsung.lookup.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import com.samsung.lookup.R;
import com.samsung.lookup.activity.HistoryWordActivity;

import java.util.ArrayList;

public class HistoryRecycleViewAdapter extends RecyclerView.Adapter<HistoryRecycleViewAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "HistoryRVAdapter";
    private Context mContext;
    private ArrayList<String> mListHistoryWord;
    private ArrayList<String> mListHistoryWordFull;

    public HistoryRecycleViewAdapter(Context mContext, ArrayList<String> mListHistoryWord) {
        this.mContext = mContext;
        this.mListHistoryWord = mListHistoryWord;
        mListHistoryWordFull = new ArrayList<>(mListHistoryWord);
    }

    public void swap(ArrayList<String> datas)
    {
        mListHistoryWord.clear();
        mListHistoryWordFull.clear();
        mListHistoryWord.addAll(datas);
        mListHistoryWordFull.addAll(datas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_history, parent, false);
        return new HistoryRecycleViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.tvWordName.setText(mListHistoryWord.get(position));
        holder.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HistoryWordActivity)mContext).showDeletePopup(holder.getAdapterPosition());
            }
        });
        holder.tvWordName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HistoryWordActivity)mContext).openWord(mListHistoryWord.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListHistoryWord.size();
    }

    @Override
    public Filter getFilter() {
        return historyWordFilter;
    }

    public void removeItemAnimation(int position) {
        mListHistoryWord.remove(position);
        mListHistoryWordFull.remove(position);
        notifyItemRemoved(position);
    }

    private Filter historyWordFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<String> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0) {
                filteredList.addAll(mListHistoryWordFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (String item: mListHistoryWordFull) {
                    if(item.toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mListHistoryWord.clear();
            mListHistoryWord.addAll((ArrayList) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvWordName;
        ImageButton btDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvWordName = itemView.findViewById(R.id.tvHistoryWordName);
            this.btDelete = itemView.findViewById(R.id.btHistoryDeleteButton);
        }
    }
}
