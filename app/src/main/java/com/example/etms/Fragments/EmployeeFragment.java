package com.example.etms.Fragments;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.etms.Adapter.AttendanceAdapter;
import com.example.etms.Database.AttendanceDatabaseHelper;
import com.example.etms.Model.AttendanceRecord;
import com.example.etms.R;

import java.util.ArrayList;
import java.util.List;


public class EmployeeFragment extends Fragment {

    private RecyclerView recyclerView;
    private AttendanceDatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_employee, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new AttendanceDatabaseHelper(getContext());
        loadAttendanceData();

        return view;
    }

    // load attendance data in recyclerview
    private void loadAttendanceData() {
        Cursor cursor = dbHelper.getAllAttendanceData();
        List<AttendanceRecord> records = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String employeeId = cursor.getString(cursor.getColumnIndexOrThrow("employeeId"));
                String checkInTime = cursor.getString(cursor.getColumnIndexOrThrow("checkInTime"));
                String checkOutTime = cursor.getString(cursor.getColumnIndexOrThrow("checkOutTime"));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("image"));

                Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                records.add(new AttendanceRecord(id, employeeId, checkInTime, checkOutTime, latitude, longitude, image));
            } while (cursor.moveToNext());

            cursor.close();
        }

        AttendanceAdapter adapter = new AttendanceAdapter(records);
        recyclerView.setAdapter(adapter);
    }
}