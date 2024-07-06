package com.example.val_info;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.val_info.database.InternalPreference;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ManagerFragment extends AppCompatActivity {

    private static final String TAG_Home = "fr_home";
    private static final String TAG_News = "fr_news";
    private static final String TAG_Profile = "fr_profile";
    private static String TAG_Current = TAG_Home;
    private int TAG_Index = 0;
    private Handler mHandler;
    private BottomNavigationView mNavigation;
    private InternalPreference mPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_manager);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 69);
        mPref = new InternalPreference(getApplication());

        mHandler = new Handler();
        mNavigation = findViewById(R.id.fm_bottom_menu);
        Global.TO_HOME = false;

        mNavigation.setOnNavigationItemSelectedListener(item -> {
            String tmp = TAG_Home;
            if (item.getItemId() == R.id.fr_home) {
                TAG_Index = 0;
                tmp = TAG_Home;
            } else if (item.getItemId() == R.id.fr_news) {
                TAG_Index = 1;
                tmp = TAG_News;
            } else if (item.getItemId() == R.id.fr_profile) {
                TAG_Index = 2;
                tmp = TAG_Profile;
            }

            if (!TAG_Current.equals(tmp)) {
                TAG_Current = tmp;
                loadFragment();
            }
            return true;
        });
        loadFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            try {
                Bitmap images = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                images.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] b = byteArrayOutputStream.toByteArray();
                mPref.setImageProfile(Base64.encodeToString(b, Base64.DEFAULT));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                loadFragment();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage("Aplikasi ini membutuhkan akses lokasi")
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 69))
                        .create()
                        .show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadFragment() {
        if (getSupportFragmentManager().findFragmentByTag(TAG_Current) != null) return;
        mHandler.post(() -> {
            Fragment fragment = getFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fr_container_view_tag, fragment, TAG_Current);
            transaction.commitAllowingStateLoss();
        });
        invalidateOptionsMenu();
    }

    private Fragment getFragment() {
        switch (TAG_Index) {
            case 1:
                return new NewsFragment();
            case 2:
                return new ProfileFragment(mPref);
            case 0:
            default:
                TAG_Index = 0;
                TAG_Current = TAG_Home;
                return new HomeFragment();
        }
    }
}
