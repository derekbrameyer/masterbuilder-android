package com.doomonafireball.masterbuilder.android.api.response;

import com.doomonafireball.masterbuilder.android.R;

/**
 * User: derek Date: 5/28/13 Time: 2:06 PM
 */
public abstract class BaseResponse {

    public static final int ERROR_NONE = 0x00;
    public static final int ERROR_IO_EXCEPTION = 0x01;

    public static final int ERROR_MESSAGE_IO_EXCEPTION = R.string.io_exception;

    public String id;
    public int error = ERROR_NONE;
    public int errorMessageResId;
}
