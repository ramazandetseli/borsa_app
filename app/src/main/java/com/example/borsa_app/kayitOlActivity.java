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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;

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


    btnKayitOl.setOnClickListener(view -> {
            String Isim=isim.getText().toString().trim();
            String Soyisim=soyisim.getText().toString().trim();
            String Email=email.getText().toString().trim();
            String Sifre=sifre.getText().toString().trim();
            String SifreTekrar=sifrekontrol.getText().toString().trim();

        if(!Sifre.equals(SifreTekrar)){
            Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show();
            return;
        }
            if(Isim.isEmpty() || Soyisim.isEmpty() || Email.isEmpty() || Sifre.isEmpty()){
                Toast.makeText(this, "Alanlar boş", Toast.LENGTH_SHORT).show();
                return;
            }



        auth.createUserWithEmailAndPassword(Email, Sifre)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Kullanıcı oluşturulamadı", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    DatabaseReference ref = FirebaseDatabase
                            .getInstance()
                            .getReference("users")
                            .child(uid);

                    ref.child("isim").setValue(Isim);
                    ref.child("soyisim").setValue(Soyisim);
                    ref.child("email").setValue(Email)
                            .addOnSuccessListener(unused -> {
                                startActivity(new Intent(this, GirisActivity.class));
                                finish();
                                Toast.makeText(this, "Kayıt başarıyla oluşturuldu\nGiriş yapabilirsiniz", Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );


    });








    }
}