package com.songtaste.weeklist.utils;

import android.content.Context;

import com.songtaste.weeklist.api.LocalTrackInfo;
import com.songtaste.weeklist.api.StTrackInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by axlecho on 2015/4/22.
 */
public class LocalFileUtil {

    /**
     * 扫描数据库里保存的文件夹下的所有mp3 (非递归)
     *
     * @param context
     * @return 封装后的mp3信息
     */
    public static List<LocalTrackInfo> scanMusic(Context context) {
        List<LocalTrackInfo> localTrackInfoList = new ArrayList<>();
        List<String> dirStringList = SqlUtil.getMusicPath(context);
        for (String dirString : dirStringList) {
            localTrackInfoList.addAll(scanMusic(dirString));
        }
        return localTrackInfoList;
    }

    /**
     * 扫描一个文件夹下的所有mp3文件 (非递归)
     *
     * @param dirString 文件夹路径
     * @return 封装后的mp3信息
     */

    public static List<LocalTrackInfo> scanMusic(String dirString) {

        List<LocalTrackInfo> localTrackInfoList = new ArrayList<>();
        File dir = new File(dirString);
        if (!dir.isDirectory()) {
            LogUtil.d(dirString + "is not a directory.");
            return localTrackInfoList;
        }

        String[] musicStringList = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(".mp3");
            }
        });

        for (String musicString : musicStringList) {
            LocalTrackInfo lti = LocalTrackInfo.buildFromLocalFileName(dirString + musicString);
            localTrackInfoList.add(lti);
        }
        return localTrackInfoList;
    }

    public static String getLyric(String songPath) {

        String lyricString = "未找到歌词";

        String temp[] = songPath.replaceAll("////", "/").split("/");
        if (temp.length <= 1) {
            return lyricString;
        }

        String fileName = temp[temp.length - 1];
        String songName = fileName.substring(0, fileName.lastIndexOf("."));
        String dirName = songPath.substring(0, songPath.lastIndexOf("/") + 1);

        LogUtil.d("dir:" + dirName + " songName" + songName);

        try {
            InputStreamReader read = new InputStreamReader(new FileInputStream(dirName + songName + ".lrc"));
            BufferedReader reader = new BufferedReader(read);
            StringBuffer lyricBuffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                lyricBuffer.append(line);
                lyricBuffer.append("\n");
            }
            lyricString = lyricBuffer.toString();

        } catch (FileNotFoundException e) {
            LogUtil.e("文件未找到:" + dirName + songName + ".lrc");
            e.printStackTrace();
        } catch (IOException e) {
            LogUtil.e("读取文件失败");
            e.printStackTrace();
        }

        return lyricString;
    }
}
