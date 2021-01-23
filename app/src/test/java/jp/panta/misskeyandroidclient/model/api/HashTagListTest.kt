package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import org.junit.Assert
import org.junit.Test

class HashTagListTest {

    @Test
    fun testM544(){
        val api  = MisskeyAPIServiceBuilder.build("https://misskey.m544.net", Version("v10"))
        val res = api.getHashTagList(
            RequestHashTagList(
                null,
                sort = RequestHashTagList.Sort().attachedLocalUsers().asc(),
                attachedToLocalUserOnly = true
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)

        val list = res.body()
        println(list)
        Assert.assertNotEquals(list, null)


    }

    @Test
    fun testV11(){
        val api  = MisskeyAPIServiceBuilder.build("https://misskey.dev", Version("v11"))
        val res = api.getHashTagList(
            RequestHashTagList(
                null,
                sort = RequestHashTagList.Sort().attachedLocalUsers().asc(),
                attachedToLocalUserOnly = true
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)

        val list = res.body()
        println(list)
        Assert.assertNotEquals(list, null)

    }

    @Test
    fun testV12(){
        val api  = MisskeyAPIServiceBuilder.build("https://misskey.io", Version("v12"))
        val res = api.getHashTagList(
            RequestHashTagList(
                null,
                sort = RequestHashTagList.Sort().attachedLocalUsers().asc(),
                attachedToLocalUserOnly = true
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)

        val list = res.body()
        println(list)
        Assert.assertNotEquals(list, null)
    }


}