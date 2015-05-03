package com.songtaste.weeklist.api;

/**
 * Created by axlecho on 2015/5/2.
 */
public interface TrackInfo {
    public String getTrackName(); // 获取音轨名

    public String getDescribe(); // 获取描述

    public int getWidth(); // 获取音乐长度 毫秒

    public String getUrl(); // 获取实际播放的地址
}
