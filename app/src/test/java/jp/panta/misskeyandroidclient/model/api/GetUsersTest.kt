package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.model.users.RequestUser
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GetUsersTest {

    lateinit var misskeyAPI: MisskeyAPI
    @Before
    fun setup(){
        misskeyAPI = MisskeyAPIServiceBuilder.build("https://misskey.m544.net", Version("v10"))
    }

    @Test
    fun ascFollower(){
        val res = misskeyAPI.getUsers(
            RequestUser(
                null,
                origin = RequestUser.Origin.LOCAL.origin,
                sort = RequestUser.Sort().follower().asc(),
                state = RequestUser.State.ALIVE.state
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)
        val list = res.body()
        println(list?.map{
            it.getDisplayName() + "\n"

        })
        Assert.assertNotEquals(list, null)
    }

    @Test
    fun ascUpdatedAt(){
        val res = misskeyAPI.getUsers(
            RequestUser(
                null,
                origin = RequestUser.Origin.LOCAL.origin,
                sort = RequestUser.Sort().updatedAt().asc()
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)
        val list = res.body()
        println(list?.map{
            it.getDisplayName() + "\n"

        })
        Assert.assertNotEquals(list, null)
    }

    @Test
    fun ascNewUser(){

        val res = misskeyAPI.getUsers(
            RequestUser(
                null,
                origin = RequestUser.Origin.LOCAL.origin,
                sort = RequestUser.Sort().createdAt().asc(),
                state = RequestUser.State.ALIVE.state
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)
        val list = res.body()
        println(list?.map{
            it.getDisplayName() + "\n"

        })
        Assert.assertNotEquals(list, null)
    }

    @Test
    fun remoteAscFollower(){
        val res = misskeyAPI.getUsers(
            RequestUser(
                null,
                origin = RequestUser.Origin.REMOTE.origin,
                sort = RequestUser.Sort().follower().asc(),
                state = RequestUser.State.ALIVE.state
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)
        val list = res.body()
        println(list?.map{
            it.getDisplayName() + "\n"

        })
        Assert.assertNotEquals(list, null)
    }

    @Test
    fun remoteAscUpdatedAt(){
        val res = misskeyAPI.getUsers(
            RequestUser(
                null,
                origin = RequestUser.Origin.COMBINED.origin,
                sort = RequestUser.Sort().updatedAt().asc(),
                state = RequestUser.State.ALIVE.state
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)
        val list = res.body()
        println(list?.map{
            it.getDisplayName() + "\n"

        })
        Assert.assertNotEquals(list, null)
    }

    @Test
    fun remoteNewUsers(){
        val res = misskeyAPI.getUsers(
            RequestUser(
                null,
                origin = RequestUser.Origin.COMBINED.origin,
                sort = RequestUser.Sort().createdAt().asc()
            )
        ).execute()

        Assert.assertEquals(true, res.code() in 200 until 300)
        val list = res.body()
        println(list?.map{
            it.getDisplayName() + "\n"
        })
        Assert.assertNotEquals(list, null)
    }
}