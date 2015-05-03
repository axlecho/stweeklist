package com.songtaste.weeklist;

import android.app.Application;

import com.songtaste.weeklist.api.TrackInfo;
import com.songtaste.weeklist.utils.SqlUtil;

import java.util.List;

/**
 * Created by axlecho on 2015/2/11.
 */
public class WkAppcation extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SqlUtil.init(getApplicationContext());
    }

    private List<? extends TrackInfo> trackInfoList;

    public List<? extends TrackInfo> getTrackInfoList() {
        return trackInfoList;
    }

    public void setTrackInfoList(List<? extends TrackInfo> trackInfoList) {
        this.trackInfoList = trackInfoList;
    }
}
