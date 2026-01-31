package ara;

import com.google.gson.annotations.SerializedName;

public class QuoteResponse {
    @SerializedName("c")
    public double currentPrice;
    @SerializedName("h")
    public double high;

    @SerializedName("l")
    public double low;


}

