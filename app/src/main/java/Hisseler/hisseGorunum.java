package Hisseler;

public class hisseGorunum {

    public String symbol;
    public long  lotValue;
    public double ortalamaFiyat;

    public  double guncelFiyat;


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
