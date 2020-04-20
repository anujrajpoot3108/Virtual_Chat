package com.example.virtualchat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

        private Toolbar mtoolbar;
        private ViewPager myViewPager;
        private TabLayout myTabLayout;
        private TabsAccessorAdaptor myTabsAccessorAdaptor;
        private FirebaseAuth mauth;
        private DatabaseReference Rootref;
        private String currentuserid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mauth=FirebaseAuth.getInstance();

        Rootref= FirebaseDatabase.getInstance().getReference();
//        System.out.println("ansj");
        mtoolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
//        System.out.println("anfj");
       setSupportActionBar(mtoolbar);
//        System.out.println("asfj");
        getSupportActionBar().setTitle("Virtual Chat");
//        System.out.println("asdj");
        myViewPager=(ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdaptor= new TabsAccessorAdaptor(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdaptor);
        myTabLayout=(TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser=mauth.getCurrentUser();
        if(currentuser==null)
        {
            SendUserToLoginActivity();
        }
        else{

            updateuserstatus("online");
            verifyuserexistence();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentuser=mauth.getCurrentUser();
        if(currentuser != null){
            updateuserstatus("Offline");

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentuser=mauth.getCurrentUser();

        if(currentuser != null){
            updateuserstatus("Offline");

        }

    }

    private void verifyuserexistence() {
        String currentuserid=mauth.getCurrentUser().getUid();
        Rootref.child("Users").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    //Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{
                    SendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.main_signout_option){

             updateuserstatus("Offline");
             mauth.signOut();
        SendUserToLoginActivity();
         }
        if(item.getItemId()==R.id.main_settings_option){
            SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_find_friends_option){
            SendUserTofindfriendsActivity();

        }
        if(item.getItemId()==R.id.main_create_group_option){
            createnewgroup();

        }
        return  true;

    }



    private void createnewgroup() {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("enter Group Name");

        final EditText groupname= new EditText(MainActivity.this);
        groupname.setHint(" e.g.  Biradaars");
        builder.setView(groupname);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName=groupname.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this,"please enter group name",Toast.LENGTH_SHORT).show();
                }
                else {
                    CreateNewGroupfun(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 dialog.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroupfun(final String groupName) {
        Rootref.child("Group").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,groupName+" created succesfully",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToSettingsActivity() {
        Intent settingsintent=new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsintent);
    }
    private void SendUserTofindfriendsActivity() {

       Intent  findfriendsintent =new Intent(MainActivity.this,findfriendsActivity.class);
       startActivity(findfriendsintent);
        }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateuserstatus(String state){

        String currentdate,currenttime;

        Calendar calfordate=Calendar.getInstance();
        SimpleDateFormat currentdateformat= null;
        currentdateformat = new SimpleDateFormat("MMM dd,yyyy");
        currentdate= currentdateformat.format(calfordate.getTime());

        Calendar calfortime=Calendar.getInstance();
        SimpleDateFormat currenttimeformat=new SimpleDateFormat("hh:mm a");
        currenttime= currenttimeformat.format(calfortime.getTime());

        HashMap<String,Object> onlinestatusmap=new HashMap<>();

        onlinestatusmap.put("time", currenttime);
        onlinestatusmap.put("date", currentdate);
        onlinestatusmap.put("state", state);

        currentuserid=mauth.getCurrentUser().getUid();

        Rootref.child("Users").child(currentuserid).child("userstate").updateChildren(onlinestatusmap);

    }
}
