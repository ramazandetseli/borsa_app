package com.example.borsa_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import Hisseler.HisseMenu;
import ara.AraMenu;
import profil.ProfilMenu;

public class MainActivity extends AppCompatActivity {

    Button btnHisse,btnAra,btnProfil;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnHisse=findViewById(R.id.HisseButtonMenu);
        btnAra=findViewById(R.id.AraButtonMenu);
        btnProfil=findViewById(R.id.ProfilButtonMenu);

        btnHisse.setOnClickListener(view -> {

        });




    }

}   