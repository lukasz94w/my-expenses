package com.example.myexpenses;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myexpenses.fragment.ChartsFragment;
import com.example.myexpenses.fragment.ListTransactionsFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawer;

    private Toolbar toolbar;

    public static int navItemIndex = 0;

    private static final String TAG_LIST_TRANSACTIONS = "list_transactions";
    private static final String TAG_CHARTS = "charts";
    public static String CURRENT_TAG = TAG_LIST_TRANSACTIONS;

    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);

        handler = new Handler();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_LIST_TRANSACTIONS;
        }

        setUpNavigationView();

        loadFragment();
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.list_transactions:
                    navItemIndex = 0;
                    CURRENT_TAG = TAG_LIST_TRANSACTIONS;
                    break;
                case R.id.charts:
                    navItemIndex = 1;
                    CURRENT_TAG = TAG_CHARTS;
                    break;
                case R.id.limits:
                    startActivity(new Intent(MainActivity.this, LimitsActivity.class));
                    drawer.closeDrawers();
                    return true;
                case R.id.export:
                    startActivity(new Intent(MainActivity.this, ExportActivity.class));
                    drawer.closeDrawers();
                    return true;
                case R.id.settings:
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    drawer.closeDrawers();
                    return true;
                case R.id.about:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    drawer.closeDrawers();
                    return true;
                default:
                    navItemIndex = 0;
            }
            menuItem.setChecked(!menuItem.isChecked());
            menuItem.setChecked(true);

            loadFragment();

            return true;

        });
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }


    private void loadFragment() {
        selectNavMenu();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        if (mPendingRunnable != null) {
            handler.post(mPendingRunnable);
        }

        drawer.closeDrawers();
        invalidateOptionsMenu();
    }

    private Fragment getFragment() {
        switch (navItemIndex) {
            case 1:
                return new ChartsFragment();
            default:
                return new ListTransactionsFragment();
        }
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        if (shouldLoadHomeFragOnBackPress) {
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_LIST_TRANSACTIONS;
                loadFragment();
                return;
            }
        }

        super.onBackPressed();
    }
}
