package jp.panta.misskeyandroidclient.api.v12

import jp.panta.misskeyandroidclient.api.notes.Note
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.api.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaToAdd
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV12Diff {

    @POST("api/antennas/create")
    fun createAntenna(@Body antennaToAdd: AntennaToAdd): Call<Antenna>

    @POST("api/antennas/delete")
    fun deleteAntenna(@Body query: AntennaQuery): Call<Unit>

    @POST("api/antennas/notes")
    fun antennasNotes(@Body noteRequest: NoteRequest): Call<List<Note>?>

    @POST("api/antennas/show")
    fun showAntenna(@Body antennaQuery: AntennaQuery): Call<Antenna>

    @POST("api/antennas/update")
    fun updateAntenna(@Body antennaToAdd: AntennaToAdd): Call<Antenna>

    @POST("api/antennas/list")
    fun getAntennas(@Body query: AntennaQuery): Call<List<Antenna>>

    @POST("api/users/search-by-username-and-host")
    fun searchByUserNameAndHost(@Body requestUser: RequestUser): Call<List<User>>
}