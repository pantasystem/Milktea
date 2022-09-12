package jp.panta.misskeyandroidclient.util

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import jp.panta.misskeyandroidclient.BuildConfig
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlipperSetupManagerImpl @Inject constructor(): DebuggerSetupManager {

    private val networkFlipperPlugin: NetworkFlipperPlugin by lazy {
        NetworkFlipperPlugin()
    }

    override fun setup(context: Context) {
        SoLoader.init(context, false)
        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(context)) {
            AndroidFlipperClient.getInstance(context).apply {
                addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
                addPlugin(DatabasesFlipperPlugin(context))
                addPlugin(SharedPreferencesFlipperPlugin(context))
                addPlugin(networkFlipperPlugin)
            }.start()
        }
    }

    fun applyNetworkFlipperPlugin(okHttpBuilder: OkHttpClient.Builder) {
        okHttpBuilder.addInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
    }
}