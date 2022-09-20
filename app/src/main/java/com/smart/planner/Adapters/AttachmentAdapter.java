package com.smart.planner.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.smart.planner.EventEditor;
import com.smart.planner.R;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {
    private ArrayList<Uri> files;
    private EventEditor editor;
    private Context context;

    public AttachmentAdapter(ArrayList<Uri> lists, EventEditor editor) {
        this.files = lists;
        this.editor = editor;
        context = this.editor.getApplicationContext();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public ImageView remove_file;
        public FrameLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            icon = itemView.findViewById(R.id.icon);
            //file_name = itemView.findViewById(R.id.file_name);
            remove_file = itemView.findViewById(R.id.remove);
        }
    }

    @Override
    public AttachmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.temp_attachment_item, parent, false);
        view.findViewById(R.id.layout).setMinimumWidth(parent.getWidth() / 3);
        return new AttachmentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AttachmentAdapter.ViewHolder holder, final int position) {
        Uri uri = this.files.get(position);
        System.out.println("AAA "+uri.getPath());
        if (uri.getPath().startsWith("/v0/b/planner-49228.appspot.com/")) {
            Glide.with(editor).load(uri).into(holder.icon);
        } else {
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType.equals("application/pdf")) {
                holder.icon.setImageResource(R.drawable.ic_attachment_pdf);
            } else if (mimeType.equals("image/png") | mimeType.equals("image/jpg") | mimeType.equals("image/jpeg")) {

                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
                    holder.icon.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
            } else if (mimeType.equals("video/mp4")) {
                holder.icon.setImageResource(R.drawable.ic_attachment_video);
            } else if (mimeType.equals("audio/mpeg")) {
                holder.icon.setImageResource(R.drawable.ic_attachment_audio);
            } else {
                holder.icon.setImageResource(R.drawable.ic_attachment_default);
                Toast.makeText(editor, mimeType, Toast.LENGTH_SHORT).show();
            }
        }

        holder.remove_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                files.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, files.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}
