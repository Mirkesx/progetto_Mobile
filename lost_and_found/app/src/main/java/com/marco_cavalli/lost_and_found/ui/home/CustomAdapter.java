package com.marco_cavalli.lost_and_found.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter {

    private final Context context;
    public ArrayList<PersonalObject> objs;

    public CustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PersonalObject> objects) {
        super(context, resource, objects);
        this.objs = objects;
        this.context = context;
        Toast.makeText(context, "Prova", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Toast.makeText(context, "Ciao", Toast.LENGTH_SHORT).show();

        LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inf.inflate(R.layout.home_custom_list_item, null);

        TextView tv = (TextView) convertView.findViewById(R.id.home_item_text);
        tv.setText(((PersonalObject)getItem(position)).getName());
        Toast.makeText(context, "Ciao", Toast.LENGTH_SHORT).show();

        return convertView;
    }
}
