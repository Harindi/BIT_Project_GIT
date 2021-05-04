package com.bitproject.driverapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;
    RecycleAdapter recycleAdapter;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    ArrayList<AddStudent> listStudent;
    RecyclerView recycleview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        floatingActionButton = findViewById(R.id.fb_add);
        recycleview = findViewById(R.id.recycleview);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycleview.setLayoutManager(layoutManager);
        recycleview.setItemAnimator(new DefaultItemAnimator());

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listStudent.size() < 10 ) {
                    DialogForm dialogForm = new DialogForm("", "", "","", "", "Add");
                    dialogForm.show(getSupportFragmentManager(), "Form");
                } else {
                    Toast.makeText(StudentActivity.this, "No more seating facility", Toast.LENGTH_SHORT).show();
                }
            }
        });

        showData();
    }

    private void showData() {
        databaseReference.child("StudentData").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listStudent = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren()) {
                    AddStudent student = item.getValue(AddStudent.class);
                    student.setKey(item.getKey());
                    listStudent.add(student);
                }
                recycleAdapter = new RecycleAdapter(listStudent, StudentActivity.this);
                recycleview.setAdapter(recycleAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}