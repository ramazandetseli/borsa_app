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

    TextView hisseName, value, txttotal,txtBakiye;
    EditText lotSayisi;

    Button stnAlBtn;

    public double total;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hisse_alis);

        hisseName = findViewById(R.id.txtHisseAdi);
        value = findViewById(R.id.txtFiyat);
        lotSayisi = findViewById(R.id.edtLot);
        txttotal = findViewById(R.id.txtToplam);
        txtBakiye=findViewById(R.id.bakiyeTxt);
        stnAlBtn = findViewById(R.id.btnBuy);
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
                try {
                    int lot = Integer.parseInt(s.toString());
                    total = lot * price;
                    if(total>0){
                        stnAlBtn.setEnabled(true);
                    } else {
                        stnAlBtn.setEnabled(false);
                    }
                    txttotal.setText("Toplam: " + String.format("%,.2f", total) + " ₺");
                } catch (NumberFormatException e) {
                    txttotal.setText("Toplam: 0 ₺");
                    stnAlBtn.setEnabled(false);
                }
            }
        });
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        Double balance = documentSnapshot.getDouble("balance");

                        if (balance == null) balance = 0.0;

                        txtBakiye.setText("Bakiye: " + String.format("%,.2f", balance) + " ₺");
                    })
                    .addOnFailureListener(e -> {
                        txtBakiye.setText("Bakiye alınamadı");
                    });
        }

        stnAlBtn.setOnClickListener(view -> {

            if (user == null) {
                Toast.makeText(this, "Lütfen giriş yapın", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            int lotToBuy;
            try {
                lotToBuy = Integer.parseInt(lotSayisi.getText().toString());
                if (lotToBuy <= 0) {
                    Toast.makeText(this, "Lütfen geçerli bir lot sayısı girin", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Lütfen geçerli bir lot sayısı girin", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalCost = lotToBuy * price;

            DocumentReference userRef = db.collection("users").document(uid);
            DocumentReference portfolioRef = userRef.collection("portfolio").document(symbol);

            db.runTransaction(transaction -> {
                DocumentSnapshot userSnapshot = transaction.get(userRef);
                DocumentSnapshot portfolioSnapshot = transaction.get(portfolioRef);

                Double currentBalance = userSnapshot.getDouble("balance");
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }

                if (currentBalance < totalCost) {
                    throw new RuntimeException("Yetersiz bakiye. Mevcut bakiye: " + String.format("%,.2f", currentBalance) + " ₺");
                }

                long newLot;
                double newAvgPrice;

                if (portfolioSnapshot.exists()) {
                    long oldLot = portfolioSnapshot.getLong("lot");
                    double oldAvgPrice = portfolioSnapshot.getDouble("avgPrice");
                    newLot = oldLot + lotToBuy;
                    newAvgPrice = ((oldLot * oldAvgPrice) + (lotToBuy * price)) / newLot;
                } else {
                    newLot = lotToBuy;
                    newAvgPrice = price;
                }

                Map<String, Object> portfolioData = new HashMap<>();
                portfolioData.put("lot", newLot);
                portfolioData.put("avgPrice", newAvgPrice);
                transaction.set(portfolioRef, portfolioData);

                double newBalance = currentBalance - totalCost;
                transaction.update(userRef, "balance", newBalance);

                return null;
            }).addOnSuccessListener(aVoid -> {
                Map<String, Object> tx = new HashMap<>();
                tx.put("symbol", symbol);
                tx.put("type", "BUY");
                tx.put("lot", lotToBuy);
                tx.put("price", price);
                tx.put("total", totalCost);
                tx.put("createdAt", FieldValue.serverTimestamp());

                db.collection("users")
                        .document(uid)
                        .collection("transactions")
                        .add(tx)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(hisseAlisActivity.this, "Alış başarılı", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(hisseAlisActivity.this, "Alış başarılı ancak işlem kaydı oluşturulamadı.", Toast.LENGTH_LONG).show();
                            finish();
                        });
            }).addOnFailureListener(e -> {
                Toast.makeText(hisseAlisActivity.this, "Alış işlemi başarısız: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
    }
}

