package com.example.virtualchat;


import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class requestFragment extends Fragment {

    private RecyclerView myrequestlist;

    private View requestview;
    private DatabaseReference chatrequestref,userref,contactsref;
    private FirebaseAuth mauth;
    private String currentuserid;


    public requestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        requestview= inflater.inflate(R.layout.fragment_request, container, false);
        myrequestlist=requestview.findViewById(R.id.request_list);

       myrequestlist.setLayoutManager(new LinearLayoutManager(getContext()));
       mauth=FirebaseAuth.getInstance();
       currentuserid=mauth.getCurrentUser().getUid();
       chatrequestref= FirebaseDatabase.getInstance().getReference().child("chat requests");
       userref=FirebaseDatabase.getInstance().getReference().child("Users");
       contactsref=FirebaseDatabase.getInstance().getReference().child("Contacts");

        return requestview;


    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chatrequestref.child(currentuserid), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,requestviewholder> adapter =new FirebaseRecyclerAdapter<Contacts, requestviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final requestviewholder requestviewholder, int i, @NonNull Contacts contacts) {

                requestviewholder.itemView.findViewById(R.id.request_decline_button).setVisibility(View.VISIBLE);
                requestviewholder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);

                final String listuserid=getRef(i).getKey();
                DatabaseReference recievedrequestsref=getRef(i).child("request_type").getRef();
               recievedrequestsref.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                       if(dataSnapshot.exists()){

                           String value=dataSnapshot.getValue().toString();
                           if(value.equals("recieved")){

                               userref.child(listuserid).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       if((dataSnapshot.hasChild("image"))){

                                          final String userimage= dataSnapshot.child("image").getValue().toString();
                                           Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(requestviewholder.userprofileimage);
                                       }
                                       final String username= dataSnapshot.child("name").getValue().toString();
                                       final String userstatus= dataSnapshot.child("status").getValue().toString();

                                       requestviewholder.userprofilename.setText(username);
                                       requestviewholder.userprofilestatus.setText("sent you a friend request");

                                       requestviewholder.itemView.findViewById(R.id.request_accept_button).setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {

                                               contactsref.child(currentuserid).child(listuserid).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                               {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {
                                                       if(task.isSuccessful()){

                                                           contactsref.child(listuserid).child(currentuserid).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                           {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   if(task.isSuccessful()){

                                                                       chatrequestref.child(currentuserid).child(listuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task) {

                                                                               if(task.isSuccessful()){

                                                                                   chatrequestref.child(listuserid).child(currentuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                       @Override
                                                                                       public void onComplete(@NonNull Task<Void> task) {

                                                                                           if(task.isSuccessful()){

                                                                                               Toast.makeText(getContext(),"new friend added",Toast.LENGTH_SHORT).show();
                                                                                           }

                                                                                       }
                                                                                   });
                                                                               }

                                                                           }
                                                                       });


                                                                   }

                                                               }
                                                           });
                                                       }

                                                   }
                                               });

                                           }
                                       });

                                        requestviewholder.itemView.findViewById(R.id.request_decline_button).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                chatrequestref.child(currentuserid).child(listuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful()){

                                                            chatrequestref.child(listuserid).child(currentuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()){

                                                                        Toast.makeText(getContext(),"Declined request",Toast.LENGTH_SHORT).show();
                                                                    }

                                                                }
                                                            });
                                                        }

                                                    }
                                                });

                                            }
                                        });

                                       requestviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               CharSequence options[]=new  CharSequence[]{

                                                       "accept",
                                                       "cancel"

                                               };
                                               AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                               builder.setTitle(username+" Chat request");

                                               builder.setItems(options, new DialogInterface.OnClickListener() {
                                                   @Override
                                                   public void onClick(DialogInterface dialog, int which) {

                                                       if(which == 0){
                                                            contactsref.child(currentuserid).child(listuserid).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                            {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){

                                                                        contactsref.child(listuserid).child(currentuserid).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>()
                                                                        {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){

                                                                                    chatrequestref.child(currentuserid).child(listuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                            if(task.isSuccessful()){

                                                                                                chatrequestref.child(listuserid).child(currentuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if(task.isSuccessful()){

                                                                                                          Toast.makeText(getContext(),"new friend added",Toast.LENGTH_SHORT).show();
                                                                                                        }

                                                                                                    }
                                                                                                });
                                                                                            }

                                                                                        }
                                                                                    });


                                                                                }

                                                                            }
                                                                        });
                                                                    }

                                                                }
                                                            });

                                                       }
                                                       if(which == 1){
                                                           chatrequestref.child(currentuserid).child(listuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {

                                                                   if(task.isSuccessful()){

                                                                       chatrequestref.child(listuserid).child(currentuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task) {

                                                                               if(task.isSuccessful()){

                                                                                   Toast.makeText(getContext(),"Declined request",Toast.LENGTH_SHORT).show();
                                                                               }

                                                                           }
                                                                       });
                                                                   }

                                                               }
                                                           });


                                                       }
                                                   }
                                               });

                                               builder.show();
                                           }
                                       });

                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });


                           }



                           else if (value.equals("sent"))
                           {
                               Button cancel_request_button = requestviewholder.itemView.findViewById(R.id.request_decline_button);
                               cancel_request_button.setText("Delete Request");

                               requestviewholder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.INVISIBLE);

                               userref.child(listuserid).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                       if (dataSnapshot.hasChild("image")) {
                                           final String requestProfileImage = dataSnapshot.child("image").getValue().toString();

                                           Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(requestviewholder.userprofileimage);
                                       }

                                       final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                       final String requestUserStatus = dataSnapshot.child("status").getValue().toString();

                                       requestviewholder.userprofilename.setText(requestUserName);
                                       requestviewholder.userprofilestatus.setText("You have sent a request to " + requestUserName);


                                       requestviewholder.itemView.findViewById(R.id.request_decline_button).setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {

                                               chatrequestref.child(currentuserid).child(listuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<Void> task) {

                                                       if(task.isSuccessful()){

                                                           chatrequestref.child(listuserid).child(currentuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {

                                                                   if(task.isSuccessful()){

                                                                       Toast.makeText(getContext(),"Declined request",Toast.LENGTH_SHORT).show();
                                                                   }

                                                               }
                                                           });
                                                       }

                                                   }
                                               });

                                           }
                                       });


                                       requestviewholder.itemView.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               CharSequence options[] = new CharSequence[]
                                                       {
                                                               "Cancel Chat Request"
                                                       };

                                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                               builder.setTitle("Already Sent Request");

                                               builder.setItems(options, new DialogInterface.OnClickListener() {
                                                   @Override
                                                   public void onClick(DialogInterface dialogInterface, int i) {
                                                       if (i == 0) {
                                                           chatrequestref.child(currentuserid).child(listuserid)
                                                                   .removeValue()
                                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                           if (task.isSuccessful()) {
                                                                               chatrequestref.child(listuserid).child(currentuserid)
                                                                                       .removeValue()
                                                                                       .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                           @Override
                                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                                               if (task.isSuccessful()) {
                                                                                                   Toast.makeText(getContext(), "you have cancelled the chat request.", Toast.LENGTH_SHORT).show();
                                                                                               }
                                                                                           }
                                                                                       });
                                                                           }
                                                                       }
                                                                   });
                                                       }
                                                   }
                                               });
                                               builder.show();
                                           }
                                       });

                                   }
                                   @Override
                                   public void onCancelled(DatabaseError databaseError) {

                                   }
                               });

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
            public requestviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                requestviewholder holder= new requestviewholder(view);


                return holder;
            }
        };

        myrequestlist.setAdapter(adapter);
        adapter.startListening();

    }



    public  static  class  requestviewholder extends RecyclerView.ViewHolder{

        TextView userprofilename,userprofilestatus;

        CircleImageView userprofileimage;
        Button accept,decline;

        public requestviewholder(@NonNull View itemView) {
            super(itemView);

            userprofileimage=itemView.findViewById(R.id.users_profile_images);
            accept=itemView.findViewById(R.id.request_accept_button);
            decline=itemView.findViewById(R.id.request_decline_button);
            userprofilename=itemView.findViewById(R.id.users_name);
            userprofilestatus=itemView.findViewById(R.id.users_status);
        }
    }
}
