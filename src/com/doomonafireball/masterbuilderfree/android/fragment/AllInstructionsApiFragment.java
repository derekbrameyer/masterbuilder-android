package com.doomonafireball.masterbuilderfree.android.fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import com.doomonafireball.masterbuilderfree.android.Datastore;
import com.doomonafireball.masterbuilderfree.android.R;
import com.doomonafireball.masterbuilderfree.android.api.CubiculusApi;
import com.doomonafireball.masterbuilderfree.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilderfree.android.api.response.BuildingInstructionsResponse;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * User: derek Date: 11/22/13 Time: 2:46 PM
 */
public class AllInstructionsApiFragment extends RoboSherlockFragment {

    public static interface FetchAllInstructionsCallback {

        void getBuildingInstructionsFinished(ArrayList<BuildingInstructions> buildingInstructions);

        void getStoredDataFinished(ArrayList<BuildingInstructions> buildingInstructions);
    }

    private static final String REFRESH_ON_CREATE = "ApiFragment_RefreshOnCreate";

    private ArrayList<BuildingInstructions> mBuildingInstructions;
    private FetchAllInstructionsCallback mFetchAllInstructionsCallback;
    private GetBuildingInstructionsTask mGetBuildingInstructionsTask;
    private GetStoredDataTask mGetStoredDataTask;

    @Inject CubiculusApi mCubiculusApi;
    @Inject Datastore mDatastore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Bundle args = getArguments();
        if (args != null && args.getBoolean(REFRESH_ON_CREATE, false)) {
            mGetBuildingInstructionsTask = new GetBuildingInstructionsTask();
            mGetBuildingInstructionsTask.execute();
        } else {
            mGetStoredDataTask = new GetStoredDataTask();
            mGetStoredDataTask.execute();
        }
    }

    public static Bundle getArgs(boolean refreshOnCreate) {
        Bundle args = new Bundle();
        args.putBoolean(REFRESH_ON_CREATE, refreshOnCreate);
        return args;
    }

    public void fetchData() {
        if (mCubiculusApi != null) {
            mGetBuildingInstructionsTask = new GetBuildingInstructionsTask();
            mGetBuildingInstructionsTask.execute();
        }
    }

    public void cancelAndRefetch() {
        if (mGetBuildingInstructionsTask != null) {
            mGetBuildingInstructionsTask.cancel(true);
            mGetBuildingInstructionsTask = new GetBuildingInstructionsTask();
            mGetBuildingInstructionsTask.execute();
        }
    }

    public ArrayList<BuildingInstructions> getBuildingInstructions() {
        return mBuildingInstructions;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFetchAllInstructionsCallback = (FetchAllInstructionsCallback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFetchAllInstructionsCallback = null;
    }

    public class GetStoredDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Get the list of building instructions
            Type collectionType = new TypeToken<ArrayList<BuildingInstructions>>() {
            }.getType();
            mBuildingInstructions = new Gson()
                    .fromJson(mDatastore.getBuildingInstructionsJson(), collectionType);

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            mFetchAllInstructionsCallback.getStoredDataFinished(mBuildingInstructions);
        }
    }

    public class GetBuildingInstructionsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Get the list of building instructions
            String apiKey = mDatastore.getApiKey();
            if (TextUtils.isEmpty(apiKey)) {
                apiKey = getString(R.string.api_key);
            }
            try {
                BuildingInstructionsResponse response = mCubiculusApi
                        .getBuildingInstructions(apiKey, getString(R.string.default_name),
                                getString(R.string.group_n));
                mBuildingInstructions = response.buildingInstructions;
                mDatastore.persistBuildingInstructionsJson(new Gson().toJson(mBuildingInstructions));
                mDatastore.persistLastRefreshTimeMillis(System.currentTimeMillis());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            mFetchAllInstructionsCallback.getBuildingInstructionsFinished(mBuildingInstructions);
        }
    }
}
