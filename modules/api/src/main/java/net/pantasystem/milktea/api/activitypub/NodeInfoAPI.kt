package net.pantasystem.milktea.api.activitypub

import retrofit2.Response
import retrofit2.http.GET

interface NodeInfoAPI {

    @GET("")
    suspend fun getNodeInfo(): Response<NodeInfoDTO>
}