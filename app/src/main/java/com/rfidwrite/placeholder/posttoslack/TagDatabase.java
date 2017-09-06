package com.rfidwrite.placeholder.posttoslack;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sonny on 2017/08/04.
 */

public class TagDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "timeTracker";
    private static final String TABLE_TAGS = "tags";

    private static final String COLUMN_ID = "_id";
    private  static final String COLUMN_TAGID = "tagId";
    private static final String COLUMN_DATE_TIME= "date_time";
    private static final String COLUMN_USED = "inUse";

    public TagDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TAGS_TABLE = "CREATE TABLE " + TABLE_TAGS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_TAGID + " TEXT," + COLUMN_DATE_TIME + " TEXT," + COLUMN_USED + " INTEGER" + ")";
        db.execSQL(CREATE_TAGS_TABLE);
        System.out.print("Creating new database");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS);
        System.out.print("Dropping table");
        onCreate(db);
    }

    public List<EntranceTag> listTags(){
        String sql = "select * from " + TABLE_TAGS;
        SQLiteDatabase db = this.getReadableDatabase();
        List<EntranceTag> bunchOfTags = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if(cursor.moveToFirst()){
            do{
                int idFromDb = Integer.parseInt(cursor.getString(0));
                String tagId = cursor.getString(1);
                String dateInfo = cursor.getString(2);
                int inUseFlag = cursor.getInt(3);
                EntranceTag oneTag = new EntranceTag(tagId, dateInfo, inUseFlag);
                oneTag.SetIdFromDb(idFromDb);
                bunchOfTags.add(oneTag);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return bunchOfTags;
    }

    public void addTag(EntranceTag newtag){
        ContentValues values = new ContentValues();

        values.put(COLUMN_TAGID, newtag.GetTagId());
        values.put(COLUMN_DATE_TIME, newtag.GetTagDateTime());
        values.put(COLUMN_USED, newtag.GetTagUsedFlag());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_TAGS, null, values);
    }

    public void updateTag(EntranceTag newtag){
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAGID, newtag.GetTagId());
        values.put(COLUMN_DATE_TIME, newtag.GetTagDateTime());
        values.put(COLUMN_USED, newtag.GetTagUsedFlag());
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(TABLE_TAGS, values, COLUMN_ID    + "    = ?", new String[] { String.valueOf(newtag.GetTagIdFromDb())});
    }

    public EntranceTag findTag(String name){
        String query = "Select * FROM "    + TABLE_TAGS + " WHERE " + COLUMN_TAGID + " = '" + name +"'";
        SQLiteDatabase db = this.getWritableDatabase();
        EntranceTag mTag = null;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()){
            int idFromDb = Integer.parseInt(cursor.getString(0));
            String tagId = cursor.getString(1);
            String dateInfo = cursor.getString(2);
            int inUseFlag = cursor.getInt(3);

            mTag = new EntranceTag(tagId, dateInfo, inUseFlag);
            mTag.SetIdFromDb(idFromDb);

        }
        cursor.close();
        return mTag;
    }

    public void deleteTag(int idInDb){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TAGS, COLUMN_ID    + "    = ?", new String[] { String.valueOf(idInDb)});
    }

    public void clearDatabase() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_TAGS,null,null); //erases everything in the table.
        db.close();
    }
}
