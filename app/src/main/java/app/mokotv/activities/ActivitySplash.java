package app.mokotv.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.utils.Tools;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import is.arontibo.library.ElasticDownloadView;
public class ActivitySplash extends AppCompatActivity {

    ElasticDownloadView mElasticDownloadView;
    private InterstitialAd interstitialAd;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initAds();
        loadInterstitialAd();
        if(!Tools.checkGooglePlayServices(this)){
            Toast.makeText(this, "Google Play Service is missing or outdated, Some features might not work.", Toast.LENGTH_LONG).show();
        }
        mElasticDownloadView = findViewById(R.id.elastic_download_view);
        mElasticDownloadView.setProgress(100);
        new CountDownTimer(Config.SPLASH_TIME, 1000) {
            @Override
            public void onFinish() {
                //mElasticDownloadView.success();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
                showInterstitialAd();
            }
            @Override
            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }

    public void initAds() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_AFTER_SPLASH) {
            MobileAds.initialize(ActivitySplash.this, getResources().getString(R.string.admob_app_id));
        }
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_AFTER_SPLASH) {
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(Tools.getAdRequest(ActivitySplash.this));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(Tools.getAdRequest(ActivitySplash.this));
                }
            });
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_AFTER_SPLASH) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        }
    }
}