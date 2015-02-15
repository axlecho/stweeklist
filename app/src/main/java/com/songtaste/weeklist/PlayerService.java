package com.songtaste.weeklist;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.songtaste.weeklist.api.Api;
import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.io.IOException;
import java.util.List;

public class PlayerService extends Service {

    private static final int NOTIFY_ID = 1;
    List<SongInfo> songInfoList;

    private MediaPlayer mp = new MediaPlayer();
    private int curIndex = 0;

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews contentView;

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private boolean state = true;

    PlayMp3AsyncTask playMp3AsyncTask;

    @Override
    public void onCreate() {
        songInfoList = ((WkAppcation) this.getApplication()).getSongInfoList();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });
        setUpNotification();

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
                setState(true);
                showNotification();
            } else if (intent.getAction().equalsIgnoreCase(ACTION_PAUSE)) {
                pause();
                setState(false);
                showNotification();
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
    }

    public void showNotification() {
        notificationManager.notify(NOTIFY_ID, notification);
    }

    public void setState(boolean playing) {
        state = playing;
        if (state) {
            contentView.setImageViewResource(R.id.start_stop_btn, R.drawable.ic_play_white);
            Intent intent = new Intent(getApplicationContext(), PlayerService.class);
            intent.setAction(ACTION_PAUSE);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
            contentView.setOnClickPendingIntent(R.id.start_stop_btn, pendingIntent);
        } else {
            contentView.setImageViewResource(R.id.start_stop_btn, R.drawable.ic_pause_white);
            Intent intent = new Intent(getApplicationContext(), PlayerService.class);
            intent.setAction(ACTION_PLAY);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
            contentView.setOnClickPendingIntent(R.id.start_stop_btn, pendingIntent);
        }
    }


    public void hideNotification() {
        notificationManager.cancel(NOTIFY_ID);
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

    private void setUpNotification() {
        notificationManager = (NotificationManager) super.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence tickerText = "st player";
        long when = System.currentTimeMillis();
        notification = new Notification(R.drawable.ic_launcher, tickerText, when);

        // 放置在"正在运行"栏目中
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        contentView = new RemoteViews(this.getPackageName(), R.layout.layout_player);
        // 指定个性化视图
        notification.contentView = contentView;

        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(ACTION_NEXT);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        contentView.setOnClickPendingIntent(R.id.next_btn, pendingIntent);

        intent = new Intent(getApplicationContext(), WeeklistActivity.class);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        contentView.setOnClickPendingIntent(R.id.player_layout, pendingIntent);

        setState(true);
    }

}
