package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

/**
 * Created by Himanshu on 28/03/2017.
 */

public class PetCursorAdapter extends CursorAdapter {


    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvName = (TextView) view.findViewById(R.id.pet_name);
        TextView tvBreed = (TextView) view.findViewById(R.id.pet_breed);
        TextView tvWeight = (TextView) view.findViewById(R.id.pet_weight);
        TextView tvGender = (TextView) view.findViewById(R.id.pet_gender);
        // Extract properties from cursor
        String pet_name = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_NAME));
        String pet_breed = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_BREED));
        String pet_weight = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_WEIGHT));
        String pet_gender = cursor.getString(cursor.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_PET_GENDER));

        // Populate fields with extracted properties
        tvName.setText(pet_name);
        tvBreed.setText(pet_breed);
        tvWeight.setText(pet_weight+" Kg");
        if(pet_gender.equals("1"))
            tvGender.setText("Male");
        else if(pet_gender.equals("2"))
            tvGender.setText("Female");
        else
            tvGender.setText("Unknown");
    }
}
