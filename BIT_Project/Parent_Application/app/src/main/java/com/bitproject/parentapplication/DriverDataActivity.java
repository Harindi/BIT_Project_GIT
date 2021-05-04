package com.bitproject.parentapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverDataActivity extends AppCompatActivity {

    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    TextView driverName, ContactNo, VehicleNo;
    String driverID;
    DatabaseReference setDriverName;
    String setContactNo;
    String setVehicleNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_data);

        driverName = (TextView) findViewById(R.id.txt_driver_name);
        ContactNo = (TextView) findViewById(R.id.txt_contact_number);
        VehicleNo = (TextView) findViewById(R.id.txt_vehicleno);

        driverID = databaseRef.child("Users").child("Driver").getParent().toString();
        setDriverName = databaseRef.child("Users").child("Driver").child(String.valueOf(driverID)).child("Name");

        driverName.setText("Driver Name: " + setDriverName);
    }
}