package com.example.etms.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.etms.Model.AttendanceRecord;
import com.example.etms.R;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private final List<AttendanceRecord> records;

    public AttendanceAdapter(List<AttendanceRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new AttendanceViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = records.get(position);

        holder.employeeId.setText("ID: " + record.getEmployeeId());
        holder.checkInTime.setText("Check-In: " + record.getCheckInTime());
        holder.checkOutTime.setText("Check-Out: " + record.getCheckOutTime());
        holder.latitude.setText("Lat: " + record.getLatitude());
        holder.longitude.setText("Lon: " + record.getLongitude());
        holder.imageView.setImageBitmap(record.getImage());
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView employeeId, checkInTime, checkOutTime, latitude, longitude;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.employeeImage);
            employeeId = itemView.findViewById(R.id.employeeId);
            checkInTime = itemView.findViewById(R.id.checkInTime);
            checkOutTime = itemView.findViewById(R.id.checkOutTime);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
        }
    }

}
