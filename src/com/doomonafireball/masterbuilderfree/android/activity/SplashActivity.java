package com.doomonafireball.masterbuilderfree.android.activity;

import com.google.inject.Inject;

import com.doomonafireball.masterbuilderfree.android.Datastore;
import com.doomonafireball.masterbuilderfree.android.R;
import com.doomonafireball.masterbuilderfree.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilderfree.android.fragment.AllInstructionsApiFragment;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import roboguice.inject.InjectView;

/**
 * User: derek Date: 2/21/14 Time: 8:41 PM
 */
public class SplashActivity extends RoboSherlockFragmentActivity
        implements AllInstructionsApiFragment.FetchAllInstructionsCallback, View.OnClickListener {

    @InjectView(R.id.parent) View parent;
    @InjectView(R.id.i_have_a_key) View iHaveAKey;

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

        iHaveAKey.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.i_have_a_key:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.input_your_api_key);
                final EditText input = new EditText(this);
                input.setHint(R.string.cubiculus_com_api_key);
                alert.setView(input);
                alert.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String apiKey = input.getText().toString();
                        mDatastore.persistApiKey(apiKey);
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
                break;
        }
    }
}
