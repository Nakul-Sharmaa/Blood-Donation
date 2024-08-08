package com.example.blooddonation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etAge, etAddress, etPhoneNumber;
    private AutoCompleteTextView etBloodGroup;
    private ProgressBar progressBar;
    private TextView tvRecommendedUnits;
    private DatabaseHelper databaseHelper;

    private static final String[] BLOOD_GROUPS = new String[] {
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etAddress = findViewById(R.id.etAddress);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        progressBar = findViewById(R.id.progressBar);
        tvRecommendedUnits = findViewById(R.id.tvRecommendedUnits);
        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, BLOOD_GROUPS);
        etBloodGroup.setAdapter(adapter);

        // Add TextWatcher to etAge
        etAge.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ageStr = s.toString().trim();
                if (!ageStr.isEmpty()) {
                    int age = Integer.parseInt(ageStr);
                    int recommendedUnits = calculateRecommendedUnits(age);
                    tvRecommendedUnits.setText("Recommended Units: " + recommendedUnits);
                } else {
                    tvRecommendedUnits.setText("Recommended Units: -");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String ageStr = etAge.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String phoneNumber = etPhoneNumber.getText().toString().trim();
                String bloodGroup = etBloodGroup.getText().toString().trim();

                if (name.isEmpty() || ageStr.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() || bloodGroup.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    int age = Integer.parseInt(ageStr);
                    int recommendedUnits = calculateRecommendedUnits(age);

                    progressBar.setVisibility(View.VISIBLE);

                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            boolean isInserted = databaseHelper.insertData(name, ageStr, address, phoneNumber, bloodGroup, recommendedUnits);
                            if (isInserted) {
                                Toast.makeText(RegisterActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }

                            progressBar.setVisibility(View.GONE);
                        }
                    }, 3000); // Simulated delay of 3 seconds
                }
            }
        });
    }

    // Method to calculate recommended units of blood donation based on age
    private int calculateRecommendedUnits(int age) {
        if (age >= 18 && age <= 24) {
            return 1; // Example: age 18-24 can donate 1 unit
        } else if (age >= 25 && age <= 34) {
            return 2; // Example: age 25-34 can donate 2 units
        } else if (age >= 35 && age <= 44) {
            return 3; // Example: age 35-44 can donate 3 units
        } else if (age >= 45 && age <= 54) {
            return 2; // Example: age 45-54 can donate 2 units
        } else if (age >= 55) {
            return 1; // Example: age 55+ can donate 1 unit
        }
        return 0;
    }
}
