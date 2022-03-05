package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.misskey.v11.MisskeyAPIV11
import org.junit.Assert
import org.junit.Test

class APITest {

    @Test
    fun testV11Following(){
        val api = MisskeyAPIServiceBuilder.build("https://misskey.io", Version("12"))
        val v12 = api as? MisskeyAPIV11
        Assert.assertNotEquals(v12, null)

    }
}