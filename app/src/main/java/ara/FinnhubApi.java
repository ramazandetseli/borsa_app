package ara;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FinnhubApi {
    @GET("api/v1/stock/symbol")
    Call<List<Hisseler.hisseGorunum>> getStocks(
            @Query("exchange") String exchange,
            @Query("token") String apiKey
    );


    @GET("api/v1/quote")
    Call<QuoteResponse> getQuote(
            @Query("symbol") String symbol,
            @Query("token") String apiKey
    );

}
