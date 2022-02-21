package kur.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
public class ConverterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.converter_fragment,container,false);

        MainActivity.webview = (WebView) rootView.findViewById(R.id.converterForex);
        MainActivity.webview.getSettings().setJavaScriptEnabled(true);
        MainActivity.webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        investingSiteClicked();

        MainActivity.toolbar.setTitle(getString(R.string.convert).toUpperCase(Locale.getDefault()));

        TextView tv = (TextView) rootView.findViewById(R.id.webviewConvDesc);
        tv.setText(Html.fromHtml(
                "<table width=\"197\"><tr><td><span style=\"font-size: 11px;color: #333333;text-decoration: none;\">Döviz Çevirici <a href=\"http://tr.investing.com/\" rel=\"nofollow\" target=\"_blank\" style=\"font-size: 11px;color: #06529D; font-weight: bold;\" class=\"underline_link\">Investing.com Türkiye</a> tarafından sağlanmaktadır.</span></td></tr></table>")
        );

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
                                ((MainActivity)getActivity()).OpenAboutFragment();
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                Log.i("EXCHANGE", "Left to Right");
                                ((MainActivity)getActivity()).OpenIndexesFragment();
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

    public void investingSiteClicked(/*View view*/) {
        String url = "http://tools.tr.forexprostools.com/currency-converter/index.php?from=12&to=9";
        MainActivity.webview.loadUrl(url);
    }
}
