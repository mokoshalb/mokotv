package app.mokotv.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import com.onesignal.OneSignal;
import org.jetbrains.annotations.NotNull;
import app.mokotv.R;
import app.mokotv.activities.MainActivity;
import app.mokotv.activities.MyApplication;

public class FragmentSettings extends Fragment {
    private Toolbar toolbar;
    private MainActivity mainActivity;
    private MyApplication MyApp;

    public FragmentSettings() {
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

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View root_view = inflater.inflate(R.layout.fragment_settings, null);
        toolbar = root_view.findViewById(R.id.toolbar);
        MyApp = MyApplication.getInstance();
        Switch notificationSwitch = root_view.findViewById(R.id.switch_notification);
        Switch networkSwitch = root_view.findViewById(R.id.switch_network);
        notificationSwitch.setChecked(MyApp.getNotification());
        networkSwitch.setChecked(MyApp.getShowNetwork());
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyApp.saveIsNotification(isChecked);
            OneSignal.setSubscription(isChecked);
        });
        networkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> MyApp.saveShowNetwork(isChecked));
        setupToolbar();
        return root_view;
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setSubtitle(getString(R.string.drawer_settings));
        mainActivity.setSupportActionBar(toolbar);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
