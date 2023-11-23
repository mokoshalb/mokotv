package app.mokotv.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import org.jetbrains.annotations.NotNull;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.activities.MainActivity;
import app.mokotv.adapters.AdapterPlaylist;
import app.mokotv.utils.M3UParser;
import app.mokotv.utils.M3UPlaylist;
import app.mokotv.utils.Tools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Objects;

public class FragmentIPTV extends Fragment implements SearchView.OnQueryTextListener {

    private InterstitialAd interstitialAd;
    private int counter = 1;
    private MainActivity mainActivity;
    private static final M3UParser parser = new M3UParser();
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterPlaylist mAdapter;
    private Toolbar toolbar;
    private static final File filepath = new File(new File(Environment.getExternalStorageDirectory().getPath() + "/.mokotv").getPath()+"/mokotv.m3u");

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View root_view = inflater.inflate(R.layout.fragment_iptv, null);
        toolbar = root_view.findViewById(R.id.toolbar);
        setupToolbar();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        RecyclerView mPlaylistList = root_view.findViewById(R.id.playlist_recycler);
        mPlaylistList.setLayoutManager(layoutManager);
        loadInterstitialAd();
        swipeRefreshLayout = root_view.findViewById(R.id.swipe_refresh_layout_home);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.red);
        mPlaylistList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mPlaylistList.setHasFixedSize(true);
        mAdapter = new AdapterPlaylist(getActivity());
        mPlaylistList.setAdapter(mAdapter);
        showInterstitialAd();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            new DownloadFileFromUrl().execute(Config.ADMIN_PANEL_URL+"/iptv.m3u");
            new _loadFile().execute(filepath.getPath());
        });
        new DownloadFileFromUrl().execute(Config.ADMIN_PANEL_URL+"/iptv.m3u");
        new _loadFile().execute(filepath.getPath());
        return root_view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.removeItem(R.id.search);
        menu.findItem(R.id.iptv_search).setVisible(true);
        MenuItem search = menu.findItem(R.id.iptv_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint("Search channel name...");
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.search).setVisible(false);
        menu.removeItem(R.id.search);
    }

    private boolean filter(String newText) {
        if (mAdapter != null) {
            if (newText.isEmpty()) {
                new _loadFile().execute(filepath.getPath());
            } else {
                mAdapter.getFilter().filter(newText);
            }
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return filter(query);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return filter(newText);
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadFileFromUrl extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... f_url) {
            try {
                URL m3u = new URL(f_url[0]);
                BufferedReader in = new BufferedReader(new InputStreamReader(m3u.openStream()));
                String inputLine;
                OutputStreamWriter myOutWriter = new FileWriter(filepath);
                while ((inputLine = in.readLine()) != null) {
                    myOutWriter.write(inputLine + "\n");
                }
                myOutWriter.flush();
                myOutWriter.close();
                in.close();
                Log.e("Fetch", "File done");
            } catch (Exception e) {
                Log.d("Fetch", "Downloading File From URL: " + e.getMessage());
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
        }

        protected void onPostExecute(String file_url) {
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class _loadFile extends AsyncTask<String, Void, Middleman> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeProgress(true);
        }

        @Override
        protected Middleman doInBackground(String... strings) {
            try {
                InputStream is = new FileInputStream(new File(strings[0]));
                M3UPlaylist playlist = parser.parseFile(is);
                Middleman m = new Middleman();
                m.playlist = playlist;
                return m;
            } catch (Exception e) {
                Log.d("Fetch", "Loading File: " + e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Middleman m) {
            mAdapter.update(m.playlist.getPlaylistItems());
            super.onPostExecute(m);
            swipeProgress(false);
        }
    }

    private class Middleman {
        M3UPlaylist playlist;
    }

    private void loadInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS) {
            MobileAds.initialize(getActivity(), getResources().getString(R.string.admob_app_id));
            interstitialAd = new InterstitialAd(Objects.requireNonNull(getActivity()));
            interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
            interstitialAd.loadAd(Tools.getAdRequest(getActivity()));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    interstitialAd.loadAd(Tools.getAdRequest(getActivity()));
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

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setSubtitle(getString(R.string.iptv));
        mainActivity.setSupportActionBar(toolbar);
    }
}