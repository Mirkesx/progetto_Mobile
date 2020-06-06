package com.marco_cavalli.lost_and_found.custom_adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class HomeCustomAdapter extends ArrayAdapter {

    private final Context context;
    public ArrayList<PersonalObject> objs;

    public HomeCustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PersonalObject> objects) {
        super(context, resource, objects);
        this.objs = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inf.inflate(R.layout.home_custom_list_item, null);

        TextView textName = (TextView) convertView.findViewById(R.id.home_item_text);
        textName.setText(objs.get(position).getName());

        ImageView icon = (ImageView) convertView.findViewById(R.id.home_item_image);
        setImage(icon, objs.get(position).getIcon());

        return convertView;
    }

    private void setImage(ImageView icon, String path) {

        try{
            File iconFile = new File(context.getFilesDir()+"/objects_images",path);
            if(path.length() > 0 && iconFile.exists()) {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(iconFile));
                icon.setImageBitmap(b);
            } else {
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference islandRef = storageRef.child("users/"+ FirebaseAuth.getInstance().getUid()+"/objects_images/"+path);
                File newFile = new File(context.getFilesDir()+"/objects_images",path);

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
