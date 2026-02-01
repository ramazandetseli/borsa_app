package profil;

import com.google.firebase.Timestamp;

public class Transaction {
    private String type;
    private String symbol;
    private double price;
    private long lot;
    private double total;
    private Timestamp createdAt;

    public Transaction() {}

    public Transaction(String type, String symbol, double price, long lot, double total, Timestamp createdAt) {
        this.type = type;
        this.symbol = symbol;
        this.price = price;
        this.lot = lot;
        this.total = total;
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getLot() {
        return lot;
    }

    public void setLot(long lot) {
        this.lot = lot;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
