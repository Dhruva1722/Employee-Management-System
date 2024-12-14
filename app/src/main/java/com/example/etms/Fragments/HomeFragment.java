package com.example.etms.Fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.Manifest;

import com.example.etms.Database.AttendanceDatabaseHelper;
import com.example.etms.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    private static final int REQUEST_CAMERA_PERMISSION = 101;


    private ImageView imageView;
    private LinearLayout checkInButton, checkOutButton;

    private EditText employeeIdEditText;

    private Bitmap capturedImage;
    private FusedLocationProviderClient fusedLocationClient;
    private AttendanceDatabaseHelper dbHelper;


    private static final String PREFS_NAME = "ETMS_Prefs";
    private static final String KEY_CHECKED_IN = "checked_in";
    private static final String KEY_EMPLOYEE_ID = "employee_id";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imageView = view.findViewById(R.id.employeePhoto);
        employeeIdEditText = view.findViewById(R.id.employeeIdEditText);
        checkInButton = view.findViewById(R.id.checkInButton);
        checkOutButton = view.findViewById(R.id.checkOutButton);

        // Restore the check-in state and update UI:
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isCheckedIn = prefs.getBoolean(KEY_CHECKED_IN, false);
        String savedEmployeeId = prefs.getString(KEY_EMPLOYEE_ID, "");

        if (isCheckedIn) {
            disableButton(checkInButton);
            enableButton(checkOutButton);
            employeeIdEditText.setText(savedEmployeeId);
            employeeIdEditText.setEnabled(false); // Disable editing employee ID after check-in
        } else {
            disableButton(checkOutButton);
            employeeIdEditText.setEnabled(true);
        }

        dbHelper = new AttendanceDatabaseHelper(getContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        imageView.setOnClickListener(v -> checkCameraPermission());

        checkInButton.setOnClickListener(v -> saveCheckIn());

        checkOutButton.setOnClickListener(v -> saveCheckOut());

        checkPermissionsAndFetchLocation();

        return view;
    }


    // request permission for camera
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Permission already granted
            dispatchTakePictureIntent();
        }
    }

    // request for capture image
    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(requireContext(), "No camera app found.", Toast.LENGTH_SHORT).show();
        }
    }

    // save data for checkin
    private void saveCheckIn() {
        String employeeId = employeeIdEditText.getText().toString().trim();
        if (employeeId.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your Employee ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (capturedImage == null) {
            Toast.makeText(getContext(), "Please capture an image first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    String checkInTime = getCurrentTime();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    byte[] imageBytes = getImageBytes(capturedImage);

                    dbHelper.insertAttendance(employeeId, checkInTime, null, latitude, longitude, imageBytes);

                    // Save check-in state
                    SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean(KEY_CHECKED_IN, true)
                            .putString(KEY_EMPLOYEE_ID, employeeId)
                            .apply();

                    Toast.makeText(getContext(), "Check-in saved successfully!", Toast.LENGTH_SHORT).show();

                    disableButton(checkInButton);
                    enableButton(checkOutButton);
                    employeeIdEditText.setEnabled(false); // Prevent changing ID after check-in
                } else {
                    Toast.makeText(getContext(), "Unable to get location.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            requestLocationPermission();
        }
    }


    // save the data for checkout
    private void saveCheckOut() {
        String employeeId = employeeIdEditText.getText().toString().trim();
        if (employeeId.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your Employee ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    String checkOutTime = getCurrentTime();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    dbHelper.updateCheckOut(employeeId, checkOutTime);

                    // Clear check-in state
                    SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean(KEY_CHECKED_IN, false)
                            .remove(KEY_EMPLOYEE_ID)
                            .apply();

                    Toast.makeText(getContext(), "Check-out saved successfully!", Toast.LENGTH_SHORT).show();

                    disableButton(checkOutButton);
                    employeeIdEditText.setEnabled(true);
                } else {
                    Toast.makeText(getContext(), "Unable to get location.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            requestLocationPermission();
        }
    }

    // request for location
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
    }

    // get current time and date
    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // compress the image
    private byte[] getImageBytes(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    // enable the button
    private void enableButton(LinearLayout button) {
        button.setEnabled(true);
        button.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.image_background));
    }

    // disable the button
    private void disableButton(LinearLayout button) {
        button.setEnabled(false);
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
    }

    // image capture and convert into byte code
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null && data.getExtras() != null) {
            capturedImage = (Bitmap) data.getExtras().get("data");
            if (capturedImage != null) {
                imageView.setImageBitmap(capturedImage);
                enableButton(checkInButton);
            }
        } else {
            Toast.makeText(getContext(), "Failed to capture image.", Toast.LENGTH_SHORT).show();
        }
    }

    // request for permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Location permission granted.", Toast.LENGTH_SHORT).show();
                    fetchCurrentLocation(); // Start fetching location immediately
                } else {
                    Toast.makeText(getContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(getContext(), "Camera permission denied.", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    //check permission for location
    private void checkPermissionsAndFetchLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            requestLocationPermission();
        }
    }

    // fetch current location
    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();
//                Toast.makeText(getContext(), "Location fetched: Lat " + latitude + ", Lon " + longitude, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Unable to fetch location. Try again.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
