package com.example.borsa_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class GirisActivity extends AppCompatActivity {
    EditText email,Sifre;
    Button girisYap,kayitOl;
    FirebaseAuth auth;

    Button sifremiUnuttum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris);
        email=findViewById(R.id.resetMailText);
        Sifre=findViewById(R.id.editTextTextPassword);
        girisYap=findViewById(R.id.btnKayitOl);
        sifremiUnuttum=findViewById(R.id.button2);
        kayitOl=findViewById(R.id.button3);
        auth = FirebaseAuth.getInstance();
        girisYap.setOnClickListener(view -> {
            String eMail=email.getText().toString().trim();
            String passWord=Sifre.getText().toString().trim();

            if(eMail.isEmpty() || passWord.isEmpty()){
                Toast.makeText(this, "Alanlar boş", Toast.LENGTH_SHORT).show();
                return;
            }

        auth.signInWithEmailAndPassword(eMail,passWord).addOnSuccessListener(authResult -> {
            Intent intent=new Intent(GirisActivity.this,MainActivity.class);
            startActivity(intent);
            finish();

        }).addOnFailureListener(e -> {
            Toast.makeText(this,e.getMessage() , Toast.LENGTH_SHORT).show();
        });
        });

    sifremiUnuttum.setOnClickListener(view -> {
        Intent intent2=new Intent(GirisActivity.this,sifreUnuttumActivity.class);
        startActivity(intent2);
    });

    kayitOl.setOnClickListener(view -> {
        Intent intent3=new Intent(GirisActivity.this,kayitOlActivity.class);
        startActivity(intent3);
    });

    }
}