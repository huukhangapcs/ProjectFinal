package com.apcs2.helperapp.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apcs2.helperapp.entity.Register;
import com.example.helperapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    CardView login;
    TextView register;
    TextView invalidLogin;
    ProgressBar progressBar;
    LinearLayout staticLayout;
    String sUser;
    String sPass;
    FirebaseAuth fAuth;
    int loginFlag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        checkInternetPermission();
        if (!checkMapPermission()) requestMapPermission();
        initComponent();
    }

    private boolean checkMapPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_DENIED)
        {
            loginFlag = 2;
            return false;
        }
        return true;
    }

    private void checkInternetPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.INTERNET)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET,},
                    1000);
        }
    }
    private void requestMapPermission() {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    1001);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "NO INTERNET", Toast.LENGTH_LONG).show();
        }
        else if (requestCode == 1001)
        {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "NO ACCESS TO MAP", Toast.LENGTH_LONG).show();
        }
    }

    private void initComponent() {
        username = (EditText)findViewById(R.id.editText);
        password = (EditText)findViewById(R.id.editText2);
        login = (CardView) findViewById(R.id.cardView);
        register = (TextView)findViewById(R.id.textView2);
        invalidLogin = (TextView)findViewById(R.id.textViewError);
        invalidLogin.setTextColor(Color.RED);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLog);
        staticLayout = findViewById(R.id.staticLayout);
        fAuth = FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                staticLayout.setVisibility(View.VISIBLE);
                if (checkLogin() && checkMapPermission()) {
                    actionLogin();
                }
                else{
                    showLoginError();
                }

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });

    }

    private void showLoginError() {
        staticLayout.setVisibility(View.GONE);
        String err;
        if (loginFlag == 1) {
            err = "Please fill in all boxes";
            invalidLogin.setText(err);
        }
        else
        {
            err = "No map permission";
            invalidLogin.setText(err);
        }
    }
    private void getLoginData() {
        sUser = username.getText().toString().trim();
        sPass = password.getText().toString().trim();
    }

    private boolean checkLogin() {
        getLoginData();
        if (TextUtils.isEmpty(sUser) || TextUtils.isEmpty(sPass))
        {
            loginFlag = 1;
            return false;
        }
        return true;
    }

    private void actionLogin() {
        fAuth.signInWithEmailAndPassword(sUser, sPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Login successfully", Toast.LENGTH_LONG).show();
                    Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(mapIntent);
                    finish();
                }
                else{
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
                staticLayout.setVisibility(View.GONE);
            }
        });
    }


}