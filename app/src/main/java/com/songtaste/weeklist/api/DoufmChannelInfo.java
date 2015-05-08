package com.songtaste.weeklist.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by axlecho on 2015/5/3.
 */
public class DoufmChannelInfo {
    private String key;
    private int music_list;
    private String name;

    private DoufmChannelInfo() {
    }

    public static DoufmChannelInfo buildFromJson(String jsonString) throws JSONException {
        return buildFromJson(new JSONObject(jsonString));
    }

    public static DoufmChannelInfo buildFromJson(JSONObject jsonObject) throws JSONException {
        DoufmChannelInfo doufmChannelInfo = new DoufmChannelInfo();
        doufmChannelInfo.key = jsonObject.getString("key");
        doufmChannelInfo.music_list = jsonObject.getInt("music_list");
        doufmChannelInfo.name = jsonObject.getString("name");
        return doufmChannelInfo;
    }

    public String getKey() {
        return key;
    }

    public int getTrackNum() {
        return music_list;
    }

    public String getDescribe() {
        return name;
    }
}
