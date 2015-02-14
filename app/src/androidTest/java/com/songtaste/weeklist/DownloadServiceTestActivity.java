package com.songtaste.weeklist;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import com.songtaste.weeklist.api.Api;
import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.util.List;

/**
 * Created by axlecho on 2015/2/14.
 */
public class DownloadServiceTestActivity extends Activity {

    private DownloadService downloadService;
    private ServiceConnection serviceConnection;

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_test);

        tv = (TextView) findViewById(R.id.test);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.d("onServiceConnected");
                downloadService = ((DownloadService.DownloadBinder) service).getService();
                new AsyncTask<Integer, Void, List<SongInfo>>() {

                    @Override
                    protected List<SongInfo> doInBackground(Integer... params) {
                        LogUtil.d("get mp3 info");
                        return Api.getWeeklist("2009-09-28");
                    }

                    @Override
                    protected void onPostExecute(List<SongInfo> songInfoList) {
                        for (SongInfo songInfo : songInfoList) {
                            downloadService.addToDownloadList(songInfo);
                        }

                        for (DownloadJob job : downloadService.getDownloadJobList()) {

                            job.addDownloadListener(new DownloadJob.OnDownloadListener() {

                                @Override
                                public void onProgressUpdate(Integer process) {
                                    tv.setText(String.valueOf(process));
                                }

                                @Override
                                public void onDownloadComplete(SongInfo songInfo) {
                                    tv.setText("完成");
                                }

                                @Override
                                public void onDownloadFailed(String errorinfo) {

                                }
                            });
                        }

                        downloadService.downloadnext();
                        super.onPostExecute(songInfoList);
                    }
                }.execute();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }

        ;
    }

    @Override
    protected void onStart() {
        Intent intent = new Intent();
        intent.setClassName("com.songtaste.weeklist", DownloadService.class.getName());
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }
}
