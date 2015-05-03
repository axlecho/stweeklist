package com.songtaste.weeklist.api;

/**
 * Created by axlecho on 2015/5/2.
 */
public class LocalTrackInfo implements TrackInfo {
    private String trackName;    // 歌曲名称
    private String trackPath;

    public static LocalTrackInfo buildFromLocalFileName(String str) {
        LocalTrackInfo localTrackInfo = new LocalTrackInfo();
        localTrackInfo.trackName = str.substring(str.lastIndexOf("/") + 1, str.lastIndexOf("."));
        localTrackInfo.trackPath = str;
        return localTrackInfo;
    }

    @Override
    public String getTrackName() {
        return trackName;
    }

    //TODO 获取音轨中的信息
    @Override
    public String getDescribe() {
        return "unknow";
    }

    @Override
    public int getWidth() {
        return -1;
    }

    @Override
    public String getUrl() {
        return trackPath;
    }
}
