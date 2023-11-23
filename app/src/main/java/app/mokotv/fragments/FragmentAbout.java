package app.mokotv.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jetbrains.annotations.NotNull;
import app.mokotv.R;
import app.mokotv.activities.MainActivity;
import app.mokotv.adapters.AdapterAbout;
import java.util.ArrayList;
import java.util.List;

public class FragmentAbout extends Fragment {

    private Toolbar toolbar;
    private MainActivity mainActivity;

    public FragmentAbout() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View root_view = inflater.inflate(R.layout.fragment_about, null);
        toolbar = root_view.findViewById(R.id.toolbar);
        setupToolbar();
        RecyclerView recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        AdapterAbout adapterAbout = new AdapterAbout(getDataInformation(), getActivity());
        recyclerView.setAdapter(adapterAbout);
        return root_view;
    }

    private List<Data> getDataInformation() {
        List<Data> data = new ArrayList<>();
        data.add(new Data(
                R.drawable.ic_other_appname,
                getResources().getString(R.string.about_app_name),
                getResources().getString(R.string.app_name)
        ));

        data.add(new Data(
                R.drawable.ic_other_build,
                getResources().getString(R.string.about_app_version),
                getResources().getString(R.string.sub_about_app_version)
        ));

        data.add(new Data(
                R.drawable.ic_other_email,
                getResources().getString(R.string.about_app_email),
                getResources().getString(R.string.sub_about_app_email)
        ));

        data.add(new Data(
                R.drawable.ic_other_web,
                getResources().getString(R.string.about_app_web),
                getResources().getString(R.string.sub_about_app_url)
        ));
/*
        data.add(new Data(
                R.drawable.ic_other_telegram,
                getResources().getString(R.string.about_app_telegram),
                getResources().getString(R.string.sub_about_join_telegram)
        ));
*/
        data.add(new Data(
                R.drawable.ic_other_copyright,
                getResources().getString(R.string.about_app_copyright),
                getResources().getString(R.string.sub_about_app_copyright)
        ));

        data.add(new Data(
                R.drawable.ic_other_privacy,
                getResources().getString(R.string.about_app_privacy_policy),
                getResources().getString(R.string.sub_about_app_privacy_policy)
        ));

        return data;
    }

    public class Data {
        private int image;
        private String title;
        private String sub_title;

        public int getImage() {
            return image;
        }

        public String getTitle() {
            return title;
        }

        public String getSub_title() {
            return sub_title;
        }

        Data(int image, String title, String sub_title) {
            this.image = image;
            this.title = title;
            this.sub_title = sub_title;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setSubtitle(getString(R.string.drawer_about));
        mainActivity.setSupportActionBar(toolbar);
    }
}