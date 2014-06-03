package com.doomonafireball.masterbuilder.android.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import com.doomonafireball.masterbuilder.android.api.model.BuildingInstructions;
import com.doomonafireball.masterbuilder.android.api.model.Step;
import com.doomonafireball.masterbuilder.android.api.response.BuildingInstructionResponse;
import com.doomonafireball.masterbuilder.android.api.response.BuildingInstructionsResponse;
import com.squareup.okhttp.OkHttpClient;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * User: derek Date: 11/22/13 Time: 2:27 PM
 */
public class CubiculusApi {

    private OkHttpClient mOkHttpClient;
    private Gson mGson;

    private static final String BUILDING_INSTRUCTIONS_URL = "http://www.cubiculus.com/api-rest/building-instruction/%s";
    private static final String BUILDING_INSTRUCTION_URL
            = "http://www.cubiculus.com/api-rest/building-instruction/%s/%s";

    @Inject
    public CubiculusApi() {
        mOkHttpClient = new OkHttpClient();
    }

    public Gson getGson() {
        mGson = new Gson();
        return mGson;
    }

    private <T> T readJsonToObject(HttpURLConnection connection, Type type) throws IOException, JsonSyntaxException {
        InputStream inputStream = connection.getInputStream();
        T response = getGson().fromJson(new InputStreamReader(inputStream), type);
        connection.disconnect();
        return response;
    }

    public BuildingInstructionsResponse getBuildingInstructions(String apiKey, String defaultName, String groupN)
            throws IOException {
        HttpURLConnection connection = mOkHttpClient.open(new URL(String.format(BUILDING_INSTRUCTIONS_URL, apiKey)));
        Type collectionType = new TypeToken<ArrayList<BuildingInstructions>>() {
        }.getType();
        BuildingInstructionsResponse response = new BuildingInstructionsResponse();
        response.buildingInstructions = readJsonToObject(connection, collectionType);
        for (BuildingInstructions bi : response.buildingInstructions) {
            parseIndividualBuildingInstructions(bi, defaultName, groupN);
        }
        return response;
    }

    public BuildingInstructionResponse getBuildingInstructions(String apiKey, String id, String defaultName,
            String groupN) throws IOException {
        HttpURLConnection connection = mOkHttpClient.open(new URL(String.format(BUILDING_INSTRUCTION_URL, apiKey, id)));
        BuildingInstructionResponse response = new BuildingInstructionResponse();
        response.buildingInstructions = readJsonToObject(connection, BuildingInstructions.class);
        parseIndividualBuildingInstructions(response.buildingInstructions, defaultName, groupN);
        return response;
    }

    public static void parseIndividualBuildingInstructions(BuildingInstructions buildingInstructions,
            String defaultName, String groupN) {
        if (buildingInstructions.steps != null) {
            int numSteps = 0;
            for (int i = 0; i < buildingInstructions.steps.size(); i++) {
                Step s = buildingInstructions.steps.get(i);
                if (TextUtils.isEmpty(s.name) || s.name.equals("null")) {
                    s.name = String.format(groupN, (i + 1));
                }
                numSteps += s.fileNames.size();
            }
            buildingInstructions.stepsCount = numSteps;
        }
        if (TextUtils.isEmpty(buildingInstructions.description)) {
            buildingInstructions.description = defaultName;
        }
    }
}
