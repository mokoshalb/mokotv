package app.mokotv.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.models.Channel;
import app.mokotv.utils.Constant;
import app.mokotv.utils.NetworkCheck;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;
import pl.droidsonroids.casty.Casty;
import pl.droidsonroids.casty.MediaData;
import app.mokotv.utils.Tools;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityDetailChannel extends AppCompatActivity {

    String str_category, str_id, str_image, str_name, str_url, str_description, str_channel_type, str_video_id;
    ImageView channel_image;
    TextView channel_name, channel_category;
    WebView channel_description;
    View view;
    Button btnReportChannel, btnCommendChannel;
    private Casty casty;
    Channel objBean;
    private InterstitialAd interstitialAd;
    int counter = 1;
    private AdView adView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        view = findViewById(android.R.id.content);
        if(Tools.checkGooglePlayServices(this) && !Tools.isTV(this)){
            MediaRouteButton mediaRouteButton = findViewById(R.id.media_route_button);
            casty = Casty.create(this).withMiniController();
            casty.setUpMediaRouteButton(mediaRouteButton);
            casty.setOnConnectChangeListener(new Casty.OnConnectChangeListener() {
                @Override
                public void onConnected() {
                    Toast.makeText(ActivityDetailChannel.this, "Cast Connected Successfully", Toast.LENGTH_SHORT).show();
                    Log.d("Casty", "Connected with Chromecast");
                }

                @Override
                public void onDisconnected() {
                    Toast.makeText(ActivityDetailChannel.this, "Cast Disconnected", Toast.LENGTH_SHORT).show();
                    Log.d("Casty", "Disconnected from Chromecast");
                }
            });
        }
        initAds();
        loadBannerAd();
        loadInterstitialAd();
        objBean = new Channel();
        channel_description = findViewById(R.id.channel_description);
        Intent intent = getIntent();
        objBean.setIsTv(true);
        if (null != intent) {
            str_category = intent.getStringExtra(Constant.KEY_CHANNEL_CATEGORY);
            str_id = intent.getStringExtra(Constant.KEY_CHANNEL_ID);
            str_name = intent.getStringExtra(Constant.KEY_CHANNEL_NAME);
            str_image = intent.getStringExtra(Constant.KEY_CHANNEL_IMAGE);
            str_url = intent.getStringExtra(Constant.KEY_CHANNEL_URL);
            str_description = intent.getStringExtra(Constant.KEY_CHANNEL_DESCRIPTION);
            str_channel_type = intent.getStringExtra(Constant.KEY_CHANNEL_TYPE);
            str_video_id = intent.getStringExtra(Constant.KEY_VIDEO_ID);
        }
        setupToolbar();
        channel_image = findViewById(R.id.channel_image);
        channel_name = findViewById(R.id.channel_name);
        channel_category = findViewById(R.id.channel_category);
        channel_description = findViewById(R.id.channel_description);
        btnReportChannel = findViewById(R.id.btn_report_channel);
        btnCommendChannel = findViewById(R.id.btn_commend_channel);
        normalLayout();
        channel_description.setBackgroundColor(Color.TRANSPARENT);
        btnReportChannel.setOnClickListener(v -> {
            Intent intent1 = new Intent(ActivityDetailChannel.this, ActivityReport.class);
            intent1.putExtra("stream", str_name);
            startActivity(intent1);
        });
        btnCommendChannel.setOnClickListener(v -> {
            Intent intent2 = new Intent(ActivityDetailChannel.this, ActivityCommend.class);
            intent2.putExtra("stream", str_name);
            startActivity(intent2);
        });
    }

    private void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    public void normalLayout() {
        channel_name.setText(str_name);
        channel_category.setText(str_category);
        if (str_channel_type != null && str_channel_type.equals("YOUTUBE")) {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMG_FRONT + str_video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(channel_image);
        } else {
            Picasso.get()
                    .load(Config.ADMIN_PANEL_URL + "/app/upload/" + str_image)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(channel_image);
        }
        channel_image.setOnClickListener(view -> {
                if (NetworkCheck.isNetworkAvailable(ActivityDetailChannel.this)) {
                    if (str_channel_type != null && str_channel_type.equals("YOUTUBE")) {
                        Intent i = new Intent(ActivityDetailChannel.this, ActivityYoutubePlayer.class);
                        i.putExtra("id", str_video_id);
                        startActivity(i);
                    } else if (str_channel_type != null && str_channel_type.equals("TWITCH")) {
                        Intent intent = new Intent(ActivityDetailChannel.this, ActivityTwitch.class);
                        intent.putExtra("url", str_url);
                        startActivity(intent);
                    } else if (str_channel_type != null && str_channel_type.equals("EMBED")) {
                        Intent intent = new Intent(ActivityDetailChannel.this, ActivityEmbed.class);
                        intent.putExtra("url", str_url);
                        startActivity(intent);
                    } else {
                        if (casty != null && casty.isConnected()) {
                            playViaCast();
                            Toast.makeText(ActivityDetailChannel.this, "Stream is being casted to your TV screen...", Toast.LENGTH_LONG).show();
                            Log.e("Casty", "Connected");
                        } else {
                            Intent intent = new Intent(ActivityDetailChannel.this, ActivityStreaming.class);
                            intent.putExtra("url", str_url);
                            startActivity(intent);
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_required), Toast.LENGTH_SHORT).show();
                }
            showInterstitialAd();
        });
        channel_description.setFocusableInTouchMode(false);
        channel_description.setFocusable(false);
        channel_description.getSettings().setDefaultTextEncodingName("UTF-8");
        WebSettings webSettings = channel_description.getSettings();
        Resources res = getResources();
        int fontSize = res.getInteger(R.integer.font_size);
        webSettings.setDefaultFontSize(fontSize);
        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = str_description;
        channel_description.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                view.getContext().startActivity(intent);
                return true;
            }
        });
        String text = "<html dir='ltr'><head>"
                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/mokotv.otf\")}body{font-family: MyFont;color: #a5a5a5;text-align:left;font-size:15px;margin-left:0px;line-height:1.2}"
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";
        channel_description.loadDataWithBaseURL(null, text, mimeType, encoding, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(Tools.checkGooglePlayServices(this) && !Tools.isTV(this)){
            casty.addMediaRouteMenuItem(menu);
        }
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.share:
                try {
                    String news_heading = android.text.Html.fromHtml(getResources().getString(R.string.share_title)  + ": \n" + str_name).toString();
                    String share_text = android.text.Html.fromHtml(getResources().getString(R.string.share_content)).toString();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, news_heading + "\n\n" + share_text + "\n\n" + Config.ADMIN_PANEL_URL);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                } catch (Exception e){
                    Toast.makeText(ActivityDetailChannel.this, "Unable to find any sharing app", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void playViaCast() {
        MediaData mediaData = new MediaData.Builder(str_url)
                .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                .setContentType(getType(str_url))
                .setMediaType(MediaData.MEDIA_TYPE_MOVIE)
                .setTitle(str_name)
                .setSubtitle(getString(R.string.app_name))
                .addPhotoUrl(Config.ADMIN_PANEL_URL + "/app/upload/" + str_image)
                .build();
        casty.getPlayer().loadMediaAndPlay(mediaData);
    }

    private String getType(String videoUrl) {
        if(videoUrl.endsWith(".mp4")) {
            return "videos/mp4";
        } else if (videoUrl.endsWith(".m3u8")) {
            return "application/x-mpegurl";
        } else {
            return "application/x-mpegurl";
        }
    }

    public void initAds() {
        if (Config.ENABLE_ADMOB_BANNER_ADS || Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            MobileAds.initialize(ActivityDetailChannel.this, getResources().getString(R.string.admob_app_id));
        }
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView);
            adView.loadAd(Tools.getAdRequest(ActivityDetailChannel.this));
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

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            interstitialAd = new InterstitialAd(getApplicationContext());
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(Tools.getAdRequest(ActivityDetailChannel.this));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(Tools.getAdRequest(ActivityDetailChannel.this));
                }
            });
        } else {
            Log.d("AdMob", "AdMob Interstitial is Disabled");
        }
    }

    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                if (counter == Config.ADMOB_INTERSTITIAL_ADS_INTERVAL) {
                    interstitialAd.show();
                    counter = 1;
                } else {
                    counter++;
                }
            } else {
                Log.d("AdMob", "Interstitial Ad is Disabled");
            }
        } else {
            Log.d("AdMob", "AdMob Interstitial is Disabled");
        }
    }
}