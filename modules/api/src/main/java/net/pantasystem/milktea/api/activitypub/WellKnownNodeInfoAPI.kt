package net.pantasystem.milktea.api.activitypub

import retrofit2.Response
import retrofit2.http.GET

interface WellKnownNodeInfoAPI {
    @GET(".well-known/nodeinfo")
    suspend fun getWellKnownNodeInfo(): Response<WellKnownNodeInfo>

}