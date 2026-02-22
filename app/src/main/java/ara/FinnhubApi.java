package ara;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FinnhubApi {
    @GET("stock/symbol")
    Call<List<Hisseler.hisseGorunum>> getStocks(
            @Query("exchange") String exchange,
            @Query("token") String apiKey
    );


    @GET("quote")
    Call<QuoteResponse> getQuote(
            @Query("symbol") String symbol,
            @Query("token") String apiKey
    );


}
