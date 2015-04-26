package com.songtaste.weeklist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.songtaste.weeklist.utils.LogUtil;

public class NotificationPlayer implements PlayerInterface {
    private static final int NOTIFY_ID = 1;
    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews contentView;
    private Context context;

    public NotificationPlayer(Context context) {
        this.context = context;
        init();
    }

    @Override
    public void setSongName(String songName) {
        LogUtil.d(songName);
        contentView.setTextViewText(R.id.download_songname_item_textview, songName);
        notification.tickerText = songName;
        show();
    }

    @Override
    public void setPlayStatus(int status) {
        if (status == PlayerInterface.PLAY_STATUS) {
            contentView.setImageViewResource(R.id.play_pause_btn, R.drawable.ic_pause);
            Intent intent = new Intent(context.getApplicationContext(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_PLAY);
            PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 1, intent, 0);
            contentView.setOnClickPendingIntent(R.id.play_pause_btn, pendingIntent);
        } else if (status == PlayerInterface.PAUSE_STATUS) {
            contentView.setImageViewResource(R.id.play_pause_btn, R.drawable.ic_play);
            Intent intent = new Intent(context.getApplicationContext(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_PAUSE);
            PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 1, intent, 0);
            contentView.setOnClickPendingIntent(R.id.play_pause_btn, pendingIntent);
        }
        show();
    }

    @Override
    public void setProgress(int progress) {

    }

    @Override
    public void setLyric(String lyric) {

    }

    public void show() {
        notificationManager.notify(NOTIFY_ID, notification);
    }

    public void hide() {
        notificationManager.cancel(NOTIFY_ID);
    }

    public void init() {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence tickerText = "st player";
        long when = System.currentTimeMillis();
        notification = new Notification(R.drawable.ic_launcher, tickerText, when);

        // 放置在"正在运行"栏目中
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        contentView = new RemoteViews(context.getPackageName(), R.layout.layout_player);
        // 指定个性化视图
        notification.contentView = contentView;

        Intent intent = new Intent(context.getApplicationContext(), PlayerService.class);
        intent.setAction(PlayerService.ACTION_NEXT);
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 1, intent, 0);
        contentView.setOnClickPendingIntent(R.id.next_btn, pendingIntent);

        intent = new Intent(context.getApplicationContext(), LocalActivity.class);
        pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0);
        contentView.setOnClickPendingIntent(R.id.player_layout, pendingIntent);

        contentView.setTextColor(R.id.download_songname_item_textview, Color.WHITE);
        contentView.setTextViewText(R.id.download_songname_item_textview, "给跪了..这都被你看到");
        contentView.setImageViewResource(R.id.play_pause_btn, R.drawable.ic_pause);
        contentView.setImageViewResource(R.id.next_btn, R.drawable.ic_next);
    }
}