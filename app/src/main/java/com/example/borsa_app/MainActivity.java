package com.example.borsa_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import Hisseler.HisseMenu;
import ara.AraMenu;
import profil.ProfilMenu;

public class MainActivity extends AppCompatActivity {



    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.hisseButtonMenu).setOnClickListener(v ->
                startActivity(new Intent(this, HisseMenu.class)));

        findViewById(R.id.AraButtonMenu).setOnClickListener(v ->
                startActivity(new Intent(this, AraMenu.class)));

        findViewById(R.id.ProfilButtonMenu).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilMenu.class)));





    }

}