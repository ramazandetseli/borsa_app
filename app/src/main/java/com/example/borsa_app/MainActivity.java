package com.example.borsa_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import Hisseler.HisseMenu; // Bu sınıfın Fragment'a dönüştürüldüğünü varsayıyorum
import ara.AraMenu;       // Bu sınıfın Fragment'a dönüştürüldüğünü varsayıyorum
import profil.ProfilMenu; // Bu sınıfın Fragment'a dönüştürüldüğünü varsayıyorum

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Başlangıçta HisseFragment'i yükle
        if (savedInstanceState == null) {
            loadFragment(new HisseMenu());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_hisseler ) {
                selectedFragment = new HisseMenu();
            } else if (itemId == R.id.nav_ara) {
                selectedFragment = new AraMenu();
            } else if (itemId == R.id.nav_profil) {
                selectedFragment = new ProfilMenu();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
