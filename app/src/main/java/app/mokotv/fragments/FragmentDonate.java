package app.mokotv.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.activities.MainActivity;

public class FragmentDonate extends Fragment {
    private static final String TAG = "Moko TV Donation";
    private boolean mDebug = false;
    private Toolbar toolbar;
    private MainActivity mainActivity;

    public FragmentDonate() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View root_view = inflater.inflate(R.layout.fragment_donate, null);
        toolbar = root_view.findViewById(R.id.toolbar);
        setupToolbar();
        return root_view;
    }

    @TargetApi(11)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
        ViewStub paypalViewStub = Objects.requireNonNull(getActivity()).findViewById(R.id.donations__paypal_stub);
        paypalViewStub.inflate();
        Button btPayPal = getActivity().findViewById(R.id.donations__paypal_donate_button);
        btPayPal.setOnClickListener(view -> donatePayPalOnClick());
        ViewStub bitcoinViewStub = getActivity().findViewById(R.id.donations__bitcoin_stub);
        bitcoinViewStub.inflate();
        Button btBitcoin = getActivity().findViewById(R.id.donations__bitcoin_button);
        btBitcoin.setOnClickListener(this::donateBitcoinOnClick);
        btBitcoin.setOnLongClickListener(v -> {
                ClipboardManager clipboard =
                        (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("37bczicSkcVgJR5nSbKpvxWMU5c366d2qD", "37bczicSkcVgJR5nSbKpvxWMU5c366d2qD");
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), R.string.donations__bitcoin_toast_copy, Toast.LENGTH_SHORT).show();
                return true;
            });
    }

    private void openDialog(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setTitle(R.string.donations__alert_dialog_title);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.setNeutralButton(R.string.donations__button_close,
                (dialog1, which) -> dialog1.dismiss()
        );
        dialog.show();
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setSubtitle(getString(R.string.drawer_donate));
        mainActivity.setSupportActionBar(toolbar);
    }

    private void donatePayPalOnClick() {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("https").authority("www.paypal.com").path("cgi-bin/webscr");
        uriBuilder.appendQueryParameter("cmd", "_donations");
        uriBuilder.appendQueryParameter("business", "premfort2@gmail.com");
        uriBuilder.appendQueryParameter("lc", "US");
        uriBuilder.appendQueryParameter("item_name", "Moko TV Donation");
        uriBuilder.appendQueryParameter("no_note", "1");
        uriBuilder.appendQueryParameter("no_shipping", "1");
        uriBuilder.appendQueryParameter("currency_code", "USD");
        Uri payPalUri = uriBuilder.build();
        if (mDebug)
            Log.d(TAG, "Opening the browser with the url: " + payPalUri.toString());
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, payPalUri);
        String title = getResources().getString(R.string.donations__paypal);
        Intent chooser = Intent.createChooser(viewIntent, title);
        if (viewIntent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            openDialog(
                    getString(R.string.donations__alert_dialog_no_browser));
        }
    }

    private void donateBitcoinOnClick(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("bitcoin:37bczicSkcVgJR5nSbKpvxWMU5c366d2qD"));
        if (mDebug)
            Log.d(TAG, "Attempting to donate Bitcoin using URI: " + i.getDataString());
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            view.findViewById(R.id.donations__bitcoin_button).performLongClick();
        }
    }
}
