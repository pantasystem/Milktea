package jp.panta.misskeyandroidclient.model.v10

import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CreateApp
import jp.panta.misskeyandroidclient.model.auth.custom.ShowApp
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.drive.FolderProperty
import jp.panta.misskeyandroidclient.model.drive.RequestFile
import jp.panta.misskeyandroidclient.model.drive.RequestFolder
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
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
import retrofit2.Call

open class MisskeyAPIV10(val misskey: MisskeyAPI) : MisskeyAPI{
    override fun blockUser(requestUser: RequestUser) = misskey.blockUser(requestUser)

    override fun children(noteRequest: NoteRequest) = misskey.children(noteRequest)

    override fun conversation(noteRequest: NoteRequest) = misskey.conversation(noteRequest)

    override fun create(createNote: CreateNote): Call<CreateNote.Response> = misskey.create(createNote)
    override fun createApp(createApp: CreateApp) = misskey.createApp(createApp)

    override fun createFavorite(noteRequest: NoteRequest) = misskey.createFavorite(noteRequest)

    override fun createMessage(messageAction: MessageAction) = misskey.createMessage(messageAction)

    override fun createReaction(reaction: CreateReaction) = misskey.createReaction(reaction)

    override fun delete(deleteNote: DeleteNote) = misskey.delete(deleteNote)

    override fun deleteFavorite(noteRequest: NoteRequest): Call<Unit> = misskey.deleteFavorite(noteRequest)

    override fun deleteMessage(messageAction: MessageAction): Call<Unit> = misskey.deleteMessage(messageAction)

    override fun deleteReaction(deleteNote: DeleteNote): Call<Unit> = misskey.deleteReaction(deleteNote)

    override fun favorites(noteRequest: NoteRequest): Call<List<Favorite>?> = misskey.favorites(noteRequest)

    override fun featured(noteRequest: NoteRequest): Call<List<Note>?> = misskey.featured(noteRequest)

    override fun followUser(requestUser: RequestUser): Call<User> = misskey.followUser(requestUser)

    override fun followers(userRequest: RequestUser): Call<List<FollowFollowerUser>> = misskey.followers(userRequest)

    override fun following(userRequest: RequestUser): Call<List<FollowFollowerUser>> = misskey.following(userRequest)

    override fun getFiles(fileRequest: RequestFile): Call<List<FileProperty>> = misskey.getFiles(fileRequest)

    override fun getFolders(folderRequest: RequestFolder): Call<List<FolderProperty>> = misskey.getFolders(folderRequest)

    override fun getMessageHistory(requestMessageHistory: RequestMessageHistory): Call<List<Message>> = misskey.getMessageHistory(requestMessageHistory)

    override fun getMessages(requestMessage: RequestMessage): Call<List<Message>> = misskey.getMessages(requestMessage)

    override fun getMeta(requestMeta: RequestMeta): Call<Meta> = misskey.getMeta(requestMeta)

    override fun globalTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskey.globalTimeline(noteRequest)

    override fun homeTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskey.homeTimeline(noteRequest)

    override fun hybridTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskey.hybridTimeline(noteRequest)

    override fun i(i: I): Call<User> = misskey.i(i)

    override fun localTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskey.localTimeline(noteRequest)

    override fun muteUser(requestUser: RequestUser): Call<Unit> = misskey.muteUser(requestUser)

    override fun myApps(i: I): Call<List<App>> = misskey.myApps(i)

    override fun noteState(noteRequest: NoteRequest): Call<State> = misskey.noteState(noteRequest)

    override fun notification(notificationRequest: NotificationRequest): Call<List<Notification>?> = misskey.notification(notificationRequest)

    override fun readMessage(messageAction: MessageAction): Call<Unit> = misskey.readMessage(messageAction)

    override fun searchByTag(noteRequest: NoteRequest): Call<List<Note>?> = misskey.searchByTag(noteRequest)

    override fun searchNote(noteRequest: NoteRequest): Call<List<Note>?> = misskey.searchNote(noteRequest)

    override fun showApp(showApp: ShowApp): Call<App> = misskey.showApp(showApp)

    override fun showNote(requestNote: NoteRequest): Call<Note> = misskey.showNote(requestNote)

    override fun showUser(requestUser: RequestUser): Call<User> = misskey.showUser(requestUser)

    override fun signIn(signIn: SignIn): Call<I> = misskey.signIn(signIn)

    override fun unFollowUser(requestUser: RequestUser): Call<User> = misskey.unFollowUser(requestUser)

    override fun unblockUser(requestUser: RequestUser): Call<Unit> = misskey.unblockUser(requestUser)

    override fun unmuteUser(requestUser: RequestUser): Call<Unit> = misskey.unmuteUser(requestUser)

    override fun userNotes(noteRequest: NoteRequest): Call<List<Note>?> = misskey.userNotes(noteRequest)

    override fun vote(vote: Vote): Call<Unit> = misskey.vote(vote)

    override fun userListTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskey.userListTimeline(noteRequest)
}