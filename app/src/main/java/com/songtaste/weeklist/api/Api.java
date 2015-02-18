package com.songtaste.weeklist.api;

import com.songtaste.weeklist.utils.LogUtil;
import com.songtaste.weeklist.utils.NetworkUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by axlecho on 2015/2/9.
 */
public class Api {

    private static String errorString;

    public static List<String> getDateList() throws ParseException {
        List<String> dateList = new ArrayList<>();
        Calendar dNow = Calendar.getInstance();
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
        sdf.applyPattern("yyyy-MM-dd");

        Date endDate = sdf.parse(ServerConst.ENDDATESTRING);
        while (dNow.getTime().compareTo(endDate) > 0) {
            dateList.add(sdf.format(dNow.getTime()));
            dNow.add(Calendar.DAY_OF_YEAR, -7);
        }
        return dateList;
    }

    public static List<SongInfo> getWeeklist() {
        Date dNow = new Date();
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
        sdf.applyPattern("yyyy-MM-dd");
        String date = sdf.format(dNow);
        LogUtil.d(date);
        return getWeeklist(date);
    }

    public static List<SongInfo> getWeeklist(String date) {
        String url = String.format(ServerConst.WEEKLIST, date);
        String htmlDocument = NetworkUtil.getInstance().get(url);
        if (htmlDocument == null) {
            return null;
        }

        List<SongInfo> songInfoList = new ArrayList<>();
        for (String line : htmlDocument.split("\n")) {
            if (line.matches("^WL.*")) {
                SongInfo si = SongInfo.buildFromString(line);
                songInfoList.add(si);
                LogUtil.i(line);
            }
        }

        return songInfoList;
    }

    public static String getMp3Url(int id) {
        String url = String.format(ServerConst.PLAYER, id);
        String htmlDocument = NetworkUtil.getInstance().get(url);

        if (htmlDocument == null) {
            return null;
        }

        String mp3Url = "";
        for (String line : htmlDocument.split("\n")) {
            if (line.matches("^WrtSongLine.*")) {
                LogUtil.d(line);
                mp3Url = praseUrl(line);
            }
        }

        mp3Url = mp3Url.replace("mediag", "media5");
        return mp3Url;
    }

    private static String praseUrl(String str) {
        for (String item : str.split(",")) {
            item = item.replaceAll("\"|\\s", "");
            if (item.matches("^http.*")) {
                LogUtil.i(item);
                return item;
            }
        }
        return "";
    }

    public static String getError() {
        String ret = errorString;
        errorString = null;
        return ret != null ? ret : NetworkUtil.getError();
    }
}
