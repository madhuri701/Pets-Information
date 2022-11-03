package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.pets.data.PetContract.PetEntry;
/**
 * {@link ContentProvider} for Pets app.
 */
public class PetProvider extends ContentProvider {
public static final String LOG_TAG = PetProvider.class.getSimpleName();
private static final int PETS = 100;
private static final int PET_ID = 101;

private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

//static initializer
    static
{
    sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS,PETS);
    sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID );


}

//Database helper object
private PetDbHelper mDbHelper;
    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch(match){
            case PETS:
                cursor = database.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;
            case PET_ID:

                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null , null , sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot Query unknown uri" + uri);
        }
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match  = sUriMatcher.match(uri);
        switch(match)
        {
            case PETS:
                return insertPets(uri,contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for" + uri);
        }

    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    private Uri insertPets(Uri uri, ContentValues values)
    {
        //check the name is not null

        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if(name==null)
        {
            throw new IllegalArgumentException("Pet requires a name");
        }
        //Gender is valid or not
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if(gender == null || !PetEntry.isValidGender(gender))
        {
            throw new IllegalArgumentException("Requires valid gender");
        }
        //if the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if(weight != null && weight<0)
        {
            throw new IllegalArgumentException("Requires valid weight");
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(PetEntry.TABLE_NAME, null, values);
        if(id==-1)
        {
            Log.e(LOG_TAG,"Failed to insert new row" + uri);
            return null;
        }
        return ContentUris.withAppendedId(uri,id);
    }
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
    final int match = sUriMatcher.match(uri);
    switch(match)
    {
        case PETS:
            return update(uri, contentValues, selection,selectionArgs);
        case PET_ID:
            selection = PetEntry._ID + "=?";
            selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            return updatePet(uri,contentValues,selection,selectionArgs);
        default:
            throw new IllegalArgumentException("update is not supported for this" + uri);
    }
    }
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        if(values.containsKey(PetEntry.COLUMN_PET_NAME))
        {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if(name==null)
            {
                throw new IllegalArgumentException("pet requires name");
            }
        }
        //for gender
        if(values.containsKey(PetEntry.COLUMN_PET_GENDER))
        {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if(gender==null || !PetEntry.isValidGender(gender))
            {
                throw new IllegalArgumentException("valid gender requires");
            }
        }
        //for weight
        if(values.containsKey(PetEntry.COLUMN_PET_WEIGHT))
        {
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if(weight!=null && weight<0)
            {
                throw new IllegalArgumentException("pet requires valid weight");
            }
        }
        if(values.size()==0)
        {
            return 0;
        }

        // no required to check breed
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // returns the number
        return database.update(PetEntry.TABLE_NAME,values,selection,selectionArgs);

    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        //get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
            case PET_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        }
        return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);

    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match)
        {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("unknown Uri" + uri + "with match"+ match);
        }
    }
}