package com.example.virtualchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupchatActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ImageButton sendmessagebutton;
    private EditText groupmessageinput;
    private ScrollView mscrollview;
    private TextView displaytextmessage;
    private String currentgroupname,currentuserid,currentUsername,currentdate,currenttime;
    private FirebaseAuth mauth;
    private DatabaseReference userref,groupnameref,groupmessagekeyref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);

        currentgroupname = getIntent().getExtras().get("groupname").toString();

        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        userref= FirebaseDatabase.getInstance().getReference().child("Users");
        groupnameref=FirebaseDatabase.getInstance().getReference().child("Group").child(currentgroupname);


        intializeMethods();

        getuserinfo();

        sendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                sendmessafeinfotoDatabase();
                groupmessageinput.setText("");
                mscrollview.fullScroll(ScrollView.FOCUS_DOWN );
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        groupnameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists()){
                    Displaymessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    Displaymessage(dataSnapshot);
                }
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

        mscrollview.fullScroll(ScrollView.FOCUS_DOWN );

    }



    private void intializeMethods() {
        mtoolbar=(Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentgroupname);
        groupmessageinput=(EditText) findViewById(R.id.input_group_message);
        mscrollview=(ScrollView) findViewById(R.id.my_scroll_view);
        displaytextmessage=(TextView) findViewById(R.id.group_chat_text_display);
        sendmessagebutton=(ImageButton) findViewById(R.id.group_send_button);

    }



    private void getuserinfo() {

        userref.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUsername=dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendmessafeinfotoDatabase()
    {

        String message=groupmessageinput.getText().toString();
        String messagekey= groupnameref.push().getKey();

        if(TextUtils.isEmpty(message)){

            Toast.makeText(GroupchatActivity.this,"please neter a message",Toast.LENGTH_SHORT).show();
        }
        else{

            Calendar calfordate=Calendar.getInstance();
            SimpleDateFormat currentdateformat=new SimpleDateFormat("MMM dd,yyyy");
            currentdate= currentdateformat.format(calfordate.getTime());

            Calendar calfortime=Calendar.getInstance();
            SimpleDateFormat currenttimeformat=new SimpleDateFormat("hh:mm a");
            currenttime= currenttimeformat.format(calfortime.getTime());

            HashMap<String,Object> groupmessagekey=new HashMap<>();
            groupnameref.updateChildren(groupmessagekey);

            groupmessagekeyref=groupnameref.child(messagekey);

            HashMap<String,Object> messageinfomap=new HashMap<>();

            messageinfomap.put("message", message);
            messageinfomap.put("time", currenttime);
            messageinfomap.put("date", currentdate);
            messageinfomap.put("name", currentUsername);

            groupmessagekeyref.updateChildren(messageinfomap);
        }


    }



    private void Displaymessage(DataSnapshot dataSnapshot)
    {
        Iterator iterator=dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String chatdate=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatmessage=(String) ((DataSnapshot)iterator.next()).getValue();
            String chatname=(String) ((DataSnapshot)iterator.next()).getValue();
            String chattime=(String) ((DataSnapshot)iterator.next()).getValue();

            displaytextmessage.append(chatname+":\n"+chatmessage+"\n"+chattime+"   "+chatdate+"\n\n");

            mscrollview.fullScroll(ScrollView.FOCUS_DOWN );

        }


    }


}
