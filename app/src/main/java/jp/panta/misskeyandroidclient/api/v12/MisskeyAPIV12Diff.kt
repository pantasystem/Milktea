package jp.panta.misskeyandroidclient.api.v12

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaDTO
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaToAdd
import retrofit2.Call
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
}