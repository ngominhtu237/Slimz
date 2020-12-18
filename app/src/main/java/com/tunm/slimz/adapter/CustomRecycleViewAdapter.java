package com.tunm.slimz.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tunm.slimz.R;

public class CustomRecycleViewAdapter extends RecyclerView.Adapter<CustomRecycleViewAdapter.ViewHolder> {

    private Context mContext;
    private String[] mTitle;
    private int[] arr_items_color;
    private int[] arr_items_icon = {
            R.mipmap.popup,
            R.mipmap.history,
            R.mipmap.icon_star,
            R.mipmap.translate,
            R.mipmap.irr_verb,
            R.mipmap.setttings,
            R.mipmap.icon_feedback
    };

    public CustomRecycleViewAdapter(Context mContext, String[] mTitle) {
        this.mContext = mContext;
        this.mTitle = mTitle;
        arr_items_color = mContext.getResources().getIntArray(R.array.arr_items_color);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);

        DisplayMetrics displaymetrics = new DisplayMetrics();

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaymetrics);
        int deviceWidth = (int) ((displaymetrics.widthPixels / 3) * 1.2);
        view.setMinimumWidth(deviceWidth);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tvTitle.setText(mTitle[position]);
        holder.ivIcon.setImageResource(arr_items_icon[position]);
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setBackgroundColor(mContext.getResources().getColor(R.color.md_grey_100));
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        view.setBackgroundColor(mContext.getResources().getColor(R.color.md_grey_100));
                        break;
                }
                return true;
            }
        });

        // int itemColor = arr_items_color[new Random().nextInt(arr_items_color.length)];
        int itemColor = arr_items_color[position];
        Drawable background = holder.ivIcon.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(itemColor);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(itemColor);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(itemColor);
        }
    }

    @Override
    public int getItemCount() {
        return mTitle.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivIcon;
        TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            this.ivIcon = itemView.findViewById(R.id.profile_image);
            this.tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
