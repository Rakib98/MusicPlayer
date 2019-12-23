package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Global vars
    private boolean isMusicPlayerInit;
    private static final int REQUEST_PERMISSIONS = 12345;
    private  static final int PERMISSIONS_COUNT = 1;
    private List<String> musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*Check for external storage permission*/
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @SuppressLint("NewApi")
    private boolean permissionsDenied() {
        for (int i = 0; i < PERMISSIONS_COUNT; i++) {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    //Permission to use the device storage
    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissionsDenied()) {
            ((ActivityManager) (this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate();
        }
        else {
            onResume();
        }
    }

    //Function that allows to add songs from a path.
    private void addMusicFrom(String path) {
        final File dir = new File(path);

        /*Check if the file exists*/
        if(!dir.exists()) {
            dir.mkdir();
            return;
        }
        final File[] files = dir.listFiles();
        for (File file : files) {
            final String absPath = file.getAbsolutePath();
            if(absPath.endsWith(".mp3")) {
                musicList.add(absPath);
            }
        }
    }

    //Add all the music files that are contained within the downloads folder, and music folder.
    private void populateMusicList() {
        musicList.clear();
        addMusicFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)));
        addMusicFrom(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
    }

    private int playSong(String path) {
        MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return mp.getDuration();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionsDenied()) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
            return;
        }

        if(!isMusicPlayerInit) {
            final ListView lvMusicList = findViewById(R.id.musicList);

            final TextAdapter textAdapter = new TextAdapter();
            musicList = new ArrayList<>();

            populateMusicList();

            textAdapter.setData(musicList);
            lvMusicList.setAdapter(textAdapter);

            final SeekBar seekBar = findViewById(R.id.seekBar);

            lvMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String musicFilePath = musicList.get(position);
                    final int songDuration = playSong(musicFilePath);
                    seekBar.setVisibility(View.VISIBLE);

                }
            });

            isMusicPlayerInit = true;
        }
    }

    class TextAdapter extends BaseAdapter{

        private List<String> data = new ArrayList<>();

        void setData(List<String> newData) {
            data.clear();
            data.addAll(newData);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        /*Show text on the items*/
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.Song)));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String item = data.get(position);
            holder.info.setText(item.substring(item.lastIndexOf('/') +1));
            return convertView;
        }

        class ViewHolder{
            TextView info;

            ViewHolder(TextView mInfo) {
                info = mInfo;
            }
        }
    }
}
