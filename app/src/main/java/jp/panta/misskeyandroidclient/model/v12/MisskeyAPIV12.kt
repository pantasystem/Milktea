package jp.panta.misskeyandroidclient.model.v12

import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CreateApp
import jp.panta.misskeyandroidclient.model.auth.custom.ShowApp
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.model.drive.*
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.list.*
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageAction
import jp.panta.misskeyandroidclient.model.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.meta.Meta
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.notes.poll.Vote
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.notification.NotificationRequest
import jp.panta.misskeyandroidclient.model.users.FollowFollowerUser
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.v11.MisskeyAPIV11
import jp.panta.misskeyandroidclient.model.v11.MisskeyAPIV11Diff
import jp.panta.misskeyandroidclient.model.v12.antenna.Antenna
import jp.panta.misskeyandroidclient.model.v12.antenna.AntennaQuery
import jp.panta.misskeyandroidclient.model.v12.antenna.AntennaToAdd
import retrofit2.Call
import retrofit2.http.Body

open class MisskeyAPIV12(misskey: MisskeyAPI, private val misskeyAPIV12Diff: MisskeyAPIV12Diff, misskeyAPIV11Diff: MisskeyAPIV11Diff) : MisskeyAPIV11(misskey, misskeyAPIV11Diff), MisskeyAPIV12Diff{



    override fun antennasNotes(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPIV12Diff.antennasNotes(noteRequest)

    override fun createAntenna(antennaToAdd: AntennaToAdd): Call<Antenna> = misskeyAPIV12Diff.createAntenna(antennaToAdd)

    override fun deleteAntenna(query: AntennaQuery): Call<Unit> = misskeyAPIV12Diff.deleteAntenna(query)

    override fun getAntennas(query: AntennaQuery): Call<List<Antenna>> = misskeyAPIV12Diff.getAntennas(query)

    override fun showAntenna(antennaQuery: AntennaQuery): Call<Antenna> = misskeyAPIV12Diff.showAntenna(antennaQuery)

    override fun updateAntenna(antennaToAdd: AntennaToAdd): Call<Antenna> = misskeyAPIV12Diff.updateAntenna(antennaToAdd)



    override fun searchByUserNameAndHost(requestUser: RequestUser) = misskeyAPIV12Diff.searchByUserNameAndHost(requestUser)

}