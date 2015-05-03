package com.songtaste.weeklist;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.songtaste.weeklist.api.STWeeklistApi;
import com.songtaste.weeklist.api.StTrackInfo;
import com.songtaste.weeklist.api.TrackInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.text.ParseException;
import java.util.List;

public class WeeklistActivity extends ActionBarActivity {

    protected ListView weeklistListView;
    protected TracklistAdapter weeklistAdapter;
    private ServiceConnection playerServiceConnection;
    private PlayerService playerService;
    private ServiceConnection downloadServiceConnection;
    private DownloadService downloadService;

    private boolean deadflag = false;

    class Player implements com.songtaste.weeklist.PlayerInterface {
        private ImageButton playBtn;
        private ImageButton nextBtn;
        private TextView songNameTextView;

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
            playBtn = (ImageButton) findViewById(R.id.play_pause_btn);
            nextBtn = (ImageButton) findViewById(R.id.next_btn);
            songNameTextView = (TextView) findViewById(R.id.download_songname_item_textview);
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

        @Override
        public void setLyric(String lyric) {

        }
    }

    private Player player = new Player();

    private GetWeeklistAsyncTask getWeeklistAsyncTask;
    private MenuItem progressMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weeklist);

        // actionbar的初始化
        try {
            final List<String> dateList = STWeeklistApi.getDateList();
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
                    progressMenu.setActionView(R.layout.actionbar_indeterminate_progress);
                    progressMenu.setVisible(true);
//                    actionBar.setSelectedNavigationItem(itemPosition);
                    return false;
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 周排行版列表初始化
        weeklistListView = (ListView) findViewById(R.id.weeklist_listview);
        weeklistAdapter = new TracklistAdapter(this);
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
                final TrackInfo ti = weeklistAdapter.getItem(position);
                new AlertDialog.Builder(WeeklistActivity.this).setMessage("下载:" + ti.getTrackName())
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadService.addToDownloadList(ti);
                                downloadService.startDownload();
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        });

        getWeeklistAsyncTask = new GetWeeklistAsyncTask();
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
        if (playerService != null) {
            playerService.hideNotification();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerService != null) {
            playerService.showNotification();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weeklist, menu);
        progressMenu = menu.findItem(R.id.refresh_loading);
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
        } else if (id == R.id.action_loacal) {
            Intent intent = new Intent();
            intent.setClass(this, LocalActivity.class);
            getWeeklistAsyncTask.cancel(true);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    class GetWeeklistAsyncTask extends AsyncTask<String, Void, List<StTrackInfo>> {

        @Override
        protected List<StTrackInfo> doInBackground(String... params) {
            String dateString = null;
            if (params.length != 0) {
                dateString = params[0];
            }
            return dateString == null ? STWeeklistApi.getWeeklist() : STWeeklistApi.getWeeklist(dateString);
        }

        @Override
        protected void onPostExecute(List<StTrackInfo> stTrackInfoList) {
            if (stTrackInfoList == null) {
                cancel(true);
                return;
            }

            weeklistAdapter.upDateData(stTrackInfoList);
            ((WkAppcation) getApplication()).setTrackInfoList(stTrackInfoList);
            playerService.updateSongList();
            weeklistListView.setSelection(0);
            weeklistAdapter.notifyDataSetChanged();

            progressMenu.setVisible(false);
            progressMenu.setActionView(null);
            super.onPostExecute(stTrackInfoList);
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(WeeklistActivity.this, STWeeklistApi.getError(), Toast.LENGTH_LONG).show();
            LogUtil.e(STWeeklistApi.getError());
            progressMenu.setVisible(false);
            progressMenu.setActionView(null);
            super.onCancelled();
        }
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
