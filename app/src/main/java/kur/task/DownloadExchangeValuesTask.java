package kur.task;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kur.db.ExchangeSourceBank;
import kur.db.ExchangeValsDB;
import kur.db.ExchangeValue;
import kur.db.ExchangeValueSet;
import kur.main.EXCHANGE_TYPES;
import kur.ui.MainFragment;

import org.json.JSONArray;

public class DownloadExchangeValuesTask extends AsyncTask<Void, Void, String> {
    public static ExchangeValue ykbExchange = new ExchangeValue(ExchangeSourceBank.YK_BANK);
    public static ExchangeValue enparaExchange = new ExchangeValue(ExchangeSourceBank.ENPARA_BANK);
    public static ExchangeValue kuveytExchange = new ExchangeValue(ExchangeSourceBank.KUVEYT_BANK);
    private MainFragment.FragmentCallback mFragmentCallback;

    public DownloadExchangeValuesTask(MainFragment.FragmentCallback fragmentCallback) {
        mFragmentCallback = fragmentCallback;
    }

    @Override
    protected String doInBackground(Void... urls) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        Callable<Void> yapiKrediTask = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try{
                    DownloadExchangeValuesTask.this.scrapeYapiKredi();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    Log.e("ERROR::", "Error reading HTML");
                    ykbExchange.setTimeOfExchange("Error");
                }

                return null;
            }
        };

        Callable<Void> enparaTask = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    DownloadExchangeValuesTask.this.scrapeEnpara();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    Log.e("ERROR::", "Error reading HTML");
                    enparaExchange.setTimeOfExchange("Error");
                }
                return null;
            }
        };

        Callable<Void> kuveytTurkTask = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    DownloadExchangeValuesTask.this.scrapeKuveytTurk();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    Log.e("ERROR::", "Error reading HTML");
                    kuveytExchange.setTimeOfExchange("Error");
                }
                return null;
            }
        };

        Future<Void> futureYapiKredi = executor.submit(yapiKrediTask);
        Future<Void> futureEnpara = executor.submit(enparaTask);
        Future<Void> futureKuveytTurk = executor.submit(kuveytTurkTask);

        try {
            futureYapiKredi.get();
            futureEnpara.get();
            futureKuveytTurk.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.e("ERROR2::", "Error reading HTML");
            ykbExchange.setTimeOfExchange("Error");
        } finally {
            executor.shutdown();
        }

        return "";
    }

    private void scrapeYapiKredi() throws IOException {
        Document doc = Jsoup.connect("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri.aspx?section=internet").userAgent("Mozilla").get();
        Element kurTable = doc.getElementById("currencyResultContent");
        Iterator<Element> iterator = kurTable.select("td").iterator();
        String time = doc.getElementsByClass("dipnote").text().replace("Güncelleme :", "").trim();
        ykbExchange.setTimeOfExchange(time);

        while (iterator.hasNext()) {
            Element currIter = iterator.next();
            switch (currIter.text().trim()) {
                case "USD":
                    iterator.next();
                    String USD_Al = iterator.next().text().trim();
                    String USD_Sat = iterator.next().text().trim();
                    ykbExchange.setExchangeSetUSD(new ExchangeValueSet(EXCHANGE_TYPES.USD, USD_Al, USD_Sat));
                    break;
                case "EUR":
                    iterator.next();
                    String EUR_Al = iterator.next().text().trim();
                    String EUR_Sat = iterator.next().text().trim();
                    ykbExchange.setExchangeSetEUR(new ExchangeValueSet(EXCHANGE_TYPES.EUR, EUR_Al, EUR_Sat));
                    break;
                case "XAU":
                    iterator.next();
                    String XAU_Al = iterator.next().text().trim();
                    String XAU_Sat = iterator.next().text().trim();
                    ykbExchange.setExchangeSetXAU(new ExchangeValueSet(EXCHANGE_TYPES.XAU, XAU_Al, XAU_Sat));
                    break;
            }
        }
    }

    private void scrapeEnpara() throws IOException {
        Document doc2 = Jsoup.connect("https://www.qnbfinansbank.enpara.com/hesaplar/doviz-ve-altin-kurlari").userAgent("Mozilla").get();
        Elements kurTableEnpara = doc2.getElementsByClass("enpara-gold-exchange-rates__table");
        Iterator<Element> iteratorEnpara = kurTableEnpara.select("div.enpara-gold-exchange-rates__table-item").iterator();

        String currentDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm", Locale.getDefault()).format(new Date());
        enparaExchange.setTimeOfExchange(currentDate);

        // USD
        String usdStr = iteratorEnpara.next().text().replace("TL", "").replace("USD ($)", "").trim();
        String USD_Al = usdStr.split("\\s+")[0];
        String USD_Sat = usdStr.split("\\s+")[1];
        enparaExchange.setExchangeSetUSD(new ExchangeValueSet(EXCHANGE_TYPES.USD, USD_Al, USD_Sat));

        // EUR
        String eurStr = iteratorEnpara.next().text().replace("TL", "").replace("EUR (€)","").trim();
        String EUR_Al = eurStr.split("\\s+")[0];
        String EUR_Sat = eurStr.split("\\s+")[1];
        enparaExchange.setExchangeSetEUR(new ExchangeValueSet(EXCHANGE_TYPES.EUR, EUR_Al, EUR_Sat));

        // XAU
        String xauStr = iteratorEnpara.next().text().replace("TL", "").replace("Altın (gram)","").trim();
        String XAU_Al = xauStr.split("\\s+")[0];
        String XAU_Sat = xauStr.split("\\s+")[1];
        enparaExchange.setExchangeSetXAU(new ExchangeValueSet(EXCHANGE_TYPES.XAU, XAU_Al, XAU_Sat));
    }

    private void scrapeKuveytTurk() throws IOException {
        String json = Jsoup.connect("https://www.kuveytturk.com.tr/ck0d84?B83A1EF44DD940F2FEC85646BDB25EA0").
                ignoreContentType(true).userAgent("Mozilla").execute().body();
        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0 ; i <  jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String currencyCode = jsonObject.getString("CurrencyCode");
                if (currencyCode.equalsIgnoreCase("USD")) {
                    String title = jsonObject.getString("Title");
                    Double buyRate = jsonObject.getDouble("BuyRate");
                    Double sellRate = jsonObject.getDouble("SellRate");
                    //double changeRate = jsonObject.getDouble("ChangeRate");

                    kuveytExchange.setExchangeSetUSD(new ExchangeValueSet(EXCHANGE_TYPES.USD, String.format("%.4f", buyRate).replace('.', ','), String.format("%.4f", sellRate).replace('.', ',')));
                }
                else if (currencyCode.equalsIgnoreCase("EUR")) {
                    String title = jsonObject.getString("Title");
                    Double buyRate = jsonObject.getDouble("BuyRate");
                    Double sellRate = jsonObject.getDouble("SellRate");
                    //double changeRate = jsonObject.getDouble("ChangeRate");

                    kuveytExchange.setExchangeSetEUR(new ExchangeValueSet(EXCHANGE_TYPES.EUR, String.format("%.4f", buyRate).replace('.', ','), String.format("%.4f", sellRate).replace('.', ',')));
                }
                else if (currencyCode.equalsIgnoreCase("ALT (gr)")) {
                    String title = jsonObject.getString("Title");
                    Double buyRate = jsonObject.getDouble("BuyRate");
                    Double sellRate = jsonObject.getDouble("SellRate");
                    //double changeRate = jsonObject.getDouble("ChangeRate");

                    kuveytExchange.setExchangeSetXAU(new ExchangeValueSet(EXCHANGE_TYPES.XAU, String.format("%.2f", buyRate).replace('.', ','), String.format("%.2f", sellRate).replace('.', ',')));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String strDate = dateFormat.format(date);
        kuveytExchange.setTimeOfExchange(strDate);
    }

    @Override
    protected void onPostExecute(String result) {
        //Log.e("EXCHANGE", "Finito:" + result);
        if (ykbExchange.GetTimeOfExchange().startsWith("HATA") == false) // Ba�lant� Hatas� yoksa
        {
            try
            {
                ExchangeValsDB.GetInstance().addExchangeValuetoDB(ykbExchange);
                Log.e("YKB ADDED to db time:", ykbExchange.GetTimeOfExchange() + " ->" + ykbExchange.toString());
            } catch (SQLiteConstraintException ex)
            {
                Log.e("YKB NOT !!! ADDED to db time:", ykbExchange.GetTimeOfExchange() + " ->" + ykbExchange.toString());
            }

            try {
                ExchangeValsDB.GetInstance().addExchangeValuetoDB(enparaExchange);
                Log.e("ENPARA ADDED db time:", enparaExchange.GetTimeOfExchange() + " ->" + enparaExchange.toString());
            } catch (SQLiteConstraintException ex)
            {
                Log.e("ENPARA NOT !!!! ADDED db time:", enparaExchange.GetTimeOfExchange() + " ->" + enparaExchange.toString());
            }

            try {
                ExchangeValsDB.GetInstance().addExchangeValuetoDB(kuveytExchange);
                Log.e("KUVEYT ADDED db time:", kuveytExchange.GetTimeOfExchange() + " ->" + kuveytExchange.toString());
            } catch (SQLiteConstraintException ex)
            {
                Log.e("KUVEYT NOT !!! ADDED db time:", kuveytExchange.GetTimeOfExchange() + " ->" + kuveytExchange.toString());
            }

        }

        try {
            mFragmentCallback.onTaskDone();
        }
        catch (Exception e)
        {
            Log.e("ERRRRRRRROR", "DownloadTask send errorrr in Download Task!!!!!");
            e.printStackTrace();
        }
    }
}
