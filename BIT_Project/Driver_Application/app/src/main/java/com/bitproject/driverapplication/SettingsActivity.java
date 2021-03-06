package com.bitproject.driverapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int galleryPic = 1;
    private Button updateAccountSettings;
    private EditText username, contactNumber, vehicleNumber;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference userProfileImageReference;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver");
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages").child("Driver");

        updateAccountSettings =  (Button) findViewById(R.id.update_settings_button);
        username = (EditText) findViewById(R.id.set_user_name);
        contactNumber = (EditText) findViewById(R.id.set_phoneNum);
        vehicleNumber = (EditText) findViewById(R.id.set_vehicleNum);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar =  new ProgressDialog(this, R.style.MyAlertDialogStyle);


        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        retrieveUserInformation();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPic && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait!");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();
                final StorageReference filePath = userProfileImageReference.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                rootRef.child(currentUserID).child("Image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingsActivity.this, "Profile image saved successfully.", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }
    }

    private void updateSettings() {
        String setUsername = username.getText().toString();
        String setPhoneNum = contactNumber.getText().toString();
        String setVehicleNum = vehicleNumber.getText().toString();


        if (TextUtils.isEmpty(setUsername)) {
            Toast.makeText(this, "Please enter your user name...",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(setPhoneNum)) {
            Toast.makeText(this, "Please enter your contact number...",Toast.LENGTH_SHORT).show();
        }
        else if (setPhoneNum.length() != 10) {
            Toast.makeText(this, "Please enter valid contact number...",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(setVehicleNum)) {
            Toast.makeText(this, "Please enter your vehicle number...",Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("UserID", currentUserID);
            profileMap.put("Name", setUsername);
            profileMap.put("ContactNo", setPhoneNum);
            profileMap.put("VehicleNo", setVehicleNum);

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
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("ContactNo") && (dataSnapshot.hasChild("VehicleNo")) && (dataSnapshot.hasChild("Name") && (dataSnapshot.hasChild("Image"))))) {
                    String retrieveUserName = dataSnapshot.child("Name").getValue().toString();
                    String retrieveContactNum = dataSnapshot.child("ContactNo").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("Image").getValue().toString();
                    String retrieveVehicleNum = dataSnapshot.child("VehicleNo").getValue().toString();

                    username.setText(retrieveUserName);
                    contactNumber.setText(retrieveContactNum);
                    vehicleNumber.setText(retrieveVehicleNum);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("ContactNo") && (dataSnapshot.hasChild("VehicleNo")) && (dataSnapshot.hasChild("Name") ))) {
                    String retrieveUserName = dataSnapshot.child("Name").getValue().toString();
                    String retrieveContactNum = dataSnapshot.child("ContactNo").getValue().toString();
                    String retrieveVehicleNum = dataSnapshot.child("VehicleNo").getValue().toString();

                    username.setText(retrieveUserName);
                    contactNumber.setText(retrieveContactNum);
                    vehicleNumber.setText(retrieveVehicleNum);
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
