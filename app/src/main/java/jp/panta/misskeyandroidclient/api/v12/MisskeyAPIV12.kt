package jp.panta.misskeyandroidclient.api.v12

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.v11.MisskeyAPIV11
import jp.panta.misskeyandroidclient.api.v11.MisskeyAPIV11Diff
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaDTO
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaToAdd
import retrofit2.Call

open class MisskeyAPIV12(misskey: MisskeyAPI, private val misskeyAPIV12Diff: MisskeyAPIV12Diff, misskeyAPIV11Diff: MisskeyAPIV11Diff) : MisskeyAPIV11(misskey, misskeyAPIV11Diff), MisskeyAPIV12Diff{



    override fun antennasNotes(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskeyAPIV12Diff.antennasNotes(noteRequest)

    override fun createAntenna(antennaToAdd: AntennaToAdd): Call<AntennaDTO> = misskeyAPIV12Diff.createAntenna(antennaToAdd)

    override fun deleteAntenna(query: AntennaQuery): Call<Unit> = misskeyAPIV12Diff.deleteAntenna(query)

    override fun getAntennas(query: AntennaQuery): Call<List<AntennaDTO>> = misskeyAPIV12Diff.getAntennas(query)

    override fun showAntenna(antennaQuery: AntennaQuery): Call<AntennaDTO> = misskeyAPIV12Diff.showAntenna(antennaQuery)

    override fun updateAntenna(antennaToAdd: AntennaToAdd): Call<AntennaDTO> = misskeyAPIV12Diff.updateAntenna(antennaToAdd)



    override fun searchByUserNameAndHost(requestUser: RequestUser) = misskeyAPIV12Diff.searchByUserNameAndHost(requestUser)

}