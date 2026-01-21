package com.example.borsa_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class kayitOlActivity extends AppCompatActivity {

    EditText isim,soyisim,email,sifre,sifrekontrol;
    Button btnKayitOl;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_kayit_ol);
        isim=findViewById(R.id.editTextText);
        soyisim=findViewById(R.id.editTextText2);
        email=findViewById(R.id.editTextTextEmailAddress);
        sifre=findViewById(R.id.editTextTextPassword3);
        sifrekontrol=findViewById(R.id.editTextTextPassword4);
        btnKayitOl=findViewById(R.id.btnKayitOl);
        MaterialToolbar toolbar= findViewById(R.id.backKayitOl);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            finish(); // geri dön
        });
        auth = FirebaseAuth.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnKayitOl.setOnClickListener(view -> {

            String Isim = isim.getText().toString().trim();
            String Soyisim = soyisim.getText().toString().trim();
            String Email = email.getText().toString().trim();
            String Sifre = sifre.getText().toString().trim();
            String SifreTekrar = sifrekontrol.getText().toString().trim();

            if (!Sifre.equals(SifreTekrar)) {
                Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show();
                return;
            }

            if (Isim.isEmpty() || Soyisim.isEmpty() || Email.isEmpty() || Sifre.isEmpty()) {
                Toast.makeText(this, "Alanlar boş", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(Email, Sifre)
                    .addOnSuccessListener(authResult -> {

                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) return;

                        String uid = user.getUid();

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("isim", Isim);
                        userData.put("soyisim", Soyisim);
                        userData.put("email", Email);
                        userData.put("balance", 0.0);
                        userData.put("createdAt", FieldValue.serverTimestamp());

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {

                                    Toast.makeText(this,
                                            "Kayıt başarılı, giriş yapabilirsiniz",
                                            Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(this, GirisActivity.class));
                                    finish();
                                });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });










    }
}