package com.example.pmusics;

import static android.content.Context.MODE_PRIVATE;
import static com.example.pmusics.MainActivity.ARTIST_NAME;
import static com.example.pmusics.MainActivity.ARTIST_TO_FRAG;
import static com.example.pmusics.MainActivity.PATH_TO_FRAG;
import static com.example.pmusics.MainActivity.SHOW_MINI_PLAYER;
import static com.example.pmusics.MainActivity.SONG_TO_FRAG;
import static com.example.pmusics.MainActivity.musicFiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView nextbtn,albumart;
    TextView artist,songname;
    FloatingActionButton playpausebtn;
    View view;
    MusicService musicService;
    public static final String MUSIC_FILE_LAST = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String SONG_NAME = "SONG NAME";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        artist = view.findViewById(R.id.artistNameBottom);
        songname = view.findViewById(R.id.songNameBottom);
        nextbtn = view.findViewById(R.id.skip_next);
        albumart = view.findViewById(R.id.bottom_art);
        playpausebtn = view.findViewById(R.id.plauPauseBottom);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(),"Next Song",Toast.LENGTH_SHORT).show();
                if (musicService != null)
                {
                    musicService.nextBtn();
                }
                if (getActivity() != null){
                SharedPreferences.Editor editor =getActivity().getSharedPreferences(MUSIC_FILE_LAST,MODE_PRIVATE).edit();
                editor.putString(MUSIC_FILE,
                        musicService.musicFiles.get(musicService.position).getPath());
                editor.putString(ARTIST_NAME,
                        musicService.musicFiles.get(musicService.position).getArtist());
                editor.putString(SONG_NAME,
                        musicService.musicFiles.get(musicService.position).getTitle());
                editor.apply();
                    SharedPreferences preferences=getActivity().getSharedPreferences(MUSIC_FILE_LAST,MODE_PRIVATE);
                    String path = preferences.getString(MUSIC_FILE,null);
                    String artistName=preferences.getString(ARTIST_NAME,null);
                    String song=preferences.getString(SONG_NAME,null);
                    if (path != null)
                    {
                        SHOW_MINI_PLAYER = true;
                        PATH_TO_FRAG = path;
                        ARTIST_TO_FRAG = artistName;
                        SONG_TO_FRAG = song;
                    }
                    else {
                        SHOW_MINI_PLAYER = false;
                        PATH_TO_FRAG = null;
                        ARTIST_TO_FRAG = null;
                        SONG_TO_FRAG = null;
                    }
                    if (SHOW_MINI_PLAYER){
                        if (PATH_TO_FRAG != null) {
                            byte[] art = getAlbum(PATH_TO_FRAG);
                            if (art != null) {
                                Glide.with(getContext()).load(art)
                                        .into(albumart);
                            }
                            else
                                Glide.with(getContext()).load(R.drawable.music_icon)
                                                .into(albumart);
                            songname.setText(SONG_TO_FRAG);
                            artist.setText(ARTIST_TO_FRAG);

                        }
                    }
                }
            }
        });
        playpausebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicService != null)
                {
                    musicService.playPauseBtn();
                    if (musicService.isPlaying()){
                        playpausebtn.setImageResource(R.drawable.ic_baseline_pause_24);
                    }
                    else
                        playpausebtn.setImageResource(R.drawable.ic_baseline_play);
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SHOW_MINI_PLAYER){
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbum(PATH_TO_FRAG);
                if(art != null) {
                    Glide.with(getContext()).load(art)
                            .into(albumart);
                }
                else {
                    Glide.with(getContext()).load(R.drawable.music_icon)
                            .into(albumart);
                }
                songname.setText(SONG_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);
                Intent intent= new Intent(getContext(),MusicService.class);
                if (getContext() != null)
                {
                    getContext().bindService(intent,this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null){
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbum(String uri)
    {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        MusicService.MyBinder binder= (MusicService.MyBinder)service;
        musicService =binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}