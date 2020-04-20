package com.example.virtualchat;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class contactsFragment extends Fragment {

    private View contactsview;
    private RecyclerView mycontactslist;
    private DatabaseReference contactsref,userref;
    private FirebaseAuth mauth;
    private String currentuserid;


    public contactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsview=inflater.inflate(R.layout.fragment_contacts, container, false);


        mycontactslist=(RecyclerView) contactsview.findViewById(R.id.contacts_list);
        mycontactslist.setLayoutManager(new LinearLayoutManager(getContext()));


        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        contactsref= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);
        userref=FirebaseDatabase.getInstance().getReference().child("Users");



        return contactsview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsref,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,contactsviewholder> adapter=new FirebaseRecyclerAdapter<Contacts, contactsviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final contactsviewholder contactsviewholder, int i, @NonNull Contacts contacts) {

                String usersid =getRef(i).getKey();
                userref.child(usersid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            if(dataSnapshot.child("userstate").hasChild("state")){
                                String state=dataSnapshot.child("userstate").child("state").getValue().toString();
                                String date=dataSnapshot.child("userstate").child("date").getValue().toString();
                                String time=dataSnapshot.child("userstate").child("time").getValue().toString();

                                if(state.equals("online")){
                                    contactsviewholder.onlineicon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("Offline")){
                                    contactsviewholder.onlineicon.setVisibility(View.INVISIBLE);
                                }

                            }
                            else{
                                contactsviewholder.onlineicon.setVisibility(View.INVISIBLE);

                            }

                            if((dataSnapshot.hasChild("image"))){
                                String usersname= dataSnapshot.child("name").getValue().toString();
                                String usersstatus= dataSnapshot.child("status").getValue().toString();
                                String usersimage= dataSnapshot.child("image").getValue().toString();

                                contactsviewholder.userprofilename.setText(usersname);
                                contactsviewholder.userprofilestatus.setText(usersstatus);
                                Picasso.get().load(usersimage).placeholder(R.drawable.profile_image).into(contactsviewholder.userprofileimage);
                            }
                            else{
                                String usersname= dataSnapshot.child("name").getValue().toString();
                                String usersstatus= dataSnapshot.child("status").getValue().toString();

                                contactsviewholder.userprofilename.setText(usersname);
                                contactsviewholder.userprofilestatus.setText(usersstatus);

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public contactsviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                contactsviewholder viewholder=new contactsviewholder(view);
                return viewholder;
            }
        };
        mycontactslist.setAdapter(adapter);
        adapter.startListening();
    }



    public  static  class  contactsviewholder extends RecyclerView.ViewHolder{

            TextView userprofilename,userprofilestatus;

            CircleImageView userprofileimage;
            ImageView onlineicon;

        public contactsviewholder(@NonNull View itemView) {
            super(itemView);

            userprofileimage=itemView.findViewById(R.id.users_profile_images);
            userprofilename=itemView.findViewById(R.id.users_name);
            userprofilestatus=itemView.findViewById(R.id.users_status);
            onlineicon=(ImageView)itemView.findViewById(R.id.users_online_icon);
        }
    }
}
