package app.mokotv.tab;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jetbrains.annotations.NotNull;
import app.mokotv.Config;
import app.mokotv.R;
import app.mokotv.activities.MainActivity;
import app.mokotv.fragments.FragmentCategory;
import app.mokotv.fragments.FragmentRecent;

public class FragmentTabCategory extends Fragment {

    private MainActivity mainActivity;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public FragmentTabCategory() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_layout, container, false);
        tabLayout = v.findViewById(R.id.tabs);
        viewPager = v.findViewById(R.id.viewpager);
        toolbar = v.findViewById(R.id.toolbar);
        setupToolbar();
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        viewPager.setCurrentItem(1);
        if (Config.ENABLE_TAB_LAYOUT) {
            tabLayout.post(() -> tabLayout.setupWithViewPager(viewPager));
        } else {
            tabLayout.setVisibility(View.GONE);
        }
        return v;
    }

    class MyAdapter extends FragmentPagerAdapter {
        MyAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            if (Config.ENABLE_TAB_LAYOUT) {
                switch (position) {
                    case 0:
                        return new FragmentRecent();
                    case 1:
                        return new FragmentCategory();
                }
            } else {
                if (position == 0) {
                    return new FragmentCategory();
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            if (Config.ENABLE_TAB_LAYOUT) {
                return 2;
            } else {
                return 1;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (Config.ENABLE_TAB_LAYOUT) {
                switch (position) {
                    case 0:
                        return getResources().getString(R.string.tab_recent);
                    case 1:
                        return getResources().getString(R.string.tab_category);
                }
            } else {
                if (position == 0) {
                    return getResources().getString(R.string.tab_category);
                }
            }
            return null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        if (Config.ENABLE_TAB_LAYOUT) {
            Log.d("Log", "Tab Layout is Enabled");
        } else {
            toolbar.setSubtitle(getString(R.string.tab_category));
        }
            mainActivity.setSupportActionBar(toolbar);
    }
}