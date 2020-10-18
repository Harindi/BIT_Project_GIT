package com.bitproject.driverapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindContactsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView findContactsRecycleList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_contacts);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Parent");

        findContactsRecycleList = (RecyclerView) findViewById(R.id.find_contacts_recycle_list);
        findContactsRecycleList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = (Toolbar) findViewById(R.id.find_contacts_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Contacts");
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, findContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, findContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull findContactsViewHolder holder, int position, @NonNull Contacts model) {
                        holder.userName.setText(model.getName());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);
                    }

                    @NonNull
                    @Override
                    public findContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        findContactsViewHolder viewHolder = new findContactsViewHolder(view);
                        return viewHolder;
                    }
                };
        findContactsRecycleList.setAdapter(adapter);

        adapter.startListening();
    }

    public static class findContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        CircleImageView profileImage;

        public findContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}