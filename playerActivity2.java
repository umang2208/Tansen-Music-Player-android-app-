package com.example.tansanmusicplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static com.example.tansanmusicplayer.ApplicationClass.ACTION_NEXT_;
import static com.example.tansanmusicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.tansanmusicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.tansanmusicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.tansanmusicplayer.MainActivity.musicFiles;
import static com.example.tansanmusicplayer.MainActivity.repeatBoolean;
import static com.example.tansanmusicplayer.MainActivity.shuffleBoolean;
import static com.example.tansanmusicplayer.albumDetailAdapter.albumFiles;

public class playerActivity2 extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    TextView song_name,artist_name,duration_played ,duration_total;
    ImageView cover_art,next_btn,prevBtn,backBtn,shuffleBtn,repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position=-1;
    static ArrayList<MusicFiles> listsongs=new ArrayList<>();
    static Uri uri;
  // static MediaPlayer mediaPlayer;
    private Handler handler=new Handler();
    private Thread nextThread;
    private Thread prevThread;
    MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player2);
        getSupportActionBar().hide();
        initViews();
        getInterMethod();


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(musicService!=null && fromUser){

                    musicService.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        playerActivity2.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService!=null){
                    int mCurrentPosition=musicService.getCurrentPosition(position)/1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }


        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffleBoolean){
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.shuffle_off);
                }
                else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(repeatBoolean){
                    repeatBoolean= false;
                    repeatBtn.setImageResource(R.drawable.repeat_off);
                }
                else{
                    repeatBoolean= true;
                    repeatBtn.setImageResource(R.drawable.repeat_on);
                }
            }
        });
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        Intent intent= new Intent( this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(this);
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    private void nextThreadBtn() {
         nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                next_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void playThreadBtn() {
        Thread playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }
    public void prevBtnClicked(){
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position=getRandom(listsongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position=((position -1)< 0 ? (listsongs.size()-1):(position-1));
            }

            uri=Uri.parse(listsongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listsongs.get(position).getTitle());
            artist_name.setText(listsongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            playerActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition(position)/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }


            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.pause);
            playPauseBtn.setBackgroundResource(R.drawable.pause);
            musicService.start();

        }
        else {
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position=getRandom(listsongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position=((position -1)< 0 ? (listsongs.size()-1):(position-1));
            }
          //  position=((position +1)< 0 ? (listsongs.size()-1):(position-1));
            uri=Uri.parse(listsongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listsongs.get(position).getTitle());
            artist_name.setText(listsongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            playerActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition(position)/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }


            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.play_arrow_);
            playPauseBtn.setBackgroundResource(R.drawable.play_arrow_);
        }

    }
  public void  nextBtnClicked(){
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position=getRandom(listsongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
//                if(position==listsongs.size()-1){
//                    position=0;
//                }
                position=((position+1) % listsongs.size());
            }
            //else position will be position
          //  position=((position +1)%listsongs.size());
            uri=Uri.parse(listsongs.get(position).getPath());
            musicService.createMediaPlayer(position );
            metaData(uri);
            song_name.setText(listsongs.get(position).getTitle());
            artist_name.setText(listsongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            playerActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition(position)/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }


            });
            musicService.onCompleted();
           musicService. showNotification(R.drawable.pause);
            playPauseBtn.setBackgroundResource(R.drawable.pause);
            musicService.start();

        }
        else{
            musicService.stop();
            musicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position=getRandom(listsongs.size()-1);
            }
            else if(!shuffleBoolean && !repeatBoolean){
                position=position+1 % listsongs.size();
            }

            uri=Uri.parse(listsongs.get(position).getPath());
           musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listsongs.get(position).getTitle());
            artist_name.setText(listsongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration()/1000);
            playerActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition(position)/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }


            });
            musicService.onCompleted();
           musicService. showNotification(R.drawable.play_arrow_);
            playPauseBtn.setBackgroundResource(R.drawable.play_arrow_);
        }

  }

    private int getRandom(int i) {
        Random random = new Random();

        return random.nextInt(i+1);
    }

    public void playPauseBtnClicked(){
        if(musicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.play_arrow_);
            musicService.showNotification(R.drawable.play_arrow_);
            musicService.pause();
            seekBar.setMax(musicService.getDuration()/1000);
            playerActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition(position)/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }


            });
        }
        else {
            playPauseBtn.setImageResource(R.drawable.pause);
            musicService.showNotification(R.drawable.pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration()/1000);
            playerActivity2.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService!=null){
                        int mCurrentPosition=musicService.getCurrentPosition(position)/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }


            });
        }
}
    private String formattedTime(int mCurrentPosition) {
        String total="";
        String totalNew="";
        String seconds=String.valueOf(mCurrentPosition %60);
        String minutes=String.valueOf(mCurrentPosition/60);
        total=minutes+":"+seconds;
        totalNew =minutes +":"+ "0"+seconds;
        if(seconds.length()==1){
            return totalNew;
        }
        else  return total;

    }
    private void getInterMethod() {
        position= getIntent().getIntExtra("position",-1);
        String sender =getIntent().getStringExtra("sender");
        if(sender !=null && sender.equals("albumDetails")){
            listsongs = albumFiles;
        }
        else{
            listsongs=musicFiles;
        }
        if(listsongs!=null){
            playPauseBtn.setImageResource(R.drawable.pause);
            uri= Uri.parse(listsongs.get(position).getPath());
        }

        Intent intent=new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);


    }

    private void initViews() {
        song_name=findViewById(R.id.song_name);
        artist_name=findViewById(R.id.song_artist);
        duration_played=findViewById(R.id.durationPlayed);
        duration_total=findViewById(R.id.durationTotal);
        cover_art=findViewById(R.id.cover_art);
        next_btn=findViewById(R.id.id_next);
        prevBtn=findViewById(R.id.id_prev);
        backBtn=findViewById(R.id.back_btn);
        shuffleBtn=findViewById(R.id.id_shuffle);
        repeatBtn=findViewById(R.id.id_repeat);
        playPauseBtn=findViewById(R.id.play_pause);
        seekBar=findViewById(R.id.seekBaar);

    }
    private void metaData(Uri uri){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal=Integer.parseInt(listsongs.get(position).getDuration())/1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art=retriever.getEmbeddedPicture();
        Bitmap bitmap;
        if(art!=null) {

            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this,cover_art,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    assert palette != null;
                    Palette.Swatch swatch=palette.getDominantSwatch();
                    if(swatch!=null){
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer=findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[] {swatch.getRgb(),0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg =new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{swatch.getRgb(),swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    }
                    else{
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer=findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[] {0xff000000,0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg =new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,new int[]{0xff000000,0xff000000});
                        mContainer.setBackground(gradientDrawableBg);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }

                }

            });

        }

        else{
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.umang)
                    .into(cover_art);
            ImageView gradient = findViewById(R.id.imageViewGradient);
            RelativeLayout mContainer=findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.DKGRAY);
        }
    }
    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context,android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
       animOut.setAnimationListener(new Animation.AnimationListener() {
           @Override
           public void onAnimationStart(Animation animation) {

           }

           @Override
           public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
           }

           @Override
           public void onAnimationRepeat(Animation animation) {

           }
       });
       imageView.startAnimation(animOut);
    }



    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder)service;
        musicService=myBinder.getService();
        musicService.setCallBack(this);

        Toast.makeText(this,"Connected"+musicService,Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        song_name.setText(listsongs.get(position).getTitle());
        artist_name.setText(listsongs.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService=null;

    }


}