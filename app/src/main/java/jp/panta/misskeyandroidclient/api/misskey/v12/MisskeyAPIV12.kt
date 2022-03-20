package jp.panta.misskeyandroidclient.api.misskey.v12

import jp.panta.misskeyandroidclient.api.misskey.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.misskey.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPI
import jp.panta.misskeyandroidclient.api.misskey.users.RequestUser
import jp.panta.misskeyandroidclient.api.misskey.v11.MisskeyAPIV11
import jp.panta.misskeyandroidclient.api.misskey.v11.MisskeyAPIV11Diff
import jp.panta.misskeyandroidclient.api.misskey.v12.antenna.AntennaDTO
import jp.panta.misskeyandroidclient.api.misskey.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.api.misskey.v12.antenna.AntennaToAdd
import jp.panta.misskeyandroidclient.api.misskey.v12.channel.*
import jp.panta.misskeyandroidclient.model.I
import retrofit2.Response

open class MisskeyAPIV12(misskey: MisskeyAPI, private val misskeyAPIV12Diff: MisskeyAPIV12Diff, misskeyAPIV11Diff: MisskeyAPIV11Diff) : MisskeyAPIV11(misskey, misskeyAPIV11Diff),
    MisskeyAPIV12Diff {



    override suspend fun antennasNotes(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskeyAPIV12Diff.antennasNotes(noteRequest)

    override suspend fun createAntenna(antennaToAdd: AntennaToAdd): Response<AntennaDTO> = misskeyAPIV12Diff.createAntenna(antennaToAdd)

    override suspend fun deleteAntenna(query: AntennaQuery): Response<Unit> = misskeyAPIV12Diff.deleteAntenna(query)

    override suspend fun getAntennas(query: AntennaQuery): Response<List<AntennaDTO>> = misskeyAPIV12Diff.getAntennas(query)

    override suspend fun showAntenna(antennaQuery: AntennaQuery): Response<AntennaDTO> = misskeyAPIV12Diff.showAntenna(antennaQuery)

    override suspend fun updateAntenna(antennaToAdd: AntennaToAdd): Response<AntennaDTO> = misskeyAPIV12Diff.updateAntenna(antennaToAdd)



    override suspend fun searchByUserNameAndHost(requestUser: RequestUser) = misskeyAPIV12Diff.searchByUserNameAndHost(requestUser)

    override suspend fun createChannel(dto: CreateChannelDTO): Response<ChannelDTO> = misskeyAPIV12Diff.createChannel(dto)
    override suspend fun featuredChannels(i: I): Response<List<ChannelDTO>> = misskeyAPIV12Diff.featuredChannels(i)

    override suspend fun followChannel(dto: FollowChannelDTO): Response<Unit> = misskeyAPIV12Diff.followChannel(dto)

    override suspend fun unFollowChannel(dto: UnFollowChannelDTO): Response<Unit> = misskeyAPIV12Diff.unFollowChannel(dto)

    override suspend fun ownedChannels(dto: FindPageable): Response<List<ChannelDTO>> = misskeyAPIV12Diff.ownedChannels(dto)
    override suspend fun showChannel(dto: ShowChannelDTO): Response<ChannelDTO> = misskeyAPIV12Diff.showChannel(dto)

    override suspend fun updateChannel(dto: UpdateChannelDTO): Response<ChannelDTO> = misskeyAPIV12Diff.updateChannel(dto)

    override suspend fun channelTimeline(dto: NoteRequest): Response<List<NoteDTO>?> = misskeyAPIV12Diff.channelTimeline(dto)

    override suspend fun followedChannels(dto: FindPageable): Response<List<ChannelDTO>> = misskeyAPIV12Diff.followedChannels(dto)
}