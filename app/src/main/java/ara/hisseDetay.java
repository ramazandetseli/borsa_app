package ara;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.borsa_app.R;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.Locale;

import Hisseler.hisseGorunum;
import HisselerIslem.hisseAlisActivity;
import HisselerIslem.hisseSatisActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class hisseDetay extends AppCompatActivity {

    MaterialToolbar toolbar;
    TextView TvMin,TvMax,TvVolume,changePrice,TvMaliyet,TvLot,TvGetiri;
    Button btnSell,btnBuy;
    double price;
    String symbol,name;

    WebView webView;
    TextView tvKarZararColor;

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
        TvMaliyet=findViewById(R.id.tvMaliyet);
        TvGetiri=findViewById(R.id.tvKarZarar);
        TvLot=findViewById(R.id.tvLot);
        changePrice=findViewById(R.id.tvValue);
        tvKarZararColor=findViewById(R.id.tvKarZarar);

        webView=findViewById(R.id.tradingView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 🔹 intentten SADECE symbol + name al
        symbol = getIntent().getStringExtra("symbol");
        name = getIntent().getStringExtra("name");
        double maliyet = getIntent().getDoubleExtra("maliyet", 0);
        long lot = getIntent().getLongExtra("lot", 0);
        double getiri = getIntent().getDoubleExtra("getiri", 0);


        toolbar.setTitle(symbol != null ? symbol : "Hisse Detay");
        FinnhubApi api = ApiClient.getClient().create(FinnhubApi.class);

        String html = "<html><body style=\"margin:0\">" +
                "<script type=\"text/javascript\" src=\"https://s3.tradingview.com/tv.js\"></script>" +
                "<script type=\"text/javascript\">" +
                "new TradingView.widget({" +
                "  \"symbol\": \"" + symbol + "\"," +
                "  \"interval\": \"D\"," +
                "  \"theme\": \"light\"," +
                "  \"style\": \"1\"," +
                "  \"locale\": \"tr\"," +
                "  \"width\": \"100%\"," +
                "  \"height\": 300" +
                "});" +
                "</script></body></html>";

        webView.loadDataWithBaseURL(
                "https://www.tradingview.com",
                html,
                "text/html",
                "UTF-8",
                null
        );


        // 🔥🔥🔥 İŞTE ARADIĞIN YER BURASI

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
                            TvLot.setText(String.valueOf(lot));
                            TvMaliyet.setText(String.format("%.2f ₺", maliyet));
                            NumberFormat format = NumberFormat.getNumberInstance(new Locale("tr", "TR"));
                            format.setMinimumFractionDigits(2);
                            format.setMaximumFractionDigits(2);

                            TvGetiri.setText("Getiri: "+format.format(getiri) + " ₺");
                            if (getiri > 0)
                                TvGetiri.setTextColor(getColor(android.R.color.holo_green_dark));
                            else if (getiri < 0)
                                TvGetiri.setTextColor(getColor(android.R.color.holo_red_dark));
                            else
                                TvGetiri.setTextColor(getColor(android.R.color.darker_gray));

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































