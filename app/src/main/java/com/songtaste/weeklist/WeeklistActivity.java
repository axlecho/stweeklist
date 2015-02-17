package com.songtaste.weeklist;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.songtaste.weeklist.api.Api;
import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class WeeklistActivity extends ActionBarActivity {

    protected ListView weeklistListView;
    protected WeeklistAdapter weeklistAdapter;

    private ServiceConnection playerServiceConnection;
    private PlayerService playerService;

    private ServiceConnection downloadServiceConnection;
    private DownloadService downloadService;

    private boolean deadflag = false;

    class Player implements com.songtaste.weeklist.PlayerInterface {
        public ImageButton playBtn;
        public ImageButton nextBtn;

        public TextView songNameTextView;
        private View.OnClickListener playListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerService.play();
            }
        };

        private View.OnClickListener pauseListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playerService.pause();
            }
        };

        public void initBtn() {
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playerService.next();
                }
            });
        }

        @Override
        public void setSongName(String songName) {
            songNameTextView.setText(songName);
            weeklistAdapter.setPlayedSongnName(songName);
            weeklistAdapter.notifyDataSetChanged();
        }

        @Override
        public void setPlayStatus(int status) {
            if (status == PlayerInterface.PLAY_STATUS) {
                playBtn.setImageResource(R.drawable.ic_pause_white);
                playBtn.setOnClickListener(pauseListener);
            } else if (status == PlayerInterface.PAUSE_STATUS) {
                playBtn.setImageResource(R.drawable.ic_play_white);
                playBtn.setOnClickListener(playListener);
            }
        }

        @Override
        public void setProgress(int progress) {

        }
    }

    private Player player = new Player();

    private GetWeeklistAsyncTask getWeeklistAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weeklist);
        // 友盟自动更新
        UmengUpdateAgent.update(this);

        try {
            final List<String> dateList = Api.getDateList();
            SpinnerAdapter adapter = new ArrayAdapter<String>(this, R.layout.item_datelist, dateList);
            final ActionBar actionBar = this.getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);


            actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {

                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                    LogUtil.d(dateList.get(itemPosition));
                    if (getWeeklistAsyncTask != null && getWeeklistAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                        LogUtil.d("getWeeklistAsyncTask is canceled");
                        getWeeklistAsyncTask.cancel(false);
                    }

                    getWeeklistAsyncTask = new GetWeeklistAsyncTask();
                    getWeeklistAsyncTask.execute(dateList.get(itemPosition));
                    return false;
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

        weeklistListView = (ListView) findViewById(R.id.weeklist_listview);
        weeklistAdapter = new WeeklistAdapter(this);
        weeklistListView.setAdapter(weeklistAdapter);
        weeklistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playerService.playSongIndex(position);
            }
        });

        weeklistListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final SongInfo si = weeklistAdapter.getItem(position);
                new AlertDialog.Builder(WeeklistActivity.this).setMessage("下载:" + si.getSongName())
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadService.addToDownloadList(si);
                                downloadService.startDownload();
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        });
        getWeeklistAsyncTask = new GetWeeklistAsyncTask();
        Intent intent = getIntent();
        if (intent.getFlags() != Intent.FLAG_ACTIVITY_CLEAR_TOP) {
            getWeeklistAsyncTask.execute();
        }

        player.playBtn = (ImageButton) findViewById(R.id.play_pause_btn);
        player.nextBtn = (ImageButton) findViewById(R.id.next_btn);
        player.songNameTextView = (TextView) findViewById(R.id.songname_textview);
        player.initBtn();

        playerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.d("service connected");
                playerService = ((PlayerService.PlayerBinder) service).getService();
                playerService.addPlayer(player);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtil.d("service disconnected");
                playerService.removePlayer(player);
            }
        };

        downloadServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.d("download service connected");
                downloadService = ((DownloadService.DownloadBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this.getApplicationContext(), PlayerService.class);
        this.startService(service);
        this.bindService(service, playerServiceConnection, Context.BIND_AUTO_CREATE);

        service = new Intent(this.getApplicationContext(), DownloadService.class);
        this.startService(service);
        this.bindService(service, downloadServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unbindService(playerServiceConnection);

        if (deadflag) {
            playerService.hideNotification();
            playerService.stopSelf();
            downloadService.stopSelf();
        }
    }

    @Override
    protected void onDestroy() {
        playerService.hideNotification();
        playerService.stopSelf();
        downloadService.stopSelf();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        if (playerService != null) {
            playerService.hideNotification();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        if (playerService != null) {
            playerService.showNotification();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weeklist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_showdownload) {
            Intent intent = new Intent();
            intent.setClass(this, DownloadActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    class GetWeeklistAsyncTask extends AsyncTask<String, Void, List<SongInfo>> {

        @Override
        protected List<SongInfo> doInBackground(String... params) {
            String dateString = null;
            if (params.length != 0) {
                dateString = params[0];
            }
            return dateString == null ? Api.getWeeklist() : Api.getWeeklist(dateString);
        }

        @Override
        protected void onPostExecute(List<SongInfo> songInfoList) {
            if (songInfoList == null) {
                cancel(false);
                return;
            }

            weeklistAdapter.upDateData(songInfoList);
            ((WkAppcation) getApplication()).setSongInfoList(songInfoList);
            playerService.updateSongList();
            weeklistListView.setSelection(0);
            weeklistAdapter.notifyDataSetChanged();
            super.onPostExecute(songInfoList);
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(WeeklistActivity.this, Api.getError(), Toast.LENGTH_LONG).show();
            super.onCancelled();
        }
    }

    class WeeklistAdapter extends BaseAdapter {
        private String playedSongnName = "";
        List<SongInfo> songInfoList = new ArrayList<>();
        Context context;

        public WeeklistAdapter(Context context) {
            this.context = context;
        }

        public void setPlayedSongnName(String playedSongnName) {
            this.playedSongnName = playedSongnName;
        }

        @Override
        public int getCount() {
            return songInfoList.size();
        }

        @Override
        public SongInfo getItem(int position) {
            return songInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_weeklist, null);
                holder = new ViewHolder();
                holder.songname = (TextView) convertView.findViewById(R.id.weeklist_item_songname_textview);
                holder.position = (TextView) convertView.findViewById(R.id.weeklist_item_position_textview);
                holder.uploader = (TextView) convertView.findViewById(R.id.weeklist_item_uploader_textview);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.songname.setText(songInfoList.get(position).getSongName());
            holder.position.setText(String.valueOf(songInfoList.get(position).getIdx()));
            holder.uploader.setText(songInfoList.get(position).getUName());

            if (playedSongnName.equals(songInfoList.get(position).getSongName())) {
                convertView.setBackgroundColor(Color.parseColor("#cccccc"));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            return convertView;
        }

        public void upDateData(List<SongInfo> songInfoList) {
            if (songInfoList == null) {
                songInfoList.clear();
                return;
            }

            this.songInfoList = songInfoList;
        }


    }

    public static class ViewHolder {
        public TextView songname;
        public TextView position;
        public TextView uploader;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this).setMessage("确认退出？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            WeeklistActivity.this.finish();
                            deadflag = true;
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();


            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
