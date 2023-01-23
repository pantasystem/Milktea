package net.pantasystem.milktea.api.activitypub

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface NodeInfoAPI {

    @GET
    suspend fun getNodeInfo(@Url url: String): Response<NodeInfoDTO>
}