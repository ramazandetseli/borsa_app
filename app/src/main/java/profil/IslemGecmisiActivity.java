package profil;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class IslemGecmisiActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvNoTransactions;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_islem_gecmisi);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbarIslemGecmisi);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_transactions);
        tvNoTransactions = findViewById(R.id.tv_no_transactions);
        progressBar = findViewById(R.id.progress_bar);

        transactionList = new ArrayList<>();
        adapter = new TransactionAdapter(this, transactionList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadTransactions();
    }

    private void loadTransactions() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Lütfen giriş yapın.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        progressBar.setVisibility(View.VISIBLE);
        tvNoTransactions.setVisibility(View.GONE);

        db.collection("users").document(uid).collection("transactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        transactionList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            transactionList.add(transaction);
                        }
                        adapter.notifyDataSetChanged();

                        if (transactionList.isEmpty()) {
                            tvNoTransactions.setVisibility(View.VISIBLE);
                        } else {
                            tvNoTransactions.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(IslemGecmisiActivity.this, "İşlemler yüklenemedi.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
