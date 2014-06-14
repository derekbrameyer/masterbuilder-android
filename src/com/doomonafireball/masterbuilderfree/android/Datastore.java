package com.doomonafireball.masterbuilderfree.android;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.doomonafireball.masterbuilderfree.android.adapter.BuildingInstructionsAdapter;

import android.content.SharedPreferences;
import android.text.TextUtils;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class Datastore {

    private static final String DEVICE_VERSION = "DeviceVersion";
    private static final String BUILDING_INSTRUCTIONS_JSON = "BuildingInstructionsJson";
    private static final String LAST_REFRESH_TIME_MILLIS = "LastRefreshTimeMillis";
    private static final String SET_ID_PREPEND = "SetIdPrepend";
    private static final String COMPLETED_SETS = "CompletedSets";
    private static final String SORTING_ALGORITHM = "SortingAlgorithm";
    private static final String API_KEY = "ApiKey";

    private Gson mGson = new Gson();

    @Inject EncryptedSharedPreferences encryptedSharedPreferences;

    private SharedPreferences.Editor getEditor() {
        return encryptedSharedPreferences.edit();
    }

    private SharedPreferences getPrefs() {
        return encryptedSharedPreferences;
    }

    public int getVersion() {
        return getPrefs().getInt(DEVICE_VERSION, 0);
    }

    public void persistVersion(int version) {
        getEditor().putInt(DEVICE_VERSION, version).commit();
    }

    public int getSortingAlgorithm() {
        return getPrefs().getInt(SORTING_ALGORITHM, BuildingInstructionsAdapter.SORT_ALPHABETICAL);
    }

    public void persistSortingAlgorithm(int sortingAlgorithm) {
        getEditor().putInt(SORTING_ALGORITHM, sortingAlgorithm).commit();
    }

    public long getLastRefreshTimeMillis() {
        return getPrefs().getLong(LAST_REFRESH_TIME_MILLIS, 0l);
    }

    public void persistLastRefreshTimeMillis(long millis) {
        getEditor().putLong(LAST_REFRESH_TIME_MILLIS, millis).commit();
    }

    public String getBuildingInstructionsJson() {
        return getPrefs().getString(BUILDING_INSTRUCTIONS_JSON, "");
    }

    public void persistBuildingInstructionsJson(String buildingInstructionsJson) {
        getEditor().putString(BUILDING_INSTRUCTIONS_JSON, buildingInstructionsJson).commit();
    }

    public int getCurrentStepForSetId(String setId) {
        return getPrefs().getInt(SET_ID_PREPEND + setId, 0);
    }

    public void persistCurrentStepForSetId(String setId, int currentStep) {
        getEditor().putInt(SET_ID_PREPEND + setId, currentStep).commit();
    }

    public Set<String> getCompletedSets() {
        String completedSetsJson = getPrefs().getString(COMPLETED_SETS, null);
        if (TextUtils.isEmpty(completedSetsJson)) {
            return new HashSet<String>();
        } else {
            Type collectionType = new TypeToken<Set<String>>() {
            }.getType();
            return mGson.fromJson(completedSetsJson, collectionType);
        }
    }

    public void persistCompletedSet(String setId) {
        Set<String> currentCompletedSets = getCompletedSets();
        if (!currentCompletedSets.contains(setId)) {
            currentCompletedSets.add(setId);
        }
        getEditor().putString(COMPLETED_SETS, mGson.toJson(currentCompletedSets)).commit();
    }

    public String getApiKey() {
        return getPrefs().getString(API_KEY, null);
    }

    public void persistApiKey(String apiKey) {
        getEditor().putString(API_KEY, apiKey).commit();
    }
}