package com.bitproject.parentapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText username;
    private CircleImageView userProfileImage;
    private String currentUserID;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Parent");

        updateAccountSettings =  (Button) findViewById(R.id.update_settings_button);
        username = (EditText) findViewById(R.id.set_user_name);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);


        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        retrieveUserInformation();
    }

    private void updateSettings() {
        String setUsername = username.getText().toString();

        if (TextUtils.isEmpty(setUsername)) {
            Toast.makeText(this, "Please enter your user name...",Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("UserID", currentUserID);
            profileMap.put("Name", setUsername);

            rootRef.child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(SettingsActivity.this, OptionActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                                Toast.makeText(SettingsActivity.this, "Profile Updates Successfully!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error! " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void retrieveUserInformation() {
        rootRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Name") && (dataSnapshot.hasChild("ProfileImage")))) {
                    String retrieveUserName = dataSnapshot.child("Name").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("ProfileImage").getValue().toString();

                    username.setText(retrieveUserName);
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Name"))) {
                    String retrieveUserName = dataSnapshot.child("Name").getValue().toString();

                    username.setText(retrieveUserName);
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Please update your profile information...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
