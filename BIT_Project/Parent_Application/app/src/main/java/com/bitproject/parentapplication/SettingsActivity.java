package com.bitproject.parentapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    private Button updateAccountSettings;
    private EditText username, childName, contactNumber, school;
    private CircleImageView userProfileImage;
    private String currentUserID;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private static final int galleryPic = 1;
    private StorageReference userProfileImageReference;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Parent");
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages").child("Parent");

        updateAccountSettings =  (Button) findViewById(R.id.update_settings_button);
        username = (EditText) findViewById(R.id.set_user_name);
        childName = (EditText) findViewById(R.id.set_child_name);
        contactNumber = (EditText) findViewById(R.id.set_phoneNum);
        school = (EditText) findViewById(R.id.set_school);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);


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

        if(requestCode == galleryPic && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
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
        String setChildName = childName.getText().toString();
        String setContactNo = contactNumber.getText().toString();
        String setSchool = school.getText().toString();

        if (TextUtils.isEmpty(setUsername)) {
            Toast.makeText(this, "Please enter your user name...",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setChildName)) {
            Toast.makeText(this, "Please enter your child name...",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setContactNo)) {
            Toast.makeText(this, "Please enter your contact number...",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setSchool)) {
            Toast.makeText(this, "Please enter your child's school...",Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("UserID", currentUserID);
            profileMap.put("ParentName", setUsername);
            profileMap.put("ChildName", setChildName);
            profileMap.put("ContactNumber", setContactNo);
            profileMap.put("School", setSchool);

            rootRef.child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(SettingsActivity.this, OptionActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
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
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("ParentName")) && (dataSnapshot.hasChild("ChildName")) && (dataSnapshot.hasChild("ContactNumber")) && (dataSnapshot.hasChild("School")) && (dataSnapshot.hasChild("Image"))) {
                    String retrieveParentName = dataSnapshot.child("ParentName").getValue().toString();
                    String retrieveChildName = dataSnapshot.child("ChildName").getValue().toString();
                    String retrieveContactNumber = dataSnapshot.child("ContactNumber").getValue().toString();
                    String retrieveSchool = dataSnapshot.child("School").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("Image").getValue().toString();

                    username.setText(retrieveParentName);
                    childName.setText(retrieveChildName);
                    contactNumber.setText(retrieveContactNumber);
                    school.setText(retrieveSchool);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("ParentName")) && (dataSnapshot.hasChild("ChildName")) && (dataSnapshot.hasChild("ContactNumber")) && (dataSnapshot.hasChild("School"))) {
                    String retrieveParentName = dataSnapshot.child("ParentName").getValue().toString();
                    String retrieveChildName = dataSnapshot.child("ChildName").getValue().toString();
                    String retrieveContactNumber = dataSnapshot.child("ContactNumber").getValue().toString();
                    String retrieveSchool = dataSnapshot.child("School").getValue().toString();

                    username.setText(retrieveParentName);
                    childName.setText(retrieveChildName);
                    contactNumber.setText(retrieveContactNumber);
                    school.setText(retrieveSchool);
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
