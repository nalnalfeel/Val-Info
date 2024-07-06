package com.example.val_info;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.val_info.database.InternalPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    String test;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        InternalPreference pref = new InternalPreference(getApplicationContext());

        if (pref.LOGIN) {
            startActivity(new Intent(MainActivity.this, ManagerFragment.class));
            finish();
            return;
        }

        if (!Global.TO_HOME) {
            // splash on
            findViewById(R.id.am_iv_splash).setVisibility(View.VISIBLE);
            findViewById(R.id.am_rl_container).setVisibility(View.INVISIBLE);
        } else {
            // splash off
            findViewById(R.id.am_iv_splash).setVisibility(View.GONE);
            findViewById(R.id.am_rl_container).setVisibility(View.VISIBLE);
        }

        EditText et_email = findViewById(R.id.editEmailLogin);
        EditText et_password = findViewById(R.id.editPasswordLogin);

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (et_email.getText().toString().length() > 0 &&
                        et_password.getText().toString().length() > 0) {
                    String tmpEmail = et_email.getText().toString();
                    String tmpPass = et_password.getText().toString();
                    if (tmpEmail.equals("ADMIN") && tmpPass.equals("12345")) {
                        pref.saveIsAdmin(true);
                        pref.saveLoginStatus(true);
                        startActivity(new Intent(MainActivity.this, ManagerFragment.class));
                        finish();
                    } else if (tmpEmail.equals(pref.EMAIL) && tmpPass.equals(pref.PASSWORD)) {
                        pref.saveLoginStatus(true);
                        pref.saveIsAdmin(false);
                        startActivity(new Intent(MainActivity.this, ManagerFragment.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Wrong Email or Password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView tv_reg = findViewById(R.id.textViewRegisterLink);
        tv_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterFragment.class));
                finish();
            }
        });

        storageRef.child("Testvalinfo.txt").getStream(new StreamDownloadTask.StreamProcessor() {
            @Override
            public void doInBackground(@NonNull StreamDownloadTask.TaskSnapshot state, @NonNull InputStream stream) throws IOException {
                long totalByte = state.getTotalByteCount();
                byte[] buffer = new byte[1024];
                boolean read = true;

                while (read) {
                    read = stream.read(buffer) != -1;
                }

                test = new String(ByteBuffer.wrap(buffer).array());
                stream.close();
            }
        }).addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, test, Toast.LENGTH_SHORT).show();
                new CountDownTimer(2000, 1000) {
                    @Override
                    public void onTick(long l) {
                    }

                    @Override
                    public void onFinish() {
                        if (!pref.USER.equals(pref.DEF_USER) && !Global.TO_HOME && pref.LOGIN) {
                            startActivity(new Intent(MainActivity.this, ManagerFragment.class));
                            finish();
                        } else {
                            findViewById(R.id.am_iv_splash).setVisibility(View.GONE);
                            findViewById(R.id.am_rl_container).setVisibility(View.VISIBLE);
                        }
                    }
                }.start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                errorMessages();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (!isNetworkAvailable()) {
                    errorMessages();
                }
            }
        }.start();
    }

    private void errorMessages() {
        findViewById(R.id.am_iv_splash).setVisibility(View.GONE);
        findViewById(R.id.am_rl_container).setVisibility(View.VISIBLE);
        AlertDialog.Builder mDial = new AlertDialog.Builder(MainActivity.this);
        mDial.setTitle("Network Error!")
                .setMessage("Please turn on data or Wifi to continue use this app!")
                .setCancelable(true)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        startActivity(getIntent());
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        MainActivity.this.finish();
                    }
                });
        AlertDialog alert = mDial.create();
        alert.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null ? cm.getActiveNetworkInfo() : null;
        return ni != null && ni.isConnected();
    }
}
