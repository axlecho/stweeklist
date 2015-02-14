package com.songtaste.weeklist;

import android.content.Intent;
import android.test.InstrumentationTestCase;

import com.songtaste.weeklist.utils.LogUtil;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by axlecho on 2015/2/13.
 */
public class DownloadTest extends InstrumentationTestCase {

    private Lock lock = new ReentrantLock();// 锁对象

    public void testDownloadJob() throws InterruptedException {
        DownloadJob dj;
        Intent intent = new Intent();
        intent.setClassName("com.songtaste.weeklist", TestActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TestActivity testActivity = (TestActivity) getInstrumentation()
                .startActivitySync(intent);
        synchronized (testActivity) {
            testActivity.wait();
        }

        LogUtil.d("totalsize" + testActivity.getDownloadJob().getTotalSize());

    }
}
