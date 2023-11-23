package app.mokotv.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.fragments.FragmentAbout;
import app.mokotv.fragments.FragmentDonate;
import app.mokotv.fragments.FragmentChatRoom;
import app.mokotv.fragments.FragmentIPTV;
import app.mokotv.fragments.FragmentSettings;
import app.mokotv.tab.FragmentTabCategory;
import app.mokotv.tab.FragmentTabRecent;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;
import app.mokotv.utils.GDPR;
import app.mokotv.utils.ItemNav;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import app.mokotv.adapters.AdapterNav;
import app.mokotv.utils.RecyclerTouchListener;
import app.mokotv.utils.Tools;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import app.loveplusplus.update.UpdateChecker;

public class MainActivity extends AppCompatActivity{

    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private final static String SELECTED_TAG = "selected_index";
    private long exitTime = 0;
    private static int selectedIndex;
    private final static int COLLAPSING_TOOLBAR = 0;
    View view;
    DrawerLayout drawer;
    ArrayList<ItemNav> mNavItem;
    public AdapterNav navAdapter;
    int previousSelect = 0;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private AdView adView;
    private boolean storagePermission = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(android.R.id.content);
        selectedIndex = COLLAPSING_TOOLBAR;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new FragmentTabRecent(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                .commit();
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        setSupportActionBar(toolbar);
        setupNavigationDrawer(toolbar);
        mNavItem = new ArrayList<>();
        fillNavItem();
        RecyclerView recyclerView = findViewById(R.id.navigation);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);
        navAdapter = new AdapterNav(MainActivity.this, mNavItem);
        recyclerView.setAdapter(navAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(MainActivity.this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                navigationClick(mNavItem.get(position).getId());
            }
            @Override
            public void onLongClick(View view, int position) {}
        }));
        loadBannerAd();
        UpdateChecker.checkForNotification(MainActivity.this);
        StartAppSDK.init(MainActivity.this, "200358476", false);
        if (Config.ENABLE_STARTAPP_SPLASH_ADS) {
            Log.d("StartApp", "StartApp Splash Ads is Enabled");
        } else {
            StartAppAd.disableSplash();
        }
        GDPR.updateConsentStatus(this);
        String[] arrayColor = this.getResources().getStringArray(R.array.UsernameColor);
        Config.COLOUR = arrayColor[new Random().nextInt(arrayColor.length)];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 8080);
        }
        generateM3UFile();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG, selectedIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.removeItem(R.id.edit_username);
        menu.removeItem(R.id.iptv_search);
        menu.findItem(R.id.search).setVisible(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                return true;
            case R.id.iptv_search:
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            exitApp();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            if (Config.ENABLE_STARTAPP_INTERSTITIAL_EXIT_ADS) {
                StartAppAd.onBackPressed(this);
            } else {
                Log.d("StartApp", "StartApp Interstitial Exit Ads is Disabled");
            }
        }
    }

    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            MobileAds.initialize(MainActivity.this, getResources().getString(R.string.admob_app_id));
            adView = findViewById(R.id.adView);
            adView.loadAd(Tools.getAdRequest(MainActivity.this));
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdFailedToLoad(int error) {
                    adView.setVisibility(View.GONE);
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Log.d("AdMob", "AdMob Banner is Disabled");
        }
    }

    private void fillNavItem() {
        mNavItem.add(new ItemNav(0, R.drawable.ic_home, getResources().getString(R.string.drawer_recent)));
        //mNavItem.add(new ItemNav(1, R.drawable.ic_iptv, getResources().getString(R.string.drawer_iptv)));
        mNavItem.add(new ItemNav(2, R.drawable.ic_category, getResources().getString(R.string.drawer_category)));
        mNavItem.add(new ItemNav(3, R.drawable.ic_chat, getResources().getString(R.string.drawer_chatroom)));
        mNavItem.add(new ItemNav(4, R.drawable.ic_request, getResources().getString(R.string.drawer_request)));
        mNavItem.add(new ItemNav(5, R.drawable.ic_setting, getResources().getString(R.string.drawer_settings)));
        mNavItem.add(new ItemNav(6, R.drawable.ic_update, getResources().getString(R.string.drawer_update)));
        mNavItem.add(new ItemNav(7, R.drawable.ic_share_app, getResources().getString(R.string.drawer_share)));
        //mNavItem.add(new ItemNav(8, R.drawable.ic_drawer_donate, getResources().getString(R.string.drawer_donate)));
        mNavItem.add(new ItemNav(9, R.drawable.ic_drawer_rate, getResources().getString(R.string.drawer_rate)));
        mNavItem.add(new ItemNav(10, R.drawable.ic_about, getResources().getString(R.string.drawer_about)));
    }

    private void navigationClick(int position) {
        drawer.closeDrawers();
        switch (position) {
            case 0:
                navigationItemSelected(0);
                hideKeyboard();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FragmentTabRecent(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                        .commit();
                previousSelect = 0;
                break;
                /*
            case 1:
                if(storagePermission){
                    navigationItemSelected(1);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentIPTV(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                            .commit();
                    previousSelect = 1;
                }else{
                    navigationItemSelected(previousSelect);
                    Toast.makeText(this, "Enable Storage Permission for IPTV feature to work.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", this.getPackageName(),null);
                    intent.setData(uri);
                    this.startActivity(intent);
                }
                break;
                 */
            case 2:
                navigationItemSelected(2);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FragmentTabCategory(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                        .commit();
                previousSelect = 2;
                break;
            case 3:
                navigationItemSelected(3);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FragmentChatRoom(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                        .commit();
                previousSelect = 3;
                break;
            case 4:
                navigationItemSelected(previousSelect);
                Intent intent = new Intent(MainActivity.this, ActivityRequest.class);
                startActivity(intent);
                break;
            case 5:
                navigationItemSelected(5);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentSettings(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                previousSelect = 5;
                break;
            case 6:
                navigationItemSelected(previousSelect);
                UpdateChecker.checkForDialog(MainActivity.this);
                break;
            case 7:
                navigationItemSelected(previousSelect);
                try {
                    String app_name = android.text.Html.fromHtml(getResources().getString(R.string.app_name)).toString();
                    String share_text = android.text.Html.fromHtml(getResources().getString(R.string.share_content)).toString();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, share_text + "\n\n" + "Download "+app_name + "\n" + Config.ADMIN_PANEL_URL);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } catch (Exception e){
                    Toast.makeText(MainActivity.this, "Unable to find any sharing app", Toast.LENGTH_SHORT).show();
                }
                break;
                /*
            case 8:
                navigationItemSelected(8);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentDonate(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                previousSelect = 8;
                break;
                 */
            case 9:
                navigationItemSelected(previousSelect);
                Intent intentRate = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                startActivity(intentRate);
                break;
            case 10:
                navigationItemSelected(10);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FragmentAbout(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                previousSelect = 10;
                break;
        }
    }
    public void navigationItemSelected(int position) {
        previousSelect = position;
        navAdapter.setSelected(position);
    }
    public void generateM3UFile() {
        try {
            File m3u = new File(Environment.getExternalStorageDirectory(), ".mokotv");
            boolean fileMade = false;
            if (!m3u.exists()) {
                 fileMade = m3u.mkdirs();
            }
            if(fileMade) {
                File m3uFile = new File(m3u, "mokotv.m3u");
                FileWriter writer = new FileWriter(m3uFile);
                writer.append("");
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,  @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 8080){
            if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    storagePermission = true;
                }
            }
        }
    }
}