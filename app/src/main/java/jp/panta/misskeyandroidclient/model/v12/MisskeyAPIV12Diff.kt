package jp.panta.misskeyandroidclient.model.v12

import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPIV12Diff {

    @POST("api/antennas/create")
    fun createAntenna(@Body antenna: Antenna): Call<Unit>

    @POST("api/antennas/delete")
    fun deleteAntenna(@Body antenna: Antenna): Call<Unit>

    @POST("api/antennas/notes")
    fun antennasNotes(@Body noteRequest: NoteRequest): Call<List<Note>>

    @POST("api/antennas/show")
    fun showAntenna(@Body antenna: Antenna): Call<Unit>

    @POST("api/antennas/update")
    fun updateAntenna(@Body antenna: Antenna): Call<Unit>
}