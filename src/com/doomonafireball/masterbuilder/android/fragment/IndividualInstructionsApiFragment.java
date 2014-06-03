package com.doomonafireball.masterbuilder.android.fragment;

import com.google.inject.Inject;

import com.doomonafireball.masterbuilder.android.R;
import com.doomonafireball.masterbuilder.android.api.CubiculusApi;
import com.doomonafireball.masterbuilder.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilder.android.api.response.BuildingInstructionResponse;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.IOException;

/**
 * User: derek Date: 11/22/13 Time: 2:46 PM
 */
public class IndividualInstructionsApiFragment extends RoboSherlockFragment {

    public static interface IndividualInstructionsCallback {

        void getBuildingInstructionsFinished(BuildingInstructions buildingInstructions);
    }

    private static final String SET_ID = "IndividualInstructionsApiFragment_SetId";

    private BuildingInstructions mBuildingInstructions;
    private IndividualInstructionsCallback mIndividualInstructionsCallback;
    private GetIndividualBuildingInstructionsTask mGetBuildingInstructionsTask;

    @Inject CubiculusApi mCubiculusApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        Bundle args = getArguments();
        if (args != null) {
            mGetBuildingInstructionsTask = new GetIndividualBuildingInstructionsTask(args.getString(SET_ID));
            mGetBuildingInstructionsTask.execute();
        }
    }

    public static Bundle getArgs(BuildingInstructions buildingInstructions) {
        Bundle args = new Bundle();
        args.putString(SET_ID, buildingInstructions.idInstruction);
        return args;
    }

    public void fetchData(String setId) {
        if (mCubiculusApi != null) {
            mGetBuildingInstructionsTask = new GetIndividualBuildingInstructionsTask(setId);
            mGetBuildingInstructionsTask.execute();
        }
    }

    public BuildingInstructions getBuildingInstructions() {
        return mBuildingInstructions;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mIndividualInstructionsCallback = (IndividualInstructionsCallback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIndividualInstructionsCallback = null;
    }

    public class GetIndividualBuildingInstructionsTask extends AsyncTask<Void, Void, Void> {

        private String setId;

        public GetIndividualBuildingInstructionsTask(String setId) {
            this.setId = setId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Get the list of building instructions
            String apiKey = getString(R.string.api_key);
            try {
                BuildingInstructionResponse response = mCubiculusApi
                        .getBuildingInstructions(apiKey, setId, getString(R.string.default_name),
                                getString(R.string.group_n));
                mBuildingInstructions = response.buildingInstructions;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            mIndividualInstructionsCallback.getBuildingInstructionsFinished(mBuildingInstructions);
        }
    }
}
