package com.songtaste.weeklist.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by axlecho on 2015/4/24.
 */
public class SqlUtil {
    public static void init(Context context) {
        LogUtil.d("create new table");

        SQLiteDatabase db = context.openOrCreateDatabase("music.db", Context.MODE_PRIVATE, null);
        try {
            db.execSQL("create table musicpath (path varchar primary key)");
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        db.close();
        LogUtil.d("init ok");
    }

    public static void saveMusicPath(Context context, String path) {
        SQLiteDatabase db = context.openOrCreateDatabase("music.db", Context.MODE_PRIVATE, null);
        ContentValues cv = new ContentValues();
        cv.put("path", path);
        db.insert("musicpath", null, cv);
        db.close();
    }

    public static List<String> getMusicPath(Context context) {
        List<String> musicPathList = new ArrayList<>();
        SQLiteDatabase db = context.openOrCreateDatabase("music.db", Context.MODE_PRIVATE, null);
        Cursor c = db.rawQuery("select * from musicpath", null);
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String musicPath = c.getString(c.getColumnIndex("path"));
            LogUtil.d(musicPath);
            musicPathList.add(musicPath);
        }
        return musicPathList;
    }
}
