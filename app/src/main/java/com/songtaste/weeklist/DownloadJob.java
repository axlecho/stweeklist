package com.songtaste.weeklist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.songtaste.weeklist.api.ServerConst;
import com.songtaste.weeklist.api.TrackInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by axlecho on 2015/2/13.
 */
public class DownloadJob {

    private Context context;
    private TrackInfo trackInfo;
    private String mp3UrlString;
    private String dirPath;
    private String filePath;
    private String errorInfo;
    private int totalSize;
    private int process;

    private List<OnDownloadListener> onDownloadListenerList = new ArrayList<>();

    public DownloadJob(Context context, TrackInfo trackInfo) {
        this.context = context;
        this.trackInfo = trackInfo;
        setDownLoadPostion();
    }

    public void addDownloadListener(OnDownloadListener listener) {
        if (listener != null) {
            onDownloadListenerList.add(listener);
        }
    }


    private void setDownLoadPostion() {
        String SDcardPath = Environment.getExternalStorageDirectory().getPath();
        dirPath = SDcardPath + File.separator
                + context.getResources().getString(R.string.app_name)
                + File.separator + ServerConst.FILEDIR;
        File dir = new File(dirPath);
        if (dir.exists() == false) {
            dir.mkdirs();
        }

        filePath = dirPath + File.separator + trackInfo.getTrackName() + ".mp3";
        LogUtil.d(filePath);
    }

    public DownloadAsyncTask start() {
        DownloadAsyncTask downloadAsyncTask = new DownloadAsyncTask();
        downloadAsyncTask.execute();
        return downloadAsyncTask;
    }

    public int getProcess() {
        return process;
    }

    public interface OnDownloadListener {
        public void onProgressUpdate(Integer process);

        public void onDownloadComplete(TrackInfo trackInfo);

        public void onDownloadFailed(String errorinfo);
    }

    class DownloadAsyncTask extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onCancelled(String s) {
            for (OnDownloadListener listener : onDownloadListenerList) {
                listener.onDownloadFailed(s);
            }
            super.onCancelled(s);
        }


        @Override
        protected String doInBackground(Void... params) {
            OutputStream output;
            InputStream input;

            mp3UrlString = trackInfo.getUrl();
            if (mp3UrlString.equals("")) {
                cancel(true);
                return "get Mp3url failed";
            }

            try {
                File mp3File = new File(filePath);
                if (mp3File.exists()) {
                    errorInfo = trackInfo.getTrackName() + " is exists";
                    LogUtil.d(errorInfo);
                    cancel(true);
                    return errorInfo;
                }

                mp3File.createNewFile();
                output = new FileOutputStream(mp3File);
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
                return trackInfo.getTrackName() + " create failed";
            }

            try {
                URL url = new URL(mp3UrlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                totalSize = Integer.parseInt(conn.getHeaderField("Content-Length"));
                LogUtil.d("totalSize:" + totalSize);
                input = conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
                return "openConnection " + mp3UrlString + " failed";
            }

            try {
                byte[] buffer = new byte[4 * 1024];
                int downedSize = 0;
                int len;
                while ((len = input.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                    // output.write(buffer);
                    downedSize += len;
                    publishProgress(downedSize * 100 / totalSize);
                    //  LogUtil.d(downedSize);
                }
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
                return "downloading error";
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            return "download success";
        }

        @Override
        protected void onPostExecute(String aVoid) {
            LogUtil.d("download complete");
            for (OnDownloadListener listener : onDownloadListenerList) {
                listener.onDownloadComplete(trackInfo);
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            process = values[0];
            for (OnDownloadListener listener : onDownloadListenerList) {
                listener.onProgressUpdate(values[0]);
            }
            super.onProgressUpdate(values);
        }
    }

    public int getTotalSize() {
        return totalSize;
    }

    public TrackInfo getTrackInfo() {
        return trackInfo;
    }
}