package com.songtaste.weeklist;

import android.test.AndroidTestCase;

import com.songtaste.weeklist.api.Api;
import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;

import junit.framework.Assert;

import java.text.ParseException;
import java.util.List;

/**
 * Created by axlecho on 2015/2/8.
 */

/*
 * 测试用例
 * WL("20","27158","不要说爱我 ","张震岳 ","0", "17468","薄荷的糖","33","43");
 */

public class ParseTest extends AndroidTestCase {

    public void testGetWeeklist() {
        List<SongInfo> SongList = Api.getWeeklist("2009-09-28");
        Assert.assertEquals(50, SongList.size());
        SongInfo si = SongList.get(19); // 第20首歌
        Assert.assertEquals(20, si.getIdx());
        Assert.assertEquals("不要说爱我 ", si.getSongPath());
        LogUtil.d(si.toString());
    }

    public void testGetMp3Url() {
        String url = Api.getMp3Url(27158);
        Assert.assertEquals("可能失败，有时间戳貌似",
                "http://media5.songtaste.com/201502092108/ba2ba9071c295b72200ab83ce37a31e2/5/53/53984a5ef68469e9ede2896f823ad576.mp3",
                url);
    }

    public void testGetDateList() {
        try {
            List<String> datelist = Api.getDateList();
            LogUtil.d(datelist);
        } catch (ParseException e) {
            LogUtil.e("结尾日期格式有问题");
            e.printStackTrace();
        }
    }
}
