package com.example.virtualchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.sql.BatchUpdateException;

public class RegisterActivity extends AppCompatActivity {

        private EditText registeremail,registerpassword;
        private Button Signup;
        private TextView alreadyhaveacc;
        private FirebaseAuth mauth;
        private ProgressDialog loadingbar;
        private DatabaseReference Rootref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mauth=FirebaseAuth.getInstance();
        Rootref= FirebaseDatabase.getInstance().getReference();
        registeremail=(EditText) findViewById(R.id.register_email);
        registerpassword=(EditText) findViewById(R.id.register_password);
        Signup=(Button) findViewById(R.id.register_Button);
        alreadyhaveacc=(TextView) findViewById(R.id.Already_have_account);
        loadingbar=new ProgressDialog(this);
        alreadyhaveacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createnewaccount();
            }
        });
    }

    private void createnewaccount() {
        String email=registeremail.getText().toString();
        String password=registerpassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"please enter email...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"please enter password...",Toast.LENGTH_SHORT).show();
        }
        else {

            loadingbar.setTitle("creating account");
            loadingbar.setMessage("please wait creating account");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            mauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){


                        String currentuserid=mauth.getCurrentUser().getUid();
                        String devicetoken= FirebaseInstanceId.getInstance().getToken();

                        Rootref.child("Users").child(currentuserid).child("device_token").setValue(devicetoken);
                        SendUserToMainActivity();
                        Toast.makeText(RegisterActivity.this,"account created",Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                    else{
                        String message=task.getException().toString();
                        Toast.makeText(RegisterActivity.this,"error"+message,Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(LoginIntent);
    }
    private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
