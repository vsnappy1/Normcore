package com.pranav.normcore.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pranav.normcore.CustomClasses.FormatDateAndTime;
import com.pranav.normcore.CustomClasses.Post;
import com.pranav.normcore.R;

import java.util.ArrayList;
import java.util.Date;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    private ArrayList<Post> list;
    private Context context;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewTime;
        ImageView imageViewImage;

        MyViewHolder(View view) {
            super(view);

            textViewName = view.findViewById(R.id.textViewPostItemName);
            textViewTime = view.findViewById(R.id.textViewPostItemTime);
            imageViewImage = view.findViewById(R.id.imageViewPostItemImage);
        }
    }

    public PostAdapter(Context context, ArrayList<Post> arrayList){
        list = arrayList;
        this.context = context;
    }


    @NonNull
    @Override
    public PostAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post,parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.MyViewHolder myViewHolder, int position) {

        myViewHolder.textViewName.setText(list.get(position).influncerName);

        Date date = new Date(list.get(position).time);
        myViewHolder.textViewTime.setText(new FormatDateAndTime().getFormattedDate(date.getTime()));

        String url = list.get(position).mediaUrl;
        boolean isImage = list.get(position).isImage;
        if(isImage){
            myViewHolder.imageViewImage.setVisibility(View.VISIBLE);
            fetchImage(url, myViewHolder.imageViewImage);
            Log.i("hhha", "loading");
        }
        else {
//           Video logic
        }
    }

    private void fetchImage(String url, ImageView imageView){
            try {
                Glide.with(context)
                        .load(url)
                        .into(imageView);
            }
            catch (Exception e){
                Log.e("ERRORRRR", e.toString());
            }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
