package jp.panta.misskeyandroidclient.model.api

import org.junit.Test

import org.junit.Assert.*

class GetUrlPreviewTest {

    @Test
    fun getUrlPreview() {
        val call = GetUrlPreview.getUrlPreview("https://misskey.io", "https://misskey.io")
        val body = call.execute()
        println(body.body())
        assert(body.body() != null)
    }
}