package com.example.tansanmusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static com.example.tansanmusicplayer.MainActivity.musicFiles;

public class AlbumDetails extends AppCompatActivity {
RecyclerView recyclerView;
ImageView albumPhoto;
String albumName ;
albumDetailAdapter albumDetailAdapter;
ArrayList<MusicFiles> albumSongs =new ArrayList<>();
  //  private Object albumDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        recyclerView =findViewById(R.id.recycleView);
        albumPhoto=findViewById(R.id.albumPhoto);
        albumName= getIntent().getStringExtra("albumName");
        int j=0;
        for(int i=0;i<musicFiles.size();i++){
          //  assert albumName != null;
            if(albumName.equals(musicFiles.get(i).getAlbum())){
                albumSongs.add(j,musicFiles.get(i));
                j++;
            }
        }
        byte[] image=getAlbumArt(albumSongs.get(0).getPath());
        if(image!=null){
            Glide.with(this).load(image).into(albumPhoto);
        }
        else {
            Glide.with(this).load(R.drawable.umang).into(albumPhoto);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!(albumSongs.size()<1)){
            albumDetailAdapter=new albumDetailAdapter(this,albumSongs);
            recyclerView.setAdapter(albumDetailAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        }
    }

    private byte[] getAlbumArt (String uri){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art= retriever.getEmbeddedPicture();
        retriever.release();
        return art;

    }
}