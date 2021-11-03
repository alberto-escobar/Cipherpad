package com.example.cipherpad;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "cipherpad.db";
    public static final String TABLE_NAME = "notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_CIPHER = "cipher";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY, title TEXT, note TEXT, cipher TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }

    public Boolean insertNote(String title, String note, String cipher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("note", note);
        contentValues.put("cipher", cipher);
        if (db.insert(TABLE_NAME, null, contentValues) == -1){
            return false;
        }
        else{
            return true;
        }
    }

    public Boolean updateNote(Integer id, String title, String note, String cipher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("note", note);
        contentValues.put("cipher", cipher);
        if (db.update(TABLE_NAME, contentValues, "id = ? ", new String[]{Integer.toString(id)}) == 0){
            return false;
        }
        else{
            return true;
        }
    }

    public Boolean deleteNote(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (db.delete(TABLE_NAME,"id = ? ", new String[]{Integer.toString(id)}) == 0){
            return false;
        }
        else{
            return true;
        }
    }

    public ArrayList<String> getNote(Integer id){
        ArrayList<String> returnNote = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("SELECT * FROM notes WHERE id = ?", new String[] {Integer.toString(id)});
        res.moveToFirst();
        returnNote.add(res.getString(res.getColumnIndex(COLUMN_TITLE)));
        returnNote.add(res.getString(res.getColumnIndex(COLUMN_NOTE)));
        returnNote.add(res.getString(res.getColumnIndex(COLUMN_CIPHER)));
        res.close();
        return returnNote;
    }

    public ArrayList<Integer> getAllIds() {
        ArrayList<Integer> idList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("SELECT * FROM notes",null   );
        res.moveToFirst();

        while(!res.isAfterLast()){
            idList.add(res.getInt(res.getColumnIndex(COLUMN_ID)));
            res.moveToNext();
        }
        res.close();
        return idList;
    }

    public ArrayList<String> getAllTitles() {
        ArrayList<String> array_list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("SELECT * FROM notes",null   );
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(COLUMN_TITLE)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }


    @SuppressLint("Recycle")
    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM notes",null).getCount();
    }

    public int getLastId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery("SELECT id FROM notes",null   );
        res.moveToLast();
        int lastId = res.getInt(res.getColumnIndex(COLUMN_ID));
        res.close();
        return lastId;
    }

}
