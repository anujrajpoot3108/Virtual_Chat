package com.example.virtualchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String recieveruserid,senderuserid,current_state;
    private TextView username,userstatus;
    private Button sendmessagebutton,declinechatrequestbutton;
    private CircleImageView userimage;
    private DatabaseReference userref,chatrequestref,contactsref,notificationref;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userref= FirebaseDatabase.getInstance().getReference().child("Users");
        chatrequestref=FirebaseDatabase.getInstance().getReference().child("chat requests");
        contactsref=FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationref=FirebaseDatabase.getInstance().getReference().child("notifications");
        mauth=FirebaseAuth.getInstance();
        senderuserid=mauth.getCurrentUser().getUid();

        recieveruserid=getIntent().getExtras().get("visit_user_id").toString();

        userimage=(CircleImageView) findViewById(R.id.visit_profile_image);
        username=(TextView) findViewById(R.id.visit_profile_name);
        userstatus=(TextView) findViewById(R.id.visit_profile_status);
        sendmessagebutton=(Button) findViewById(R.id.send_message_button);
        declinechatrequestbutton=(Button) findViewById(R.id.decline_message_request_button);
        current_state="new";


        Retrieveuserinfo();

    }

    private void Retrieveuserinfo() {

        userref.child(recieveruserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())&&(dataSnapshot.hasChild("image"))){
                    String userprofileimage=dataSnapshot.child("image").getValue().toString();
                    String userprofilename=dataSnapshot.child("name").getValue().toString();
                    String userprofilestatus=dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userprofileimage).placeholder(R.drawable.profile_image).into(userimage);
                    username.setText(userprofilename);
                    userstatus.setText(userprofilestatus);

                    managechatrequest();

                }
                else{

                    String userprofilename=dataSnapshot.child("name").getValue().toString();
                    String userprofilestatus=dataSnapshot.child("status").getValue().toString();

                    username.setText(userprofilename);
                    userstatus.setText(userprofilestatus);
                    managechatrequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void managechatrequest() {


            chatrequestref.child(senderuserid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(recieveruserid)){
                        String request_type=dataSnapshot.child(recieveruserid).child("request_type").getValue().toString();

                        if(request_type.equals("sent")){
                            current_state="request_sent";
                            sendmessagebutton.setText("Cancel request");
                        }
                        else if(request_type.equals("recieved")){
                            current_state="request recieved";
                            sendmessagebutton.setText("accept chat request");
                            declinechatrequestbutton.setVisibility(View.VISIBLE);
                            declinechatrequestbutton.setEnabled(true);
                            declinechatrequestbutton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancelchatrequest();

                                }
                            });
                        }

                    }
                    else {
                        contactsref.child(senderuserid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(recieveruserid)){
                                    current_state="friends";
                                    sendmessagebutton.setText("delete contact");

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            if(senderuserid.equals(recieveruserid)){
                sendmessagebutton.setVisibility(View.INVISIBLE);


            }
            else{

                sendmessagebutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendmessagebutton.setEnabled(false);
                        if(current_state.equals("new")){
                            sendchatrequest();

                        }
                        if(current_state.equals("request_sent")){
                            cancelchatrequest();
                        }
                        if(current_state.equals("request recieved")){
                            Acceptchatrequest();
                        }
                        if(current_state.equals("friends")){
                            deletecontact();
                        }

                    }
                });

            }

    }

    private void deletecontact() {

        contactsref.child(senderuserid).child(recieveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactsref.child(recieveruserid).child(senderuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            sendmessagebutton.setEnabled(true);
                            current_state="new";
                            sendmessagebutton.setText("Send Message");
                            declinechatrequestbutton.setVisibility(View.INVISIBLE);
                            declinechatrequestbutton.setEnabled(false);
                        }
                    });
                }

            }
        });
    }


    private void Acceptchatrequest() {

      contactsref.child(senderuserid).child(recieveruserid).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {

              if(task.isSuccessful()){
                  contactsref.child(recieveruserid).child(senderuserid).child("Contacts").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {

                          if(task.isSuccessful()){
                              chatrequestref.child(senderuserid).child(recieveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                  @Override
                                  public void onComplete(@NonNull Task<Void> task) {

                                      if(task.isSuccessful()){

                                          chatrequestref.child(recieveruserid).child(senderuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                              @Override
                                              public void onComplete(@NonNull Task<Void> task) {
                                                  if(task.isSuccessful()){

                                                      sendmessagebutton.setEnabled(true);
                                                      current_state="friends";
                                                      sendmessagebutton.setText("Delete Contact");
                                                      declinechatrequestbutton.setVisibility(View.INVISIBLE);
                                                      declinechatrequestbutton.setEnabled(false);

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

    private void cancelchatrequest() {

        chatrequestref.child(senderuserid).child(recieveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    chatrequestref.child(recieveruserid).child(senderuserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                sendmessagebutton.setEnabled(true);
                                current_state="new";
                                sendmessagebutton.setText("Send Message");
                                declinechatrequestbutton.setVisibility(View.INVISIBLE);
                                declinechatrequestbutton.setEnabled(false);
                            }

                        }
                    });
                }

            }
        });
    }

    private void sendchatrequest() {
            chatrequestref.child(senderuserid).child(recieveruserid).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        chatrequestref.child(recieveruserid).child(senderuserid).child("request_type").setValue("recieved").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()) {

                                   HashMap<String, String> chatnotificationmap = new HashMap<>();
                                   chatnotificationmap.put("from", senderuserid);
                                   chatnotificationmap.put("type", "request");

                                   notificationref.child(recieveruserid).push().setValue(chatnotificationmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {

                                           sendmessagebutton.setEnabled(true);
                                           current_state = "request_sent";
                                           sendmessagebutton.setText("Cancel request");
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
