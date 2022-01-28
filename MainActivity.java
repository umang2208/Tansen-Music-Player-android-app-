package com.example.tansanmusicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.app.SearchManager;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.internal.ParcelableSparseBooleanArray;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int REQUEST_CODE = 1;
    static  ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean =false,repeatBoolean=false;
    static  ArrayList<MusicFiles> albums = new ArrayList<>();
    private String Sort_By_Pref="sortOrder";
    public static final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE="STORED_MUSIC";
    public static boolean SHOE_MINI_PLAYER= false;
    public static String PATH_TO_FRAG=null;
    public static String ARTIST_TO_FRAG=null;
    public static String SONG_TO_FRAG=null;
    public static final String ARTIST_LAST_PLAYED="ARTIST NAME";
    public static final String SONG_NAME="SONG NAME";
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void permission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
        }
        else{
       //     Toast.makeText(this,"Permission Granted !",Toast.LENGTH_SHORT).show();
           musicFiles= getAllAudio(this);
            initViewPager();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){

                musicFiles=getAllAudio(this);
                initViewPager();
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);
            }
        }
    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout= findViewById(R.id.tabLayout);
        ViewPagerAdapter viewPagerAdapter =new ViewPagerAdapter((getSupportFragmentManager()));
        viewPagerAdapter.addFragments(new songsFragment(),"Songs");

        viewPagerAdapter.addFragments(new AlbumFragment(),"Album");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }



    public static class  ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.titles=new ArrayList<>();
            this.fragments=new ArrayList<>();
        }
        void addFragments(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
  //  @RequiresApi(api = Build.VERSION_CODES.Q)

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public  ArrayList<MusicFiles> getAllAudio(Context context){
        SharedPreferences preferences=getSharedPreferences(Sort_By_Pref,MODE_PRIVATE);
       String sortOrder= preferences.getString("sorting","sortByName");
        ArrayList<String> dublicate = new ArrayList<>();
        albums.clear();
        ArrayList<MusicFiles> temp=new ArrayList<>();
        String order = null;
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        assert sortOrder != null;
        switch (sortOrder){
            case "sortByName":
                order=MediaStore.MediaColumns.DISPLAY_NAME +"ASC";
                break;
            case "sortByDate":
                order=MediaStore.MediaColumns.DATE_ADDED +"ASC";
                break;
            case "sortBySize":
                order=MediaStore.MediaColumns.SIZE +"DESC";
                break;
        }
        String[] projection={
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor =context.getContentResolver().query(uri,projection,null,null,null);
        if(cursor!=null){
            while(cursor.moveToNext()){
                String album=cursor.getString(0);
                String title=cursor.getString(1);
                String duration=cursor.getString(2);
                String path=cursor.getString(3);
                String artist=cursor.getString(4);
                String id=cursor.getString(5);
                MusicFiles musicFiles=new MusicFiles(path,title,artist,album,duration,id);
                temp.add(musicFiles);
                if( !dublicate.contains(album)){
                    albums.add(musicFiles);
                    dublicate.add(album);
                }
            }
            cursor.close();
        }
        return temp;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search,menu);
        MenuItem menuItem =menu.findItem(R.id.search_option);
       SearchView searchView=(SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener( this);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFiles> myFiles = new ArrayList<>();
        for (MusicFiles song :musicFiles){
            if(song.getTitle().toLowerCase().contains(userInput)){
                myFiles.add(song);
            }
        }
        songsFragment.musicAdapter.UpdateList(myFiles);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor=getSharedPreferences(Sort_By_Pref,MODE_PRIVATE).edit();
        switch (item.getItemId()){
            case R.id.by_name:
                editor.putString("sorting","sortByName");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_date:
                editor.putString("sorting","sortByDate");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_size:
                editor.putString("sorting","sortBySize");
                editor.apply();
                this.recreate();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences =getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
        String artist= preferences.getString(ARTIST_LAST_PLAYED,null);
        String PATH= preferences.getString(MUSIC_FILE,null);
        String song_Name= preferences.getString(SONG_NAME,null);
        if(PATH!=null){
            SHOE_MINI_PLAYER =true;
            PATH_TO_FRAG=PATH;
            ARTIST_TO_FRAG=artist;
            SONG_TO_FRAG=song_Name;
        }
        else {
            SHOE_MINI_PLAYER=false;
            PATH_TO_FRAG=null;
            ARTIST_TO_FRAG=null;
            SONG_TO_FRAG=null;
        }
    }
}