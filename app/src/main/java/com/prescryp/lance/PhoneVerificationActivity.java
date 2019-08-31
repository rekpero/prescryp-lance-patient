package com.prescryp.lance;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prescryp.lance.Session.MobileNumberSessionManager;

import java.util.concurrent.TimeUnit;

public class PhoneVerificationActivity extends AppCompatActivity {

    private ImageView backBtn;
    private TextView phoneNumberTextView, timer, change_phone_number;
    private PinView otp_value;
    private FloatingActionButton continueBtn;
    private CountDownTimer countDownTimer;
    private long timeLeftInMilliSeconds = 30000; // 30sec
    private boolean timerFinished = false;
    private String verificationCode;
    private FirebaseAuth mAuth;
    String newPhoneNumber;
    FirebaseAuth.AuthStateListener firebaseAuthListener;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        backBtn = findViewById(R.id.backBtn);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        otp_value = findViewById(R.id.otp_value);
        continueBtn = findViewById(R.id.continueBtn);
        timer = findViewById(R.id.timer);
        change_phone_number = findViewById(R.id.change_phone_number);

        mAuth = FirebaseAuth.getInstance();


        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Intent intent = new Intent(PhoneVerificationActivity.this, BookAmbulanceActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        };

        String phoneNumber = "";
        if (getIntent() != null){
            phoneNumber = getIntent().getStringExtra("Mobile_Number");
        }
        newPhoneNumber = "+91" + phoneNumber;
        sendVerificationCode(newPhoneNumber);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        String fullPhoneNumber = "+91 " + phoneNumber;
        phoneNumberTextView.setText(fullPhoneNumber);

        startTimer();

        final String finalPhoneNumber = newPhoneNumber;
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerFinished){
                    resendVerificationCode(finalPhoneNumber);
                    startTimer();
                }
            }
        });


        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otp_value.getText().toString();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);
                signInWithPhone(credential);
            }
        });

        change_phone_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void signInWithPhone(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String user_id = mAuth.getCurrentUser().getUid();
                            final DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                            current_user_db.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        current_user_db.setValue(true);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            MobileNumberSessionManager session = new MobileNumberSessionManager(getApplicationContext());
                            session.createMobileNumberSession(newPhoneNumber, user_id);

                            Intent intent = new Intent(PhoneVerificationActivity.this, BookAmbulanceActivity.class);
                            intent.putExtra("mobile_number", newPhoneNumber);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(PhoneVerificationActivity.this,"Incorrect OTP",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startTimer() {
        timeLeftInMilliSeconds = 30000;
        countDownTimer = new CountDownTimer(timeLeftInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliSeconds = millisUntilFinished;
                updateTimer();
            }

            @Override
            public void onFinish() {
                timerFinished = true;
                timer.setText("Resend code");
                timer.setTextColor(getResources().getColor(R.color.themeBlue));
                change_phone_number.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void updateTimer() {
        int second = (int) timeLeftInMilliSeconds / 1000;
        String timeLeftText;

        timeLeftText = "00:";
        if (second < 10) timeLeftText += "0";
        timeLeftText += second;
        String timeLeftFullText = "Resend code in " + timeLeftText;
        timer.setText(timeLeftFullText);

    }

    private void resendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                resendingToken);
    }


    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCode = s;
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            timerFinished = true;
            timer.setText("Resend code");
            timer.setTextColor(getResources().getColor(R.color.themeBlue));
            change_phone_number.setVisibility(View.VISIBLE);
        }
    };


}
