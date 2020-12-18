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
import com.samsung.lookup.activity.MarkWordActivity;
import com.samsung.lookup.model.WorkMark;

import java.util.ArrayList;

public class FavoriteRecycleViewAdapter extends RecyclerView.Adapter<FavoriteRecycleViewAdapter.ViewHolder> implements Filterable {

    private Context mContext;
    private ArrayList<WorkMark> mArrWorkMark;
    private ArrayList<WorkMark> mArrWorkMarkFull;

    public FavoriteRecycleViewAdapter(Context mContext, ArrayList<WorkMark> mArrWorkMark) {
        this.mContext = mContext;
        this.mArrWorkMark = mArrWorkMark;
        mArrWorkMarkFull = new ArrayList<>(mArrWorkMark);
    }

    public void swap(ArrayList<WorkMark> datas)
    {
        mArrWorkMark.clear();
        mArrWorkMarkFull.clear();
        mArrWorkMark.addAll(datas);
        mArrWorkMarkFull.addAll(datas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_mark, parent, false);
        return new FavoriteRecycleViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Must to reset color
        holder.btColorYellow.setVisibility(View.GONE);
        holder.btColorBlue.setVisibility(View.GONE);
        holder.btColorPink.setVisibility(View.GONE);

        final WorkMark item = mArrWorkMark.get(position);
        holder.tvMarkWordName.setText(item.getWorkName());
        switch (item.getColor()) {
            case "yellow":
                holder.btColorYellow.setVisibility(View.VISIBLE);
                break;
            case "blue":
                holder.btColorBlue.setVisibility(View.VISIBLE);
                break;
            case "pink":
                holder.btColorPink.setVisibility(View.VISIBLE);
                break;
        }
        holder.tvMarkWordName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MarkWordActivity)mContext).openWord(item.getWorkName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArrWorkMark.size();
    }

    @Override
    public Filter getFilter() {
        return markWordFilter;
    }

    private Filter markWordFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<WorkMark> filteredList = new ArrayList<>();

            if(constraint == null || constraint.length() == 0) {
                filteredList.addAll(mArrWorkMarkFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (WorkMark workMark: mArrWorkMarkFull) {
                    if(workMark.getWorkName().toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(workMark);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mArrWorkMark.clear();
            mArrWorkMark.addAll((ArrayList<WorkMark>) filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvMarkWordName;
        ImageButton btColorYellow, btColorBlue, btColorPink ;

        public ViewHolder(View itemView) {
            super(itemView);
            this.tvMarkWordName = itemView.findViewById(R.id.tvMarkWordName);
            this.btColorYellow = itemView.findViewById(R.id.btMarkStarYellow);
            this.btColorBlue = itemView.findViewById(R.id.btMarkStarBlue);
            this.btColorPink = itemView.findViewById(R.id.btMarkStarPink);
        }
    }
}
