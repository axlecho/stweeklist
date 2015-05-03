package com.songtaste.weeklist.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by axlecho on 2015/5/2.
 */
public class DoufmTrackInfo implements TrackInfo {

    private String key; // 歌曲key
    private String title; // 歌曲名
    private String artist; // 艺术家
    private String album; // 专辑
    private String company; // 唱片公司
    private String public_time; // 出版年份
    private String kbps;// 码率
    private String cover; // 专辑封面URL
    private String audio; // 音频URL
    private String upload_date; //上传时间

    // Todo
    public static DoufmTrackInfo buildFromJson(String json) throws JSONException {
        JSONObject doufmTrackJsonObject = new JSONObject(json);
        DoufmTrackInfo doufmTrackInfo = new DoufmTrackInfo();
        doufmTrackInfo.key = doufmTrackJsonObject.getString("key");
        doufmTrackInfo.title = doufmTrackJsonObject.getString("title");
        doufmTrackInfo.artist = doufmTrackJsonObject.getString("artist");
        doufmTrackInfo.album = doufmTrackJsonObject.getString("album");
        doufmTrackInfo.company = doufmTrackJsonObject.getString("company");
        doufmTrackInfo.public_time = doufmTrackJsonObject.getString("public_time");
        doufmTrackInfo.kbps = doufmTrackJsonObject.getString("kbps");
        doufmTrackInfo.cover = doufmTrackJsonObject.getString("cover");
        doufmTrackInfo.audio = doufmTrackJsonObject.getString("audio");
        doufmTrackInfo.upload_date = doufmTrackJsonObject.getString("upload_date");
        return doufmTrackInfo;
    }

    @Override
    public String getTrackName() {
        return title;
    }

    @Override
    public String getDescribe() {
        return artist;
    }

    @Override
    public int getWidth() {
        return -1;
    }

    @Override
    public String getUrl() {
        return audio;
    }
}
