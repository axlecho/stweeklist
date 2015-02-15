package com.songtaste.weeklist.api;

import java.io.Serializable;

/**
 * Created by axlecho on 2015/2/9.
 */
public class SongInfo implements Serializable {
    private int Idx;            // 周排名
    private int SongID;         // 歌曲Id
    private String SongName;    // 歌曲名称
    private String Singer;      // 歌手名称
    private int Sex;            // 性别 1男 2女 0未明
    private int UID;            // 上传者Id
    private String UName;       // 上传者名称
    private int Width;          // ？？？
    private int LsnNum;         // 点赞数

    /**
     * @param str 从服务器抓下的信息
     *            WL("1","3469833","Fade＜无限循环极品 这一刻 唤醒了2014所有的痛苦与极乐＞ ","稀罕谁 ","2", "5007036","稀罕谁","220","52");
     * @return 歌曲信息
     */
    public static SongInfo buildFromString(String str) {
        SongInfo songInfo = new SongInfo();
        String[] paramlist = str.split("\",\"|\", \"");
        songInfo.Idx = Integer.parseInt(paramlist[0].replaceAll("\\D", ""));
        songInfo.SongID = Integer.parseInt(paramlist[1].replaceAll("\\D", ""));
        songInfo.SongName = paramlist[2].replaceAll("\"", "");
        songInfo.Singer = paramlist[3].replaceAll("\"", "");
        songInfo.Sex = Integer.parseInt(paramlist[4].replaceAll("\\D", ""));
        songInfo.UID = Integer.parseInt(paramlist[5].replaceAll("\\D", ""));
        songInfo.UName = paramlist[6].replaceAll("\"", "");
        songInfo.Width = Integer.parseInt(paramlist[7].replaceAll("\\D", ""));
        songInfo.LsnNum = Integer.parseInt(paramlist[8].replaceAll("\\D", ""));
        return songInfo;
    }

    @Override
    public String toString() {
        return "[Idx:" + Idx + " SongID:" + SongID + " SongName:" + SongName + " Singer:" + Singer
                + " Sex:" + Sex + " UID:" + UID + " UName:" + UName + " Width:" + Width + " LsnNum:"
                + LsnNum + "]";
    }

    public int getIdx() {
        return Idx;
    }

    public String getSongName() {
        return SongName;
    }

    public int getSongID() {
        return SongID;
    }

    public String getUName() {
        return UName;
    }
}
