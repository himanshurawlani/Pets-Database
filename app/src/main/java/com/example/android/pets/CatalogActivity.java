/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity {

    private PetDbHelper mDbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new PetDbHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        // Empty array is equivalent to 'Select * from ...'
        String[] projection = new String[]{};

        // Filter results WHERE "selection" = 'selectionArgs'
        String selection = null;
        String[] selectionArgs = new String[]{};

        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = db.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            displayView.setText("Number of rows in pets database table: " + cursor.getCount() + "\n\n");
            displayView.append(PetEntry._ID + " - " + PetEntry.COLUMN_PET_NAME + " - " + PetEntry.COLUMN_PET_BREED
            + " - " + PetEntry.COLUMN_PET_GENDER + " - " + PetEntry.COLUMN_PET_WEIGHT + "\n");

            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);
            while(cursor.moveToNext()){
                int id = cursor.getInt(cursor.getColumnIndex(PetEntry._ID));
                String name = cursor.getString(nameColumnIndex);
                String breed = cursor.getString(breedColumnIndex);
                String gender = cursor.getString(genderColumnIndex);
                int weight = cursor.getInt(weightColumnIndex);

                displayView.append("\n" + id + " - " + name + " - " + breed + " - " + gender + " - " + weight);
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    private void insertPet(){

        // Create and/or open a database to write to it
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values where column names are keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,7);

        long newRowId = db.insert(PetEntry.TABLE_NAME,null,values);
        if(newRowId == -1){
            Toast.makeText(this,"Could not insert dummy data into the database",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Dummy data successfully inserted into the database",Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePet(){
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Deletes all entries in the table
        int deletedRows = db.delete(PetEntry.TABLE_NAME,null,null);

        if(deletedRows >0){
            Toast.makeText(this,deletedRows+" rows deleted !",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deletePet();
                displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
