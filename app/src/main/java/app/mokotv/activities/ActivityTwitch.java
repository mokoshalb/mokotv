package app.mokotv.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import app.mokotv.R;
import cz.msebera.android.httpclient.Header;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ActivityTwitch extends Activity implements OnPreparedListener {

    private VideoView videoView;
    String url;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_streaming);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        url = getIntent().getStringExtra("url");
        AsyncHttpClient client = new AsyncHttpClient();
        String clientID = "jzkbprff40iqj646a697cyrvl0zt2m6";
        client.addHeader("Client-ID", clientID);
        client.addHeader("Host", "api.twitch.tv");
        client.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; rv:43.0) Gecko/20100101 Firefox/43.0 Seamonkey/2.40");
        client.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        client.get("https://api.twitch.tv/api/channels/"+url+"/access_token/", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    String token = mainJson.getString("token");
                    String sig = mainJson.getString("sig");
                    url = "http://usher.twitch.tv/api/channel/hls/"+url+".m3u8?player=twitchweb&token="+java.net.URLEncoder.encode(token,"UTF-8")+"&sig="+sig;
                    videoView = findViewById(R.id.video_view);
                    videoView.setOnPreparedListener(ActivityTwitch.this);
                    Uri videoUri = Uri.parse(url);
                    videoView.setVideoURI(videoUri);
                    videoView.setHandleAudioFocus(false);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFinish(){}
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error){}
        });
    }

    @Override
    public void onPrepared() {
        videoView.start();
        videoView.setHandleAudioFocus(false);
    }

    public void onBackPressed() {
        super.onBackPressed();
        if(mHandler!=null){
            mHandler.removeCallbacks(runnable);
            mHandler=null;
        }
    }

    private Runnable runnable = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
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
