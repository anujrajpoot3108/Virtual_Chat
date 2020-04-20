package com.example.virtualchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetpasswordActivity extends AppCompatActivity {

    private EditText forgetemail;
    private ProgressDialog loadingbar;
    private Button sendlink;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);

        mauth=FirebaseAuth.getInstance();
        loadingbar=new ProgressDialog(this);

        forgetemail=(EditText) findViewById(R.id.forget_email);
        sendlink=(Button) findViewById(R.id.send_link);


        sendlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=forgetemail.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(ForgetpasswordActivity.this,"enter your email",Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingbar.setTitle("Sending Link");
                    loadingbar.setMessage("please wait ");
                    loadingbar.setCanceledOnTouchOutside(true);
                    loadingbar.show();
                    mauth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ForgetpasswordActivity.this,"We have a Link to your Email..",Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                            else{
                                Toast.makeText(ForgetpasswordActivity.this,"Error in Sending Link",Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();

                            }

                        }
                    });
                }
            }
        });
    }
}
