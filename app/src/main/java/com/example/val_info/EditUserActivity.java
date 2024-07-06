package com.example.val_info;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.val_info.R;

public class EditUserActivity extends AppCompatActivity {

    private EditText etUsername, etNip, etStatus, etPhone, etEmail, etDivision;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_edit_data_users);

        etUsername = findViewById(R.id.et_username);
        etNip = findViewById(R.id.et_nip);
        etStatus = findViewById(R.id.et_status);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etDivision = findViewById(R.id.et_division);
        btnSave = findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implementasi penyimpanan data user
            }
        });
    }
}
