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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private ProgressDialog loadingbar;
    private FirebaseAuth mauth;
    private Button loginbutton;
    private EditText useremail,userpassword;
    private TextView newaccountlink,forgetpasswordlink;
    private DatabaseReference userref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mauth=FirebaseAuth.getInstance();
        userref= FirebaseDatabase.getInstance().getReference().child("Users");
        initializemethods();
        newaccountlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });





        forgetpasswordlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToForgetPasswordActivity();
            }
        });
    }

    private void login() {
        String email=useremail.getText().toString();
        String password=userpassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"please enter email...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"please enter password...",Toast.LENGTH_SHORT).show();
        }
        else{

            loadingbar.setTitle("Login In Process");
            loadingbar.setMessage("please wait ");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();
            mauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String currentuserid=mauth.getCurrentUser().getUid();
                        String devicetoken= FirebaseInstanceId.getInstance().getToken();

                        userref.child(currentuserid).child("device_token").setValue(devicetoken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){

                                    SendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this,"login successfully",Toast.LENGTH_SHORT).show();
                                    loadingbar.dismiss();
                                }

                            }
                        });

                    }
                    else
                    {
                        String message=task.getException().toString();
                        Toast.makeText(LoginActivity.this,"error"+message,Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }

                }
            });
        }
    }

    private void initializemethods() {
        loginbutton=(Button) findViewById(R.id.Login_Button);
        useremail=(EditText)findViewById(R.id.Login_email);
        userpassword=(EditText) findViewById(R.id.Login_password);
        newaccountlink=(TextView) findViewById(R.id.Need_new_account);
        forgetpasswordlink=(TextView) findViewById(R.id.forget_password_link);
        loadingbar=new ProgressDialog(this);
    }



    private void SendUserToMainActivity() {



        Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void SendUserToRegisterActivity() {
        Intent RegisterIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(RegisterIntent);
    }

    private void SendUserToForgetPasswordActivity(){
        Intent forgetIntent=new Intent(LoginActivity.this,ForgetpasswordActivity.class);
        startActivity(forgetIntent);
    }
}
