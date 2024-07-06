package com.example.val_info;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;


import com.example.val_info.database.InternalPreference;
import com.google.android.material.button.MaterialButton;


public class RegisterFragment extends AppCompatActivity {

    private RadioButton rb_m1, rb_m2, rb_m3, rb_dbs, rb_maybank;
    private boolean isChangePassword = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register);
        InternalPreference pref = new InternalPreference(getApplicationContext());
        EditText et_user = (EditText)findViewById(R.id.editTextFullName);
        EditText et_email = (EditText)findViewById(R.id.editTextEmail);
        EditText et_password = (EditText)findViewById(R.id.editTextPassword);
        EditText et_phone = (EditText)findViewById(R.id.editTextNumberPhone);
        EditText et_nip = (EditText)findViewById(R.id.editTextNip);

        rb_m1 = (RadioButton)findViewById(R.id.rb_m1);
        rb_m2 = (RadioButton)findViewById(R.id.rb_m2);
        rb_m3 = (RadioButton)findViewById(R.id.rb_m3);
        rb_dbs = (RadioButton)findViewById(R.id.rb_DBS);
        rb_maybank = (RadioButton)findViewById(R.id.rb_Maybank);

        Intent intent = getIntent();
        if (intent != null && "CHANGE PASSWORD".equals(intent.getStringExtra("ACTION"))){
            isChangePassword = true;

            et_user.setVisibility(View.GONE);
            et_email.setVisibility(View.GONE);
            et_phone.setVisibility(View.GONE);
            et_nip.setVisibility(View.GONE);
            rb_m1.setVisibility(View.GONE);
            rb_m2.setVisibility(View.GONE);
            rb_m3.setVisibility(View.GONE);
            rb_dbs.setVisibility(View.GONE);
            rb_maybank.setVisibility(View.GONE);
        }

        ((MaterialButton) findViewById(R.id.fr_btn_register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isChangePassword) {
                    // Change password logic
                    if (et_password.getText().toString().length() > 0) {
                        pref.savePassword(et_password.getText().toString());
                        Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(RegisterFragment.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter a new password", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Register logic
                    int mitra;
                    if (rb_m1.isChecked()) mitra = 1;
                    else if (rb_m2.isChecked()) mitra = 2;
                    else mitra = 3;

                    int divisi;
                    if (rb_dbs.isChecked()) divisi = 1;
                    else divisi = 2;

                    if (et_user.getText().toString().length() > 0 &&
                            et_email.getText().toString().length() > 0 &&
                            et_phone.getText().toString().length() > 0 &&
                            et_password.getText().toString().length() > 0) {
                        String[] user = et_user.getText().toString().split(" ");
                        StringBuilder tmpUser = new StringBuilder();
                        for (int i = 0; i <= user.length - 1; i++) {
                            char tmp = user[i].charAt(0);
                            tmpUser.append(String.valueOf(tmp).toUpperCase() + user[i].substring(1));
                            tmpUser.append(" ");
                        }
                        String email = et_email.getText().toString().toLowerCase();
                        if (!email.contains("@") || !email.contains(".")) {
                            Toast.makeText(getApplicationContext(), "Invalid email", Toast.LENGTH_LONG).show();
                            return;
                        }

                        pref.saveUser(tmpUser.toString().trim(),
                                email,
                                et_password.getText().toString(), mitra, divisi,
                                Long.parseLong(et_phone.getText().toString()), et_nip.getText().toString(), false);

                        startActivity(new Intent(RegisterFragment.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}