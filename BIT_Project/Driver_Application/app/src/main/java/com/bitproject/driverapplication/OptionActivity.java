package com.bitproject.driverapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OptionActivity extends AppCompatActivity {

    private Button mGoToMap,mMassage,mStudentData, mSettings;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver");

        mGoToMap = (Button) findViewById(R.id.btnVehicalLocation);
        mMassage = (Button) findViewById(R.id.btnMessage);
        mStudentData = (Button) findViewById(R.id.btnStudentData);
        mSettings = (Button) findViewById(R.id.btnSettings);

        mGoToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        mMassage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionActivity.this, MessageActivity.class);
                startActivity(intent);
            }
        });

        mStudentData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionActivity.this, StudentActivity.class);
                startActivity(intent);
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OptionActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        verifyExistance();
    }

    private void verifyExistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child("Name").exists())) {
                    Intent intent = new Intent(OptionActivity.this, SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
