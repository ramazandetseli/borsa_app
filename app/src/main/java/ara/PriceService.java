package ara;

import com.example.borsa_app.BuildConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceService {

    private static final FinnhubApi api =
            ApiClient.getClient().create(FinnhubApi.class);

    public interface PriceCallback {
        void onPrice(double price);
        void onError(Throwable t);
    }


    public static void getPrice(String symbol, PriceCallback callback) {

        api.getQuote(symbol, BuildConfig.FINNHUB_API_KEY)
                .enqueue(new Callback<QuoteResponse>() {

                    @Override
                    public void onResponse(Call<QuoteResponse> call,
                                           Response<QuoteResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            callback.onPrice(response.body().currentPrice);
                        } else {
                            callback.onError(
                                    new Exception("API response error"));
                        }
                    }

                    @Override
                    public void onFailure(Call<QuoteResponse> call, Throwable t) {
                        callback.onError(t);
                    }
                });
    }
}
