package com.songtaste.weeklist.api;

import android.os.AsyncTask;

import com.songtaste.weeklist.utils.LogUtil;
import com.songtaste.weeklist.utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by axlecho on 2015/5/3.
 */

public class DoufmApi {
    public static List<DoufmChannelInfo> getChannelInfo() {
        List<DoufmChannelInfo> doufmChannelInfoList = new ArrayList<>();

        String url = ServerConst.DOUFM_HOST + ServerConst.DOUFM_PLAYLIST;
        LogUtil.v(url);
        String htmlDocument = NetworkUtil.getInstance().get(url);
        if (htmlDocument == null) {
            LogUtil.e("http get failed");
            return doufmChannelInfoList;
        }
        LogUtil.v(htmlDocument);

        try {
            JSONArray doufmChannelInfoJsonArray = new JSONArray(htmlDocument);
            for (int i = 0; i < doufmChannelInfoJsonArray.length(); ++i) {
                DoufmChannelInfo doufmChannelInfo = DoufmChannelInfo.buildFromJson(doufmChannelInfoJsonArray.getJSONObject(i));
                doufmChannelInfoList.add(doufmChannelInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return doufmChannelInfoList;
    }

    public static List<DoufmTrackInfo> getTrackInfoFormChannel(String channelId) {
        List<DoufmTrackInfo> doufmTrackInfoList = new ArrayList<>();
        String url = ServerConst.DOUFM_HOST + ServerConst.DOUFM_PLAYLIST + channelId + "/?num=" + ServerConst.PAGESIZE;
        LogUtil.v(url);
        String htmlDocument = NetworkUtil.getInstance().get(url);
        if (htmlDocument == null) {
            LogUtil.e("http get failed");
            return doufmTrackInfoList;
        }
        LogUtil.v(htmlDocument);

        try {
            JSONArray doufmTrackInfoJsonArray = new JSONArray(htmlDocument);
            for (int i = 0; i < doufmTrackInfoJsonArray.length(); ++i) {
                DoufmTrackInfo doufmTrackInfo = DoufmTrackInfo.buildFromJson(doufmTrackInfoJsonArray.getJSONObject(i));
                doufmTrackInfoList.add(doufmTrackInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return doufmTrackInfoList;
    }

    public static AsyncTask<String, Void, List<DoufmChannelInfo>> getDoufmChannelInfoAsyncTask;

    public static void getChannelInfo(final OnGetChannelInfoCompleteListener listener) {
        if (getDoufmChannelInfoAsyncTask != null) {
            getDoufmChannelInfoAsyncTask.cancel(true);
        }

        getDoufmChannelInfoAsyncTask = new AsyncTask<String, Void, List<DoufmChannelInfo>>() {
            @Override
            protected List<DoufmChannelInfo> doInBackground(String... params) {
                return DoufmApi.getChannelInfo();
            }

            @Override
            protected void onPostExecute(List<DoufmChannelInfo> doufmChannelInfoList) {
                if (doufmChannelInfoList == null) {
                    cancel(true);
                    return;
                }

                listener.complete(doufmChannelInfoList);
                super.onPostExecute(doufmChannelInfoList);
            }

            @Override
            protected void onCancelled() {
                listener.failed();
                super.onCancelled();
            }
        };
        getDoufmChannelInfoAsyncTask.execute();
    }

    public static AsyncTask<String, Void, List<DoufmTrackInfo>> getDoufmTrakcInfoAsyncTask;

    public static void getTrackInfoFormChannel(final String channelId, final OnGetTrackInfoCompleteListener listener) {
        if (getDoufmTrakcInfoAsyncTask != null) {
            getDoufmTrakcInfoAsyncTask.cancel(true);
        }

        getDoufmTrakcInfoAsyncTask = new AsyncTask<String, Void, List<DoufmTrackInfo>>() {
            @Override
            protected List<DoufmTrackInfo> doInBackground(String... params) {
                return DoufmApi.getTrackInfoFormChannel(channelId);
            }

            @Override
            protected void onPostExecute(List<DoufmTrackInfo> doufmTrackInfoList) {
                if (doufmTrackInfoList == null) {
                    cancel(true);
                    return;
                }

                listener.complete(doufmTrackInfoList);
                super.onPostExecute(doufmTrackInfoList);
            }

            @Override
            protected void onCancelled() {
                listener.failed();
                super.onCancelled();
            }
        };
        getDoufmTrakcInfoAsyncTask.execute();
    }

    public interface OnGetChannelInfoCompleteListener {
        public void complete(List<DoufmChannelInfo> doufmChannelList);

        public void failed();
    }

    public interface OnGetTrackInfoCompleteListener {
        public void complete(List<DoufmTrackInfo> doufmTrackInfoList);

        public void failed();
    }
}
