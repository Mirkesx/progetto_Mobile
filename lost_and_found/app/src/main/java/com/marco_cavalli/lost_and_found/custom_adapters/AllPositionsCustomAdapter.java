package com.marco_cavalli.lost_and_found.custom_adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.Position;

import java.util.ArrayList;

public class AllPositionsCustomAdapter extends ArrayAdapter {


    private final Context context;
    private final ArrayList<Position> objects;

    public AllPositionsCustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Position> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inf.inflate(R.layout.positions_custom_list_item, null);

        TextView textDesc = (TextView) convertView.findViewById(R.id.found_lost_user_name);
        TextView textDate = (TextView) convertView.findViewById(R.id.found_lost_object_name);
        ImageView image = (ImageView) convertView.findViewById(R.id.home_position_map);

        textDesc.setText(objects.get(position).getDescription());
        if(objects.get(position).getDate() != null) {
            textDate.setText(objects.get(position).getDate());
        } else {
            textDate.setVisibility(View.INVISIBLE);
        }

        if(objects.get(position).getLatitude() != null && objects.get(position).getLongitude() != null) {
            image.setOnClickListener(v -> {
                Position pos = objects.get(position);
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri;
                gmmIntentUri = Uri.parse("geo:0,0?q="+pos.getLatitude()+","+pos.getLongitude()+"()");
                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");
                // Attempt to start an activity that can handle the Intent
                context.startActivity(mapIntent);
            });
        } else {
            image.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
}
