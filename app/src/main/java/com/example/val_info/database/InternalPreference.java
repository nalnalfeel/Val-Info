package com.example.val_info.database;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.app.AlertDialog;

import com.example.val_info.NewsAdmin;
import com.example.val_info.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class InternalPreference extends ContextWrapper {
    private final String DATABASE_APP = "com-example-val-info";
    private static final String KEY_ADMIN_NEWS = "com.val.info.admin.news";
    private final String KEY_ADMIN = "com.val.info.admin";
    private final String KEY_USER = "com.val.info.user";
    private final String KEY_EMAIL = "com.val.info.email";
    private final String KEY_PHONE = "com.val.info.phone";
    private final String KEY_PASSWORD = "com.val.info.password";
    private final String KEY_STATUS = "com.val.info.status";
    private final String KEY_DIVISI = "com.val.info.divisi";
    private final String KEY_PROFILE = "com.val.info.profile";
    private final String KEY_NIP = "com.val.info.nip";
    private final String KEY_LOGIN = "com.val.info.login";
    private final String KEY_CHECKIN_TIME = "com.val.info.checkin_time";
    private final String KEY_CHECKOUT_TIME = "com.val.info.checkout_time";
    public final String DEF_USER = "default.user";
    public final String DEF_PROFILE = "default.profile";
    public boolean ADMIN = false;
    public String USER = "default.user";
    public String EMAIL = "default.email";
    public String PASSWORD = "default.password";
    public String NIP = "default.nip";
    public long PHONE = 0;
    public int STATUS = 0;
    public int DIVISI = 0;
    public boolean LOGIN = false;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;
    private DatabaseReference mDatabase;

    public InternalPreference(Context c) {
        super(c);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        openData();
        ADMIN = mPref.getBoolean(KEY_ADMIN, false);
        USER = mPref.getString(KEY_USER, DEF_USER);
        EMAIL = mPref.getString(KEY_EMAIL, EMAIL);
        PHONE = mPref.getLong(KEY_PHONE, PHONE);
        STATUS = mPref.getInt(KEY_STATUS, STATUS);
        PASSWORD = mPref.getString(KEY_PASSWORD, PASSWORD);
        DIVISI = mPref.getInt(KEY_DIVISI, DIVISI);
        NIP = mPref.getString(KEY_NIP, NIP);
        LOGIN = mPref.getBoolean(KEY_LOGIN, LOGIN);
        closeData();
    }

    public void saveUser(String user, String email, String password, int status, int divisi, long phone, String nip, boolean isAdmin) {
        openData();
        mEdit.putBoolean(KEY_ADMIN, isAdmin);
        mEdit.putString(KEY_USER, user);
        mEdit.putString(KEY_EMAIL, email);
        mEdit.putLong(KEY_PHONE, phone);
        mEdit.putString(KEY_PASSWORD, password);
        mEdit.putInt(KEY_STATUS, status);
        mEdit.putInt(KEY_DIVISI, divisi);
        mEdit.putString(KEY_NIP, nip);
        mEdit.putBoolean(KEY_LOGIN, true); // Set login status to true
        closeData();
    }

    public void saveIsAdmin(boolean val) {
        openData();
        mEdit.putBoolean(KEY_ADMIN, val);
        closeData();
    }

    public void saveLoginStatus(boolean val) {
        openData();
        mEdit.putBoolean(KEY_LOGIN, val);
        closeData();
    }

    public void setImageProfile(String encode) {
        openData();
        mEdit.putString(KEY_PROFILE, encode);
        closeData();
    }

    public String getImageProfile() {
        openData();
        String tmp = mPref.getString(KEY_PROFILE, DEF_PROFILE);
        closeData();
        return tmp;
    }

    public void saveCheckInTime(String checkinTime) {
        openData();
        mEdit.putString(KEY_CHECKIN_TIME, checkinTime);
        closeData();
    }

    public void saveCheckOutTime(String checkoutTime) {
        openData();
        mEdit.putString(KEY_CHECKOUT_TIME, checkoutTime);
        closeData();
    }

    public String getCheckInTime() {
        openData();
        String checkinTime = mPref.getString(KEY_CHECKIN_TIME, "");
        closeData();
        return checkinTime;
    }

    public String getCheckOutTime() {
        openData();
        String checkoutTime = mPref.getString(KEY_CHECKOUT_TIME, "");
        closeData();
        return checkoutTime;
    }

    public void resetCheckInAndOutTime() {
        openData();
        mEdit.putString(KEY_CHECKIN_TIME, "");
        mEdit.putString(KEY_CHECKOUT_TIME, "");
        closeData();
    }

    public AlertDialog.Builder getAlertDialog(Context context) {
        return new AlertDialog.Builder(context, R.style.MyAlertDialodBackground);
    }

    public Dialog getDialog(Context context) {
        return new Dialog(context, R.style.MyAlertDialodBackground);
    }

    private void closeData() {
        mEdit.commit();
        mEdit.apply();
    }

    private void openData() {
        mPref = getSharedPreferences(DATABASE_APP, Context.MODE_PRIVATE);
        mEdit = mPref.edit();
    }

    public void savePassword(String password) {
        openData();
        mEdit.putString(KEY_PASSWORD, password);
        closeData();
    }

    public void saveAdminNews(List<NewsAdmin> newsList) {
        openData();
        Gson gson = new Gson();
        String json = gson.toJson(newsList);
        mEdit.putString(KEY_ADMIN_NEWS, json);
        closeData();
    }

    public List<NewsAdmin> getAdminNews() {
        openData();
        Gson gson = new Gson();
        String json = mPref.getString(KEY_ADMIN_NEWS, null);
        Type type = new TypeToken<ArrayList<NewsAdmin>>() {}.getType();
        List<NewsAdmin> newsList = gson.fromJson(json, type);
        closeData();
        return newsList != null ? newsList : new ArrayList<NewsAdmin>();
    }

    // Data Model untuk Absensi
    public class Attendance {
        private String date;
        private String checkInTime;
        private String checkOutTime;

        public String getDate() {
            return date;
        }

        // Constructor, Getter, and Setter
        public Attendance(String date, String checkInTime, String checkOutTime) {
            this.date = date;
            this.checkInTime = checkInTime;
            this.checkOutTime = checkOutTime;
        }

        public String getCheckInTime() {
            return checkInTime;
        }

        public void setCheckInTime(String checkInTime) {
            this.checkInTime = checkInTime;
        }

        public String getCheckOutTime() {
            return checkOutTime;
        }

        public void setCheckOutTime(String checkOutTime) {
            this.checkOutTime = checkOutTime;
        }
    }

    public void saveUserToFirebase(String user, String email, String password, int status, int divisi, long phone, String nip, boolean isAdmin) {
        User userObj = new User(user, email, password, status, divisi, phone, nip, isAdmin);
        mDatabase.child("users").child(user).setValue(userObj);
    }

    public class User {
        public String user;
        public String email;
        public String password;
        public int status;
        public int divisi;
        public long phone;
        public String nip;
        public boolean isAdmin;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String user, String email, String password, int status, int divisi, long phone, String nip, boolean isAdmin) {
            this.user = user;
            this.email = email;
            this.password = password;
            this.status = status;
            this.divisi = divisi;
            this.phone = phone;
            this.nip = nip;
            this.isAdmin = isAdmin;
        }
    }

    public void getUserFromFirebase(String user) {
        mDatabase.child("users").child(user).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User userObj = dataSnapshot.getValue(User.class);
                if (userObj != null) {
                    // Update local variables with data from Firebase
                    ADMIN = userObj.isAdmin;
                    USER = userObj.user;
                    EMAIL = userObj.email;
                    PHONE = userObj.phone;
                    STATUS = userObj.status;
                    PASSWORD = userObj.password;
                    DIVISI = userObj.divisi;
                    NIP = userObj.nip;
                    LOGIN = true; // Assuming user is logged in after fetching data
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }


}
