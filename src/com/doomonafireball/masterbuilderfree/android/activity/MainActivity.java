package com.doomonafireball.masterbuilderfree.android.activity;

import com.google.android.gms.common.SignInButton;
import com.google.gson.Gson;
import com.google.inject.Inject;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.doomonafireball.masterbuilderfree.android.Datastore;
import com.doomonafireball.masterbuilderfree.android.R;
import com.doomonafireball.masterbuilderfree.android.adapter.BuildingInstructionsAdapter;
import com.doomonafireball.masterbuilderfree.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilderfree.android.fragment.AllInstructionsApiFragment;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import roboguice.inject.InjectView;

public class MainActivity extends BaseGameActivity
        implements AllInstructionsApiFragment.FetchAllInstructionsCallback, View.OnClickListener {

    @InjectView(R.id.progress) private ProgressBar progress;
    @InjectView(R.id.grid_view) private GridView gridView;
    @InjectView(R.id.sign_in_button) private SignInButton signInButton;
    @InjectView(R.id.sign_in_container) private View signInButtonContainer;

    @Inject Datastore mDatastore;

    private static final int ACHIEVEMENTS_INTENT_CODE = 42;

    private AllInstructionsApiFragment mApiFragment;
    private Gson mGson = new Gson();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.GONE);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarAlpha(1.0f);
        tintManager.setStatusBarTintColor(Color.parseColor("#222222"));
        int padding = getResources().getDimensionPixelSize(R.dimen.default_padding);
        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            gridView.setPadding(padding, padding + config.getPixelInsetTop(true), padding + config.getPixelInsetRight(),
                    padding + config.getPixelInsetBottom());
            signInButtonContainer.setPadding(0, 0, config.getPixelInsetRight(), config.getPixelInsetBottom());
        } else {
            gridView.setPadding(padding, padding, padding, padding);
        }

        FragmentManager fm = getSupportFragmentManager();
        mApiFragment = (AllInstructionsApiFragment) fm.findFragmentByTag("api");

        Set<String> completedSets = mDatastore.getCompletedSets();
        int sortingAlgorithm = mDatastore.getSortingAlgorithm();

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mApiFragment == null) {
            mApiFragment = new AllInstructionsApiFragment();
            Bundle args = AllInstructionsApiFragment.getArgs(false);
            mApiFragment.setArguments(args);
            fm.beginTransaction().add(mApiFragment, "api").commit();
            gridView.setAdapter(new BuildingInstructionsAdapter(this, null, completedSets, sortingAlgorithm));
        } else if (mApiFragment.getBuildingInstructions() != null) {
            gridView.setAdapter(
                    new BuildingInstructionsAdapter(this, mApiFragment.getBuildingInstructions(), completedSets,
                            sortingAlgorithm));
            progress.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        } else {
            gridView.setAdapter(new BuildingInstructionsAdapter(this, null, completedSets, sortingAlgorithm));
        }
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BuildingInstructions bi = (BuildingInstructions) adapterView.getAdapter().getItem(i);
                startActivity(StepsActivity.getIntent(MainActivity.this, mGson, bi));
            }
        });

        signInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signInButton.setEnabled(false);
                beginUserInitiatedSignIn();
                return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BuildingInstructionsAdapter) gridView.getAdapter()).setCompletedSets(mDatastore.getCompletedSets());
        ((BuildingInstructionsAdapter) gridView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void getBuildingInstructionsFinished(ArrayList<BuildingInstructions> buildingInstructions) {
        setSupportProgressBarIndeterminateVisibility(false);
        if (buildingInstructions == null || buildingInstructions.size() == 0) {
            SplashActivity.showErrorDialog(this, mApiFragment);
            return;
        }
        ((BuildingInstructionsAdapter) gridView.getAdapter()).setData(buildingInstructions);
        ((BuildingInstructionsAdapter) gridView.getAdapter()).notifyDataSetChanged();

        progress.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);
    }

    @Override
    public void getStoredDataFinished(ArrayList<BuildingInstructions> buildingInstructions) {
        setSupportProgressBarIndeterminateVisibility(false);
        if (buildingInstructions == null || buildingInstructions.size() == 0) {
            SplashActivity.showErrorDialog(this, mApiFragment);
            return;
        }
        ((BuildingInstructionsAdapter) gridView.getAdapter()).setData(buildingInstructions);
        ((BuildingInstructionsAdapter) gridView.getAdapter()).notifyDataSetChanged();

        progress.setVisibility(View.GONE);
        gridView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_activity_menu, menu);
        menu.findItem(R.id.sign_out).setVisible(getGamesClient().isConnected());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                mApiFragment.fetchData();
                setSupportProgressBarIndeterminateVisibility(true);
                return true;
            /*
            case R.id.sort_alphabetically:
                mDatastore.persistSortingAlgorithm(BuildingInstructionsAdapter.SORT_ALPHABETICAL);
                ((BuildingInstructionsAdapter) gridView.getAdapter()).setSortingAlgorithm(
                        BuildingInstructionsAdapter.SORT_ALPHABETICAL);
                ((BuildingInstructionsAdapter) gridView.getAdapter()).notifyDataSetChanged();
                return true;
            case R.id.sort_by_steps_ascending:
                mDatastore.persistSortingAlgorithm(BuildingInstructionsAdapter.SORT_STEPS_ASCENDING);
                ((BuildingInstructionsAdapter) gridView.getAdapter()).setSortingAlgorithm(
                        BuildingInstructionsAdapter.SORT_STEPS_ASCENDING);
                ((BuildingInstructionsAdapter) gridView.getAdapter()).notifyDataSetChanged();
                return true;
            case R.id.sort_by_steps_descending:
                mDatastore.persistSortingAlgorithm(BuildingInstructionsAdapter.SORT_STEPS_DESCENDING);
                ((BuildingInstructionsAdapter) gridView.getAdapter())
                        .setSortingAlgorithm(BuildingInstructionsAdapter.SORT_STEPS_DESCENDING);
                ((BuildingInstructionsAdapter) gridView.getAdapter()).notifyDataSetChanged();
                return true;
                */
            case R.id.achievements:
                if (getGamesClient().isConnected()) {
                    startActivityForResult(getGamesClient().getAchievementsIntent(), ACHIEVEMENTS_INTENT_CODE);
                } else {
                    Toast.makeText(this, R.string.sign_in_to_gplus, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.sign_out:
                signOut();
                invalidateOptionsMenu();
                signInButton.setVisibility(View.VISIBLE);
                signInButton.setEnabled(true);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSignInFailed() {
        signInButton.setVisibility(View.VISIBLE);
        signInButton.setEnabled(true);
        invalidateOptionsMenu();
    }

    @Override
    public void onSignInSucceeded() {
        signInButton.setVisibility(View.GONE);
        signInButton.setEnabled(false);
        invalidateOptionsMenu();
    }
}

