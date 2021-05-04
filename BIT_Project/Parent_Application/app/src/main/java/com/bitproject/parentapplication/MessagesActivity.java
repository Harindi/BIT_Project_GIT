package com.bitproject.parentapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MessagesActivity extends AppCompatActivity {

    private Toolbar mMassageToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabsAccessorAdaptor;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef,rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Parent");
        rootRef = FirebaseDatabase.getInstance().getReference().child("Messages");

        mMassageToolbar = (Toolbar) findViewById(R.id.message_page_toolbar);
        setSupportActionBar(mMassageToolbar);
        getSupportActionBar().setTitle("Messages");

        mViewPager = (ViewPager) findViewById(R.id.message_tab_pager);
        mTabsAccessorAdaptor = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccessorAdaptor);

        mTabLayout = (TabLayout) findViewById(R.id.message_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.option_menu, menu );

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.message_create_group_option) {
            requestNewGroup();
        }
        if (item.getItemId() == R.id.message_find_people_option) {
            sendToFindContactsActivity();
        }

        return true;
    }

    private void sendToFindContactsActivity() {
        Intent intent = new Intent(MessagesActivity.this, FindContactsActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MessagesActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MessagesActivity.this);
        groupNameField.setTextColor(getColor(R.color.colorText));
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MessagesActivity.this, "Please enter a group name!", Toast.LENGTH_SHORT).show();
                }
                else {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String groupName) {
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MessagesActivity.this, groupName + " group is created successfully!", Toast.LENGTH_SHORT).show();
                        }
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
                if (!(dataSnapshot.child("ParentName").exists())) {
                    Intent intent = new Intent(MessagesActivity.this, SettingsActivity.class);
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
