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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDbHelper;

import java.util.List;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int URL_LOADER = 1;

    /** EditText field to enter the pet's name */
    private EditText mNameEditText;

    /** EditText field to enter the pet's breed */
    private EditText mBreedEditText;

    /** EditText field to enter the pet's weight */
    private EditText mWeightEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    private PetDbHelper mDbHelper;
    Uri intentUri;
    private boolean mPetHasChanged=false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();

        intentUri = getIntent().getData();
        Log.v(LOG_TAG,"intentUri : "+intentUri);

        if(intentUri == null)
        {
            setTitle("Add a pet");
            // This method invokes the onPrepareOptionsMenu() being called (again)
            // so you can dynamically (at runtime) change menu items.
            invalidateOptionsMenu();
        }
        else
        {
            setTitle("Edit pet");
            getLoaderManager().initLoader(URL_LOADER,null,this);
        }

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
    }

    // Hook up the back button
    @Override
    public void onBackPressed() {
        if(!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "Discard" button, close the current activity.
                finish();
            }
        };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener)
    {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");

        // The second parameter is a click listener
        // Create a click listener to handle the user confirming that changes should be discarded or not.
        builder.setPositiveButton("Discard", discardButtonClickListener);

        builder.setNegativeButton("Keep editing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInterface != null)
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                dialogInterface.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    // onCreateOptionsMenu is called first
    // then onPrepareOptionsMenu is called
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if(intentUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void savePet(){

        // Create a new map of values where column names are keys
        ContentValues values = new ContentValues();

        String name = mNameEditText.getText().toString().trim();
        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(this,"Name cannot be blank",Toast.LENGTH_SHORT).show();
            return;
        }
        else
            values.put(PetEntry.COLUMN_PET_NAME,name);

        String breed = mBreedEditText.getText().toString().trim();
        if(TextUtils.isEmpty(breed))
        {
            Toast.makeText(this,"Breed cannot be blank",Toast.LENGTH_SHORT).show();
            return;
        }
        else
            values.put(PetEntry.COLUMN_PET_BREED,breed);

        values.put(PetEntry.COLUMN_PET_GENDER,mGender);

        try {
            values.put(PetEntry.COLUMN_PET_WEIGHT, Integer.parseInt(mWeightEditText.getText().toString().trim()));
        }catch (Exception e){
            Log.e(LOG_TAG,"Please specify weight "+e);
            Toast.makeText(this, "Please Specify a valid Weight",Toast.LENGTH_SHORT).show();
            return;
        }

        Uri newRowUri;
        int rowsAffected;
        if(intentUri == null)
        {
            newRowUri = getContentResolver().insert(PetEntry.CONTENT_URI,values);
            if(newRowUri == null){
                Toast.makeText(this,"Error inserting in the database.",Toast.LENGTH_SHORT).show();
            }else {
                long newRowId = ContentUris.parseId(newRowUri);
                Toast.makeText(this,"Pet Row inserted with id : "+newRowId,Toast.LENGTH_SHORT).show();
                // Close the editor activity on successful insertion
                finish();
            }
        }
        else
        {
            if(mPetHasChanged) {
                String selection = PetEntry._ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(intentUri))};
                rowsAffected = getContentResolver().update(intentUri, values, selection, selectionArgs);
                if (rowsAffected == 0)
                    Toast.makeText(this, "Error updating the row in database.", Toast.LENGTH_SHORT).show();
                else {
                    long updatedRowId = ContentUris.parseId(intentUri);
                    Toast.makeText(this, "Updated row id " + updatedRowId + " in database.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }else{
                Toast.makeText(this, "Nothing updated in database.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void deletePet(){

        // Deletes the received pet Uri from the table
        int deletedRows = getContentResolver().delete(intentUri, null, null);

        if(deletedRows >0) {
            Toast.makeText(this, "Pet deleted with row id " + ContentUris.parseId(intentUri), Toast.LENGTH_SHORT).show();
            finish();
        }
        else
            Toast.makeText(this,"Error in deleting pet",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                savePet();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                deletePet();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                if(!mPetHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Hook up the up button
                // If there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };
        return new CursorLoader(this, intentUri, projection,null,null,null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {

        if(cursor.moveToFirst()) {

            // mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
            mNameEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_NAME)));

            // mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
            mBreedEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_BREED)));

            // mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
            mWeightEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_WEIGHT)));

            // mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
            mGenderSpinner.setSelection(cursor.getInt(cursor.getColumnIndexOrThrow(PetEntry.COLUMN_PET_GENDER)));
        }
        else{
            Log.v(LOG_TAG,"cursor.moveToFirst() returned false !");
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }
}