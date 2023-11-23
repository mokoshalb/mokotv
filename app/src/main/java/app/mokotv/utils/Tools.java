package app.mokotv.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import app.mokotv.R;

import static android.content.Context.UI_MODE_SERVICE;

public class Tools {
    private static final String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom random = new SecureRandom();

    public static AdRequest getAdRequest(Activity activity) {
        return new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(activity))
                .build();
    }
    public static boolean checkGooglePlayServices(Context context){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int resultCode = api.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    @SuppressLint("HardwareIds")
    public static String getUniqueID(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String formatted_date(long timestamp) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mma");
        sdf.setTimeZone(tz);//set time zone.
        String localTime = sdf.format(new Date(timestamp * 1000));
        return localTime.toLowerCase();
    }

    public static Snackbar showErrorSnackBar(Activity mContext, View rootView, String message) {
        if (message.equals("")) {
            message = "There was an error.";
        }
        Snackbar snack_error = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View view = snack_error.getView();
        TextView tv = view.findViewById(R.id.snackbar_text);
        view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.material_red));
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        return snack_error;
    }

    public static boolean isTV(Context context){
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++){
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}