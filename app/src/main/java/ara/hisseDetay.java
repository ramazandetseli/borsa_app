package ara;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;

import HisselerIslem.hisseAlisActivity;
import HisselerIslem.hisseSatisActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class hisseDetay extends AppCompatActivity {

    MaterialToolbar toolbar;
    TextView TvMin,TvMax,TvVolume,changePrice;
    Button btnSell,btnBuy;
    double price;
    String symbol,name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hisse_detay);


        toolbar=findViewById(R.id.backHisseDetay);
        btnBuy=findViewById(R.id.btnBuy);
        btnSell=findViewById(R.id.btnSell);
        TvMin=findViewById(R.id.tvMin);
        TvMax=findViewById(R.id.tvMax);
        TvVolume=findViewById(R.id.tvVolume);
        changePrice=findViewById(R.id.tvValue);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 🔹 intentten SADECE symbol + name al
        symbol = getIntent().getStringExtra("symbol");
        name = getIntent().getStringExtra("name");

        toolbar.setTitle(symbol != null ? symbol : "Hisse Detay");

        // 🔥🔥🔥 İŞTE ARADIĞIN YER BURASI
        FinnhubApi api = ApiClient.getClient().create(FinnhubApi.class);

        api.getQuote(symbol, "d5kb0j9r01qjaedu5jigd5kb0j9r01qjaedu5jj0")
                .enqueue(new Callback<QuoteResponse>() {
                    @Override
                    public void onResponse(Call<QuoteResponse> call,
                                           Response<QuoteResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            price = response.body().currentPrice;
                            double min = response.body().low;
                            double max = response.body().high;

                            TvMin.setText(String.valueOf(min));
                            TvMax.setText(String.valueOf(max));
                            changePrice.setText(String.valueOf(price));
                            TvVolume.setText("—");
                        }
                    }

                    @Override
                    public void onFailure(Call<QuoteResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

        // 🔘 Butonlar
        btnBuy.setOnClickListener(v -> {
            Intent intent=new Intent(this, hisseAlisActivity.class);
            intent.putExtra("symbol", symbol);
            intent.putExtra("price", price);
            startActivity(intent);
        });

        btnSell.setOnClickListener(v -> {
            Intent intent=new Intent(this, hisseSatisActivity.class);
            intent.putExtra("symbol", symbol);
            intent.putExtra("price", price);
            startActivity(intent);
        });
    }
































}