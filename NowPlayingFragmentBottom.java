package com.example.tansanmusicplayer;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import static com.example.tansanmusicplayer.MainActivity.ARTIST_LAST_PLAYED;
import static com.example.tansanmusicplayer.MainActivity.ARTIST_TO_FRAG;
import static com.example.tansanmusicplayer.MainActivity.PATH_TO_FRAG;
import static com.example.tansanmusicplayer.MainActivity.SHOE_MINI_PLAYER;
import static com.example.tansanmusicplayer.MainActivity.SONG_TO_FRAG;

public class NowPlayingFragmentBottom extends Fragment {
ImageView nextBtn,albumArt;
TextView artist,songName;
FloatingActionButton play_pauseButton;
View view;
MediaPlayer mediaPlayer;
int position =-1;
    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_now_playing_bottom,container,false);
        artist = view.findViewById(R.id.artist_name_miniPlayer);
        songName = view.findViewById(R.id.song_name_miniPlayer);
       albumArt = view.findViewById(R.id.bottom_Album_Art);
        nextBtn = view.findViewById(R.id.skip_next_Bottom);
        play_pauseButton = view.findViewById(R.id.play_pause_miniPlayer);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(mediaPlayer.isPlaying()){
//                    nextBtn.setImageResource(R.drawable.play_arrow_);
//                    mediaPlayer.pause();
//                }
//                else {
//                    nextBtn.setImageResource(R.drawable.pause);
//                    mediaPlayer.start();
//                }
            }
        });
        play_pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(mediaPlayer.isPlaying()){
//                    play_pauseButton.setImageResource(R.drawable.play_arrow_);
//                    mediaPlayer.pause();
//                }
//                else {
//                    play_pauseButton.setImageResource(R.drawable.pause);
//                    mediaPlayer.start();
//                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOE_MINI_PLAYER){
            if(PATH_TO_FRAG!= null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art!=null){
                    Glide.with(Objects.requireNonNull(getContext())).load(art).into(albumArt);
                }
                else{

                    Glide.with(Objects.requireNonNull(getContext())).load(R.drawable.umang).into(albumArt);
                }
                songName.setText(SONG_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);

            }
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