package ara;

import com.google.gson.annotations.SerializedName;

public class hisseModel {
    @SerializedName("symbol")
    public String symbol;
    @SerializedName("description")
    public String name;


    public hisseModel(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
