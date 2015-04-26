package com.songtaste.weeklist;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.songtaste.weeklist.api.SongInfo;

import java.util.ArrayList;
import java.util.List;

class WeeklistAdapter extends BaseAdapter {
    private String playedSongnName = "";
    List<SongInfo> songInfoList = new ArrayList<>();
    Context context;

    public WeeklistAdapter(Context context) {
        this.context = context;
    }

    public void setPlayedSongnName(String playedSongnName) {
        this.playedSongnName = playedSongnName;
    }

    @Override
    public int getCount() {
        return songInfoList.size();
    }

    @Override
    public SongInfo getItem(int position) {
        return songInfoList.get(position);
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


        holder.songname.setText(songInfoList.get(position).getSongName());
        holder.position.setText("" + position);
        holder.uploader.setText(songInfoList.get(position).getUName());

        if (playedSongnName.equals(songInfoList.get(position).getSongPath())) {
            convertView.setBackgroundColor(Color.parseColor("#cccccc"));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
        return convertView;
    }

    public void upDateData(List<SongInfo> songInfoList) {
        if (songInfoList == null) {
            songInfoList.clear();
            return;
        }

        this.songInfoList = songInfoList;
    }

    public static class ViewHolder {
        public TextView songname;
        public TextView position;
        public TextView uploader;
    }
}