package jp.panta.misskeyandroidclient.api.misskey.v12

import jp.panta.misskeyandroidclient.api.misskey.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.misskey.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.misskey.users.RequestUser
import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import jp.panta.misskeyandroidclient.api.misskey.v12.antenna.AntennaDTO
import jp.panta.misskeyandroidclient.api.misskey.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.api.misskey.v12.antenna.AntennaToAdd
import jp.panta.misskeyandroidclient.api.misskey.v12.channel.*
import jp.panta.misskeyandroidclient.model.I
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV12Diff {

    @POST("api/antennas/create")
    suspend fun createAntenna(@Body antennaToAdd: AntennaToAdd): Response<AntennaDTO>

    @POST("api/antennas/delete")
    suspend fun deleteAntenna(@Body query: AntennaQuery): Response<Unit>

    @POST("api/antennas/notes")
    suspend fun antennasNotes(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/antennas/show")
    suspend fun showAntenna(@Body antennaQuery: AntennaQuery): Response<AntennaDTO>

    @POST("api/antennas/update")
    suspend fun updateAntenna(@Body antennaToAdd: AntennaToAdd): Response<AntennaDTO>

    @POST("api/antennas/list")
    suspend fun getAntennas(@Body query: AntennaQuery): Response<List<AntennaDTO>>

    @POST("api/users/search-by-username-and-host")
    suspend fun searchByUserNameAndHost(@Body requestUser: RequestUser): Response<List<UserDTO>>

    @POST("api/channels/create")
    suspend fun createChannel(@Body dto: CreateChannelDTO): Response<ChannelDTO>

    @POST("api/channels/featured")
    suspend fun featuredChannels(@Body i: I): Response<List<ChannelDTO>>

    @POST("api/channels/follow")
    suspend fun followChannel(@Body dto: FollowChannelDTO): Response<Unit>

    @POST("api/channels/unfollow")
    suspend fun unFollowChannel(@Body dto: UnFollowChannelDTO): Response<Unit>

    @POST("api/channels/followed")
    suspend fun followedChannels(@Body dto: FindPageable): Response<List<ChannelDTO>>

    @POST("api/channels/owned")
    suspend fun ownedChannels(@Body dto: FindPageable): Response<List<ChannelDTO>>

    @POST("api/channels/show")
    suspend fun showChannel(@Body dto: ShowChannelDTO): Response<ChannelDTO>

    @POST("api/channels/update")
    suspend fun updateChannel(@Body dto: UpdateChannelDTO): Response<ChannelDTO>

    @POST("api/channels/timeline")
    suspend fun channelTimeline(@Body dto: NoteRequest): Response<List<NoteDTO>?>
}