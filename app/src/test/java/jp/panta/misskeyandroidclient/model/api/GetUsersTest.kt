package jp.panta.misskeyandroidclient.model.api

class GetUsersTest {

//    private lateinit var misskeyAPI: MisskeyAPI
//    @Before
//    fun setup(){
//        misskeyAPI = MisskeyAPIServiceBuilder.build("https://misskey.m544.net", Version("v10"))
//    }
//
//    @Test
//    suspend fun ascFollower(){
//        val res = misskeyAPI.getUsers(
//            RequestUser(
//                null,
//                origin = RequestUser.Origin.LOCAL.origin,
//                sort = RequestUser.Sort().follower().asc(),
//                state = RequestUser.State.ALIVE.state
//            )
//        )
//
//        Assert.assertEquals(true, res.code() in 200 until 300)
//        val list = res.body()
//        println(list?.map{
//            it.displayName + "\n"
//
//        })
//        Assert.assertNotEquals(list, null)
//    }
//
//    @Test
//    suspend fun ascUpdatedAt(){
//        val res = misskeyAPI.getUsers(
//            RequestUser(
//                null,
//                origin = RequestUser.Origin.LOCAL.origin,
//                sort = RequestUser.Sort().updatedAt().asc()
//            )
//        )
//
//        Assert.assertEquals(true, res.code() in 200 until 300)
//        val list = res.body()
//        println(list?.map{
//            it.displayName + "\n"
//
//        })
//        Assert.assertNotEquals(list, null)
//    }
//
//    @Test
//    suspend fun ascNewUser(){
//
//        val res = misskeyAPI.getUsers(
//            RequestUser(
//                null,
//                origin = RequestUser.Origin.LOCAL.origin,
//                sort = RequestUser.Sort().createdAt().asc(),
//                state = RequestUser.State.ALIVE.state
//            )
//        )
//
//        Assert.assertEquals(true, res.code() in 200 until 300)
//        val list = res.body()
//        println(list?.map{
//            it.displayName + "\n"
//
//        })
//        Assert.assertNotEquals(list, null)
//    }
//
//    @Test
//    suspend fun remoteAscFollower(){
//        val res = misskeyAPI.getUsers(
//            RequestUser(
//                null,
//                origin = RequestUser.Origin.REMOTE.origin,
//                sort = RequestUser.Sort().follower().asc(),
//                state = RequestUser.State.ALIVE.state
//            )
//        )
//
//        Assert.assertEquals(true, res.code() in 200 until 300)
//        val list = res.body()
//        println(list?.map{
//            it.displayName + "\n"
//
//        })
//        Assert.assertNotEquals(list, null)
//    }
//
//    @Test
//    suspend fun remoteAscUpdatedAt(){
//        val res = misskeyAPI.getUsers(
//            RequestUser(
//                null,
//                origin = RequestUser.Origin.COMBINED.origin,
//                sort = RequestUser.Sort().updatedAt().asc(),
//                state = RequestUser.State.ALIVE.state
//            )
//        )
//
//        Assert.assertEquals(true, res.code() in 200 until 300)
//        val list = res.body()
//        println(list?.map{
//            it.displayName + "\n"
//
//        })
//        Assert.assertNotEquals(list, null)
//    }
//
//    @Test
//    suspend fun remoteNewUsers(){
//        val res = misskeyAPI.getUsers(
//            RequestUser(
//                null,
//                origin = RequestUser.Origin.COMBINED.origin,
//                sort = RequestUser.Sort().createdAt().asc()
//            )
//        )
//
//        Assert.assertEquals(true, res.code() in 200 until 300)
//        val list = res.body()
//        println(list?.map{
//            it.displayName + "\n"
//        })
//        Assert.assertNotEquals(list, null)
//    }
}