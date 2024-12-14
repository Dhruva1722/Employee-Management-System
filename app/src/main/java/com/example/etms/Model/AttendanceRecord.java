package com.example.etms.Model;

import android.graphics.Bitmap;

public class AttendanceRecord {

    private int id;
    private String employeeId;
    private String checkInTime;
    private String checkOutTime;
    private double latitude;
    private double longitude;
    Bitmap image;

    public AttendanceRecord(int id, String employeeId, String checkInTime, String checkOutTime, double latitude, double longitude, Bitmap image) {
        this.id = id;
        this.employeeId = employeeId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
    }

    // Getters
    public int getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getCheckInTime() { return checkInTime; }
    public String getCheckOutTime() { return checkOutTime; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Bitmap getImage() { return image; }
}



