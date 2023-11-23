package app.mokotv.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import com.wang.avi.AVLoadingIndicatorView;
import app.mokotv.R;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityEmbed extends AppCompatActivity {

    WebView webView;
    String streamUrl;
    AVLoadingIndicatorView progressBar;
    private TextView network_speed;
    private Handler mHandler;
    private long total_receive;
    private long total_send;
    private long prev_upload_speed;
    private long prev_download_speed;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_embed);
        streamUrl = getIntent().getStringExtra("url");
        webView = findViewById(R.id.video);
        progressBar = findViewById(R.id.load);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.loadUrl(streamUrl);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        });
        network_speed = findViewById(R.id.network_speed);
        total_receive = TrafficStats.getTotalRxBytes();
        total_send = TrafficStats.getTotalTxBytes();
        prev_download_speed = -1;
        prev_upload_speed = -1;
        mHandler = new Handler();
        if(MyApplication.getInstance().getShowNetwork()){
            if(mHandler!=null){
                mHandler.removeCallbacks(runnable);
                mHandler.post(runnable);
            }
        } else {
            network_speed.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.loadUrl("");
        }
        if(mHandler!=null){
            mHandler.removeCallbacks(runnable);
            mHandler=null;
        }
    }

    private Runnable runnable = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run(){
            final long download_speed =  (TrafficStats.getTotalRxBytes()-total_receive)/1024;
            final long upload_speed = (TrafficStats.getTotalTxBytes()-total_send)/1024;
            if((prev_download_speed!= download_speed || prev_upload_speed != upload_speed) ){
                prev_download_speed = download_speed;
                prev_upload_speed = upload_speed;
                long total_speed = download_speed + upload_speed;
                if(total_speed < 10){
                    network_speed.setText(String.format("Speed: %3s KB/s - Poor", total_speed));
                    network_speed.setTextColor(Color.RED);
                }else if(total_speed < 100){
                    network_speed.setText(String.format("Speed: %3s KB/s - Fair", total_speed));
                    network_speed.setTextColor(Color.parseColor("#ffa500"));
                }else if(total_speed < 1000){
                    network_speed.setText(String.format("Speed: %3s KB/s - Good", total_speed));
                    network_speed.setTextColor(Color.YELLOW);
                }else{
                    double total_speed_mb = total_speed/1000.00;
                    network_speed.setText(String.format("Speed: %.2f MB/s - Perfect", total_speed_mb));
                    network_speed.setTextColor(Color.GREEN);
                }
            }
            total_receive = TrafficStats.getTotalRxBytes();
            total_send = TrafficStats.getTotalTxBytes();
            if(mHandler!=null){
                mHandler.postDelayed(this,1000L);
            }
        }
    };
}
