package kur.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import kur.main.EXCHANGE_TYPES;


public class ExchangeValsDB extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 3;
    // Database Name
    private static final String DATABASE_NAME = "ExchangeDB";
    // Books table name
    private static final String TABLE_EXCHANGES = "exchanges";

    // Exchanges Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_BANK = "bank";
    private static final String KEY_EUR_AL = "EUR_Al";
    private static final String KEY_EUR_SAT = "EUR_Sat";
    private static final String KEY_USD_AL = "USD_Al";
    private static final String KEY_USD_SAT = "USD_Sat";
    private static final String KEY_XAU_AL = "XAU_Al";
    private static final String KEY_XAU_SAT = "XAU_Sat";
    private static final String KEY_EXCHANGE_TIME = "exchangeTime";

    private static final String[] COLUMNS = {KEY_ID, KEY_BANK, KEY_EUR_AL, KEY_EUR_SAT, KEY_USD_AL, KEY_USD_SAT, KEY_XAU_AL, KEY_XAU_SAT, KEY_EXCHANGE_TIME};
    private static ExchangeValsDB exchangeValsDBInstance = null;

    public ExchangeValsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void SetInstance(ExchangeValsDB exchangeValsDB) {
        exchangeValsDBInstance = exchangeValsDB;
    }

    public void addExchangeValuetoDB(ExchangeValue exchangeValue) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_BANK, exchangeValue.getBankSource().name());

        values.put(KEY_EUR_AL, exchangeValue.exchangeSet[EXCHANGE_TYPES.EUR].alis);
        values.put(KEY_EUR_SAT, exchangeValue.exchangeSet[EXCHANGE_TYPES.EUR].satis);
        values.put(KEY_USD_AL, exchangeValue.exchangeSet[EXCHANGE_TYPES.USD].alis);
        values.put(KEY_USD_SAT, exchangeValue.exchangeSet[EXCHANGE_TYPES.USD].satis);
        values.put(KEY_XAU_AL, exchangeValue.exchangeSet[EXCHANGE_TYPES.XAU].alis);
        values.put(KEY_XAU_SAT, exchangeValue.exchangeSet[EXCHANGE_TYPES.XAU].satis);

        if (exchangeValue.getBankSource() == ExchangeSourceBank.YK_BANK &&
                exchangeValue.UpdateTimeStr.matches("\\d{2}.\\d{2}.\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
            // Convert time format 31.07.2014 13:22:00 YKB
            // To format 2014-07-31 13:22:00

            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date;
            try {
                date = inputFormat.parse(exchangeValue.UpdateTimeStr);
                String outputDateStr = outputFormat.format(date);

                values.put(KEY_EXCHANGE_TIME, outputDateStr);

                // 3. insert
                db.insert(TABLE_EXCHANGES, // exchange
                        null, //nullColumnHack
                        values); // key/value -> keys = column names/ values = column values

            } catch (ParseException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
        } else if (exchangeValue.getBankSource() == ExchangeSourceBank.ENPARA_BANK &&
                exchangeValue.UpdateTimeStr.matches("\\d{2}.\\d{2}.\\d{4}-\\d{2}:\\d{2}")) {
            // 01.08.2015-23:55 ENPARA

            SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date date;
            try {
                date = inputFormat.parse(exchangeValue.UpdateTimeStr);
                String outputDateStr = outputFormat.format(date);

                values.put(KEY_EXCHANGE_TIME, outputDateStr);

                // 3. insert
                db.insert(TABLE_EXCHANGES, // exchange
                        null, //nullColumnHack
                        values); // key/value -> keys = column names/ values = column values

            } catch (ParseException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
        } else {
            Log.d("EXCHANGE", "Add Exchange value to DB is failed for>" + exchangeValue.getBankSource().name());
        }

        // 4. close
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create exchange table
        String CREATE_BOOK_TABLE = "CREATE TABLE exchanges ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "BANK TEXT," +
                "EUR_Al TEXT, " +
                "EUR_Sat TEXT, " +
                "USD_Al TEXT, " +
                "USD_Sat TEXT, " +
                "XAU_Al TEXT, " +
                "XAU_Sat TEXT, " +
                "exchangeTime TEXT, UNIQUE(BANK, EUR_Al, EUR_Sat, USD_Al, USD_Sat, XAU_Al, XAU_Sat, exchangeTime))";

        // create books table
        db.execSQL(CREATE_BOOK_TABLE);
    }

    public String GetLastExchangeString(ExchangeSourceBank bank, int exchangeType) {
        String returnExchanges = "";
        List<ExchangeValue> lastRecords = getLastExchanges(bank, 5);

        for (int i = 0; i < lastRecords.size(); i++) {
            returnExchanges += lastRecords.get(i).UpdateTimeStr + " -> " + lastRecords.get(i).exchangeSet[exchangeType].alis + "\t" + lastRecords.get(i).exchangeSet[exchangeType].satis + "\n";
        }

        return returnExchanges;
    }

    // Get All Books
    public List<ExchangeValue> getLastExchanges(ExchangeSourceBank bank, int num) {
        List<ExchangeValue> exchanges = new LinkedList<ExchangeValue>();

        //31.07.2014 13:22:00
        //datetime(substr(col, 7, 4) || '-' || substr(col, 4, 2) || '-' || substr(col, 1, 2))

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_EXCHANGES + " WHERE BANK='" + bank.name() + "' ORDER BY datetime(" + KEY_EXCHANGE_TIME + ") DESC LIMIT " + num;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        ExchangeValue exchangeValue = null;
        if (cursor.moveToFirst()) {
            do {
                exchangeValue = new ExchangeValue();
                exchangeValue.setId(Integer.parseInt(cursor.getString(0)));
                exchangeValue.setBankSource((cursor.getString(1) == ExchangeSourceBank.ENPARA_BANK.name()) ? ExchangeSourceBank.ENPARA_BANK : ExchangeSourceBank.YK_BANK);

                String alis = cursor.getString(2);
                String satis = cursor.getString(3);
                alis = parseFloatString(4, alis);
                satis = parseFloatString(4, satis);
                exchangeValue.exchangeSet[EXCHANGE_TYPES.EUR].alis = alis;
                exchangeValue.exchangeSet[EXCHANGE_TYPES.EUR].satis = satis;

                alis = cursor.getString(4);
                satis = cursor.getString(5);
                alis = parseFloatString(4, alis);
                satis = parseFloatString(4, satis);
                exchangeValue.exchangeSet[EXCHANGE_TYPES.USD].alis = alis;
                exchangeValue.exchangeSet[EXCHANGE_TYPES.USD].satis = satis;

                alis = cursor.getString(6);
                satis = cursor.getString(7);
                alis = parseFloatString(2, alis);
                satis = parseFloatString(2, satis);
                exchangeValue.exchangeSet[EXCHANGE_TYPES.XAU].alis = alis;
                exchangeValue.exchangeSet[EXCHANGE_TYPES.XAU].satis = satis;

                exchangeValue.UpdateTimeStr = cursor.getString(8);

                // Add book to books
                exchanges.add(exchangeValue);

            } while (cursor.moveToNext());
        }
        //Log.d("EXCHANGE", "QUERY: :" + query);
        //Log.d("getAllBooks() with num:", exchanges.size() + "\n" + exchanges.toString());

        // return books
        return exchanges;
    }

    private String parseFloatString(int fieldSize, String alissatis) {
        try
        {
            String format = "%." + fieldSize + "f";
            return String.format(format, Float.parseFloat(alissatis.replace(".", "").replace(',', '.')));
        }
         catch(Exception ex)
         {
            return  "0";
         }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS exchanges");

        // create fresh books table
        this.onCreate(db);

    }

    public static ExchangeValsDB GetInstance() {
        // TODO Auto-generated method stub
        return exchangeValsDBInstance;
    }

    public void setExchangeLastDirections(ExchangeSourceBank bank, TextView[] exchangeValuesTextView, ImageView[] exchangeImages) {
        List<ExchangeValue> lastRecords = getLastExchanges(bank, 2);
        if (lastRecords.size() == 2) // 2 result is found, so show icons
        {
            for (int i = 0; i < EXCHANGE_TYPES.NUMBER_OF_EXCHANGES; i++) {
                int r = 0;
                r = lastRecords.get(0).exchangeSet[i].alis.compareTo(lastRecords.get(1).exchangeSet[i].alis);
                if (r > 0) {
                   // exchangeImages[i].setImageResource(R.drawable.ic_action_upload);
                } else if (r == 0) {
                   // exchangeImages[i].setImageResource(R.drawable.ic_action_pause);
                } else {
                   // exchangeImages[i].setImageResource(R.drawable.ic_action_download);
                }
            }

        }
    }

    public int deleteRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EXCHANGES, null, null);
    }
}
