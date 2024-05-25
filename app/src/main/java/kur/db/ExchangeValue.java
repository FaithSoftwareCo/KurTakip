package kur.db;

import kur.main.EXCHANGE_TYPES;

public class ExchangeValue {
    private int id;
    private ExchangeSourceBank bankSource = ExchangeSourceBank.YK_BANK;
    public ExchangeValueSet exchangeSet[] = new ExchangeValueSet[EXCHANGE_TYPES.NUMBER_OF_EXCHANGES];

    public String UpdateTimeStr = "";

    public ExchangeValue() {
        for (int i = 0; i < EXCHANGE_TYPES.NUMBER_OF_EXCHANGES; i++) {
            exchangeSet[i] = new ExchangeValueSet();
        }
    }

    public ExchangeValue(ExchangeSourceBank source, String EUR_Al, String EUR_Sat, String USD_Al, String USD_Sat, String XAU_Al, String XAU_Sat,
                         String exchangeRetreiveTime) {
        super();

        for (int i = 0; i < EXCHANGE_TYPES.NUMBER_OF_EXCHANGES; i++) {
            exchangeSet[i] = new ExchangeValueSet();
        }
        this.exchangeSet[EXCHANGE_TYPES.EUR].alis = EUR_Al.trim();
        this.exchangeSet[EXCHANGE_TYPES.EUR].satis = EUR_Sat.trim();
        this.exchangeSet[EXCHANGE_TYPES.USD].alis = USD_Al.trim();
        this.exchangeSet[EXCHANGE_TYPES.USD].satis = USD_Sat.trim();
        this.exchangeSet[EXCHANGE_TYPES.XAU].alis = XAU_Al.trim();
        this.exchangeSet[EXCHANGE_TYPES.XAU].satis = XAU_Sat.trim();
        this.UpdateTimeStr = exchangeRetreiveTime;
    }

    public ExchangeValue(ExchangeSourceBank bank) {
        bankSource = bank;
        for (int i = 0; i < EXCHANGE_TYPES.NUMBER_OF_EXCHANGES; i++) {
            exchangeSet[i] = new ExchangeValueSet();
        }
    }

    //getters & setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ExchangeSourceBank getBankSource() {
        return bankSource;
    }

    public void setBankSource(ExchangeSourceBank bankSource) {
        this.bankSource = bankSource;
    }

    @Override
    public String toString() {
        String retStr = "";
        retStr = (bankSource == ExchangeSourceBank.YK_BANK) ? "YK_BANK" : (bankSource == ExchangeSourceBank.KUVEYT_BANK ?"KUVEYT" : "ENPARA") + " ";
        for (int i = 0; i < EXCHANGE_TYPES.NUMBER_OF_EXCHANGES; i++) {
            retStr += " " + EXCHANGE_TYPES.ToString(i) + this.exchangeSet[i].alis + " - " + this.exchangeSet[i].satis + " ||| ";
        }

        return retStr;
    }

    public String GetTimeOfExchange() {
        return this.UpdateTimeStr;
    }

    public String GetTimeOnlyOfExchange() {
        String time = "-";
        try {
            time = this.UpdateTimeStr.split(" ")[1];
        }
        catch (Exception e){}
        return time;
    }

    public void setExchangeSetEUR(ExchangeValueSet exchangeSet) {
        this.exchangeSet[EXCHANGE_TYPES.EUR] = exchangeSet;
    }

    public void setExchangeSetUSD(ExchangeValueSet exchangeSet) {
        this.exchangeSet[EXCHANGE_TYPES.USD] = exchangeSet;
    }

    public void setExchangeSetXAU(ExchangeValueSet exchangeSet) {
        this.exchangeSet[EXCHANGE_TYPES.XAU] = exchangeSet;
    }

    public void setTimeOfExchange(String time) {
        this.UpdateTimeStr = time;
    }
}
