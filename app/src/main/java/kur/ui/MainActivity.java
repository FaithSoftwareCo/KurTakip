package kur.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TableRow;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import kur.db.ExchangeSourceBank;
import kur.db.ExchangeValsDB;
import kur.main.R;


public class MainActivity extends AppCompatActivity {

    public static WebView webview = null;
    public static Toolbar toolbar = null;
    private static NavigationView navigationView = null;
    public static ExchangeSourceBank selectedBank = ExchangeSourceBank.ENPARA_BANK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExchangeValsDB db = new ExchangeValsDB(this);
        ExchangeValsDB.SetInstance(db);

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_preferences) {
                    OpenMainFragment();
                }
                else if (id == R.id.nav_bitcoin) {
                    OpenBitcoinFragment();
                }
                else if (id == R.id.nav_graphics) {
                    OpenGraphicsFragment();
                }
                else if (id == R.id.nav_last_data) {
                    OpenBankGraphicsFragment();
                }
                else if (id == R.id.nav_indexes) {
                    OpenIndexesFragment();
                }
                else if (id == R.id.nav_converter) {
                    OpenConverterFragment();
                }
                else if (id == R.id.nav_about) {
                   OpenAboutFragment();
                }
                else if (id == R.id.nav_exit) {
                    exit();
                    return true;
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        OpenMainFragment();

        AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice("9800AD2F2DDD9FBFDD1280AB9B7344F6").build();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    public void OpenAboutFragment() {
        AboutFragment fragment = new AboutFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        if( navigationView != null )
        {
            navigationView.setCheckedItem(R.id.nav_about);
        }
    }

    public void OpenBankGraphicsFragment() {
        GraphicsBanksFragment fragment = new GraphicsBanksFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        if( navigationView != null )
        {
            navigationView.setCheckedItem(R.id.nav_last_data);
        }
    }


    public void OpenConverterFragment() {
        ConverterFragment fragment = new ConverterFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        if( navigationView != null )
        {
            navigationView.setCheckedItem(R.id.nav_converter);
        }
    }

    public void OpenIndexesFragment() {
        IndexesFragment fragment = new IndexesFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        if( navigationView != null )
        {
            navigationView.setCheckedItem(R.id.nav_indexes);
        }
    }

    public void OpenGraphicsFragment() {
        GraphicsFragment fragment = new GraphicsFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        if( navigationView != null )
        {
            navigationView.setCheckedItem(R.id.nav_graphics);
        }
    }

    public void OpenMainFragment() {
        MainFragment fragment = new MainFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        if( navigationView != null )
        {
            navigationView.setCheckedItem(R.id.nav_preferences);
        }
    }

    public void OpenBitcoinFragment() {
        BitcoinFragment fragment = new BitcoinFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        if( navigationView != null )
        {
            navigationView.setCheckedItem(R.id.nav_bitcoin);
        }
    }

    public void OpenNewsFeedFragment() {
        NewsFeedFragment fragment = new NewsFeedFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();

        if( navigationView != null )
        {
            navigationView.setCheckedItem(R.id.nav_newsfeed);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( webview != null ) webview.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("EXCHANGE", "On Resume!!!!!!!!!!!!!!!!!");
        try {
            if (webview != null) {
                webview.resumeTimers();
            }
        }
        catch (Exception e)
        {
            Log.e("ERRRRRRRROR", "OnResume vebbiew errorrr!!!!!");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("EXCHANGE", "On Stop!!!!!!!!!!!!!!!!!");
        if( webview != null ) webview.pauseTimers();
    }

    private void exit() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            //Intent i = new Intent(this, UserSettingActivity.class);
            //startActivityForResult(i, 1);

            exit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

