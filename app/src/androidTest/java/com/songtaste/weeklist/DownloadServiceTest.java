package com.songtaste.weeklist;

import android.content.Intent;
import android.test.ServiceTestCase;

import com.songtaste.weeklist.api.STWeeklistApi;
import com.songtaste.weeklist.api.StTrackInfo;
import com.songtaste.weeklist.utils.LogUtil;

/**
 * Created by axlecho on 2015/2/14.
 */
public class DownloadServiceTest extends ServiceTestCase<DownloadService> {

    public DownloadServiceTest() {
        super(DownloadService.class);
    }

    public void testDownloadService() throws InterruptedException {
        Intent intent = new Intent();
        intent.setClassName("com.songtaste.weeklist", DownloadService.class.getName());
        DownloadService downloadService = ((DownloadService.DownloadBinder) bindService(intent)).getService();
        for (int i = 0; i < 20; i++) {
            StTrackInfo si = STWeeklistApi.getWeeklist("2009-09-28").get(i);
            LogUtil.d(si);
            downloadService.addToDownloadList(si);
        }

        downloadService.downloadnext();

        for (DownloadJob job : downloadService.getDownloadJobList()) {
            job.addDownloadListener(new DownloadJob.OnDownloadListener() {
                @Override
                public void onProgressUpdate(Integer process) {
                }

                @Override
                public void onDownloadComplete(StTrackInfo stTrackInfo) {
                    LogUtil.d("downloadComplete");
                }

                @Override
                public void onDownloadFailed(String errorinfo) {

                }
            });
        }

        synchronized (downloadService) {
            downloadService.wait();
        }
    }

    public void testDownloadServiceInActivity() throws InterruptedException {
        Intent intent = new Intent();
        intent.setClassName("com.songtaste.weeklist", DownloadServiceTestActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);

        synchronized (this) {
            wait();
        }
    }
}
