package com.example.val_info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.val_info.database.DatabaseHelper;
import com.example.val_info.database.InternalPreference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int GEOFENCE_RADIUS = 200;
    private double offLat = -6.189924029774242;
    private double offLng = 106.84577483721131;
    private double currLat, currLng;
    private AlertDialog.Builder mDial;
    private Calendar mCalendar;
    private InternalPreference mPref;
    private StorageReference mStorage;
    private DatabaseReference newsRef;
    private String[] times;
    private TextView timeDisplayCheckIn;
    private TextView timeDisplayCheckOut;
    private boolean hasCheckedIn = false;
    private boolean hasCheckedOut = false;
    private RadioButton ciWfh, ciWfo;

    private DatabaseReference mDatabase;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_home, container, false);
        FirebaseApp.initializeApp(parent.getContext());
        mStorage = FirebaseStorage.getInstance().getReference();
        mPref = new InternalPreference(getContext());
        newsRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mPref.ADMIN) {
            ((LinearLayout) parent.findViewById(R.id.layout_user)).setVisibility(View.GONE);
            ((Button) parent.findViewById(R.id.fh_admin_edit_news)).setOnClickListener(this);
            ((Button) parent.findViewById(R.id.fh_admin_edit_user)).setOnClickListener(this);
            ((Button) parent.findViewById(R.id.fh_admin_edit_prod)).setOnClickListener(this);
        } else {
            ((LinearLayout) parent.findViewById(R.id.layout_admin)).setVisibility(View.GONE);
            mDial = mPref.getAlertDialog(parent.getContext());
            ((Button) parent.findViewById(R.id.fp_checkInButton)).setOnClickListener(this);
            ((Button) parent.findViewById(R.id.fp_checkOutButton)).setOnClickListener(this);
        }

        TextView dateTextView = parent.findViewById(R.id.dateTextView);
        TextView greetingText = parent.findViewById(R.id.fp_text_greeting);
        ((TextView) parent.findViewById(R.id.fh_username_greeting)).setText(mPref.ADMIN ? "Admin" : mPref.USER);
        timeDisplayCheckIn = parent.findViewById(R.id.timeDisplayCheckIn);
        timeDisplayCheckOut = parent.findViewById(R.id.timeDisplayCheckOut);

        mCalendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE-dd-MMMM-yyyy-HH-mm", Locale.ENGLISH);
        times = dateFormat.format(mCalendar.getTime()).split("-");

        dateTextView.setText(times[0] + ", " + times[1] + " " + times[2] + " " + times[3]);
        greetingText.setText("Hi, " + getGreeting(Integer.parseInt(times[4])));

        // Restore check-in and check-out times
        String savedCheckInTime = mPref.getCheckInTime();
        String savedCheckOutTime = mPref.getCheckOutTime();

        if (!savedCheckInTime.isEmpty()) {
            timeDisplayCheckIn.setText(savedCheckInTime);
            hasCheckedIn = true;
        }

        if (!savedCheckOutTime.isEmpty()) {
            timeDisplayCheckOut.setText(savedCheckOutTime);
            hasCheckedOut = true;
        }

        // Check if it's a new day
        checkForNewDay();

        return parent;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fp_checkInButton) {
            handleCheckIn();
        } else if (view.getId() == R.id.fp_checkOutButton) {
            handleCheckOut();
        } else if (view.getId() == R.id.fh_admin_edit_news) {
            handleEditNews();
        } else if (view.getId() == R.id.fh_admin_edit_user) {
            handleEditUser();
        } else if (view.getId() == R.id.fh_admin_edit_prod) {
            handleProductivity();
        }
    }

    private void handleEditUser() {
        Dialog editUserDialog = new Dialog(getContext());
        editUserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editUserDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editUserDialog.setContentView(R.layout.admin_edit_data_users);
        editUserDialog.setCancelable(false);
        editUserDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        editUserDialog.getWindow().setDimAmount(0.75f);

        EditText etUsername = editUserDialog.findViewById(R.id.et_username);
        EditText etNip = editUserDialog.findViewById(R.id.et_nip);
        EditText etStatus = editUserDialog.findViewById(R.id.et_status);
        EditText etPhone = editUserDialog.findViewById(R.id.et_phone);
        EditText etEmail = editUserDialog.findViewById(R.id.et_email);
        EditText etDivision = editUserDialog.findViewById(R.id.et_division);
        Button btnSave = editUserDialog.findViewById(R.id.btn_save);
        /*Button btnClose = editUserDialog.findViewById(R.id.btn_close);*/

        btnSave.setOnClickListener(view -> {
            String username = etUsername.getText().toString();
            String nip = etNip.getText().toString();
            String status = etStatus.getText().toString();
            String phone = etPhone.getText().toString();
            String email = etEmail.getText().toString();
            String division = etDivision.getText().toString();

            if (username.isEmpty() || nip.isEmpty() || status.isEmpty() || phone.isEmpty() || email.isEmpty() || division.isEmpty()) {
                Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save user data to Firebase or other storage
            saveUserData(username, nip, status, phone, email, division);
            editUserDialog.dismiss();
        });

        btnSave.setOnClickListener(view -> editUserDialog.dismiss());

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(editUserDialog.getWindow().getAttributes());
        lp.width = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.9f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        editUserDialog.show();
        editUserDialog.getWindow().setAttributes(lp);
    }

    private void saveUserData(String username, String nip, String status, String phone, String email, String division) {
        // Implement saving user data logic, e.g., saving to Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(nip);
        userRef.child("username").setValue(username);
        userRef.child("status").setValue(status);
        userRef.child("phone").setValue(phone);
        userRef.child("email").setValue(email);
        userRef.child("division").setValue(division).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "User data updated successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to update user data", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleEditNews() {
        Dialog editNewsDialog = new Dialog(getContext());
        editNewsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editNewsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editNewsDialog.setContentView(R.layout.admin_edit_news);
        editNewsDialog.setCancelable(false);
        editNewsDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        editNewsDialog.getWindow().setDimAmount(0.75f);

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }

        List<String> years = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            years.add(String.valueOf(i + 2023));
        }

        ArrayAdapter<String> addays = new ArrayAdapter<>(getContext(), R.layout.spinner_item, days);
        addays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> admonth = new ArrayAdapter<>(getContext(), R.layout.spinner_item, months);
        admonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> adyear = new ArrayAdapter<>(getContext(), R.layout.spinner_item, years);
        adyear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spDays = editNewsDialog.findViewById(R.id.aen_pick_date);
        Spinner spMonths = editNewsDialog.findViewById(R.id.aen_pick_month);
        Spinner spYears = editNewsDialog.findViewById(R.id.aen_pick_year);
        spDays.setAdapter(addays);
        spMonths.setAdapter(admonth);
        spYears.setAdapter(adyear);

        EditText etDesc = editNewsDialog.findViewById(R.id.ean_description);
        Button btnSubmit = editNewsDialog.findViewById(R.id.ean_btn_submit);
        Button btnClose = editNewsDialog.findViewById(R.id.ean_btn_close);

        btnSubmit.setOnClickListener(view -> {
            String date = spDays.getSelectedItem().toString() + "-" + spMonths.getSelectedItem().toString() + "-" + spYears.getSelectedItem().toString();
            String description = etDesc.getText().toString();

            if (description.isEmpty()) {
                Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            NewsAdmin news = new NewsAdmin(date, description);
            newsRef.push().setValue(news).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "News added successfully", Toast.LENGTH_SHORT).show();
                editNewsDialog.dismiss();
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to add news", Toast.LENGTH_SHORT).show();
            });
        });

        btnClose.setOnClickListener(view -> editNewsDialog.dismiss());

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(editNewsDialog.getWindow().getAttributes());
        lp.width = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.9f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        editNewsDialog.show();
        editNewsDialog.getWindow().setAttributes(lp);
    }

    private void handleProductivity() {
        Log.d("handleProductivity", "Method called");

        Dialog productivityDialog = new Dialog(requireActivity());
        productivityDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        productivityDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        productivityDialog.setContentView(R.layout.admin_edit_prod);
        productivityDialog.setCancelable(false);
        productivityDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        productivityDialog.getWindow().setDimAmount(0.75f);

        EditText etProductivity = productivityDialog.findViewById(R.id.et_productivity);
        Button btnSubmit = productivityDialog.findViewById(R.id.btn_submit_productivity);
        Button btnClose = productivityDialog.findViewById(R.id.btn_close_productivity);

        if (etProductivity == null) {
            Log.e("handleProductivity", "EditText etProductivity is null");
        } else {
            Log.d("handleProductivity", "EditText etProductivity found");
        }

        if (btnSubmit == null) {
            Log.e("handleProductivity", "Button btnSubmit is null");
        } else {
            Log.d("handleProductivity", "Button btnSubmit found");
        }

        if (btnClose == null) {
            Log.e("handleProductivity", "Button btnClose is null");
        } else {
            Log.d("handleProductivity", "Button btnClose found");
        }

        btnSubmit.setOnClickListener(view -> {
            String productivityText = etProductivity.getText().toString();

            if (productivityText.isEmpty()) {
                Toast.makeText(requireContext(), "Productivity text cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Assuming productivityText is in a specific format (e.g., "userId,month,year,value")
            String[] parts = productivityText.split(",");
            if (parts.length != 4) {
                Toast.makeText(requireContext(), "Productivity text format is incorrect", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = Integer.parseInt(parts[0].trim());
            int month = Integer.parseInt(parts[1].trim());
            int year = Integer.parseInt(parts[2].trim());
            int value = Integer.parseInt(parts[3].trim());

            saveProductivityDetails(userId, month, year, value);
            productivityDialog.dismiss();
        });

        btnClose.setOnClickListener(view -> productivityDialog.dismiss());

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(productivityDialog.getWindow().getAttributes());
        lp.width = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.9f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        productivityDialog.getWindow().setAttributes(lp);

        Log.d("handleProductivity", "Showing dialog");
        productivityDialog.show();
    }

    private void saveProductivityDetails(int userId, int month, int year, int value) {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        boolean isSaved = databaseHelper.saveProductivityDetails(userId, month, year, value);

        if (isSaved) {
            Log.d("HomeFragment", "Productivity details saved successfully");
        } else {
            Log.e("HomeFragment", "Failed to save productivity details");
        }
    }


    private void handleCheckIn() {
        if (hasCheckedIn) {
            Toast.makeText(getContext(), "You have already checked in today", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog checkInDialog = new Dialog(getContext());
        checkInDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        checkInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkInDialog.setContentView(R.layout.check_in);
        checkInDialog.setCancelable(false);
        checkInDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        checkInDialog.getWindow().setDimAmount(0.75f);

        Button ciClose = checkInDialog.findViewById(R.id.ci_btn_close);
        Button ciSubmit = checkInDialog.findViewById(R.id.ci_btn_submit);
        TextView ciName = checkInDialog.findViewById(R.id.ci_tv_fullname);
        TextView ciDate = checkInDialog.findViewById(R.id.ci_tv_timedate);
        TextView ciDate2 = checkInDialog.findViewById(R.id.ci_tv_timedate2);
        TextView ciDivisi = checkInDialog.findViewById(R.id.ci_tv_divisi);
        ciWfh = checkInDialog.findViewById(R.id.ci_rb_home);
        ciWfo = checkInDialog.findViewById(R.id.ci_rb_office);

        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setOnCameraMoveListener(null);
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                    mMap.getUiSettings().setZoomControlsEnabled(false);
                    mMap.getUiSettings().setZoomGesturesEnabled(false);
                    mMap.setMyLocationEnabled(true);
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                currLat = location.getLatitude();
                                currLng = location.getLongitude();
                            }
                        }
                    });
                }
            });
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm 'WIB'", Locale.ENGLISH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH);
        String checkinTime = timeFormat.format(mCalendar.getTime());
        String checkinDate = dateFormat.format(mCalendar.getTime());
        ciDate.setText("Time: " + checkinTime);
        ciDate2.setText("Date: " + checkinDate);
        ciClose.setOnClickListener(v -> {
            checkInDialog.dismiss();
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
        });
        ciName.setText("Name: " + mPref.USER);
        ciDivisi.setText("Divisi: " + (mPref.DIVISI == 1 ? "DBS" : "Maybank"));

        ciSubmit.setOnClickListener(v -> {
            if (ciWfo != null && !ciWfo.isChecked()) {
                float[] result = new float[1];
                Location.distanceBetween(offLat, offLng, currLat, currLng, result);
                if (result[0] > GEOFENCE_RADIUS) {
                    Toast.makeText(getContext(), "You are outside the 200 meters radius for check-in", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            saveCheckInAndOutDetails("checkin", checkinTime, ciWfh != null && ciWfh.isChecked() ? "Work From Home" : "Work From Office");
            mPref.saveCheckInTime(checkinTime);
            timeDisplayCheckIn.setText(checkinTime);
            hasCheckedIn = true;
            checkInDialog.dismiss();
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(checkInDialog.getWindow().getAttributes());
        lp.width = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.9f);
        lp.height = (int) (getActivity().getResources().getDisplayMetrics().heightPixels * 0.75f);
        checkInDialog.show();
        checkInDialog.getWindow().setAttributes(lp);
    }

    private void handleCheckOut() {
        if (hasCheckedOut) {
            Toast.makeText(getContext(), "You have already checked out today", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                currLat = location.getLatitude();
                currLng = location.getLongitude();
                if (ciWfo != null && !ciWfo.isChecked()) {
                    float[] result = new float[1];
                    Location.distanceBetween(offLat, offLng, currLat, currLng, result);
                    if (result[0] > GEOFENCE_RADIUS) {
                        Toast.makeText(getContext(), "You are outside the 200 meters radius for check-out", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                mDial.setTitle("Check out")
                        .setMessage("Are you sure you want to check out?")
                        .setCancelable(false)
                        .setPositiveButton("Checkout", (dialogInterface, i) -> {
                            String checkoutTime = new SimpleDateFormat("HH:mm 'WIB'", Locale.ENGLISH).format(Calendar.getInstance().getTime());
                            saveCheckInAndOutDetails("checkout", checkoutTime, null);
                            mPref.saveCheckOutTime(checkoutTime);
                            timeDisplayCheckOut.setText(checkoutTime);
                            Toast.makeText(getContext(), "Checked out successfully", Toast.LENGTH_SHORT).show();
                            hasCheckedOut = true;
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = mDial.create();
                alert.show();
            }
        });
    }

    private void saveCheckInAndOutDetails(String action, String time, String location) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("HomeFragment", "User not authenticated");
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = mDatabase.child("check-in-out").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", mPref.USER);

                if (action.equals("checkin")) {
                    userData.put("location", location);
                    userData.put("checkin_time", time);
                } else if (action.equals("checkout")) {
                    userData.put("checkout_time", time);
                }

                userRef.updateChildren(userData)
                        .addOnSuccessListener(aVoid -> Log.d("HomeFragment", "Data uploaded successfully"))
                        .addOnFailureListener(e -> Log.e("HomeFragment", "Failed to upload data", e));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("HomeFragment", "Failed to read data", error.toException());
            }
        });
    }


    private String getGreeting(int hour) {
        if (hour > 18) return "Good Evening";
        else if (hour > 11) return "Good Afternoon";
        else return "Good Morning";
    }

    private void checkForNewDay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        String currentDate = dateFormat.format(mCalendar.getTime());
        String lastCheckInDate = mPref.getCheckInTime().split(" ")[0];

        if (!currentDate.equals(lastCheckInDate)) {
            mPref.resetCheckInAndOutTime();
            timeDisplayCheckIn.setText("--:--");
            timeDisplayCheckOut.setText("--:--");
            hasCheckedIn = false;
            hasCheckedOut = false;
        }
    }

}
