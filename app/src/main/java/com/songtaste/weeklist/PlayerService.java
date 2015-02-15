package com.songtaste.weeklist;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.songtaste.weeklist.api.Api;
import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerService extends Service {

    List<SongInfo> songInfoList;
    List<PlayerInterface> playerList = new ArrayList<>();

    private MediaPlayer mp = new MediaPlayer();
    private int curIndex = 0;

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private boolean state = true;

    PlayMp3AsyncTask playMp3AsyncTask;
    NotificationPlayerInterface notificationPlayer;

    @Override
    public void onCreate() {
        songInfoList = ((WkAppcation) this.getApplication()).getSongInfoList();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });
        notificationPlayer = new NotificationPlayerInterface();
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

    public void next() {
        curIndex++;
        if (curIndex >= songInfoList.size()) {
            curIndex = 0;
        }

        pause();
        for (PlayerInterface player : playerList) {
            player.setSongName(songInfoList.get(curIndex).getSongName());
        }
        if (playMp3AsyncTask != null && playMp3AsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            playMp3AsyncTask.cancel(false);
            LogUtil.d("playMp3AsyncTask is canceled");
        }
        playMp3AsyncTask = new PlayMp3AsyncTask();
        playMp3AsyncTask.execute(songInfoList.get(curIndex).getSongID());
    }

    public void previous() {
        curIndex--;
        if (curIndex < 0) {
            curIndex = songInfoList.size() - 1;
        }
        pause();
        for (PlayerInterface player : playerList) {
            player.setSongName(songInfoList.get(curIndex).getSongName());
        }

        if (playMp3AsyncTask != null && playMp3AsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            playMp3AsyncTask.cancel(false);
            LogUtil.d("playMp3AsyncTask is canceled");
        }
        playMp3AsyncTask = new PlayMp3AsyncTask();
        playMp3AsyncTask.execute(songInfoList.get(curIndex).getSongID());
    }

    public void playSongIndex(int position) {
        LogUtil.d("play:" + position + " " + songInfoList.get(position).getSongName());
        if (position > songInfoList.size()) {
            return;
        }
        curIndex = position;
        pause();
        for (PlayerInterface player : playerList) {
            player.setSongName(songInfoList.get(curIndex).getSongName());
        }

        if (playMp3AsyncTask != null && playMp3AsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            playMp3AsyncTask.cancel(false);
            LogUtil.d("playMp3AsyncTask is canceled");
        }
        playMp3AsyncTask = new PlayMp3AsyncTask();
        playMp3AsyncTask.execute(songInfoList.get(curIndex).getSongID());
    }

    public void updateSongList() {
        songInfoList = ((WkAppcation) this.getApplication()).getSongInfoList();
        curIndex = 0;
        if (!mp.isPlaying()) {
            for (PlayerInterface player : playerList) {
                player.setSongName(songInfoList.get(curIndex).getSongName());
            }
        }
    }

    public void hideNotification() {
        notificationPlayer.hide();
        removePlayer(notificationPlayer);
    }

    public void showNotification() {
        notificationPlayer.show();
        addPlayer(notificationPlayer);
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
                play();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class NotificationPlayerInterface implements PlayerInterface {
        private static final int NOTIFY_ID = 1;
        private NotificationManager notificationManager;
        private Notification notification;
        private RemoteViews contentView;

        public NotificationPlayerInterface() {
            init();
        }

        @Override
        public void setSongName(String songName) {
            LogUtil.d(songName);
            contentView.setTextViewText(R.id.songname_textview, songName);
            notification.tickerText = songName;
            show();
        }

        @Override
        public void setPlayStatus(int status) {
            if (status == PlayerInterface.PLAY_STATUS) {
                contentView.setImageViewResource(R.id.play_pause_btn, R.drawable.ic_pause);
                Intent intent = new Intent(getApplicationContext(), PlayerService.class);
                intent.setAction(ACTION_PLAY);
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
                contentView.setOnClickPendingIntent(R.id.play_pause_btn, pendingIntent);
            } else if (status == PlayerInterface.PAUSE_STATUS) {
                contentView.setImageViewResource(R.id.play_pause_btn, R.drawable.ic_play);
                Intent intent = new Intent(getApplicationContext(), PlayerService.class);
                intent.setAction(ACTION_PAUSE);
                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
                contentView.setOnClickPendingIntent(R.id.play_pause_btn, pendingIntent);
            }
            show();
        }

        @Override
        public void setProgress(int progress) {

        }

        public void show() {
            notificationManager.notify(NOTIFY_ID, notification);
        }

        public void hide() {
            notificationManager.cancel(NOTIFY_ID);
        }

        public void init() {
            notificationManager = (NotificationManager) PlayerService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence tickerText = "st player";
            long when = System.currentTimeMillis();
            notification = new Notification(R.drawable.ic_launcher, tickerText, when);

            // 放置在"正在运行"栏目中
            notification.flags = Notification.FLAG_ONGOING_EVENT;

            contentView = new RemoteViews(PlayerService.this.getPackageName(), R.layout.layout_player);
            // 指定个性化视图
            notification.contentView = contentView;

            Intent intent = new Intent(getApplicationContext(), PlayerService.class);
            intent.setAction(ACTION_NEXT);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
            contentView.setOnClickPendingIntent(R.id.next_btn, pendingIntent);

            intent = new Intent(getApplicationContext(), WeeklistActivity.class);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            contentView.setOnClickPendingIntent(R.id.player_layout, pendingIntent);

            contentView.setTextColor(R.id.songname_textview, Color.WHITE);
            contentView.setTextViewText(R.id.songname_textview, "给跪了..这都被你看到");
            contentView.setImageViewResource(R.id.play_pause_btn, R.drawable.ic_pause);
            contentView.setImageViewResource(R.id.next_btn, R.drawable.ic_next);
        }
    }

    public void addPlayer(PlayerInterface player) {
        //TODO 添加播放器状态
        if (mp.isPlaying()) {
            player.setPlayStatus(PlayerInterface.PLAY_STATUS);
        } else {
            player.setPlayStatus(PlayerInterface.PAUSE_STATUS);
        }
        if (songInfoList != null) {
            player.setSongName(songInfoList.get(curIndex).getSongName());
        }
        playerList.add(player);
    }

    public void removePlayer(PlayerInterface player) {
        playerList.remove(player);
    }
}
