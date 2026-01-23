package Hisseler;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HisseMenu extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid = FirebaseAuth.getInstance().getUid();
    TextView tvBalance;

    Button islemMenuGit;
    RecyclerView recyclerView;

    ArrayList<hisseGorunum> hisseList = new ArrayList<>();
    hisseGorunumAdapter adapter;

    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hisse_menu);
        islemMenuGit=findViewById(R.id.btnIslemMenuGit);
        MaterialToolbar toolbar = findViewById(R.id.backHissePortfoyden);

        islemMenuGit.setOnClickListener(view -> {
            Intent intent=new Intent(this,ara.AraMenu.class);
            startActivity(intent);
        });

        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);


        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        tvBalance=findViewById(R.id.bakiyeHisseMenu);

        ImageButton btnRefresh=findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(view -> {
            bakiye();
            loadPortfolio();
        });




        bakiye();


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
                        long lot = doc.getLong("lot");
                        double avgPrice = doc.getDouble("avgPrice");

                        hisseList.add(

                                new hisseGorunum(symbol, lot, avgPrice)
                        );
                    }

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void bakiye(){
        progressBar.setVisibility(View.GONE);
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                Double balance = documentSnapshot.getDouble("balance");

                if (balance != null) {
                    NumberFormat format = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
                    format.setMinimumFractionDigits(2);
                    format.setMaximumFractionDigits(2);

                    tvBalance.setText(format.format(balance) + " ₺");
                }
            }
        });
    }



}