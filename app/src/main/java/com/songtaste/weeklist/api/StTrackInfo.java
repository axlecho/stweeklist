package com.songtaste.weeklist.api;

/**
 * Created by axlecho on 2015/2/9.
 */
public class StTrackInfo implements TrackInfo {
    private int Idx;            // 周排名
    private int SongID;         // 歌曲Id
    private String SongName;    // 歌曲名称
    private String Singer;      // 歌手名称
    private int Sex;            // 性别 1男 2女 0未明
    private int UID;            // 上传者Id
    private String UName;       // 上传者名称
    private int Width;          // ？？？
    private int LsnNum;         // 点赞数

    private StTrackInfo() {
    }

    /**
     * @param str 从服务器抓下的信息
     *            WL("1","3469833","Fade＜无限循环极品 这一刻 唤醒了2014所有的痛苦与极乐＞ ","稀罕谁 ","2", "5007036","稀罕谁","220","52");
     * @return 歌曲信息
     */
    public static StTrackInfo buildFromString(String str) {
        StTrackInfo stTrackInfo = new StTrackInfo();
        String[] paramlist = str.split("\",\"|\", \"");
        stTrackInfo.Idx = Integer.parseInt(paramlist[0].replaceAll("\\D", ""));
        stTrackInfo.SongID = Integer.parseInt(paramlist[1].replaceAll("\\D", ""));
        stTrackInfo.SongName = paramlist[2].replaceAll("\"", "");
        stTrackInfo.Singer = paramlist[3].replaceAll("\"", "");
        stTrackInfo.Sex = Integer.parseInt(paramlist[4].replaceAll("\\D", ""));
        stTrackInfo.UID = Integer.parseInt(paramlist[5].replaceAll("\\D", ""));
        stTrackInfo.UName = paramlist[6].replaceAll("\"", "");
        stTrackInfo.Width = Integer.parseInt(paramlist[7].replaceAll("\\D", ""));
        stTrackInfo.LsnNum = Integer.parseInt(paramlist[8].replaceAll("\\D", ""));
        return stTrackInfo;
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

    public int getSongID() {
        return SongID;
    }

    @Override
    public String getTrackName() {
        return SongName;
    }

    @Override
    public String getDescribe() {
        return UName;
    }

    @Override
    public int getWidth() {
        return Width;
    }

    @Override
    public String getUrl() {
        return "";
    }
}
