package com.example.pmusics;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.internal.ContextUtils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context context;
    static ArrayList<MusicFiles> mFiles;

    MusicAdapter(Context context,ArrayList<MusicFiles> mFiles)
    {
        this.context=context;
        this.mFiles=mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.music_list,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.fileName.setText(mFiles.get(position).getTitle());
        byte[] image=getAlbum(mFiles.get(position).getPath());
        if (image!=null)
        {
            Glide.with(context).asBitmap()
                    .load(image)
                    .into(holder.album_art);
        }
        else {
            Glide.with(context)
                    .load(R.drawable.music_icon)
                    .into(holder.album_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,PlayerActivity.class);
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu=new PopupMenu(context,view);
                popupMenu.getMenuInflater().inflate(R.menu.menu_list, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item) -> {
                    switch (item.getItemId()){
                        case R.id.delete:
                            deleteFile(position,view);
                            break;
                        case R.id.share:
                            Toast.makeText(view.getContext(), "OK",Toast.LENGTH_SHORT).show();
                            shareSong(position,view);
                            break;
                    }
                    return true;
                });
            }
        });

    }



    private void deleteFile(int position, View view) {
        Uri contentUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId()));
        File file=new File(mFiles.get(position).getPath());
        boolean deleted= file.delete();
        if (deleted) {
            Toast.makeText(context,"Deleted ",Toast.LENGTH_SHORT).show();
            context.getContentResolver().delete(contentUri,null,null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(view, "File Deleted : ", Snackbar.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context,"Can't be deleted",Toast.LENGTH_SHORT).show();
            Snackbar.make(view, "File can't be deleted!", Snackbar.LENGTH_LONG).show();

        }
    }
    private void shareSong(int position, View view) {
        Intent shareIntent =   new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Insert Subject here");
        String app_url =  mFiles.get(position).getPath();
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,app_url);
        context.startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    @Override
    public int getItemCount() {

        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView fileName;
        ImageView album_art,menuMore;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName=itemView.findViewById(R.id.songName);
            album_art=itemView.findViewById(R.id.music_img);
            menuMore=itemView.findViewById(R.id.more);
        }

    }
    private byte[] getAlbum(String uri)
    {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
    void  updateList(ArrayList<MusicFiles> musicFilesArrayList)
    {
        mFiles=new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
