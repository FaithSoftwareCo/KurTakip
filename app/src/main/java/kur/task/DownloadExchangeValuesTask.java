package kur.task;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import kur.db.ExchangeSourceBank;
import kur.db.ExchangeValsDB;
import kur.db.ExchangeValue;
import kur.db.ExchangeValueSet;
import kur.main.EXCHANGE_TYPES;
import kur.ui.MainFragment;


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
        String response = "";
        String EUR_Al, EUR_Sat, USD_Al, USD_Sat, XAU_Al, XAU_Sat, time;
        Document doc, doc2, doc3;
        try {
            //http://m.tr.investing.com/

            doc = Jsoup.connect("https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-bilgileri.aspx?section=internet").userAgent("Mozilla").get();
            Element kurTable = doc.getElementById("currencyResultContent");

            doc2 = Jsoup.connect("https://www.qnbfinansbank.enpara.com/hesaplar/doviz-ve-altin-kurlari").userAgent("Mozilla").get();

            doc3 = Jsoup.connect("https://www.kuveytturk.com.tr/finans-portali/").userAgent("Mozilla").get();

            Elements kurTableEnpara = doc2.getElementsByClass("enpara-gold-exchange-rates__table");
            //Log.e("EXCHANGE ENPARA", ">>>>>:" + kurTableEnpara.text());
            Iterator<Element> iteratorEnpara = kurTableEnpara.select("div.enpara-gold-exchange-rates__table-item").iterator();

            //String lastUpdateTimeEnparaStr = doc2.select("p:matchesOwn(Son güncellenme tarihi:)").first().text().replace("Son güncellenme tarihi:", "").trim();
            //enparaExchange.setTimeOfExchange(lastUpdateTimeEnparaStr);
            //Log.e("EXCHANGE ENPARA", ">>>>> Date:" + lastUpdateTimeEnparaStr);
            String currentDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm", Locale.getDefault()).format(new Date());
            enparaExchange.setTimeOfExchange(currentDate);

            //iteratorEnpara.next();
            String usdStr = iteratorEnpara.next().text().replace("TL", "").replace("USD ($)", "").trim();
            USD_Al = usdStr.split("\\s+")[0];
            USD_Sat = usdStr.split("\\s+")[1];
            enparaExchange.setExchangeSetUSD(new ExchangeValueSet(EXCHANGE_TYPES.USD, USD_Al, USD_Sat));

            // EUR // EUR (€)6,292398  6,777343
            String eurStr = iteratorEnpara.next().text().replace("TL", "").replace("EUR (€)","").trim();
            EUR_Al = eurStr.split("\\s+")[0];
            EUR_Sat = eurStr.split("\\s+")[1];
            enparaExchange.setExchangeSetEUR(new ExchangeValueSet(EXCHANGE_TYPES.EUR, EUR_Al, EUR_Sat));

            //Altın (gram)272,077994  294,085010
            //iteratorEnpara.next(); // XAU
            String xauStr = iteratorEnpara.next().text().replace("TL", "").replace("Altın (gram)","").trim();
            XAU_Al = xauStr.split("\\s+")[0];
            XAU_Sat = xauStr.split("\\s+")[1];
            enparaExchange.setExchangeSetXAU(new ExchangeValueSet(EXCHANGE_TYPES.XAU, XAU_Al, XAU_Sat));

   		  /* YAPI KREDI */
            //Log.e("EXCHANGE", ">>>>>:" + kurTable.text());
            //Log.e("EXCHANGE 2", ">>>>>:" + lastUpdateTime.size());

            //Log.e("EXCHANGE KUR3:", ">>>>>:" + kurTable.html());
            //Log.e("EXCHANGE KUR4:", ">>>>>:" + kurTable.childNodeSize());

            Iterator<Element> iterator = kurTable.select("td").iterator();
            //time = doc.select("span:matchesOwn(Güncelleme :)").first().text().replace("Güncelleme :", "").trim();
            time = doc.getElementsByClass("dipnote").text().replace("Güncelleme :", "").trim();;
            ykbExchange.setTimeOfExchange(time);//nextSibling().toString(); //"";//lastUpdateTime.text();

            Element currIter = null;
            while((currIter=iterator.next()) != null)
            {
                //Log.e("EXCHANGE KUR145:", ">>>>>:" +  currIter.text());
               if( currIter.text().trim().compareTo("USD") == 0 )
               {
                   iterator.next();
                   USD_Al = iterator.next().text().trim();
                   USD_Sat = iterator.next().text().trim();
                   ykbExchange.setExchangeSetUSD(new ExchangeValueSet(EXCHANGE_TYPES.USD, USD_Al, USD_Sat));
               }
                else if( currIter.text().trim().compareTo("EUR") == 0 )
               {
                   iterator.next();
                   EUR_Al = iterator.next().text().trim();
                   EUR_Sat = iterator.next().text().trim();
                   ykbExchange.setExchangeSetEUR(new ExchangeValueSet(EXCHANGE_TYPES.EUR, EUR_Al, EUR_Sat));
               }
               else if( currIter.text().trim().compareTo("XAU") == 0 )
               {
                   iterator.next();
                   XAU_Al = iterator.next().text().trim();
                   XAU_Sat = iterator.next().text().trim();
                   ykbExchange.setExchangeSetXAU(new ExchangeValueSet(EXCHANGE_TYPES.XAU, XAU_Al, XAU_Sat));
                   break;
               }
            }

            // kuveyt
            Elements kurTableKuveyt = doc3.getElementsByClass("col-md-4 col-sm-6");

            USD_Al = kurTableKuveyt.get(0).getElementsByClass("cellbox insidebox").get(0).text().replace("Alış", "").trim();
            USD_Sat = kurTableKuveyt.get(0).getElementsByClass("cellbox insidebox").get(1).text().replace("Satış", "").trim();

            kuveytExchange.setExchangeSetUSD(new ExchangeValueSet(EXCHANGE_TYPES.USD, USD_Al, USD_Sat));

            EUR_Al = kurTableKuveyt.get(1).getElementsByClass("cellbox insidebox").get(0).text().replace("Alış", "").trim();
            EUR_Sat = kurTableKuveyt.get(1).getElementsByClass("cellbox insidebox").get(1).text().replace("Satış", "").trim();

            kuveytExchange.setExchangeSetEUR(new ExchangeValueSet(EXCHANGE_TYPES.EUR, EUR_Al, EUR_Sat));

            XAU_Al = kurTableKuveyt.get(2).getElementsByClass("cellbox insidebox").get(0).text().replace("Alış", "").trim();
            XAU_Sat = kurTableKuveyt.get(2).getElementsByClass("cellbox insidebox").get(1).text().replace("Satış", "").trim();

            kuveytExchange.setExchangeSetXAU(new ExchangeValueSet(EXCHANGE_TYPES.XAU, XAU_Al, XAU_Sat));

            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String strDate = dateFormat.format(date);
            kuveytExchange.setTimeOfExchange(strDate);

            //Log.e("EXCHANGE KUR:", ">>>>>:" + kurTable.lastElementSibling().html());
            //Log.e("EXCHANGE", ">>>>>:" + doc.html());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("ERROR::", "HATAAAAAAAAAAAAAAA html okurken");
            ykbExchange.setTimeOfExchange("HATA");//nextSibling().toString(); //"";//lastUpdateTime.text();
        }

        return response;
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
