package com.doomonafireball.masterbuilder.android.fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import com.doomonafireball.masterbuilder.android.Datastore;
import com.doomonafireball.masterbuilder.android.R;
import com.doomonafireball.masterbuilder.android.api.CubiculusApi;
import com.doomonafireball.masterbuilder.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilder.android.api.response.BuildingInstructionsResponse;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * User: derek Date: 11/22/13 Time: 2:46 PM
 */
public class ApiFragment extends RoboSherlockFragment {

    public static interface Callback {

        void getBuildingInstructionsFinished(ArrayList<BuildingInstructions> buildingInstructions);

        void getStoredDataFinished(ArrayList<BuildingInstructions> buildingInstructions);
    }

    private static final String REFRESH_ON_CREATE = "ApiFragment_RefreshOnCreate";

    private ArrayList<BuildingInstructions> mBuildingInstructions;
    private Callback mCallback;
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

    public ArrayList<BuildingInstructions> getBuildingInstructions() {
        return mBuildingInstructions;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (Callback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
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
            mCallback.getStoredDataFinished(mBuildingInstructions);
        }
    }

    public class GetBuildingInstructionsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Get the list of building instructions
            String apiKey = getString(R.string.api_key);
            try {
                BuildingInstructionsResponse response = mCubiculusApi
                        .getBuildingInstructions(apiKey, getString(R.string.default_name), getString(R.string.group_n));
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
            mCallback.getBuildingInstructionsFinished(mBuildingInstructions);
        }
    }
}
