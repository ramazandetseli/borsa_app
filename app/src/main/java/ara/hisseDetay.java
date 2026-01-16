package ara;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.borsa_app.R;
import com.google.android.material.appbar.MaterialToolbar;

public class hisseDetay extends AppCompatActivity {

    MaterialToolbar toolbar;
    TextView TvMin,TvMax,TvVolume;
    Button btnSell,btnBuy;

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
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        symbol=getIntent().getStringExtra("symbol");
        name=getIntent().getStringExtra("name");
        toolbar.setTitle(symbol != null ? symbol : "Hisse Detay");

        // 🧪 Şimdilik fake data
        TvMin.setText("120.30");
        TvMax.setText("128.90");
        TvVolume.setText("14.2M");

        // 🔘 Butonlar
        btnBuy.setOnClickListener(v -> {
            // ileride alış ekranı
        });

        btnSell.setOnClickListener(v -> {
            // ileride satış ekranı
        });
    }
}