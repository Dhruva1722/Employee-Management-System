package com.example.etms.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class AttendanceDatabaseHelper extends SQLiteOpenHelper {

    // Database configuration
    private static final String DATABASE_NAME = "AttendanceDB";
    private static final int DATABASE_VERSION = 2; // Increment version for schema updates

    // Table and column names
    private static final String TABLE_ATTENDANCE = "attendance";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMPLOYEE_ID = "employeeId";
    private static final String COLUMN_CHECK_IN_TIME = "checkInTime";
    private static final String COLUMN_CHECK_OUT_TIME = "checkOutTime";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_IMAGE = "image";

    // Create table SQL query
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_ATTENDANCE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EMPLOYEE_ID + " TEXT, " +
                    COLUMN_CHECK_IN_TIME + " TEXT, " +
                    COLUMN_CHECK_OUT_TIME + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_IMAGE + " BLOB);";

    public AttendanceDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE); // Create the attendance table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old table if it exists and create a new one
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
            onCreate(db);
        }
    }

    // Method to insert check-in data
    public long insertAttendance(String employeeId, String checkInTime, String checkOutTime, double latitude, double longitude, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMPLOYEE_ID, employeeId);
        values.put(COLUMN_CHECK_IN_TIME, checkInTime);
        values.put(COLUMN_CHECK_OUT_TIME, checkOutTime);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_IMAGE, image);

        // Insert the row and return the new row ID
        return db.insert(TABLE_ATTENDANCE, null, values);
    }

    // Method to update check-out time
    public int updateCheckOut(String employeeId, String checkOutTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CHECK_OUT_TIME, checkOutTime);

        String whereClause = COLUMN_EMPLOYEE_ID + " = ? AND " + COLUMN_CHECK_OUT_TIME + " IS NULL";
        String[] whereArgs = {employeeId};

        // Update the row and return the number of rows affected
        return db.update(TABLE_ATTENDANCE, values, whereClause, whereArgs);
    }

    // Method to retrieve all attendance data
    public Cursor getAllAttendanceData() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query all rows in the table
        return db.query(TABLE_ATTENDANCE, null, null, null, null, null, null);
    }

}
