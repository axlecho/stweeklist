package com.songtaste.weeklist;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.songtaste.weeklist.api.Api;
import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;


public class TestActivity extends Activity {

    private DownloadJob dj;

    DownloadJob.OnDownloadListener listener = new DownloadJob.OnDownloadListener() {
        @Override
        public void onProgressUpdate(Integer process) {
            LogUtil.d(process);
        }

        @Override
        public void onDownloadComplete(SongInfo songInfo) {
            LogUtil.d(songInfo);
            TestActivity.this.notify();
        }

        @Override
        public void onDownloadFailed(String errorInfo) {
            LogUtil.d(errorInfo);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                dj = new DownloadJob(TestActivity.this, Api.getWeeklist("2009-09-28").get(19));
                dj.addDownloadListener(listener);
                dj.start();
                return null;
            }
        }.execute();

    }

    public DownloadJob getDownloadJob() {
        return dj;
    }

}
