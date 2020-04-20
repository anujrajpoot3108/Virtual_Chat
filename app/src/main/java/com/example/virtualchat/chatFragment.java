package com.example.virtualchat;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class chatFragment extends Fragment {

    private View privatechatview;
    private RecyclerView mychatlist;
    private DatabaseReference chatlistref,userref;
    private FirebaseAuth mauth;
    private String currentuserid;


    public chatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privatechatview=inflater.inflate(R.layout.fragment_chat, container, false);

        mychatlist=(RecyclerView) privatechatview.findViewById(R.id.private_chat_list);
        mychatlist.setLayoutManager(new LinearLayoutManager(getContext()));

        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        userref=FirebaseDatabase.getInstance().getReference().child("Users");
        chatlistref= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);




        return privatechatview;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatlistref,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,chatviewholder> adapter=new FirebaseRecyclerAdapter<Contacts, chatviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatviewholder chatviewholder, int i, @NonNull Contacts contacts) {

                final String userids=getRef(i).getKey();
                final String[] userimage = {"default_image"};

                userref.child(userids).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                      if(dataSnapshot.exists()){

                          if ((dataSnapshot.hasChild("image"))) {

                                userimage[0] = dataSnapshot.child("image").getValue().toString();
                              Picasso.get().load(userimage[0]).placeholder(R.drawable.profile_image).into(chatviewholder.userprofileimage);
                          }

                          final String username = dataSnapshot.child("name").getValue().toString();

                          chatviewholder.userprofilename.setText(username);

                          if(dataSnapshot.child("userstate").hasChild("state")){
                              String state=dataSnapshot.child("userstate").child("state").getValue().toString();
                              String date=dataSnapshot.child("userstate").child("date").getValue().toString();
                              String time=dataSnapshot.child("userstate").child("time").getValue().toString();

                              if(state.equals("online")){
                                  chatviewholder.userprofilestatus.setText("online ");
                              }
                              else if(state.equals("Offline")){
                                  chatviewholder.userprofilestatus.setText(date+"  "+time);
                              }

                          }
                          else{
                              chatviewholder.userprofilestatus.setText("Offline ");

                          }


                          chatviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {

                                  Intent chatintent= new Intent(getContext(),ChatActivity.class);
                                  chatintent.putExtra("visit_user_ids",userids);
                                  chatintent.putExtra("visit_user_name",username);
                                  chatintent.putExtra("visit_user_image", userimage[0]);
                                  startActivity(chatintent);

                              }
                          });


                      }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public chatviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                return new chatviewholder(view);
            }
        };

        mychatlist.setAdapter(adapter);
        adapter.startListening();

    }



    public  static  class  chatviewholder extends RecyclerView.ViewHolder{

        TextView userprofilename,userprofilestatus;

        CircleImageView userprofileimage;

        public chatviewholder(@NonNull View itemView) {
            super(itemView);

            userprofileimage=itemView.findViewById(R.id.users_profile_images);
            userprofilestatus=itemView.findViewById(R.id.users_status);
            userprofilename=itemView.findViewById(R.id.users_name);
        }
    }

}



