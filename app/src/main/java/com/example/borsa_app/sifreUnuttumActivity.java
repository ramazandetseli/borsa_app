package com.example.borsa_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class sifreUnuttumActivity extends AppCompatActivity {

    EditText editmail;
    Button btnreset;

    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sifre_unuttum);
        editmail=findViewById(R.id.resetMailText);
        btnreset=findViewById(R.id.btnKayitOl);
        MaterialToolbar toolbar= findViewById(R.id.backGirisSayfasi);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // geri dön
        });
        auth=FirebaseAuth.getInstance();


        btnreset.setOnClickListener(view -> {
            String email=editmail.getText().toString().trim();

            if(email.isEmpty()){
                Toast.makeText(this, "E-posta gir", Toast.LENGTH_SHORT).show();
                return;
            }

        auth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
            Toast.makeText(this,"Şifre sıfırlama maili gönderildi",Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
            
        });

            Intent intent =new Intent(sifreUnuttumActivity.this,GirisActivity.class);
            startActivity(intent);
        });









    }
}
