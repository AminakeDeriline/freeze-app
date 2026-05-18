package com.example.freeze;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "FreezeDB";
    private static final int DB_VERSION = 3; // Increased version because we changed the table structure

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table for frozen app packages
        db.execSQL("CREATE TABLE frozen_apps (package_name TEXT PRIMARY KEY, target_time LONG)");

        // Correct Table for Timer: Only stores the ONE absolute millisecond value
        db.execSQL("CREATE TABLE timer_settings (target_time LONG)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // If you change the database, this clears the old one and starts fresh
        db.execSQL("DROP TABLE IF EXISTS frozen_apps");
        db.execSQL("DROP TABLE IF EXISTS timer_settings");
        onCreate(db);
    }

    // --- Timer Methods (Fixes the 5-hour Offset) ---

    public void saveTargetMillis(long millis) { //detects the specific mooment the app should unlock
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM timer_settings"); // Clear any previous timer
        ContentValues cv = new ContentValues();
        cv.put("target_time", millis);
        db.insert("timer_settings", null, cv);
    }

    public long getTargetMillis() { //pulls target time from database so active adapter can calculate the time left
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT target_time FROM timer_settings", null);
        long time = 0;
        if (cursor != null && cursor.moveToFirst()) {
            time = cursor.getLong(0);
            cursor.close();
        }
        return time;
    }

    // --- App Management Methods ---

    public void addApps(ArrayList<AppModel> apps, long targetTime) { //writes the app into the database
        SQLiteDatabase db = this.getWritableDatabase();
        for (AppModel app : apps) {
            ContentValues cv = new ContentValues();
            cv.put("package_name", app.getPackageName());
            cv.put("target_time", targetTime);
            db.insertWithOnConflict("frozen_apps", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public boolean isAppFrozen(String pkg) {//checks with apps check service if app is open show pop up of freeze page
        SQLiteDatabase db = this.getReadableDatabase();
        long now = System.currentTimeMillis();
        Cursor cursor = db.rawQuery("SELECT * FROM frozen_apps WHERE package_name=? AND target_time > ?", new String[]{pkg, String.valueOf(now)});//
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public void unfreezeSingleApp(String pkg) {//delete an app that was unlocked from the database
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("frozen_apps", "package_name=?", new String[]{pkg});
    }

    public ArrayList<AppModel> getFrozenApps() {//get the list of everyapp that is currently lock
        ArrayList<AppModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM frozen_apps", null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                AppModel app = new AppModel("", null, cursor.getString(0));
                app.setTargetTime(cursor.getLong(1));
                list.add(app);
            }
            cursor.close();
        }
        return list;
    }
}