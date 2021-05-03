package com.bitproject.driverapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.MyViewHolder> {

    private List<AddStudent> mList;
    private Activity activity;
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

    public RecycleAdapter(List<AddStudent> mList, Activity activity) {
        this.mList = mList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecycleAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.layout_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleAdapter.MyViewHolder holder, int position) {
        final AddStudent stu = mList.get(position);
        holder.ID.setText("ID: " + stu.getID());
        holder.textViewName.setText("Name: " + stu.getStudentName());
        holder.textViewSchool.setText("School: " + stu.getSchool());
        holder.textViewContactNumber.setText("Contact Number: " + stu.getContactNumber());

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseRef.child("StudentData").child(stu.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(activity, "Successfully deleted!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(activity, "Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setMessage("Are you sure want to delete " + stu.getStudentName() + "'s details?");
                builder.show();
            }
        });

        holder.card_View.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                FragmentManager manager = ((AppCompatActivity)activity).getSupportFragmentManager();
                DialogForm dialogForm = new DialogForm(stu.getID(),stu.getStudentName(),
                        stu.getSchool(),
                        stu.getContactNumber(),
                        stu.getKey(),
                        "Edit");

                dialogForm.show(manager, "form");
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView ID;
        TextView textViewName;
        TextView textViewSchool;
        TextView textViewContactNumber;

        CardView card_View;
        ImageView imgDelete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ID = itemView.findViewById(R.id.txt_id);
            textViewName = itemView.findViewById(R.id.txt_name);
            textViewSchool = itemView.findViewById(R.id.txt_school);
            textViewContactNumber = itemView.findViewById(R.id.txt_contactno);

            card_View = itemView.findViewById(R.id.cardView);
            imgDelete= itemView.findViewById(R.id.delete);
        }
    }
}
