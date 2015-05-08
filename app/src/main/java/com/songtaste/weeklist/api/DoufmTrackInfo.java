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

    public static DoufmTrackInfo buildFromJson(JSONObject jsonObject) throws JSONException {
        DoufmTrackInfo doufmTrackInfo = new DoufmTrackInfo();
        doufmTrackInfo.key = jsonObject.getString("key");
        doufmTrackInfo.title = jsonObject.getString("title");
        doufmTrackInfo.artist = jsonObject.getString("artist");
        doufmTrackInfo.album = jsonObject.getString("album");
        doufmTrackInfo.company = jsonObject.getString("company");
        doufmTrackInfo.public_time = jsonObject.getString("public_time");
        doufmTrackInfo.kbps = jsonObject.getString("kbps");
        doufmTrackInfo.cover = jsonObject.getString("cover");
        doufmTrackInfo.audio = jsonObject.getString("audio");
        doufmTrackInfo.upload_date = jsonObject.getString("upload_date");
        return doufmTrackInfo;
    }

    public static DoufmTrackInfo buildFromJson(String jsonString) throws JSONException {
        return buildFromJson(new JSONObject(jsonString));
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
        return ServerConst.DOUFM_HOST + audio;
    }
}
