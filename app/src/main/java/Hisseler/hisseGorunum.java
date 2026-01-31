package Hisseler;

import com.google.gson.annotations.SerializedName;

public class hisseGorunum {

    @SerializedName("symbol")
    public String symbol;
    @SerializedName("description")
    public String name;
    public long  lotValue;
    public double ortalamaFiyat;

    public  double guncelFiyat;

    public hisseGorunum(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public hisseGorunum(String symbol, long lotValue, double ortalamaFiyat, double guncelFiyat) {
        this.symbol = symbol;
        this.lotValue = lotValue;
        this.ortalamaFiyat = ortalamaFiyat;
        this.guncelFiyat = guncelFiyat;
    }

    public long getLotValue() {
        return lotValue;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getOrtalamaFiyat() {
        return ortalamaFiyat;
    }

    public double karZarar(){
        return guncelFiyat-ortalamaFiyat;
    }
}
