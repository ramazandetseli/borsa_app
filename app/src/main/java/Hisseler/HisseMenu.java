package Hisseler;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import HisselerIslem.hisseAlisActivity;
import ara.AraMenu;
import ara.FinnhubApi;
import ara.PriceService;
import ara.hisseAdapter;
import ara.hisseDetay;

public class HisseMenu extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid = FirebaseAuth.getInstance().getUid();
    TextView tvBalance;

    Button islemMenuGit;
    RecyclerView recyclerView;

    ArrayList<hisseGorunum> hisseList = new ArrayList<>();
    hisseGorunumAdapter adapter;

    ProgressBar progressBar;

    TextView totalKarZarar;
    public double plusBakiye = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hisse_menu);
        islemMenuGit=findViewById(R.id.btnIslemMenuGit);
        totalKarZarar=findViewById(R.id.karZararTable);

        MaterialToolbar toolbar = findViewById(R.id.backHissePortfoyden);

        islemMenuGit.setOnClickListener(view -> {
            Intent intent=new Intent(this, AraMenu.class);
            startActivity(intent);
        });

        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);


        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        tvBalance=findViewById(R.id.bakiyeHisseMenu);




        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new hisseGorunumAdapter(hisseList);
        recyclerView.setAdapter(adapter);


        loadPortfolio();
    }
    private void loadPortfolio() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid2 = user.getUid();

        db.collection("users")
                .document(uid2)
                .collection("portfolio")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    hisseList.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String symbol = doc.getId();
                        Long lotVal = doc.getLong("lot");
                        Double avg = doc.getDouble("avgPrice");
                        double guncelFiyat = -1;

                        long lot = lotVal != null ? lotVal : 0;
                        double avgPrice = avg != null ? avg : 0.0;

                        hisseList.add(
                                new hisseGorunum(symbol, lot, avgPrice,guncelFiyat)
                        );
                    }
                    for (int i = 0; i < hisseList.size(); i++) {
                        int index = i;
                        hisseGorunum h = hisseList.get(i);

                        PriceService.getPrice(h.getSymbol(), new PriceService.PriceCallback() {
                            @Override
                            public void onPrice(double price) {

                                // ❗ 0 veya negatif gelirse overwrite ETME
                                if (price > 0) {
                                    h.guncelFiyat = price;
                                    adapter.notifyItemChanged(index);

                                    hesaplaKarZarar();
                                    portfoyDegeri();
                                    bakiye(); // 🔥 UI güncelle
                                }

                            }

                            @Override
                            public void onError(Throwable t) {
                                // sessiz geç
                            }
                        });
                    }

                    adapter = new hisseGorunumAdapter(hisseList);
                    recyclerView.setAdapter(adapter);

                    adapter.setOnHisseClickListener(hisse -> {
                        Intent intent = new Intent(HisseMenu.this, hisseDetay.class);
                        intent.putExtra("symbol", hisse.symbol);
                        intent.putExtra("maliyet", hisse.ortalamaFiyat);
                        intent.putExtra("lot",hisse.lotValue);
                        intent.putExtra("getiri",hisse.karZarar()*hisse.lotValue);
                        startActivity(intent);
                    });

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);



                });


    }
    private void bakiye(){
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {

                    if(!documentSnapshot.exists()) return;

                    Double nakit = documentSnapshot.getDouble("balance");
                    if (nakit == null) nakit = 0.0;

                    double toplamVarlik = nakit + plusBakiye;

                    NumberFormat format = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
                    format.setMinimumFractionDigits(2);
                    format.setMaximumFractionDigits(2);

                    tvBalance.setText(format.format(toplamVarlik) + " ₺");
                });
    }


    private void hesaplaKarZarar(){
    double toplam = 0;
    for (hisseGorunum h : hisseList) {
        if (h.guncelFiyat > 0) {
            toplam += (h.guncelFiyat - h.ortalamaFiyat) * h.lotValue;
        }
    }
    NumberFormat format = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(2);

    totalKarZarar.setText(format.format(toplam) + " ₺");


}

    private double portfoyDegeri(){
        double toplam = 0;

        for (hisseGorunum h : hisseList) {
            if (h.guncelFiyat > 0) {
                toplam += h.guncelFiyat * h.lotValue;
            }
        }

        plusBakiye = toplam;
        return toplam;
    }


}