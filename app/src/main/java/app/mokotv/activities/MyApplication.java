package app.mokotv.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;
import app.mokotv.Config;
import app.mokotv.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import com.onesignal.OneSignal;
import com.onesignal.OSNotificationOpenResult;
import org.json.JSONObject;

public class MyApplication extends Application {

    public SharedPreferences preferences;
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/mokotv.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new ExampleNotificationOpenedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        mInstance = this;
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public boolean getNotification() {
        preferences = this.getSharedPreferences(Config.ARRAY_NAME, 0);
        return preferences.getBoolean("IsNotification", true);
    }

    public boolean getShowNetwork() {
        preferences = this.getSharedPreferences(Config.ARRAY_NAME, 0);
        return preferences.getBoolean("IsShowNetwork", true);
    }

    public void saveIsNotification(boolean flag) {
        preferences = this.getSharedPreferences(Config.ARRAY_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("IsNotification", flag);
        editor.apply();
    }

    public void saveShowNetwork(boolean flag) {
        preferences = this.getSharedPreferences(Config.ARRAY_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("IsShowNetwork", flag);
        editor.apply();
    }

    private class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            JSONObject data = result.notification.payload.additionalData;
            Log.e("data", "" + data);
            String isExternalLink;
            if (data != null) {
                isExternalLink = data.optString("external_link", null);
                if (!isExternalLink.equals("")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(isExternalLink));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e){
                        Toast.makeText(MyApplication.this, "Unable to find any browser app", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Intent intent = new Intent(MyApplication.this, ActivitySplash.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }
}
