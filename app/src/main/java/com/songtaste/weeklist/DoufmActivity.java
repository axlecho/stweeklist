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
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.songtaste.weeklist.api.DoufmApi;
import com.songtaste.weeklist.api.DoufmChannelInfo;
import com.songtaste.weeklist.api.DoufmTrackInfo;
import com.songtaste.weeklist.api.LocalFileApi;
import com.songtaste.weeklist.api.LocalTrackInfo;
import com.songtaste.weeklist.api.STWeeklistApi;
import com.songtaste.weeklist.api.StTrackInfo;
import com.songtaste.weeklist.api.TrackInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class DoufmActivity extends ActionBarActivity {

    class Player implements PlayerInterface {
        private View playerView;
        private ImageButton playBtn;
        private ImageButton nextBtn;
        private TextView songNameTextView;
        private Button playmodeBtn;

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

            playerView = findViewById(R.id.player_layout);
            playmodeBtn = (Button) findViewById(R.id.playmode_btn);
            updatePlayModeBtn();

            playmodeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlayerService.PlayMode currentPlayMode = playerService.getPlaymode();
                    PlayerService.PlayMode nextPlayMode = PlayerService.PlayMode.nextMode(currentPlayMode);
                    playerService.setPlayMode(nextPlayMode);
                    updatePlayModeBtn();
                }
            });
        }

        private void updatePlayModeBtn() {
            switch (playerService.getPlaymode()) {
                case LIST_REPEATED:
                    playmodeBtn.setText(R.string.playmode_listrepeated);
                    break;
                case LIST_ONCE:
                    playmodeBtn.setText(R.string.playmode_listonce);
                    break;
                case ONE_REPEATED:
                    playmodeBtn.setText(R.string.playmode_onerepeated);
                    break;
            }
        }

        @Override
        public void setSongName(String songName) {
            songNameTextView.setText(songName);
            bgManager.setNextBg();
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
            lyricTextView.setText(lyric);
        }

        public void setOnClickListener(View.OnClickListener listener) {
            playerView.setOnClickListener(listener);
        }

    }

    class BgManager {
        private int[] bgArray = new int[]{
                R.drawable.bg01,
                R.drawable.bg02,
                R.drawable.bg03,
                R.drawable.bg04,
                R.drawable.bg05,
                R.drawable.bg06,
                R.drawable.bg07,
                R.drawable.bg08,
                R.drawable.bg10,
        };
        private int currentbg = bgArray.length - 1;
        public ImageView bgImageView;

        public void setNextBg() {
            currentbg = (currentbg + 1) % bgArray.length;
            bgImageView.setImageResource(bgArray[currentbg]);
        }
    }

    protected ListView weeklistListView;
    protected TracklistAdapter weeklistAdapter;
    private ServiceConnection playerServiceConnection;
    private PlayerService playerService;
    private ServiceConnection downloadServiceConnection;
    private DownloadService downloadService;

    private boolean deadflag = false;

    private Player player = new Player();

    private View lyricView;
    private TextView lyricTextView;
    private BgManager bgManager = new BgManager();

    private DoufmChannelInfo currentChannelInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);

        // 音乐列表初始化
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
                new AlertDialog.Builder(DoufmActivity.this).setMessage("下载:" + ti.getTrackName())
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

        weeklistListView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                LogUtil.v("加载更多");
                loadMoreSong();
            }
        });

        lyricView = findViewById(R.id.lyric_scrollview);
        bgManager.bgImageView = (ImageView) findViewById(R.id.bg_imageview);

        lyricTextView = (TextView) findViewById(R.id.lyric_textview);
        bgManager.setNextBg();

        playerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.d("service connected");
                playerService = ((PlayerService.PlayerBinder) service).getService();
                player.initBtn();
                player.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (lyricView.getVisibility() == View.GONE) {
                            lyricView.setVisibility(View.VISIBLE);
                            weeklistListView.setVisibility(View.GONE);
                        } else {
                            lyricView.setVisibility(View.GONE);
                            weeklistListView.setVisibility(View.VISIBLE);
                        }
                    }
                });

                playerService.addPlayer(player);
                updateSong();
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
        this.unbindService(downloadServiceConnection);

        if (deadflag) {
            playerService.hideNotification();
            playerService.stopSelf();
            playerService.stopSelf();
            downloadService.stopSelf();
        }
    }

    @Override
    protected void onDestroy() {
        playerService.hideNotification();
        playerService.stopSelf();
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

    public void loadMoreSong() {
        DoufmApi.getTrackInfoFormChannel(currentChannelInfo.getKey(), new DoufmApi.OnGetTrackInfoCompleteListener() {
            @Override
            public void complete(List<DoufmTrackInfo> doufmTrackInfoList) {
                weeklistAdapter.addData(doufmTrackInfoList);
                ((WkAppcation) getApplication()).setTrackInfoList(weeklistAdapter.getData());
                playerService.updateSongList();
                weeklistAdapter.notifyDataSetChanged();
            }

            @Override
            public void failed() {
                LogUtil.e("get trackInfo form channel failed");
            }
        });
    }

    public void updateSong() {
        DoufmApi.getChannelInfo(new DoufmApi.OnGetChannelInfoCompleteListener() {
            @Override
            public void complete(List<DoufmChannelInfo> doufmChannelInfoList) {
                for (DoufmChannelInfo ci : doufmChannelInfoList) {
                    if (ci.getDescribe().equals("动漫")) {
                        currentChannelInfo = ci;
                    }
                }

                if (currentChannelInfo == null) {
                    LogUtil.e("can not find channel:动漫");
                    return;
                }

                loadMoreSong();
            }

            @Override
            public void failed() {
                LogUtil.e("get channel info failed");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            updateSong();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
