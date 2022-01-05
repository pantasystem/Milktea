package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.api.MisskeyGetMeta
import org.junit.Test

class MisskeyGetMetaTest{

    @Test
    fun getMetaTest(){
        val response = MisskeyGetMeta.getMeta("https://misskey.io").execute()

        val version = response.body()?.getVersion()

        // テスト段階ではV12だった
        assert(version?.isInRange(Version.Major.V_12) == true)
        assert(response.body() != null)
    }
}