package Hisseler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import ara.AraMenu;

import ara.PriceService;
import ara.hisseDetay;

@SuppressLint("ResourceAsColor")
public class HisseMenu extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid = FirebaseAuth.getInstance().getUid();
    TextView tvBalance;

    Button islemMenuGit;
    RecyclerView recyclerView;

    ArrayList<hisseGorunum> hisseList = new ArrayList<>();
    hisseGorunumAdapter adapter;

    ProgressBar progressBar;

    private Double nakit;
    TextView totalKarZarar;
    private double plusBakiye = 0.0;
    LinearLayout bakiyeDetay;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hisse_menu);
        islemMenuGit=findViewById(R.id.btnIslemMenuGit);
        totalKarZarar=findViewById(R.id.karZararTable);
        bakiyeDetay=findViewById(R.id.bakiyeDetayGor);
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



        bakiyeDetay.setOnClickListener(view -> {
            showBottomSheet();
        });


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

                                // 0 veya negatif gelirse overwrite ETME
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

                    nakit = documentSnapshot.getDouble("balance");
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
        if (toplam > 0) {
            totalKarZarar.setTextColor(
                    getResources().getColor(android.R.color.holo_green_dark)
            );

        } else if (toplam < 0) {
            totalKarZarar.setTextColor(
                    getResources().getColor(android.R.color.holo_red_dark)
            );

        } else {
            totalKarZarar.setTextColor(
                    getResources().getColor(android.R.color.darker_gray)
            );
        }



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
    private void showBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_portfoy, null);

        TextView tvTotal = view.findViewById(R.id.tvBottomTotal);
        TextView tvKarZarar = view.findViewById(R.id.tvBottomKarZarar);
        TextView tvCash = view.findViewById(R.id.tvBottomCash);

        NumberFormat format = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        tvTotal.setText("Toplam: " + format.format(plusBakiye+nakit) + " ₺");
        tvKarZarar.setText("Kar/Zarar: " + totalKarZarar.getText());
        tvCash.setText("Nakit: " + format.format(nakit) + " ₺");

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }


}