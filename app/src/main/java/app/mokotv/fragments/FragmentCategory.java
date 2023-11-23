package app.mokotv.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import org.jetbrains.annotations.NotNull;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.activities.ActivityDetailCategory;
import app.mokotv.adapters.AdapterCategory;
import app.mokotv.callbacks.CallbackCategories;
import app.mokotv.models.Category;
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

public class FragmentCategory extends Fragment {

    private View root_view;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterCategory adapterCategory;
    private static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    private Call<CallbackCategories> callbackCall = null;
    private InterstitialAd interstitialAd;
    private int counter = 1;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_category, null);
        loadInterstitialAd();
        swipeRefreshLayout = root_view.findViewById(R.id.swipe_refresh_layout_category);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue, R.color.red);
        recyclerView = root_view.findViewById(R.id.recyclerViewCategory);
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager gaggeredGridLayoutManager = new StaggeredGridLayoutManager(1, 1);
        recyclerView.setLayoutManager(gaggeredGridLayoutManager);
        adapterCategory = new AdapterCategory(new ArrayList<>());
        recyclerView.setAdapter(adapterCategory);
        adapterCategory.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityDetailCategory.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
            showInterstitialAd();
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            adapterCategory.resetListData();
            requestAction();
        });
        requestAction();
        return root_view;
    }

    private void displayApiResult(final List<Category> categories) {
        adapterCategory.setListData(categories);
        swipeProgress(false);
        if (categories.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestCategoriesApi() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getAllCategories();
        callbackCall.enqueue(new Callback<CallbackCategories>() {
            @Override
            public void onResponse(@NotNull Call<CallbackCategories> call, @NotNull Response<CallbackCategories> response) {
                CallbackCategories resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.categories);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(@NotNull Call<CallbackCategories> call, @NotNull Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        showNoItemView(false);
        new Handler().postDelayed(this::requestCategoriesApi, Constant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if(callbackCall != null && callbackCall.isExecuted()){
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_category);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        (root_view.findViewById(R.id.failed_retry)).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_category);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_category_found);
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