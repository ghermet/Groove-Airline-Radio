package com.guillaume_hermet.www.grooveairlineradio.adapters;

/**
 * Created by Guillaume on 10/4/16.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guillaume_hermet.www.grooveairlineradio.R;
import com.guillaume_hermet.www.grooveairlineradio.models.ActionButton;

import java.util.List;

import co.dift.ui.SwipeToAction;


public class ButtonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<ActionButton> items;


    /**
     * References to the views for each data item
     **/
    private class BookViewHolder extends SwipeToAction.ViewHolder<ActionButton> {
        private final ImageView actionLogoLeft;
        private final ImageView actionLogoRight;
        TextView titleView;
        RelativeLayout actionBackgroundRight;
        RelativeLayout actionBackgroundLeft;

        BookViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.title);
            actionBackgroundLeft = (RelativeLayout) v.findViewById(R.id.actionBackgoundLeft);
            actionLogoLeft = (ImageView) v.findViewById(R.id.actionLogoLeft);
            actionBackgroundRight = (RelativeLayout) v.findViewById(R.id.actionBackgoundRight);
            actionLogoRight = (ImageView) v.findViewById(R.id.actionLogoRight);

        }
    }

    /**
     * Constructor
     **/
    public ButtonAdapter(Context mContext, List<ActionButton> items) {
        this.mContext = mContext;
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ActionButton item = items.get(position);
        BookViewHolder vh = (BookViewHolder) holder;
        vh.titleView.setText(item.getTitle());
        Drawable drawable = mContext.getResources().getDrawable(item.getBackground());
        vh.actionBackgroundLeft.setBackground(drawable);
        vh.actionLogoLeft.setImageDrawable(mContext.getResources().getDrawable(item.getLogoLeft()));
        vh.actionBackgroundRight.setBackground(drawable);
        vh.actionLogoRight.setImageDrawable(mContext.getResources().getDrawable(item.getLogoRight()));
        vh.data = item;
    }
}