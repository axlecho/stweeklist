package com.songtaste.weeklist;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class DownloadService extends Service {

    public static final String ACTION_START_DOWNLOAD = "action_start_download";

    private List<DownloadJob> downloadJobList = new ArrayList<>();

    private boolean downloadFlag = false;

    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase(ACTION_START_DOWNLOAD)) {
                downloadnext();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    synchronized public void addToDownloadList(SongInfo songInfo) {
        DownloadJob downloadJob = new DownloadJob(this, songInfo);
        downloadJobList.add(downloadJob);
    }

    public void startDownload() {
        if (!downloadFlag) {
            downloadnext();
        }
    }

    public void downloadnext() {

        if (downloadJobList.size() == 0) {
            downloadFlag = false;
            return;
        }

        downloadFlag = true;
        final DownloadJob currentJob = downloadJobList.get(0);
        LogUtil.d("start download:" + currentJob.getSongInfo().getSongName());
        final DownloadJob.OnDownloadListener listener = new DownloadJob.OnDownloadListener() {

            @Override
            public void onProgressUpdate(Integer process) {
            }

            @Override
            public void onDownloadComplete(SongInfo songInfo) {
                LogUtil.d("download complete");
                downloadJobList.remove(currentJob);
                downloadnext();
            }

            @Override
            public void onDownloadFailed(String errorinfo) {
                LogUtil.d(errorinfo);
                Toast.makeText(DownloadService.this, errorinfo, Toast.LENGTH_LONG).show();
                downloadJobList.remove(currentJob);
                downloadnext();
            }
        };

        LogUtil.d(listener);
        currentJob.addDownloadListener(listener);
        AsyncTask currentTask = currentJob.start();
    }

    public List<DownloadJob> getDownloadJobList() {
        return downloadJobList;
    }
}
