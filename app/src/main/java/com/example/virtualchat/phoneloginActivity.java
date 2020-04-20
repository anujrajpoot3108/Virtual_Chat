package com.example.virtualchat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class phoneloginActivity extends AppCompatActivity {

    private EditText phoneno,verificationcode;
    private FirebaseAuth mauth;
    private ProgressDialog loadingbar;
    private Button createacc,verifycode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonelogin);

         mauth= FirebaseAuth.getInstance();
         loadingbar=new ProgressDialog(this);
        phoneno=(EditText) findViewById(R.id.phone_login_text);
        verificationcode=(EditText) findViewById(R.id.verification_code);
        createacc=(Button) findViewById(R.id.sendcodebutton);
        verifycode=(Button) findViewById(R.id.verifybutton);


        createacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String mobno=phoneno.getText().toString();
                if(TextUtils.isEmpty(mobno)){
                    Toast.makeText(phoneloginActivity.this,"please enter phone no",Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingbar.setTitle("Verifying Code");
                    loadingbar.setMessage("please wait");
                    loadingbar.show();
                    loadingbar.setCanceledOnTouchOutside(false);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            mobno,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            phoneloginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }

            }
        });

        verifycode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneno.setVisibility(View.INVISIBLE);
                createacc.setVisibility(View.INVISIBLE);

                String code=verificationcode.getText().toString();
                if(TextUtils.isEmpty(code)){
                    Toast.makeText(phoneloginActivity.this,"please enter code",Toast.LENGTH_SHORT).show();
                }
                else{
                    loadingbar.setTitle("Verifying Code");
                    loadingbar.setMessage("please wait");
                    loadingbar.show();
                    loadingbar.setCanceledOnTouchOutside(false);

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);

                }
            }
        });

       callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
           @Override
           public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

               signInWithPhoneAuthCredential(phoneAuthCredential);
           }

           @Override
           public void onVerificationFailed(FirebaseException e) {
                loadingbar.dismiss();
                Toast.makeText(phoneloginActivity.this,"Invalid Code or phone no",Toast.LENGTH_SHORT).show();
               phoneno.setVisibility(View.VISIBLE);
               createacc.setVisibility(View.VISIBLE);
               verificationcode.setVisibility(View.INVISIBLE);
               verifycode.setVisibility(View.INVISIBLE);
           }

           public void onCodeSent( String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
               mVerificationId = verificationId;
               mResendToken = token;
               loadingbar.dismiss();
               Toast.makeText(phoneloginActivity.this,"phone verification code sent",Toast.LENGTH_SHORT).show();
               phoneno.setVisibility(View.INVISIBLE);
               createacc.setVisibility(View.INVISIBLE);
               verificationcode.setVisibility(View.VISIBLE);
               verifycode.setVisibility(View.VISIBLE);
           }
       };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingbar.dismiss();
                            Toast.makeText(phoneloginActivity.this,"account created",Toast.LENGTH_SHORT).show();
                             Intent mainintent=new Intent(phoneloginActivity.this,MainActivity.class);
                             startActivity(mainintent);
                        }

                        else {
                            String message=task.getException().toString();
                            Toast.makeText(phoneloginActivity.this,"Wrong Code "+message,Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}
