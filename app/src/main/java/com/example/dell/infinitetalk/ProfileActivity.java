package com.example.dell.infinitetalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Button update;
    private EditText userName, userStatus;
    private CircleImageView userProfile;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private StorageReference userProfileImage;
    private Toolbar toolbar;
    private ProgressDialog loadingBar;
    private static final int galleryPick = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference();
        userProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Images");
        toolbar = findViewById(R.id.myToolBar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Profile");


        InitializeFields();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserData();

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });
    }

    private void InitializeFields() {
       update = (Button) findViewById(R.id.update);
       userName = (EditText) findViewById(R.id.set_user_name);
       userStatus = (EditText) findViewById(R.id.set_profile_status);
       userProfile = (CircleImageView) findViewById(R.id.profile_image);
       loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImage.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ProfileActivity.this, "Profile Image Updated ", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String message =task.getException().toString();
                            Toast.makeText(ProfileActivity.this, "Error" +message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

    }

    private void UpdateSettings() {
        String uName = userName.getText().toString();
        String uStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(uName)){
            Toast.makeText(this, "Enter User Name", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(uStatus)){
            Toast.makeText(this, "Enter Status", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Updating Profile");
            loadingBar.setMessage("Please Wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("Uid", currentUserID);
            profileMap.put("Name", uName);
            profileMap.put("Status", uStatus);
            profileMap.put("Phone", user.getPhoneNumber());
            ref.child("User").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  if (task.isSuccessful()){
                      loadingBar.dismiss();
                      SendToMainPage();
                      Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                  }
                  else {
                      loadingBar.dismiss();
                      String message = task.getException().toString();
                      Toast.makeText(ProfileActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                  }
                }
            });
        }

    }

    private void RetrieveUserData() {
        ref.child("User").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Name") && (dataSnapshot.hasChild("Image")))){
                    String retrieveUserName = dataSnapshot.child("Name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("Status").getValue().toString();
                    String retrieveUserImage = dataSnapshot.child("Image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("Name"))){
                    String retrieveUserName = dataSnapshot.child("Name").getValue().toString();
                    String retrieveUserStatus = dataSnapshot.child("Status").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                }
                else {
                    Toast.makeText(ProfileActivity.this, "Update Your Profile Information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendToMainPage() {
        Intent userIntent = new Intent(ProfileActivity.this, MainPageActivity.class);
        startActivity(userIntent);
    }
}
