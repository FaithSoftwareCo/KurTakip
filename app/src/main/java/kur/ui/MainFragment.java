package kur.ui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import kur.db.ExchangeSourceBank;
import kur.db.ExchangeValsDB;
import kur.db.ExchangeValue;
import kur.main.EXCHANGE_TYPES;
import kur.main.R;
import kur.task.DownloadExchangeValuesTask;

/**
 * Created by Fatih on 28.11.2016.
 */
public class MainFragment extends Fragment implements View.OnClickListener {
    private Button btnDownloadStart = null;

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.tableRow2:
            case R.id.tableRow3:
            case R.id.tableRow4:
                // open open YKB
                MainActivity.selectedBank = ExchangeSourceBank.YK_BANK;
                ((MainActivity) getActivity()).OpenBankGraphicsFragment();
                break;

            case R.id.tableRow22:
            case R.id.tableRow23:
            case R.id.tableRow24:
                // open open Enpara
                MainActivity.selectedBank = ExchangeSourceBank.ENPARA_BANK;
                ((MainActivity) getActivity()).OpenBankGraphicsFragment();
                break;
        }
    }

    public interface FragmentCallback {
        public void onTaskDone();
    }

    TextView yk_eur_alis_tv = null;
    TextView yk_usd_alis_tv = null;
    TextView yk_xau_alis_tv = null;
    TextView yk_eur_satis_tv = null;
    TextView yk_usd_satis_tv = null;
    TextView yk_xau_satis_tv = null;
    TextView yk_eur_time_tv = null;
    TextView yk_usd_time_tv = null;
    TextView yk_xau_time_tv = null;

    TextView enpara_eur_alis_tv = null;
    TextView enpara_usd_alis_tv = null;
    TextView enpara_xau_alis_tv = null;
    TextView enpara_eur_satis_tv = null;
    TextView enpara_usd_satis_tv = null;
    TextView enpara_xau_satis_tv = null;
    TextView enpara_eur_time_tv = null;
    TextView enpara_usd_time_tv = null;
    TextView enpara_xau_time_tv = null;

    static boolean firstErrDetected = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_fragment,container,false);

        yk_eur_alis_tv = (TextView) rootView.findViewById(R.id.yk_eur_alis_tv);
        yk_eur_satis_tv = (TextView) rootView.findViewById(R.id.yk_eur_satis_tv);
        yk_eur_time_tv = (TextView) rootView.findViewById(R.id.yk_eur_time_tv);
        yk_usd_alis_tv = (TextView) rootView.findViewById(R.id.yk_usd_alis_tv);
        yk_usd_satis_tv = (TextView) rootView.findViewById(R.id.yk_usd_satis_tv);
        yk_usd_time_tv = (TextView) rootView.findViewById(R.id.yk_usd_time_tv);
        yk_xau_alis_tv = (TextView) rootView.findViewById(R.id.yk_xau_alis_tv);
        yk_xau_satis_tv = (TextView) rootView.findViewById(R.id.yk_xau_satis_tv);
        yk_xau_time_tv = (TextView) rootView.findViewById(R.id.yk_xau_time_tv);

        enpara_eur_alis_tv = (TextView) rootView.findViewById(R.id.enpara_eur_alis_tv);
        enpara_eur_satis_tv = (TextView) rootView.findViewById(R.id.enpara_eur_satis_tv);
        enpara_eur_time_tv = (TextView) rootView.findViewById(R.id.enpara_eur_time_tv);
        enpara_usd_alis_tv = (TextView) rootView.findViewById(R.id.enpara_usd_alis_tv);
        enpara_usd_satis_tv = (TextView) rootView.findViewById(R.id.enpara_usd_satis_tv);
        enpara_usd_time_tv = (TextView) rootView.findViewById(R.id.enpara_usd_time_tv);
        enpara_xau_alis_tv = (TextView) rootView.findViewById(R.id.enpara_xau_alis_tv);
        enpara_xau_satis_tv = (TextView) rootView.findViewById(R.id.enpara_xau_satis_tv);
        enpara_xau_time_tv = (TextView) rootView.findViewById(R.id.enpara_xau_time_tv);

        btnDownloadStart = (Button) rootView.findViewById(R.id.btn_start);
        btnDownloadStart.setOnClickListener(onStartDownloadListener());

        MainActivity.webview = (WebView) rootView.findViewById(R.id.webViewForex);
        MainActivity.webview.getSettings().setJavaScriptEnabled(true);
        MainActivity.webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                super.onPageStarted(view, url, favicon);

                //      webview.setVisibility(View.GONE);
                //    investingTextView.setVisibility(View.VISIBLE);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        MainActivity.toolbar.setTitle(getString(R.string.piyasalar).toUpperCase(Locale.getDefault()));
        investingSiteClicked();
        displayLastRates();

        TableRow row = (TableRow) rootView.findViewById(R.id.tableRow2);
        row.setOnClickListener(this);
        row = (TableRow) rootView.findViewById(R.id.tableRow3);
        row.setOnClickListener(this);
        row = (TableRow) rootView.findViewById(R.id.tableRow4);
        row.setOnClickListener(this);

        row = (TableRow) rootView.findViewById(R.id.tableRow22);
        row.setOnClickListener(this);
        row = (TableRow) rootView.findViewById(R.id.tableRow23);
        row.setOnClickListener(this);
        row = (TableRow) rootView.findViewById(R.id.tableRow24);
        row.setOnClickListener(this);
      //  requestExchangeValues();

        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        Log.i("EXCHANGE", "onFling has been called!");
                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                return false;
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                Log.i("EXCHANGE", "Right to Left");
                                ((MainActivity)getActivity()).OpenBitcoinFragment();
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                Log.i("EXCHANGE", "Left to Right");
                                ((MainActivity)getActivity()).OpenAboutFragment();
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });

        return rootView;
    }


    /**
     * Handling button Download event
     * @return
     */
    private View.OnClickListener onStartDownloadListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestExchangeValues();
            }
        };
    }
    private void displayLastRates() {
        Log.e("EXCHANGE", "Show last rates");

        ExchangeSourceBank bank;
        ExchangeValue lastExchange;

        try {
            bank = ExchangeSourceBank.YK_BANK;

            List<ExchangeValue> list = ExchangeValsDB.GetInstance().getLastExchanges(bank, 1);
            if( list.size() != 0 && list != null )
            {
                lastExchange = list.get(0);
                yk_eur_alis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.EUR].alis);
                yk_eur_satis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.EUR].satis);
                yk_eur_time_tv.setText(lastExchange.GetTimeOnlyOfExchange());

                yk_usd_alis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.USD].alis);
                yk_usd_satis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.USD].satis);
                yk_usd_time_tv.setText(lastExchange.GetTimeOnlyOfExchange());

                yk_xau_alis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.XAU].alis);
                yk_xau_satis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.XAU].satis);
                yk_xau_time_tv.setText(lastExchange.GetTimeOnlyOfExchange());

                bank = ExchangeSourceBank.ENPARA_BANK;

                lastExchange = ExchangeValsDB.GetInstance().getLastExchanges(bank, 1).get(0);
                enpara_eur_alis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.EUR].alis);
                enpara_eur_satis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.EUR].satis);
                enpara_eur_time_tv.setText(lastExchange.GetTimeOnlyOfExchange());

                enpara_usd_alis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.USD].alis);
                enpara_usd_satis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.USD].satis);
                enpara_usd_time_tv.setText(lastExchange.GetTimeOnlyOfExchange());

                enpara_xau_alis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.XAU].alis);
                enpara_xau_satis_tv.setText(lastExchange.exchangeSet[EXCHANGE_TYPES.XAU].satis);
                enpara_xau_time_tv.setText(lastExchange.GetTimeOnlyOfExchange());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void investingSiteClicked(/*View view*/) {
        //String url = "http://fxrates.tr.forexprostools.com/index.php?force_lang=10&pairs_ids=66;50655;18;1;&header-text-color=%23000000&header-bg=%23b2daec&curr-name-color=%230059b0&inner-text-color=%23000000&green-text-color=%232A8215&green-background=%23B7F4C2&red-text-color=%23DC0001&red-background=%23FFE2E2&inner-border-color=%23CBCBCB&border-color=%23156ca6&bg1=%23F6F6F6&bg2=%23ffffff&bid=show&ask=show&last=hide&open=hide&high=hide&low=hide&change=hide&last_update=show";

        final String investingurl = "https://tr.widgets.investing.com/live-currency-cross-rates?theme=lightTheme&hideTitle=true&roundedCorners=true&cols=bid,ask,changePerc,time&pairs=18,66,50655,1&border-color=%23156ca6";

        //String url = "file:///android_asset/exchanges.html";
        firstErrDetected = false;
        String url = "http://fxrates.tr.forexprostools.com/index.php?force_lang=10&pairs_ids=66;50655;18;1;21;&header-text-color=%23000000&header-bg=%23b2daec&curr-name-color=%230059b0&inner-text-color=%23000000&green-text-color=%232A8215&green-background=%23B7F4C2&red-text-color=%23DC0001&red-background=%23FFE2E2&inner-border-color=%23CBCBCB&border-color=%23156ca6&bg1=%23F6F6F6&bg2=%23ffffff&bid=show&ask=show&last=hide&open=hide&high=hide&low=hide&change=hide&last_update=show";
        MainActivity.webview.loadUrl(url);


        MainActivity.webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                MainActivity.webview.loadUrl(investingurl);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if(!firstErrDetected)
                {
                    MainActivity.webview.loadUrl(investingurl);
                }
                firstErrDetected = true;
            }
        });
    }

    private void requestExchangeValues() {
        Log.e("EXCHANGE", "********************* Start Request ******************");
        btnDownloadStart.setText(getString(R.string.refreshing));
        btnDownloadStart.setEnabled(false);
        btnDownloadStart.setClickable(false);
        DownloadExchangeValuesTask downloadTask = new DownloadExchangeValuesTask(new FragmentCallback() {
            @Override
            public void onTaskDone() {
                displayLastRates();
                btnDownloadStart.setEnabled(true);
                btnDownloadStart.setClickable(true);
                btnDownloadStart.setText(getString(R.string.menuitem_refresh));
            }
        });

        downloadTask.execute();
    }
}
