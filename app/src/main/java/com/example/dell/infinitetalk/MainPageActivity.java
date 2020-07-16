package com.example.dell.infinitetalk;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class MainPageActivity extends AppCompatActivity {


    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private Toolbar toolbar;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference();

        myTabLayout = findViewById(R.id.tabLayout);
        myViewPager = findViewById(R.id.viewPager);

        toolbar = findViewById(R.id.myToolBar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("InfiniteTalk");
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());

        myTabLayout.setupWithViewPager(myViewPager);

        myViewPager.setAdapter(myTabsAccessorAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_groups) {
           NewGroups();
        }
        if (item.getItemId() == R.id.main_find_friends) {

        }
        if (item.getItemId() == R.id.main_settings) {
            UserAccount();
        }
        if (item.getItemId() == R.id.main_logout) {
            mAuth.signOut();
            Intent loginIntent = new Intent(MainPageActivity.this, MainActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
        return true;
    }

    private void NewGroups() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainPageActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name");

        final EditText groupNameField = new EditText(MainPageActivity.this);
        groupNameField.setHint("E.g Loli Lovers");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainPageActivity.this, "Please Enter a Group Name", Toast.LENGTH_SHORT).show();
                }
                else {
                    createNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String groupName) {
        ref.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainPageActivity.this, groupName + " Group is Created Successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    String message = task.getException().toString();
                    Toast.makeText(MainPageActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void UserAccount() {
        Intent userIntent = new Intent(MainPageActivity.this, ProfileActivity.class);
        startActivity(userIntent);
    }
}
