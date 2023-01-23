package net.pantasystem.milktea.api.misskey.v12

import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.users.RequestUser
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaDTO
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaQuery
import net.pantasystem.milktea.api.misskey.v12.antenna.AntennaToAdd
import net.pantasystem.milktea.api.misskey.v12.channel.*
import net.pantasystem.milktea.api.misskey.v12.user.reaction.UserReaction
import net.pantasystem.milktea.api.misskey.v12.user.reaction.UserReactionRequest
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

    @POST("api/users/reactions")
    suspend fun getUserReactions(@Body request: UserReactionRequest): Response<List<UserReaction>>
}