package ara;

import com.example.borsa_app.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import Hisseler.hisseGorunum;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AraMenu extends Fragment {

    EditText etSearch;
    RecyclerView rvList;
    hisseAdapter adapter;
    ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_ara_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        MaterialToolbar toolbar = view.findViewById(R.id.backMenuAramadan);
        // Fragment içinde toolbar ayarı
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        etSearch = view.findViewById(R.id.etSearch);
        rvList = view.findViewById(R.id.rvList);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));

        FinnhubApi api = ApiClient.getClient().create(FinnhubApi.class);

        api.getStocks("US", "d5kb0j9r01qjaedu5jigd5kb0j9r01qjaedu5jj0")
                .enqueue(new Callback<List<Hisseler.hisseGorunum>>() {
                    @Override
                    public void onResponse(Call<List<Hisseler.hisseGorunum>> call,
                                           Response<List<Hisseler.hisseGorunum>> response) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            ArrayList<Hisseler.hisseGorunum> liste = new ArrayList<>();
                            for (Hisseler.hisseGorunum h : response.body()) {
                                liste.add(new Hisseler.hisseGorunum(h.symbol, h.name));
                            }

                            adapter = new hisseAdapter(liste);
                            rvList.setAdapter(adapter);

                            adapter.setOnHisseClickListener(hisse -> {
                                Intent intent = new Intent(getContext(), hisseDetay.class);
                                intent.putExtra("symbol", hisse.symbol);
                                intent.putExtra("name", hisse.name);
                                startActivity(intent);
                            });

                            etSearch.addTextChangedListener(new TextWatcher() {
                                @Override public void beforeTextChanged(CharSequence s,int a,int b,int c){}
                                @Override public void afterTextChanged(Editable s){}

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    adapter.filter(s.toString());
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<hisseGorunum>> call, Throwable t) {
                        if (isAdded()) progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
