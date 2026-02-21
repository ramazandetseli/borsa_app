package Hisseler;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import ara.PriceService;
import ara.hisseDetay;

public class HisseMenu extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvBalance, totalKarZarar;
    private Button islemMenuGit;
    private RecyclerView recyclerView;
    private ArrayList<hisseGorunum> hisseList = new ArrayList<>();
    private hisseGorunumAdapter adapter;
    private ProgressBar progressBar;
    private Double nakit = 0.0;
    private double plusBakiye = 0.0;
    private LinearLayout bakiyeDetay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_hisse_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        islemMenuGit = view.findViewById(R.id.btnIslemMenuGit);
        totalKarZarar = view.findViewById(R.id.karZararTable);
        bakiyeDetay = view.findViewById(R.id.bakiyeDetayGor);
        MaterialToolbar toolbar = view.findViewById(R.id.backHissePortfoyden);
        progressBar = view.findViewById(R.id.progressBar);
        tvBalance = view.findViewById(R.id.bakiyeHisseMenu);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Toolbar geri butonu (Fragment olduğu için finish yerine activity'ye sorabiliriz)
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        bakiyeDetay.setOnClickListener(v -> showBottomSheet());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new hisseGorunumAdapter(hisseList);
        recyclerView.setAdapter(adapter);

        // Tıklama olaylarını bağla
        adapter.setOnHisseClickListener(hisse -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), hisseDetay.class);
                intent.putExtra("symbol", hisse.symbol);
                intent.putExtra("maliyet", hisse.ortalamaFiyat);
                intent.putExtra("lot", hisse.lotValue);
                intent.putExtra("getiri", hisse.karZarar() * hisse.lotValue);
                startActivity(intent);
            }
        });

        loadInitialData();
    }

    private void loadInitialData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        progressBar.setVisibility(View.VISIBLE);
        
        // Önce nakit bakiyeyi çek, sonra portföyü yükle
        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!isAdded()) return;
                
                if (documentSnapshot.exists()) {
                    nakit = documentSnapshot.getDouble("balance");
                    if (nakit == null) nakit = 0.0;
                }
                
                // Bakiyeyi çektikten sonra portföyü yükle
                loadPortfolio(user.getUid());
            })
            .addOnFailureListener(e -> {
                if (isAdded()) progressBar.setVisibility(View.GONE);
            });
    }

    private void loadPortfolio(String uid) {
        db.collection("users")
                .document(uid)
                .collection("portfolio")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    
                    hisseList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String symbol = doc.getId();
                        Long lotVal = doc.getLong("lot");
                        Double avg = doc.getDouble("avgPrice");
                        hisseList.add(new hisseGorunum(symbol, lotVal != null ? lotVal : 0, avg != null ? avg : 0.0, -1));
                    }
                    
                    if (hisseList.isEmpty()) {
                        updateUI();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    // Fiyatları çek
                    for (int i = 0; i < hisseList.size(); i++) {
                        int index = i;
                        hisseGorunum h = hisseList.get(i);
                        PriceService.getPrice(h.getSymbol(), new PriceService.PriceCallback() {
                            @Override
                            public void onPrice(double price) {
                                if (price > 0 && isAdded()) {
                                    h.guncelFiyat = price;
                                    adapter.notifyItemChanged(index);
                                    updateUI();
                                }
                                // Tüm fiyatlar yüklendikten veya hata aldığında progress barı kapatmak için basit bir mantık:
                                if (index == hisseList.size() - 1) {
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                            @Override public void onError(Throwable t) {
                                if (isAdded() && index == hisseList.size() - 1) progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) progressBar.setVisibility(View.GONE);
                });
    }

    private void updateUI() {
        if (!isAdded()) return;

        double toplamKarZararVal = 0;
        double toplamPortfoyDegeri = 0;

        for (hisseGorunum h : hisseList) {
            if (h.guncelFiyat > 0) {
                toplamKarZararVal += (h.guncelFiyat - h.ortalamaFiyat) * h.lotValue;
                toplamPortfoyDegeri += h.guncelFiyat * h.lotValue;
            }
        }

        plusBakiye = toplamPortfoyDegeri;
        double toplamVarlik = nakit + plusBakiye;

        NumberFormat format = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        tvBalance.setText(format.format(toplamVarlik) + " ₺");
        totalKarZarar.setText(format.format(toplamKarZararVal) + " ₺");

        if (toplamKarZararVal > 0) totalKarZarar.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        else if (toplamKarZararVal < 0) totalKarZarar.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        else totalKarZarar.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void showBottomSheet() {
        if (getContext() == null) return;
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.bottomsheet_portfoy, null);

        TextView tvTotal = view.findViewById(R.id.tvBottomTotal);
        TextView tvKarZarar = view.findViewById(R.id.tvBottomKarZarar);
        TextView tvCash = view.findViewById(R.id.tvBottomCash);

        NumberFormat format = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        tvTotal.setText("Toplam: " + format.format(plusBakiye + nakit) + " ₺");
        tvKarZarar.setText("Kar/Zarar: " + totalKarZarar.getText());
        tvCash.setText("Nakit: " + format.format(nakit) + " ₺");

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }
}
