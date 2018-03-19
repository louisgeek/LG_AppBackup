package com.louisgeek.appbackup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by classichu on 2018/3/16.
 */

public class MyBaseAdapter extends BaseAdapter {
    private List<AppBean> mAppBeanList;

    public MyBaseAdapter(List<AppBean> appBeanList) {
        mAppBeanList = appBeanList;
    }

    @Override
    public int getCount() {
        return mAppBeanList.size();
    }

    @Override
    public AppBean getItem(int position) {
        return mAppBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            viewHolder = new MyViewHolder();
            viewHolder.mImageView = convertView.findViewById(R.id.id_iv);
            viewHolder.mTextView = convertView.findViewById(R.id.id_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyViewHolder) convertView.getTag();
        }
        AppBean appBean = mAppBeanList.get(position);
        viewHolder.mImageView.setImageDrawable(appBean.mDrawable);
        viewHolder.mTextView.setText(appBean.mName + "\n\n"+appBean.mPackageName);

        return convertView;
    }

    class MyViewHolder {
        ImageView mImageView;

        TextView mTextView;
    }

    public void refreshData(List<AppBean> appBeanList) {
        mAppBeanList.clear();
        mAppBeanList.addAll(appBeanList);
        notifyDataSetChanged();
    }
}
