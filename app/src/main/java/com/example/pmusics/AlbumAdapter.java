package com.example.pmusics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder> {
    private Context context;
    private ArrayList<MusicFiles> albumFiles;
    View view;
    public AlbumAdapter(Context context, ArrayList<MusicFiles> albumFiles) {
        this.context = context;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view= LayoutInflater.from(context).inflate(R.layout.album_list,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.album_name.setText(albumFiles.get(position).getAlbum());
        byte[] image=getAlbum(albumFiles.get(position).getPath());
        if (image!=null)
        {
            Glide.with(context).asBitmap()
                    .load(image)
                    .into(holder.album_img);
        }
        else {
            Glide.with(context)
                    .load(R.drawable.music_icon)
                    .into(holder.album_img);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,AlbumDetails.class);
                intent.putExtra("albumName",albumFiles.get(position).getAlbum());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder{
        ImageView album_img;
        TextView album_name;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            album_img=itemView.findViewById(R.id.album_img);
            album_name=itemView.findViewById(R.id.album_name);
        }
    }
    private byte[] getAlbum(String uri)  {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
       /* if (Build.VERSION.SDK_INT>=14){
           try {
               retriever.setDataSource(uri,new HashMap<String,String>());
            }catch (RuntimeException e){

           }
        }
        else {*/
        retriever.setDataSource(uri);
        //}

        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
