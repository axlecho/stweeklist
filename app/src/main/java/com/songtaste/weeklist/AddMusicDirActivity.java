package com.songtaste.weeklist;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.songtaste.weeklist.utils.LogUtil;
import com.songtaste.weeklist.utils.SqlUtil;

import java.io.File;
import java.util.Comparator;


public class AddMusicDirActivity extends Activity {

    private ListView pathListView;
    private TextView pathTextView;
    private Button okBtn;
    private ArrayAdapter<String> pathListViewAdapter;

    private String sdcardRootPath = "/";  // 根目录
    private String currentDirPath = sdcardRootPath;    //当前显示的路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music_dir);

        pathListView = (ListView) findViewById(R.id.path_listview);
        pathTextView = (TextView) findViewById(R.id.path_textview);
        okBtn = (Button) findViewById(R.id.ok_btn);

        pathListViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        pathListView.setAdapter(pathListViewAdapter);
        pathListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) { //返回父目录
                    currentDirPath = new File(currentDirPath).getParent();
                } else {
                    currentDirPath += pathListViewAdapter.getItem(position) + "/";
                }
                LogUtil.d("current dir path:" + currentDirPath);
                getCurrentSubDirList();
            }
        });

        getCurrentSubDirList();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SqlUtil.saveMusicPath(AddMusicDirActivity.this, currentDirPath);
                AddMusicDirActivity.this.finish();
            }
        });
    }

    private void getCurrentSubDirList() {
        pathTextView.setText(currentDirPath);
        File currentDir = new File(currentDirPath);
        if (!currentDir.isDirectory()) {
            LogUtil.e(currentDir + " is not a directory");
            return;
        }

        pathListViewAdapter.clear();
        if (!currentDirPath.equals("/")) {
            pathListViewAdapter.add("..");
        }
        File[] subDirList = currentDir.listFiles();

        for (File subDir : subDirList) {
            if (subDir.isDirectory()) {
                pathListViewAdapter.add(subDir.getName());
            }
        }

        pathListViewAdapter.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        pathListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_music_dir, menu);
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
}
