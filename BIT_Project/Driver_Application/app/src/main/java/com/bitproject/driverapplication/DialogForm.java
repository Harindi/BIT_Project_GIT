package com.bitproject.driverapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DialogForm extends DialogFragment {

    String studentName, school, contactNumber;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public DialogForm(String studentName, String school, String contactNumber) {
        this.studentName = studentName;
        this.school = school;
        this.contactNumber = contactNumber;
    }

    TextView input_name;
    TextView input_school;
    TextView input_contactNumber;

    Button btn_register;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.input_form, container, false);

        input_name = view.findViewById(R.id.input_name);
        input_school = view.findViewById(R.id.input_school);
        input_contactNumber = view.findViewById(R.id.input_contactNumber);

        input_name.setText(studentName);
        input_school.setText(school);
        input_contactNumber.setText(contactNumber);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentName = input_name.getText().toString();
                String school = input_school.getText().toString();
                String contactNumber = input_contactNumber.getText().toString();

                if (TextUtils.isEmpty(studentName)) {
                    input((EditText) input_name, "StudentName");
                } else if (TextUtils.isEmpty(school)) {
                    input((EditText) input_school, "School");
                } else if (TextUtils.isEmpty(contactNumber)) {
                    input((EditText) input_contactNumber, "ContactNumber");
                } else {
                    databaseReference.child("StudentData").push().setValue(new AddStudent(studentName, school, contactNumber)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(view.getContext(), "", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        return view;
    }

    private void input(EditText txt,String s) {
        txt.setError(s+ " can't be empty");
        txt.requestFocus();
    }
}
