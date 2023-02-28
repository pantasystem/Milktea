package net.pantasystem.milktea.common_android.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

@Suppress("DEPRECATION")
fun Context.isWifiConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    } else {
        val networkInfo = connectivityManager.activeNetworkInfo
        networkInfo != null && networkInfo.isConnected && networkInfo.type == ConnectivityManager.TYPE_WIFI
    }
}