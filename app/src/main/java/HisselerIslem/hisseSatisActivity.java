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

public class hisseSatisActivity extends AppCompatActivity {

    String symbol;
    double price;

    TextView hisseName, value, txttotal;
    EditText lotSayisi;

    Button stnSatBtn;

    public double total;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hisse_satis);

        hisseName = findViewById(R.id.txtHisseAdi);
        value = findViewById(R.id.txtFiyat);
        lotSayisi = findViewById(R.id.edtLot);
        txttotal = findViewById(R.id.txtToplam);
        stnSatBtn=findViewById(R.id.btnSell);
        stnSatBtn.setEnabled(false);
        MaterialToolbar toolbar = findViewById(R.id.toolbarSatis);
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
                    stnSatBtn.setEnabled(false);
                    return;
                }
                try {
                    int lot = Integer.parseInt(s.toString());
                    total = lot * price;
                    if(total>0){
                        stnSatBtn.setEnabled(true);
                    } else {
                        stnSatBtn.setEnabled(false);
                    }
                    txttotal.setText("Toplam: " + String.format("%,.2f", total) + " ₺");
                } catch (NumberFormatException e) {
                    txttotal.setText("Toplam: 0 ₺");
                    stnSatBtn.setEnabled(false);
                }
            }
        });

        stnSatBtn.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Lütfen giriş yapın", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            int lotToSell;
            try {
                lotToSell = Integer.parseInt(lotSayisi.getText().toString());
                if (lotToSell <= 0) {
                    Toast.makeText(this, "Lütfen geçerli bir lot sayısı girin", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Lütfen geçerli bir lot sayısı girin", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalSaleValue = lotToSell * price;

            DocumentReference userRef = db.collection("users").document(uid);
            DocumentReference portfolioRef = userRef.collection("portfolio").document(symbol);

            db.runTransaction(transaction -> {
                DocumentSnapshot userSnapshot = transaction.get(userRef);
                DocumentSnapshot portfolioSnapshot = transaction.get(portfolioRef);

                if (!portfolioSnapshot.exists()) {
                    throw new RuntimeException("Satılacak hisse portföyünüzde bulunmuyor.");
                }

                Long currentLotVal = portfolioSnapshot.getLong("lot");
                if (currentLotVal == null) {
                    throw new RuntimeException("Portföydeki hisse lot bilgisi bozuk.");
                }
                long currentLot = currentLotVal;

                if (currentLot < lotToSell) {
                    throw new RuntimeException("Yetersiz lot. Sahip olduğunuz lot: " + currentLot);
                }

                Double currentBalance = userSnapshot.getDouble("balance");
                if (currentBalance == null) {
                    currentBalance = 0.0;
                }

                long newLot = currentLot - lotToSell;
                double newBalance = currentBalance + totalSaleValue;

                if (newLot == 0) {
                    transaction.delete(portfolioRef);
                } else {
                    transaction.update(portfolioRef, "lot", newLot);
                }
                
                transaction.update(userRef, "balance", newBalance);

                return null;
            }).addOnSuccessListener(aVoid -> {
                Map<String, Object> tx = new HashMap<>();
                tx.put("symbol", symbol);
                tx.put("type", "SELL");
                tx.put("lot", lotToSell);
                tx.put("price", price);
                tx.put("total", totalSaleValue);
                tx.put("createdAt", FieldValue.serverTimestamp());

                db.collection("users")
                        .document(uid)
                        .collection("transactions")
                        .add(tx)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(hisseSatisActivity.this, "Satış başarılı", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(hisseSatisActivity.this, "Satış başarılı ancak işlem kaydı oluşturulamadı.", Toast.LENGTH_LONG).show();
                            finish();
                        });
            }).addOnFailureListener(e -> {
                Toast.makeText(hisseSatisActivity.this, "Satış işlemi başarısız: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
    }
}
