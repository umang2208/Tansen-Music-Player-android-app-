package com.example.tansanmusicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.net.URI;
import java.util.ArrayList;

import static com.example.tansanmusicplayer.ApplicationClass.ACTION_NEXT_;
import static com.example.tansanmusicplayer.ApplicationClass.ACTION_PLAY;
import static com.example.tansanmusicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.example.tansanmusicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.tansanmusicplayer.MainActivity.musicFiles;
import static com.example.tansanmusicplayer.playerActivity2.listsongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    MyBinder myBinder =new MyBinder();
    MediaPlayer mediaPlayer;
    ActionPlaying actionPlaying;
    ArrayList<MusicFiles> musicFiles=new ArrayList<>();
    Uri uri;
    int position=-1;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE="STORED_MUSIC";
    public static final String ARTIST_NAME="ARTIST NAME";
    public static final String SONG_NAME="SONG NAME";
    @Override
    public void onCreate() {
        super.onCreate();
       // musicFiles=listsongs;
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"MY Audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind","Method");
        return myBinder;
    }

    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition=intent.getIntExtra("servicePosition",-1);
        String actionName =intent.getStringExtra("ActionName");

        if(myPosition!=-1){
           playMedia(myPosition);
        }
        if(actionName !=null){
            switch (actionName){
                case "playPause":
                  //  Toast.makeText(this,"playPause",Toast.LENGTH_SHORT).show();
                    if(actionPlaying!=null){
                        actionPlaying.playPauseBtnClicked();
                    }
                    break;
                case "next":
                //    Toast.makeText(this,"next",Toast.LENGTH_SHORT).show();
                    if(actionPlaying!=null){
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                //    Toast.makeText(this,"previous",Toast.LENGTH_SHORT).show();
                    if(actionPlaying!= null){
                        actionPlaying.prevBtnClicked();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles=listsongs;
        position=startPosition;
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles!=null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else{
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start(){
        mediaPlayer.start();
    }
    boolean isPlaying(){
      return  mediaPlayer.isPlaying();
    }
    void stop(){
        mediaPlayer.stop();
    }
    void pause(){
        mediaPlayer.pause();
    }
    void release(){
        mediaPlayer.release();
    }
    int getDuration(){
        return mediaPlayer.getDuration();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition(int position){
      //  uri= Uri.parse(musicFiles.get(position).getPath());
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int positionInner){
        position=positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
      //  editor.apply();
        editor.putString(ARTIST_NAME ,musicFiles.get(position).getArtist());
    //    editor.apply();
        editor.putString(SONG_NAME,musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer=MediaPlayer.create(getBaseContext(),uri);
    }
    void onCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying!=null){
            actionPlaying.nextBtnClicked();
            if(mediaPlayer !=null){
                createMediaPlayer(position);
                mediaPlayer.start();
                onCompleted();
            }
        }
    }
    void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying=actionPlaying;
    }
    void showNotification(int playPauseBtn){
        Intent intent =new Intent(this,playerActivity2.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);
        Intent prevIntent =new Intent(this,NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this,0,prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent pauseIntent =new Intent(this,NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this,0,pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent =new Intent(this,NotificationReceiver.class).setAction(ACTION_NEXT_);
        PendingIntent nextPending = PendingIntent.getBroadcast(this,0,nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        byte[] picture =null;
        picture =getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb =null;
        if(picture!=null){
            thumb = BitmapFactory.decodeByteArray(picture,0, picture.length);
        }
        else {
            thumb=BitmapFactory.decodeResource(getResources(),R.drawable.umang);
        }
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.skip_previous,"previous",prevPending)
                .addAction(playPauseBtn,"previous",pausePending)
                .addAction(R.drawable.skip_next,"previous",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();
        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
    }
    private byte[] getAlbumArt (String uri){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art= retriever.getEmbeddedPicture();
        retriever.release();
        return art;

    }
}
