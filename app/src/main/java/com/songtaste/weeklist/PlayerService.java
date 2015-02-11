package com.songtaste.weeklist;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.songtaste.weeklist.api.Api;
import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.io.IOException;
import java.util.List;

public class PlayerService extends Service {

    List<SongInfo> songInfoList;

    private MediaPlayer mp = new MediaPlayer();
    private int curIndex = 0;

    @Override
    public void onCreate() {
        songInfoList = ((WkAppcation) this.getApplication()).getSongInfoList();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerBinder();
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    public void play() {
        if (!mp.isPlaying()) {
            mp.start();
        }
    }

    public void pause() {
        if (mp != null && mp.isPlaying()) {
            mp.pause();
        }
    }

    public void stop() {
        if (mp != null) {
            mp.stop();
            try {
                mp.prepare();  // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void next() {
        curIndex++;
        if (curIndex >= songInfoList.size()) {
            curIndex = 0;
        }

        new PlayMp3AsyncTask().execute(songInfoList.get(curIndex).getSongID());
    }

    public void last() {
        curIndex--;
        if (curIndex < 0) {
            curIndex = songInfoList.size() - 1;
        }
        new PlayMp3AsyncTask().execute(songInfoList.get(curIndex).getSongID());
    }

    public void playSongIndex(int position) {
        LogUtil.d("play:" + position + " " + songInfoList.get(position).getSongName());
        if (position > songInfoList.size()) {
            return;
        }
        curIndex = position;
        new PlayMp3AsyncTask().execute(songInfoList.get(curIndex).getSongID());
    }

    public void updateSongList() {
        songInfoList = ((WkAppcation) this.getApplication()).getSongInfoList();
        curIndex = 0;
    }

    @Override
    public void onDestroy() {
        if (mp != null) {
            mp.stop();
            mp.release();
        }
    }

    class PlayMp3AsyncTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            int id = params[0];
            LogUtil.d(id);
            return Api.getMp3Url(id);
        }

        @Override
        protected void onPostExecute(String mp3Url) {
            try {
                if (mp3Url == null) {
                    return;
                }
                LogUtil.d(mp3Url);
                mp.reset();
                mp.setDataSource(mp3Url);
                mp.prepare();
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
