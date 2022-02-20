package kur.main;

public class EXCHANGE_TYPES {
    public static final int EUR = 0;
    public static final int USD = 1;
    public static final int XAU = 2;

    public static final int NUMBER_OF_EXCHANGES = 3;

    public static String ToString(int i) {
        switch (i) {
            case EUR:
                return "EUR";
            case USD:
                return "USD";
            case XAU:
                return "XAU";
            default:
                break;
        }

        return "";
    }

}
