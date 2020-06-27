package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.model.meta.Meta
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MisskeyGetMetaTest{

    @Test
    fun getMetaTest(){
        val response = MisskeyGetMeta.getMeta("https://misskey.io").execute()

        val version = response.body()?.getVersion()

        // テスト段階ではV12だった
        assert(version?.isInRange(Version.Major.V_12) == true)
        assert(response?.body() != null)
    }
}