package com.smarttoolfactory.opensky.utils

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtils {

    fun isOnline(context: Context?): Boolean {

        val connectivityManager = context?.applicationContext
                ?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}
