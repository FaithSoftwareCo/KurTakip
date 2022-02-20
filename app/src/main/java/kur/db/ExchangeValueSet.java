package kur.db;

public class ExchangeValueSet {
    public int exchangeType;
    public String alis;
    public String satis;

    public ExchangeValueSet() {
        // TODO Auto-generated constructor stub
    }

    public ExchangeValueSet(int type, String alis, String satis) {
        this.exchangeType = type;
        this.alis = alis;
        this.satis = satis;
    }
};
