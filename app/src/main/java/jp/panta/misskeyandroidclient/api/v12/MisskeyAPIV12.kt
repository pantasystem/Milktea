package jp.panta.misskeyandroidclient.api.v12

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.v11.MisskeyAPIV11
import jp.panta.misskeyandroidclient.api.v11.MisskeyAPIV11Diff
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaDTO
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaToAdd
import retrofit2.Response

open class MisskeyAPIV12(misskey: MisskeyAPI, private val misskeyAPIV12Diff: MisskeyAPIV12Diff, misskeyAPIV11Diff: MisskeyAPIV11Diff) : MisskeyAPIV11(misskey, misskeyAPIV11Diff), MisskeyAPIV12Diff{



    override suspend fun antennasNotes(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskeyAPIV12Diff.antennasNotes(noteRequest)

    override suspend fun createAntenna(antennaToAdd: AntennaToAdd): Response<AntennaDTO> = misskeyAPIV12Diff.createAntenna(antennaToAdd)

    override suspend fun deleteAntenna(query: AntennaQuery): Response<Unit> = misskeyAPIV12Diff.deleteAntenna(query)

    override suspend fun getAntennas(query: AntennaQuery): Response<List<AntennaDTO>> = misskeyAPIV12Diff.getAntennas(query)

    override suspend fun showAntenna(antennaQuery: AntennaQuery): Response<AntennaDTO> = misskeyAPIV12Diff.showAntenna(antennaQuery)

    override suspend fun updateAntenna(antennaToAdd: AntennaToAdd): Response<AntennaDTO> = misskeyAPIV12Diff.updateAntenna(antennaToAdd)



    override suspend fun searchByUserNameAndHost(requestUser: RequestUser) = misskeyAPIV12Diff.searchByUserNameAndHost(requestUser)

}