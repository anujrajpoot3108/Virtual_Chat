package com.example.virtualchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messagereceiverid,messagereceivername,messagereceiverimage,messagesenderid;
    private TextView username,userlastseen;
    private CircleImageView userimage;
    private Toolbar chattoolbar;
    private FirebaseAuth mauth;
    private DatabaseReference rootref;
    private ImageButton sendbutton,sendfilesbutton;
    private EditText messageinput;
    private final List<messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private message_adapter messageAdapter;
    private RecyclerView usermessagelist;
    private String currentdate,myurl,currenttime,checker="";
    private StorageTask uploadtask;
    private Uri fileuri;
    private ProgressDialog loadingbar;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mauth=FirebaseAuth.getInstance();
        messagesenderid=mauth.getCurrentUser().getUid();
        rootref= FirebaseDatabase.getInstance().getReference();


        messagereceiverid=getIntent().getExtras().get("visit_user_ids").toString();
        messagereceivername=getIntent().getExtras().get("visit_user_name").toString();
        messagereceiverimage=getIntent().getExtras().get("visit_user_image").toString();

        intializecontrolers();

        username.setText(messagereceivername);
        Picasso.get().load(messagereceiverimage).placeholder(R.drawable.profile_image).into(userimage);
        updatelastseen();

        userimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChatActivity.this,imageviewerActivity.class);
                intent.putExtra("url",messagereceiverimage);
                startActivity(intent);
            }
        });


        sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessage();

            }
        });

        sendfilesbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[]=new  CharSequence[]
                        {
                          "Images",
                          "PDF files",
                          "Ms Word files"

                        };
                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select Files");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which==0){

                            checker="image";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(Intent.createChooser(intent,"select image"),438);

                        }
                        else if(which==1){
                            checker="pdf";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf*");
                            startActivityForResult(Intent.createChooser(intent,"select PDF File"),438);
                        }
                       else  if(which==2){
                           checker="docx";
                            Intent intent=new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword*");
                            startActivityForResult(Intent.createChooser(intent,"select MS Word File"),438);

                        }

                    }
                });
                builder.show();


            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void intializecontrolers() {

        chattoolbar=(Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chattoolbar);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        LayoutInflater layoutInflater=(LayoutInflater)  this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewactionbar=layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(viewactionbar);

        userimage=(CircleImageView)findViewById(R.id.custom_profile_image);
        username=(TextView) findViewById(R.id.custom_profile_name);
        userlastseen=(TextView)findViewById(R.id.last_Seen);
        sendbutton=(ImageButton) findViewById(R.id.send_message_private_button);
        sendfilesbutton=(ImageButton)findViewById(R.id.send_files_private_button);
        messageinput=(EditText) findViewById(R.id.input_message);
        messageAdapter=new message_adapter(messagesList);
        usermessagelist=(RecyclerView) findViewById(R.id.private_chat_profiles);
        linearLayoutManager=new LinearLayoutManager(this);
        usermessagelist.setLayoutManager(linearLayoutManager);
        usermessagelist.setAdapter(messageAdapter);
        loadingbar=new ProgressDialog(ChatActivity.this);


        Calendar calfordate=Calendar.getInstance();
        SimpleDateFormat currentdateformat= null;
        currentdateformat = new SimpleDateFormat("MMM dd,yyyy");
        currentdate= currentdateformat.format(calfordate.getTime());

        Calendar calfortime=Calendar.getInstance();
        SimpleDateFormat currenttimeformat=new SimpleDateFormat("hh:mm a");
        currenttime= currenttimeformat.format(calfortime.getTime());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            loadingbar.setTitle("Sending File");
            loadingbar.setMessage("please wait");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();
            fileuri=data.getData();
            if(!checker.equals("image")){

                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messagesenderref="message/" + messagesenderid + "/" + messagereceiverid;
                final String messagereceiverref="message/" + messagereceiverid + "/" + messagesenderid;

                DatabaseReference usermessagekeyref=rootref.child("message").child(messagesenderid).child(messagereceiverid).push();

                final  String messagepushid=usermessagekeyref.getKey();

                final StorageReference filepath=storageReference.child(messagepushid+"."+checker);
                filepath.putFile(fileuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            uploadtask = filepath.putFile(fileuri);
                            uploadtask.continueWithTask(new Continuation() {
                                @Override
                                public Object then(@NonNull Task task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return filepath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {

                                        Uri downloadurl = task.getResult();
                                        myurl = downloadurl.toString();

                                        Map messagetextbody = new HashMap();
                                        messagetextbody.put("message", myurl);
                                        messagetextbody.put("name", fileuri.getLastPathSegment());
                                        messagetextbody.put("type", checker);
                                        messagetextbody.put("from", messagesenderid);
                                        messagetextbody.put("to", messagereceiverid);
                                        messagetextbody.put("messageId", messagepushid);
                                        messagetextbody.put("time", currenttime);

                                        Map messagebodyDetails = new HashMap();
                                        messagebodyDetails.put(messagesenderref + "/" + messagepushid, messagetextbody);
                                        messagebodyDetails.put(messagereceiverref + "/" + messagepushid, messagetextbody);

                                        rootref.updateChildren(messagebodyDetails).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                loadingbar.dismiss();
                                                Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                                            }
                                        });


                                    }


                                }
                            });

                        }
                    }
                    });
                }

            else if(checker.equals("image")) {

                            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                            final String messagesenderref = "message/" + messagesenderid + "/" + messagereceiverid;
                            final String messagereceiverref = "message/" + messagereceiverid + "/" + messagesenderid;

                            DatabaseReference usermessagekeyref = rootref.child("message").child(messagesenderid).child(messagereceiverid).push();

                            final String messagepushid = usermessagekeyref.getKey();

                            final StorageReference filepath = storageReference.child(messagepushid + ".jpg");
                            uploadtask = filepath.putFile(fileuri);
                            uploadtask.continueWithTask(new Continuation() {
                                @Override
                                public Object then(@NonNull Task task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return filepath.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {

                                        Uri downloadurl = task.getResult();
                                        myurl = downloadurl.toString();

                                        Map messagetextbody = new HashMap();
                                        messagetextbody.put("message", myurl);
                                        messagetextbody.put("name", fileuri.getLastPathSegment());
                                        messagetextbody.put("type", checker);
                                        messagetextbody.put("from", messagesenderid);
                                        messagetextbody.put("to", messagereceiverid);
                                        messagetextbody.put("messageId", messagepushid);
                                        messagetextbody.put("time", currenttime);

                                        Map messagebodyDetails = new HashMap();
                                        messagebodyDetails.put(messagesenderref + "/" + messagepushid, messagetextbody);
                                        messagebodyDetails.put(messagereceiverref + "/" + messagepushid, messagetextbody);

                                        rootref.updateChildren(messagebodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {

                                                if (task.isSuccessful()) {
                                                    messageinput.setText("");
                                                    loadingbar.dismiss();
                                                    Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();

                                                } else {
                                                    loadingbar.dismiss();
                                                    Toast.makeText(ChatActivity.this, "message not sent", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });

                                    }

                                }
                            });

            }
            else{
                loadingbar.dismiss();
            }

        }

        }



    private void updatelastseen(){

        rootref.child("Users").child(messagereceiverid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("userstate").hasChild("state")){
                    String state=dataSnapshot.child("userstate").child("state").getValue().toString();
                    String date=dataSnapshot.child("userstate").child("date").getValue().toString();
                    String time=dataSnapshot.child("userstate").child("time").getValue().toString();

                    if(state.equals("online")){
                        userlastseen.setText("online ");
                    }
                    else if(state.equals("Offline")){
                        userlastseen.setText(date + "  " + time);
                    }

                }
                else{
                    userlastseen.setText("Offline ");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        rootref.child("message").child(messagesenderid).child(messagereceiverid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                messages message=dataSnapshot.getValue(messages.class);
                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();
                usermessagelist.smoothScrollToPosition(usermessagelist.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendmessage() {

        String message=messageinput.getText().toString();


        if(TextUtils.isEmpty(message)){

            Toast.makeText(ChatActivity.this,"please enter a message",Toast.LENGTH_SHORT).show();
        }
        else{
            String messagesenderref="message/" + messagesenderid + "/" + messagereceiverid;
            String messagereceiverref="message/" + messagereceiverid + "/" + messagesenderid;

            DatabaseReference usermessagekeyref=rootref.child("Messages").child(messagesenderid).child(messagereceiverid).push();

            String messagepushid=usermessagekeyref.getKey();

            Map messagetextbody=new HashMap();
            messagetextbody.put("message",message);
            messagetextbody.put("type","text");
            messagetextbody.put("from",messagesenderid);
            messagetextbody.put("to",messagereceiverid);
            messagetextbody.put("messageId",messagepushid);
            messagetextbody.put("time",currenttime);

            Map messagebodyDetails=new HashMap();
            messagebodyDetails.put(messagesenderref+"/"+messagepushid,messagetextbody);
            messagebodyDetails.put(messagereceiverref+"/"+messagepushid,messagetextbody);

            rootref.updateChildren(messagebodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if(task.isSuccessful()){
                        messageinput.setText("");
                    }
                    else{
                        Toast.makeText(ChatActivity.this,"message not sent",Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

    }


}
