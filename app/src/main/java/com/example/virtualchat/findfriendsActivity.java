
package com.example.virtualchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import okio.Options;

public class findfriendsActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private RecyclerView findfriendsrecycler;
    private DatabaseReference userref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findfriends);

        userref = FirebaseDatabase.getInstance().getReference().child("Users");
        findfriendsrecycler = (RecyclerView) findViewById(R.id.find_friends_recycler_view);
        findfriendsrecycler.setLayoutManager(new LinearLayoutManager(this));
        mtoolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userref, Contacts.class).build();


        FirebaseRecyclerAdapter<Contacts, findfriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, findfriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull findfriendsViewHolder findfriendsviewholder, final int positions, @NonNull Contacts contacts) {

                findfriendsviewholder.username.setText(contacts.getName());
                findfriendsviewholder.userstatus.setText(contacts.getStatus());
                Picasso.get().load(contacts.getImage()).placeholder(R.drawable.profile_image).into(findfriendsviewholder.userprofileimage);

                findfriendsviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String visit_user_id=getRef(positions).getKey();
                        Intent profileintent=new Intent(findfriendsActivity.this,ProfileActivity.class);
                        profileintent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileintent);
                    }
                });
            }

            @NonNull
            @Override
            public findfriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                findfriendsViewHolder viewHolder = new findfriendsViewHolder(view);
                return viewHolder;
            }

        };
        findfriendsrecycler.setAdapter(adapter);
        adapter.startListening();

    }


    public static class findfriendsViewHolder extends RecyclerView.ViewHolder {

        TextView username, userstatus;
        CircleImageView userprofileimage;


        public findfriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.users_name);
            userstatus = itemView.findViewById(R.id.users_status);
            userprofileimage = itemView.findViewById(R.id.users_profile_images);

        }
    }
}
