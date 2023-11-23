package app.mokotv.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import org.jetbrains.annotations.NotNull;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.activities.ActivityDetailChannel;
import app.mokotv.adapters.AdapterChannel;
import app.mokotv.callbacks.CallbackChannel;
import app.mokotv.models.Channel;
import app.mokotv.rests.ApiInterface;
import app.mokotv.rests.RestAdapter;
import app.mokotv.utils.Constant;
import app.mokotv.utils.NetworkCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.mokotv.utils.Tools;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentRecent extends Fragment {

    private View root_view;
    private RecyclerView recyclerView;
    private AdapterChannel adapterChannel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackChannel> callbackCall = null;
    private int post_total = 0;
    private int failed_page = 0;
    private InterstitialAd interstitialAd;
    private int counter = 1;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_recent, null);
        loadInterstitialAd();
        swipeRefreshLayout = root_view.findViewById(R.id.swipe_refresh_layout_home);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.red);
        recyclerView = root_view.findViewById(R.id.recyclerViewHome);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        Objects.requireNonNull(getActivity()).getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        adapterChannel = new AdapterChannel(recyclerView, new ArrayList<>());
        recyclerView.setAdapter(adapterChannel);
        adapterChannel.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityDetailChannel.class);
            intent.putExtra(Constant.KEY_CHANNEL_CATEGORY, obj.category_name);
            intent.putExtra(Constant.KEY_CHANNEL_ID, obj.channel_id);
            intent.putExtra(Constant.KEY_CHANNEL_NAME, obj.channel_name);
            intent.putExtra(Constant.KEY_CHANNEL_IMAGE, obj.channel_image);
            intent.putExtra(Constant.KEY_CHANNEL_URL, obj.channel_url);
            intent.putExtra(Constant.KEY_CHANNEL_DESCRIPTION, obj.channel_description);
            intent.putExtra(Constant.KEY_CHANNEL_TYPE, obj.channel_type);
            intent.putExtra(Constant.KEY_VIDEO_ID, obj.video_id);
            startActivity(intent);
            showInterstitialAd();
        });

        adapterChannel.setOnLoadMoreListener(current_page -> {
            if (post_total > adapterChannel.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestAction(next_page);
            } else {
                adapterChannel.setLoaded();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterChannel.resetListData();
            requestAction(1);
        });
        requestAction(1);
        return root_view;
    }

    private void displayApiResult(final List<Channel> channels) {
        adapterChannel.insertData(channels);
        swipeProgress(false);
        if (channels.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getPostByPage(page_no, Config.LOAD_MORE);
        callbackCall.enqueue(new Callback<CallbackChannel>() {
            @Override
            public void onResponse(@NotNull Call<CallbackChannel> call, @NotNull Response<CallbackChannel> response) {
                CallbackChannel resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post_total = resp.count_total;
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(@NotNull Call<CallbackChannel> call, @NotNull Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }
        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        adapterChannel.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterChannel.setLoading();
        }
        new Handler().postDelayed(() -> requestListPostApi(page_no), Constant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_home);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        (root_view.findViewById(R.id.failed_retry)).setOnClickListener(view -> requestAction(failed_page));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_home);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
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
}