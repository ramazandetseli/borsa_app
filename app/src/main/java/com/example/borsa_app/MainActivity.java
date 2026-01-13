package com.example.borsa_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.borsa_app.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import Hisseler.HisselerFragment;
import ara.AraFragment;
import profil.ProfilFragment;

public class MainActivity extends AppCompatActivity {


    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav=findViewById(R.id.bottomNav);


        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,new HisselerFragment()).commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment=null;

            if(item.getItemId()==R.id.nav_hisseler){
                selectedFragment=new HisselerFragment();
            }else if(item.getItemId()==R.id.nav_ara){
                selectedFragment=new AraFragment();
            }else if(item.getItemId()==R.id.nav_profil){
                selectedFragment=new ProfilFragment();
            }

            if(selectedFragment!=null)
            {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,selectedFragment).commit();
            }

            return true;
        });


    }

}   