package com.doomonafireball.masterbuilder.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * User: derek Date: 2/21/14 Time: 6:32 PM
 */
public class Step {

    @SerializedName("fileNames") public ArrayList<String> fileNames;
    @SerializedName("name") public String name;
}
