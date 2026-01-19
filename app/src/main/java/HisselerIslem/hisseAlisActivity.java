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
import com.google.firebase.firestore.DocumentReference;
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
                double total = lot * price;
                if(total>0){
                    stnAlBtn.setEnabled(true);
                }

                txttotal.setText("Toplam: " + String.format("%,.2f", total) + " ₺");
            }
        });


        stnAlBtn.setOnClickListener(view -> {
            int lot = Integer.parseInt(lotSayisi.getText().toString());
            double total = lot * price;

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = FirebaseAuth.getInstance().getUid();

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
                    .add(tx)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Alış başarılı", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                    );


            DocumentReference ref = db.collection("users")
                    .document(uid)
                    .collection("portfolio")
                    .document(symbol);

            ref.get().addOnSuccessListener(doc -> {

                if (doc.exists()) {
                    long oldLot = doc.getLong("lot");
                    double oldAvg = doc.getDouble("avgPrice");

                    long newLot = oldLot + lot;
                    double newAvg = ((oldLot * oldAvg) + (lot * price)) / newLot;

                    ref.update("lot", newLot, "avgPrice", newAvg);

                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("lot", lot);
                    data.put("avgPrice", price);
                    ref.set(data);
                }
            });

        });

    }
}



















