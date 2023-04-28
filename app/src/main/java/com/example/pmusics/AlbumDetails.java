package com.example.pmusics;

import static com.example.pmusics.MainActivity.musicFiles;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

public class AlbumDetails extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView albumImg;
    String albumName;
    AlbumDetailsAdapter albumDetailsAdapter;
    ArrayList<MusicFiles> albumFiles=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        recyclerView=findViewById(R.id.recyclerView);
        albumImg=findViewById(R.id.albumImg);
        albumName= getIntent().getStringExtra("albumName");
        int j=0;
        for (int i=0 ; i<musicFiles.size() ; i++)
        {
                if (albumName.equals(musicFiles.get(i).getAlbum()))
            {
                albumFiles.add(j,musicFiles.get(i));
                j++;
            }
        }
        byte[] image=getAlbum(albumFiles.get(0).getPath());
        if (image != null)
        {
            Glide.with(this)
                    .load(image)
                    .into(albumImg);
        }
        else {
            Glide.with(this)
                    .load(R.drawable.music_icon)
                    .into(albumImg);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(! (albumFiles.size()<1))
        {
            albumDetailsAdapter=new AlbumDetailsAdapter(this,albumFiles);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        }
    }

    private byte[] getAlbum(String uri)
    {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
      /* if (Build.VERSION.SDK_INT>=14){
           try {
               retriever.setDataSource(uri,new HashMap<String,String>());
            }catch (RuntimeException e){

           }
        }
        else{*/
        retriever.setDataSource(uri);
      //  }

        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}