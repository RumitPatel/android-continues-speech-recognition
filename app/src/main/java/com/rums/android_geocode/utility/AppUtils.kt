package com.rums.android_geocode.utility

import android.content.Context
import android.location.Geocoder
import android.text.TextUtils
import android.util.Log
import java.io.IOException
import java.util.Locale

const val TAG = "rum==##"

fun errorLog (msg:String?) {
    Log.e(TAG, msg!!)
}