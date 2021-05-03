package com.bitproject.driverapplication;

import android.app.Dialog;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DialogForm extends DialogFragment {

    String id, studentName, school, contactNumber, key, select;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public DialogForm(String id, String studentName, String school, String contactNumber, String key, String select) {
        this.id = id;
        this.studentName = studentName;
        this.school = school;
        this.contactNumber = contactNumber;
        this.key = key;
        this.select = select;
    }

    TextView input_name;
    TextView input_school;
    TextView input_contactNumber;
    TextView ID;

    Button btn_register;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.input_form, container, false);

        input_name = view.findViewById(R.id.input_name);
        input_school = view.findViewById(R.id.input_school);
        input_contactNumber = view.findViewById(R.id.input_contactNumber);
        btn_register = view.findViewById(R.id.btn_studentRegister);
        ID = view.findViewById(R.id.input_id);

        ID.setText(id);
        input_name.setText(studentName);
        input_school.setText(school);
        input_contactNumber.setText(contactNumber);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = ID.getText().toString();
                String studentName = input_name.getText().toString();
                String school = input_school.getText().toString();
                String contactNumber = input_contactNumber.getText().toString();

                if (TextUtils.isEmpty(id)) {
                    input((EditText) ID, "ID");
                }else if (TextUtils.isEmpty(studentName)) {
                    input((EditText) input_name, "Student Name");
                } else if (TextUtils.isEmpty(school)) {
                    input((EditText) input_school, "School");
                } else if (TextUtils.isEmpty(contactNumber)) {
                    input((EditText) input_contactNumber, "Contact Number");
                } else {
                    if (select.equals("Add")) {
                        if(databaseReference.child("StudentData").child("id").equals(id)){
                            Toast.makeText(view.getContext(), "ID no exists", Toast.LENGTH_SHORT).show();
                        } else {
                            databaseReference.child("StudentData").push().setValue(new AddStudent(id, studentName, school, contactNumber)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(view.getContext(), "Successfully added new student", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(view.getContext(), "Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        dismiss();
                    } else if (select.equals("Edit")) {
                        databaseReference.child("StudentData").child(key).setValue(new AddStudent(id,studentName, school, contactNumber)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(view.getContext(), "Data updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(view.getContext(), "Data failed to change", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dismiss();
                    }
                }
            }
        });

        return view;
    }

    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();

        if (dialog != null)
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void input(EditText txt,String s) {
        txt.setError(s+ " can't be empty");
        txt.requestFocus();
    }
}
