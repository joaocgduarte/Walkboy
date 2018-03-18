package com.example.andro.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "song_drawer.db";
    private static final String TABLE_DOWNLOADS = "downloads";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_NUM_DOWNLOADS = "number_of_downloads";

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql_create_table = "CREATE TABLE " + TABLE_DOWNLOADS + "("
                + KEY_ARTIST + " TEXT NOT NULL, "
                + KEY_NUM_DOWNLOADS + " INTEGER NOT NULL);";
        db.execSQL(sql_create_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DBManager.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        String sql_delete_table = "DROP TABLE IF EXISTS " + TABLE_DOWNLOADS;
        db.execSQL(sql_delete_table);
        onCreate(db);
    }

    public boolean insert_record(String artist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues content_values = new ContentValues();
        content_values.put(KEY_ARTIST, artist);
        content_values.put(KEY_NUM_DOWNLOADS, 1);
        long result = db.insert(TABLE_DOWNLOADS, null, content_values);
        return result != -1;
    }

    public Cursor get_number_of_downloads_by_artist(String artist) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT number_of_downloads FROM \"" + TABLE_DOWNLOADS + "\" WHERE artist = \"" + artist + "\"";
        Cursor result = db.rawQuery(query, null);
        return result;
    }

    public Cursor get_all_data() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_DOWNLOADS + " ORDER BY " + KEY_NUM_DOWNLOADS + " DESC";
        Cursor result = db.rawQuery(query, null);
        return result;
    }

    public boolean update_number_of_downloads(String artist, int new_number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues content_values = new ContentValues();
        content_values.put(KEY_NUM_DOWNLOADS, new_number);
        String where_clause = KEY_ARTIST + " = ?";
        db.update(TABLE_DOWNLOADS, content_values, where_clause, new String[]{artist});
        return true;
    }

    public boolean update_number_of_downloads_from_artist(String artist) {
        Cursor old_number_of_downloads_cursor = get_number_of_downloads_by_artist(artist);
        if (old_number_of_downloads_cursor.getCount() > 0) {
            old_number_of_downloads_cursor.moveToFirst();
            return update_number_of_downloads(artist, old_number_of_downloads_cursor.getInt(0) + 1);
        } else {
            return insert_record(artist);
        }
    }

}
