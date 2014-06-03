package com.doomonafireball.masterbuilder.android.activity;

import com.google.inject.Inject;

import com.doomonafireball.masterbuilder.android.Datastore;
import com.doomonafireball.masterbuilder.android.R;
import com.doomonafireball.masterbuilder.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilder.android.fragment.AllInstructionsApiFragment;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.View;

import java.util.ArrayList;

import roboguice.inject.InjectView;

/**
 * User: derek Date: 2/21/14 Time: 8:41 PM
 */
public class SplashActivity extends RoboSherlockFragmentActivity implements AllInstructionsApiFragment.FetchAllInstructionsCallback {

    @InjectView(R.id.parent) View parent;

    @Inject Datastore mDatastore;

    private AllInstructionsApiFragment mApiFragment;
    private Handler mHandler = new Handler();

    private static final long THREE_DAYS_IN_MILLIS = 3 * 24 * 60 * 60 * 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SystemBarTintManager.SystemBarConfig config = new SystemBarTintManager(this).getConfig();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            parent.setPadding(0, config.getPixelInsetTop(false), config.getPixelInsetRight(),
                    config.getPixelInsetBottom());
        }

        boolean fetchData = false;

        long currentTime = System.currentTimeMillis();
        long lastRefreshTime = mDatastore.getLastRefreshTimeMillis();

        if (currentTime - lastRefreshTime >= THREE_DAYS_IN_MILLIS) {
            fetchData = true;
        }

        FragmentManager fm = getSupportFragmentManager();
        mApiFragment = (AllInstructionsApiFragment) fm.findFragmentByTag("api");

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mApiFragment == null) {
            mApiFragment = new AllInstructionsApiFragment();
            Bundle args = AllInstructionsApiFragment.getArgs(fetchData);
            mApiFragment.setArguments(args);
            fm.beginTransaction().add(mApiFragment, "api").commit();
        }
    }

    public static void showErrorDialog(Context context, final AllInstructionsApiFragment apiFragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.an_error_occurred);
        builder.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                apiFragment.fetchData();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void getBuildingInstructionsFinished(ArrayList<BuildingInstructions> buildingInstructions) {
        if (buildingInstructions == null || buildingInstructions.size() == 0) {
            showErrorDialog(this, mApiFragment);
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void getStoredDataFinished(ArrayList<BuildingInstructions> buildingInstructions) {
        if (buildingInstructions == null || buildingInstructions.size() == 0) {
            showErrorDialog(this, mApiFragment);
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 1000);
    }
}
