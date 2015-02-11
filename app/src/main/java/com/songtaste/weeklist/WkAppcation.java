package com.songtaste.weeklist;

import android.app.Application;

import com.songtaste.weeklist.api.SongInfo;

import java.util.List;

/**
 * Created by axlecho on 2015/2/11.
 */
public class WkAppcation extends Application {
    private List<SongInfo> songInfoList;

    public List<SongInfo> getSongInfoList() {
        return songInfoList;
    }

    public void setSongInfoList(List<SongInfo> songInfoList) {
        this.songInfoList = songInfoList;
    }
}
