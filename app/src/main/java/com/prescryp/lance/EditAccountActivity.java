package com.prescryp.lance;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditAccountActivity extends AppCompatActivity {

    private TextInputLayout name_input, phone_no_input, email_input;
    private CardView saveProfileUpdate;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private String userId;
    private String mName, mPhone, mEmail, mProfileImageUrl;
    private Uri resultUri;

    private TextView editPic;
    private CircleImageView profile_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        name_input = findViewById(R.id.name_input);
        phone_no_input = findViewById(R.id.phone_no_input);
        email_input = findViewById(R.id.email_input);
        saveProfileUpdate = findViewById(R.id.saveProfileUpdate);
        editPic = findViewById(R.id.editPic);
        profile_pic = findViewById(R.id.profile_pic);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);

        getUserInformation();

        saveProfileUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

        editPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });


    }

    private void getUserInformation(){
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null){
                        mName = map.get("name").toString();
                        name_input.getEditText().setText(mName);
                    }
                    if (map.get("email") != null){
                        mEmail = map.get("email").toString();
                        email_input.getEditText().setText(mEmail);
                    }
                    if (map.get("profileImageUrl") != null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(profile_pic);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mPhone = mAuth.getCurrentUser().getPhoneNumber();
        phone_no_input.getEditText().setText(mPhone);
        phone_no_input.getEditText().setEnabled(false);
    }

    private void saveUserInformation() {
        mName = name_input.getEditText().getText().toString();
        mEmail = email_input.getEditText().getText().toString();


        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("email", mEmail);
        userInfo.put("phone", mPhone);

        mUserDatabase.updateChildren(userInfo);

        if (resultUri != null){
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_image").child(userId);
            Bitmap bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, boas);
            byte[] data = boas.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;

                            Map newImage = new HashMap();
                            newImage.put("profileImageUrl", downloadUri.toString());
                            mUserDatabase.updateChildren(newImage);

                            finish();
                            return;
                        }
                    });

                }
            });
        }else {
            finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profile_pic.setImageURI(resultUri);
        }
    }
}
