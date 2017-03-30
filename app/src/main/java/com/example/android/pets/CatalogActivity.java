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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

import java.util.List;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static int URL_LOADER = 0;
    public static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    PetCursorAdapter cursorAdapter;
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

        // Find the ListView which will be populated with the pet data
        ListView lv = (ListView) findViewById(R.id.list);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View empty_view = findViewById(R.id.empty_view);
        lv.setEmptyView(empty_view);

        // Find ListView to populate
        lv = (ListView) findViewById(R.id.list);
        // Setup cursorAdapter using null as it will be initialized by LoaderManager
        cursorAdapter = new PetCursorAdapter(this,null);
        // Attach cursor adapter to the ListView
        lv.setAdapter(cursorAdapter);

        // Setup the item click listener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent i = new Intent(CatalogActivity.this,EditorActivity.class);
                i.setData(ContentUris.withAppendedId(PetEntry.CONTENT_URI, id));
                startActivity(i);
            }
        });

        // Prepare the loader.  Either re-connect with an existing one or start a new one.
        getLoaderManager().initLoader(URL_LOADER,null,this);
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
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values where column names are keys
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetEntry.COLUMN_PET_BREED,"Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER,PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT,7);

        //long newRowId = db.insert(PetEntry.TABLE_NAME,null,values);
        Uri newRowUri = getContentResolver().insert(PetEntry.CONTENT_URI,values);
        long newRowId = ContentUris.parseId(newRowUri);
        if(newRowId == -1){
            Toast.makeText(this,"Could not insert dummy data into the database",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"Dummy data inserted with row id "+newRowId,Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAllPets(){
        // Create and/or open a database to read from it
        // SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Deletes all entries in the table
        int deletedRows = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);

        if(deletedRows >0){
            Toast.makeText(this,deletedRows+" rows deleted !",Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete all the pets?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllPets();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null)
                    // User clicked the "Cancel" button, so dismiss the dialog
                    // and continue editing the pet.
                    dialogInterface.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // This is called when a new Loader needs to be created.
    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        // The CursorLoader finds our PetProvider using the CONTENT_URI
        // and calls the query method whenever needed
        Log.v(LOG_TAG,"onCreateLoader called !");
        return new CursorLoader(this,PetEntry.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        Log.v(LOG_TAG,"onLoadFinished called !");
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        Log.v(LOG_TAG,"onLoaderReset called !");
        cursorAdapter.swapCursor(null);
    }
}
