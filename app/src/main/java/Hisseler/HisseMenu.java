package Hisseler;

import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HisseMenu extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid = FirebaseAuth.getInstance().getUid();
    TextView tvBalance;


    RecyclerView recyclerView;

    ArrayList<hisseGorunum> hisseList = new ArrayList<>();
    hisseGorunumAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hisse_menu);
        MaterialToolbar toolbar = findViewById(R.id.backHissePortfoyden);



        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        tvBalance=findViewById(R.id.bakiyeHisseMenu);


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




        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new hisseGorunumAdapter(hisseList);
        recyclerView.setAdapter(adapter);






















    }
}