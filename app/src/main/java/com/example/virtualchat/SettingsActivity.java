package com.example.virtualchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
        private Button updatesettings;
        private EditText username,status;
        private CircleImageView Image;
        private FirebaseAuth mauth;
        private String currentuserid;
        private DatabaseReference Rootref;
        private static final int GalleryPic = 1;
        private StorageReference userprofileref;
        private ProgressDialog loadingbar;
        private Uri Imageuri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mauth=FirebaseAuth.getInstance();
        currentuserid=mauth.getCurrentUser().getUid();
        Rootref= FirebaseDatabase.getInstance().getReference();
        userprofileref= FirebaseStorage.getInstance().getReference().child("Profile Images");

        initializefields();


        updatesettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Updatesettings();
            }
        });
        Retrieveuserinfo();

        Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryintent=new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                 galleryintent.setType("image/*");
                 startActivityForResult(galleryintent,GalleryPic);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPic && resultCode==RESULT_OK && data != null){

             Imageuri= data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK) {

                loadingbar.setTitle("Setting Profile Image");
                loadingbar.setMessage("please wait");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();
                Uri resulturi = result.getUri();


            final  StorageReference filepath=userprofileref.child(currentuserid + ".jpg");

                StorageTask uploadtask = filepath.putFile(Imageuri);

                uploadtask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this,"profile image updated ",Toast.LENGTH_SHORT).show();

                           Uri downloadUrl= task.getResult();
                            String myurl=downloadUrl.toString();
                            Rootref.child("Users").child(currentuserid).child("image").setValue(myurl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(SettingsActivity.this,"image stored in database",Toast.LENGTH_SHORT).show();
                                        loadingbar.dismiss();
                                    }
                                    else{
                                        String message = task.getException().toString();

                                        Toast.makeText(SettingsActivity.this,"error "+message,Toast.LENGTH_SHORT).show();
                                        loadingbar.dismiss();

                                    }

                                }
                            });
                        }
                        else{
                            String message=task.getException().toString();
                            Toast.makeText(SettingsActivity.this,"error "+message,Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    }


                });
            }
        }
    }

    private void Updatesettings() {
        String setusername=username.getText().toString();
        String setstatus=status.getText().toString();
        if(TextUtils.isEmpty(setusername)){
            Toast.makeText(SettingsActivity.this,"please enter user name",Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String,Object> profilemap= new HashMap<>();
            profilemap.put("uid",currentuserid);
            profilemap.put("name",setusername);
            profilemap.put("status",setstatus);
            Rootref.child("Users").child(currentuserid).updateChildren(profilemap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SettingsActivity.this,"profile updated",Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();
                    }
                    else{
                        String error=task.getException().toString();
                        Toast.makeText(SettingsActivity.this,"error"+error,Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }
    }

    private void Retrieveuserinfo() {
        Rootref.child("Users").child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))) {
                    String getusername=dataSnapshot.child("name").getValue().toString();
                    String getstatus =dataSnapshot.child("status").getValue().toString();
                    String getimage=dataSnapshot.child("image").getValue().toString();
                    //System.out.println("hello");
                    username.setText(getusername);
                    status.setText(getstatus);
                    Picasso.get().load(getimage).fit().centerCrop().into(Image);
                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") )){
                    String getusername=dataSnapshot.child("name").getValue().toString();
                    //String getimage=dataSnapshot.child("image").getValue().toString();
                    String getstatus =dataSnapshot.child("status").getValue().toString();
                    username.setText(getusername);
                    status.setText(getstatus);
                }
                else{
                    Toast.makeText(SettingsActivity.this,"please set your profile",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializefields() {
        updatesettings=(Button) findViewById(R.id.update_settings);
        username=(EditText) findViewById(R.id.set_user_name);
        status=(EditText) findViewById(R.id.set_Profile_status);
        Image=(CircleImageView) findViewById(R.id.set_profile_image);
        loadingbar=new ProgressDialog(this);
    }
    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
