package com.example.val_info.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "val_info.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";

    // Tabel Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_ROLE = "role"; // "admin" atau "user"
    public static final String COLUMN_USER_DIVISION = "division"; // 1: DBS, 2: Maybank
    public static final String COLUMN_USER_STATUS = "status"; // Mitra 1, Mitra 2, Mitra 3
    public static final String COLUMN_USER_NIP = "nip";

    // Tabel Productivity
    public static final String TABLE_PRODUCTIVITY = "productivity";
    public static final String COLUMN_PROD_ID = "prod_id";
    public static final String COLUMN_PROD_USER_ID = "user_id";
    public static final String COLUMN_PROD_MONTH = "month";
    public static final String COLUMN_PROD_YEAR = "year";
    public static final String COLUMN_PROD_VALUE = "value";

    // Tabel News
    public static final String TABLE_NEWS = "news";
    public static final String COLUMN_NEWS_ID = "news_id";
    public static final String COLUMN_NEWS_DATE = "date";
    public static final String COLUMN_NEWS_DESCRIPTION = "description";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Membuat tabel Users
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_NAME + " TEXT, "
                + COLUMN_USER_EMAIL + " TEXT, "
                + COLUMN_USER_PASSWORD + " TEXT, "
                + COLUMN_USER_PHONE + " TEXT, "
                + COLUMN_USER_ROLE + " TEXT, "
                + COLUMN_USER_DIVISION + " INTEGER, "
                + COLUMN_USER_STATUS + " INTEGER, "
                + COLUMN_USER_NIP + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Membuat tabel Productivity
        String CREATE_PRODUCTIVITY_TABLE = "CREATE TABLE " + TABLE_PRODUCTIVITY + " ("
                + COLUMN_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PROD_USER_ID + " INTEGER, "
                + COLUMN_PROD_MONTH + " INTEGER, "
                + COLUMN_PROD_YEAR + " INTEGER, "
                + COLUMN_PROD_VALUE + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_PROD_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
                + ")";
        db.execSQL(CREATE_PRODUCTIVITY_TABLE);

        // Membuat tabel News
        String CREATE_NEWS_TABLE = "CREATE TABLE " + TABLE_NEWS + " ("
                + COLUMN_NEWS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NEWS_DATE + " TEXT, "
                + COLUMN_NEWS_DESCRIPTION + " TEXT"
                + ")";
        db.execSQL(CREATE_NEWS_TABLE);

        // Membuat indeks untuk meningkatkan kinerja
        db.execSQL("CREATE INDEX idx_user_email ON " + TABLE_USERS + " (" + COLUMN_USER_EMAIL + ")");
        db.execSQL("CREATE INDEX idx_prod_user_id ON " + TABLE_PRODUCTIVITY + " (" + COLUMN_PROD_USER_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTIVITY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public boolean saveProductivityDetails(int userId, int month, int year, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROD_USER_ID, userId);
        values.put(COLUMN_PROD_MONTH, month);
        values.put(COLUMN_PROD_YEAR, year);
        values.put(COLUMN_PROD_VALUE, value);

        long result = db.insert(TABLE_PRODUCTIVITY, null, values);
        db.close();

        return result != -1;
    }

}
