package com.songtaste.weeklist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.songtaste.weeklist.api.StTrackInfo;
import com.songtaste.weeklist.api.TrackInfo;

import java.util.ArrayList;
import java.util.List;

class TracklistAdapter extends BaseAdapter {
    private String playedSongnName = "";
    List<? extends TrackInfo> trackInfoList = new ArrayList<>();
    Context context;

    public TracklistAdapter(Context context) {
        this.context = context;
    }

    public void setPlayedSongnName(String playedSongnName) {
        this.playedSongnName = playedSongnName;
    }

    @Override
    public int getCount() {
        return trackInfoList.size();
    }

    @Override
    public TrackInfo getItem(int position) {
        return trackInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_weeklist, null);
            holder = new ViewHolder();
            holder.songname = (TextView) convertView.findViewById(R.id.weeklist_item_songname_textview);
            holder.position = (TextView) convertView.findViewById(R.id.weeklist_item_position_textview);
            holder.uploader = (TextView) convertView.findViewById(R.id.weeklist_item_uploader_textview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.songname.setText(trackInfoList.get(position).getTrackName());
        holder.position.setText("" + position);
        holder.uploader.setText(trackInfoList.get(position).getDescribe());

        if (playedSongnName.equals(trackInfoList.get(position).getUrl())) {
            convertView.setBackgroundColor(Color.parseColor("#cccccc"));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    public void upDateData(List<? extends TrackInfo> trackInfoList) {
        if (trackInfoList == null) {
            trackInfoList.clear();
            return;
        }

        this.trackInfoList = trackInfoList;
    }

    public static class ViewHolder {
        public TextView songname;
        public TextView position;
        public TextView uploader;
    }
}