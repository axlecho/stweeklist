package com.songtaste.weeklist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.songtaste.weeklist.api.SongInfo;
import com.songtaste.weeklist.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;


public class DownloadActivity extends ActionBarActivity {
    protected ListView downaloadListView;
    protected DownloadAdapter downloadAdapter;
    protected DownloadService downloadService;
    private ServiceConnection downloadServiceConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        downaloadListView = (ListView) findViewById(R.id.download_listview);
        downloadAdapter = new DownloadAdapter(this);
        downaloadListView.setAdapter(downloadAdapter);
        downloadServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                LogUtil.d("service connected");
                downloadService = ((DownloadService.DownloadBinder) service).getService();
                downloadAdapter.updateData(downloadService.getDownloadJobList());
                downloadAdapter.notifyDataSetChanged();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                LogUtil.d("service disconnected");
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent service = new Intent(this.getApplicationContext(), DownloadService.class);
        this.startService(service);
        this.bindService(service, downloadServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unbindService(downloadServiceConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_download, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadAdapter extends BaseAdapter {
        Context context;
        List<DownloadJob> downloadJobList = new ArrayList<>();
        List<DownloadJob> downloadedJobList = new ArrayList<>();

        public DownloadAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return downloadJobList.size();
        }

        @Override
        public DownloadJob getItem(int position) {
            return downloadJobList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_downloadlist, null);
                holder = new ViewHolder();
                holder.songname = (TextView) convertView.findViewById(R.id.songname_textview);
                holder.process = (TextView) convertView.findViewById(R.id.process_textview);
                holder.progressBar = (ProgressBar) convertView.findViewById(R.id.process_progressbar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            DownloadJob dj = downloadJobList.get(position);

            dj.addDownloadListener(new DownloadJob.OnDownloadListener() {

                @Override
                public void onProgressUpdate(Integer process) {
                    DownloadAdapter.this.notifyDataSetChanged();
                }

                @Override
                public void onDownloadComplete(SongInfo songInfo) {
                    DownloadAdapter.this.notifyDataSetChanged();
                }

                @Override
                public void onDownloadFailed(String errorinfo) {
                    Toast.makeText(DownloadActivity.this.getApplicationContext(), errorinfo, Toast.LENGTH_LONG);
                }
            });

            SongInfo si = dj.getSongInfo();
            holder.songname.setText(si.getSongName());
            holder.process.setText(String.valueOf(dj.getProcess()));
            holder.progressBar.setProgress(dj.getProcess());
            return convertView;
        }

        public void updateData(List<DownloadJob> downloadJobList) {
            this.downloadJobList = downloadJobList;
        }
    }

    public class ViewHolder {
        public TextView songname;
        public TextView process;
        public ProgressBar progressBar;
    }
}
