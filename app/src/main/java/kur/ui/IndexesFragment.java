package kur.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
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

import java.util.Locale;

import androidx.fragment.app.Fragment;
import kur.main.R;

/**
 * Created by Fatih on 28.11.2016.
 */
public class IndexesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.indexes_fragment,container,false);

        MainActivity.webview = (WebView) rootView.findViewById(R.id.graphics1Forex);
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

        MainActivity.toolbar.setTitle(getString(R.string.borsalar).toUpperCase(Locale.getDefault()));

        investingSiteClicked();

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
                                ((MainActivity)getActivity()).OpenConverterFragment();
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                Log.i("EXCHANGE", "Left to Right");
                                ((MainActivity)getActivity()).OpenGraphicsFragment();
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
       //String url = "http://tr.dailyforex.com/forex-widget/rateswidget/16374";
        //String url = "http://tvchart.tradingeconomics.com/c?s=XU100:IND&interval=W&locale=tr";
        String url = "https://tr.tradingview.com/chart/?symbol=BIST%3AXU100";
        MainActivity.webview.loadUrl(url);
    }

}
