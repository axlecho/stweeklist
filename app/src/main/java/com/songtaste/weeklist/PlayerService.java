package com.songtaste.weeklist;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import com.songtaste.weeklist.api.Api;
import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LocalFileUtil;
import com.songtaste.weeklist.utils.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerService extends Service {
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    public enum PlayMode {
        LIST_REPEATED, LIST_ONCE, ONE_REPEATED;

        public static PlayMode nextMode(PlayMode playMode) {
            PlayMode nextPlayMode = playMode;
            switch (playMode) {
                case LIST_ONCE:
                    nextPlayMode = ONE_REPEATED;
                    break;
                case LIST_REPEATED:
                    nextPlayMode = LIST_ONCE;
                    break;
                case ONE_REPEATED:
                    nextPlayMode = LIST_REPEATED;
                    break;
            }
            return nextPlayMode;
        }
    }

    private List<SongInfo> songInfoList;

    private MediaPlayer mp = new MediaPlayer();
    private int curIndex = 0;
    private PlayMode playmode = PlayMode.LIST_REPEATED;

    PlayMp3AsyncTask playMp3AsyncTask;


    @Override
    public void onCreate() {
        songInfoList = ((WkAppcation) this.getApplication()).getSongInfoList();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextWithPlayMode();
            }
        });
        notificationPlayer = new NotificationPlayer(this);
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
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }


    public void play() {
        if (!mp.isPlaying()) {
            mp.start();
            for (PlayerInterface player : playerList) {
                player.setPlayStatus(PlayerInterface.PLAY_STATUS);
            }
        }
    }

    public void pause() {
        if (mp != null && mp.isPlaying()) {
            mp.pause();
            for (PlayerInterface player : playerList) {
                player.setPlayStatus(PlayerInterface.PAUSE_STATUS);
            }
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

            for (PlayerInterface player : playerList) {
                player.setPlayStatus(PlayerInterface.PAUSE_STATUS);
            }
        }
    }

    public void previous() {
        curIndex--;
        if (curIndex < 0) {
            curIndex = songInfoList.size() - 1;
        }
        pause();
        setLyric();
        reset();
    }

    public void next() {
        curIndex++;
        if (curIndex >= songInfoList.size()) {
            curIndex = 0;
        }
        pause();
        setLyric();
        reset();
    }

    private void nextWithPlayMode() {
        switch (playmode) {
            case LIST_REPEATED:
                next();
                break;
            case LIST_ONCE:
                if (curIndex == playerList.size() - 1) return;
                next();
                break;
            case ONE_REPEATED:
                playSongIndex(curIndex);
                break;
        }
    }

    public void playSongIndex(int position) {
        LogUtil.d("play:" + position + " " + songInfoList.get(position).getSongPath());
        if (position > songInfoList.size()) {
            return;
        }
        curIndex = position;
        pause();
        setLyric();
        reset();
    }

    public void setPlayMode(PlayMode playmode) {
        this.playmode = playmode;
    }

    public PlayMode getPlaymode() {
        return playmode;
    }

    private void setLyric() {
        for (PlayerInterface player : playerList) {
            player.setSongName(songInfoList.get(curIndex).getSongName());
            String IyricString = LocalFileUtil.getLyric(songInfoList.get(curIndex).getSongPath());
            player.setLyric(IyricString);
        }
    }

    private void reset() {
        if (songInfoList.get(curIndex).getIdx() == -1) {
            String mp3Url = songInfoList.get(curIndex).getSongPath();
            try {
                if (mp3Url == null) {
                    return;
                }
                LogUtil.d(mp3Url);
                mp.reset();
                mp.setDataSource(mp3Url);
                mp.prepare();
                play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            startPlayMp3AsyncTask();
        }
    }

    private void startPlayMp3AsyncTask() {
        if (playMp3AsyncTask != null && playMp3AsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            playMp3AsyncTask.cancel(false);
            LogUtil.d("playMp3AsyncTask is canceled");
        }
        playMp3AsyncTask = new PlayMp3AsyncTask();
        playMp3AsyncTask.execute(songInfoList.get(curIndex).getSongID());
    }

    public void updateSongList() {
        songInfoList = ((WkAppcation) this.getApplication()).getSongInfoList();
        if (songInfoList.size() == 0) {
            return;
        }

        curIndex = 0;
        if (!mp.isPlaying()) {
            setLyric();
        }
    }

    private NotificationPlayer notificationPlayer;

    public void hideNotification() {
        notificationPlayer.hide();
        removePlayer(notificationPlayer);
    }

    public void showNotification() {
        notificationPlayer.show();
        addPlayer(notificationPlayer);
    }


    private void handleIntent(Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase(ACTION_PLAY)) {
                play();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_PAUSE)) {
                pause();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_PREVIOUS)) {
                previous();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_NEXT)) {
                next();
            }
        }
    }

    private class PlayMp3AsyncTask extends AsyncTask<Integer, Void, String> {
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
                play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<PlayerInterface> playerList = new ArrayList<>();

    public void addPlayer(PlayerInterface player) {
        //TODO 添加播放器状态
        if (mp.isPlaying()) {
            player.setPlayStatus(PlayerInterface.PLAY_STATUS);
        } else {
            player.setPlayStatus(PlayerInterface.PAUSE_STATUS);
        }
        if (songInfoList != null && songInfoList.size() != 0) {
            player.setSongName(songInfoList.get(curIndex).getSongName());
            String IyricString = new LocalFileUtil().getLyric(songInfoList.get(curIndex).getSongPath());
            player.setLyric(IyricString);
        }
        playerList.add(player);
    }

    public void removePlayer(PlayerInterface player) {
        playerList.remove(player);
    }

    @Override
    public void onDestroy() {
        if (mp != null) {
            mp.stop();
            mp.release();
        }
    }

}
