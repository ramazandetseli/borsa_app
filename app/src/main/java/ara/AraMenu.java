package ara;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AraMenu extends AppCompatActivity {

    EditText etSearch;
    RecyclerView rvList;
    hisseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ara_menu);

        MaterialToolbar toolbar = findViewById(R.id.backMenuAramadan);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etSearch);
        rvList = findViewById(R.id.rvList);
        rvList.setLayoutManager(new LinearLayoutManager(this));

        FinnhubApi api = ApiClient.getClient().create(FinnhubApi.class);

        api.getStocks("US", "d5kb0j9r01qjaedu5jigd5kb0j9r01qjaedu5jj0")
                .enqueue(new Callback<List<hisseModel>>() {
                    @Override
                    public void onResponse(Call<List<hisseModel>> call,
                                           Response<List<hisseModel>> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            ArrayList<hisseModel> liste = new ArrayList<>();
                            for (hisseModel h : response.body()) {
                                liste.add(new hisseModel(h.symbol, h.name));
                            }

                            adapter = new hisseAdapter(liste);
                            rvList.setAdapter(adapter);

                            etSearch.addTextChangedListener(new TextWatcher() {
                                @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
                                @Override public void afterTextChanged(Editable s){}

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    adapter.filter(s.toString());
                                }
                            });
                        }
                        //tıklanma olayı
                        adapter.setOnHisseClickListener(hisse -> {
                            Intent intent = new Intent(AraMenu.this, hisseDetay.class);
                            intent.putExtra("symbol", hisse.symbol);
                            intent.putExtra("name", hisse.name);
                            startActivity(intent);
                        });

                    }

                    @Override
                    public void onFailure(Call<List<hisseModel>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });


    }
}
