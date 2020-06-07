package com.marco_cavalli.lost_and_found.custom_adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.FoundItem;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class LostCustomAdapter extends ArrayAdapter {

    private final Context context;
    private final ArrayList<FoundItem> objects;

    public LostCustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<FoundItem> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inf.inflate(R.layout.found_lost_custom_item, null);

        TextView userName = convertView.findViewById(R.id.found_lost_user_name);
        TextView objectName = convertView.findViewById(R.id.found_lost_object_name);
        TextView date = convertView.findViewById(R.id.found_lost_date);
        ImageView icon = convertView.findViewById(R.id.found_lost_image);

        userName.setText(objects.get(position).getUser_name());
        objectName.setText(objects.get(position).getObject_name());
        date.setText(objects.get(position).getDate());
        if(objects.get(position).getIcon().length() > 0)
            setImage(icon, objects.get(position).getIcon());

        if(objects.get(position).getSetFound()) {
            convertView.findViewById(R.id.found_lost_item).setBackgroundColor(context.getColor(R.color.found_item));
            userName.setTextColor(Color.BLACK);
            objectName.setTextColor(Color.BLACK);
            date.setTextColor(Color.BLACK);
        } else {
            convertView.findViewById(R.id.found_lost_item).setBackgroundColor(context.getColor(R.color.not_found_item));
            userName.setTextColor(Color.WHITE);
            objectName.setTextColor(Color.WHITE);
            date.setTextColor(Color.WHITE);
        }

        return convertView;
    }

    private void setImage(ImageView icon, String path) {
        try{
            File iconFile = new File(context.getFilesDir()+"/losts_images",path);
            if(path.length() > 0 && iconFile.exists()) {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(iconFile));
                icon.setImageBitmap(b);
            } else {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference islandRef = storageRef.child("losts/"+path);
                File newFile = new File(context.getFilesDir()+"/losts_images",path);

                islandRef.getFile(newFile).addOnSuccessListener(taskSnapshot -> {
                    icon.setImageURI(Uri.fromFile(newFile));
                }).addOnFailureListener(exception -> {
                    exception.printStackTrace();
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
