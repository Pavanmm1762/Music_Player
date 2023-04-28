package com.example.pmusics;


import static com.example.pmusics.ApplicationClass.ACTION_NEXT;
import static com.example.pmusics.ApplicationClass.ACTION_PLAY;
import static com.example.pmusics.ApplicationClass.ACTION_PREVIOUS;
import static com.example.pmusics.ApplicationClass.CHANNEL_ID_2;
import static com.example.pmusics.MainActivity.repeat;
import static com.example.pmusics.PlayerActivity.listSongs;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder=new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles=new ArrayList<>();
    Uri uri;
    int position=-1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_FILE_LAST = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String SONG_NAME = "SONG NAME";
    public static final String ARTIST_NAME = "ARTIST NAME";
    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"My Audio");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    public class MyBinder extends Binder{
        MusicService getService(){
            return  MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPostion=intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPostion != -1) {
            playMedia(myPostion);
        }
        if (actionName != null){
            switch (actionName){
                case "playPause":
                    playPauseBtn();
                    break;
                case "next":
                   nextBtn();
                    break;
                case "previous":
                    prevBtn();
                    break;
            }
        }
        return START_STICKY;
    }



    private void playMedia(int startPosition) {
        musicFiles=listSongs;
        position=startPosition;
        if (mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    void start(){
        mediaPlayer.start();
    }
    boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }
    void stop(){

        mediaPlayer.stop();
    }
    void release() {
        mediaPlayer.release();
    }
    int getDuration(){
      return mediaPlayer.getDuration();
    }
    void seekTo(int position){
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int positionInner){
        position=positionInner;
        uri=Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST,MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE,uri.toString());
        editor.putString(ARTIST_NAME,musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME,musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer=MediaPlayer.create(getBaseContext(),uri);
    }
    void pause(){
        mediaPlayer.pause();
    }
    void onCompleted(){
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!repeat)
        {
           if(actionPlaying != null)
               actionPlaying.nextBtnClicked();
            createMediaPlayer(position);
            mediaPlayer.start();
            onCompleted();

        }
        else {
            createMediaPlayer(position);
            mediaPlayer.start();
            onCompleted();
        }
         /*else {
            uri=Uri.parse(listSongs.get(position).getPath());
            createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            seekBar.setMax(getDuration()/1000);
            mediaPlayer.start();
        }*/
    }
    void setCallBack(ActionPlaying actionPlaying){
            this.actionPlaying = actionPlaying;
    }

    void showNotification(int playPauseBtn){
        Intent intent=new Intent(this,PlayerActivity.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,
                intent,0);

        Intent prevIntent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending=PendingIntent.getBroadcast(this,0,
                prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending=PendingIntent.getBroadcast(this,0,
                pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending=PendingIntent.getBroadcast(this,0,
                nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture=null;
        picture= getAlbum(musicFiles.get(position).getPath());
        Bitmap thumb=null;
        if (picture != null)
        {
            thumb= BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else
        {
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.music_icon);
        }
        Notification notification= new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous_24,"Previous",prevPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .addAction(R.drawable.ic_baseline_skip_next_24,"Next",nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        startForeground(1,notification);
    }
    private byte[] getAlbum(String uri)
    {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        return art;
    }
    void playPauseBtn()
    {
        if (actionPlaying != null){
            actionPlaying.playPauseBtnClicked();
        }

    }
     void prevBtn() {
        if (actionPlaying != null){
            actionPlaying.prevBtnClicked();
        }
    }

     void nextBtn() {
        if (actionPlaying != null){
            actionPlaying.nextBtnClicked();
        }
    }
}
