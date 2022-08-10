@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.common_android.platform

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map

@RequiresApi(Build.VERSION_CODES.N)
@ExperimentalCoroutinesApi
fun ConnectivityManager.activeNetworkFlow(): Flow<Boolean> {

    return channelFlow {

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(false)
            }


        }
        registerDefaultNetworkCallback(callback)

        awaitClose {
            unregisterNetworkCallback(callback)
        }
    }
}

@ExperimentalCoroutinesApi
fun Context.receiveNetworkEvent() : Flow<NetworkInfo?>{
    val conn = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return receive(IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)).map {
        conn.activeNetworkInfo
    }
}

@ExperimentalCoroutinesApi
fun Context.activeNetworkFlow(): Flow<Boolean> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkFlow()
    }else{
        receiveNetworkEvent().map {
            it?.isConnected == true
        }
    }
}

