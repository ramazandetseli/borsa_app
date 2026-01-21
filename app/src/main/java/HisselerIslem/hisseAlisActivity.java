package HisselerIslem;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class hisseAlisActivity extends AppCompatActivity {

    String symbol;
    double price;

    TextView hisseName, value, txttotal;
    EditText lotSayisi;

    Button stnAlBtn;

    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hisse_alis);

        hisseName = findViewById(R.id.txtHisseAdi);
        value = findViewById(R.id.txtFiyat);
        lotSayisi = findViewById(R.id.edtLot);
        txttotal = findViewById(R.id.txtToplam);
        stnAlBtn=findViewById(R.id.btnBuy);
        stnAlBtn.setEnabled(false);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAlis);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Intent verileri
        symbol = getIntent().getStringExtra("symbol");
        price = getIntent().getDoubleExtra("price", 0.0);

        hisseName.setText(symbol);
        value.setText(price + " ₺");

        lotSayisi.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == 0) {
                    txttotal.setText("Toplam: 0 ₺");
                    stnAlBtn.setEnabled(false);
                    return;
                }

                int lot = Integer.parseInt(s.toString());
                total = lot * price;
                if(total>0){
                    stnAlBtn.setEnabled(true);
                }

                txttotal.setText("Toplam: " + String.format("%,.2f", total) + " ₺");
            }
        });


        stnAlBtn.setOnClickListener(view -> {

                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                if(user==null) return;

                String uid=user.getUid();
                FirebaseFirestore db=FirebaseFirestore.getInstance();

                DocumentReference userRef=db.collection("users").document(uid);

                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot=transaction.get(userRef);

                    Double balance=snapshot.getDouble("balance");
                    if(balance==null) balance=0.0;

                    if(balance< total){
                        throw new RuntimeException("Yetersiz Bakiye");
                    }

                    transaction.update(userRef, "balance", balance - total);

                    return null;

                }).addOnSuccessListener(unused ->{
                    int lot = Integer.parseInt(lotSayisi.getText().toString());
                    double total = lot * price;

                    if (user == null) {
                        Toast.makeText(this, "Lütfen giriş yapın", Toast.LENGTH_SHORT).show();
                        return;
                    }



                    Map<String, Object> tx = new HashMap<>();
                    tx.put("symbol", symbol);
                    tx.put("type", "BUY");
                    tx.put("lot", lot);
                    tx.put("price", price);
                    tx.put("total", total);
                    tx.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("users")
                            .document(uid)
                            .collection("transactions")
                            .add(tx);

                    // 2️⃣ PORTFÖY (ATOMIC)
                    DocumentReference ref = db.collection("users")
                            .document(uid)
                            .collection("portfolio")
                            .document(symbol);
                    db.runTransaction(transaction -> {
                        DocumentSnapshot snap = transaction.get(ref);

                        long newLot;
                        double newAvg;

                        if (snap.exists()) {
                            long oldLot = snap.getLong("lot");
                            double oldAvg = snap.getDouble("avgPrice");

                            newLot = oldLot + lot;
                            newAvg = ((oldLot * oldAvg) + (lot * price)) / newLot;
                        } else {
                            newLot = lot;
                            newAvg = price;
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("lot", newLot);
                        data.put("avgPrice", newAvg);

                        transaction.set(ref, data);
                        return null;
                    }).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Alış başarılı", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                });





        });

    }
}



















