package jp.panta.misskeyandroidclient.model.v11

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
import retrofit2.Call

open class MisskeyAPIV11(private val misskeyAPI: MisskeyAPI, private val apiDiff: MisskeyAPIV11Diff): MisskeyAPI{

    override fun blockUser(requestUser: RequestUser): Call<Unit> = misskeyAPI.blockUser(requestUser)
    override fun children(noteRequest: NoteRequest): Call<List<Note>> = misskeyAPI.children(noteRequest)
    override fun conversation(noteRequest: NoteRequest): Call<List<Note>> = misskeyAPI.conversation(noteRequest)
    override fun create(createNote: CreateNote): Call<CreateNote.Response> = misskeyAPI.create(createNote)
    override fun createApp(createApp: CreateApp): Call<App> = misskeyAPI.createApp(createApp)
    override fun createFavorite(noteRequest: NoteRequest): Call<Unit> = misskeyAPI.createFavorite(noteRequest)
    override fun createFolder(createFolder: CreateFolder): Call<Unit> = misskeyAPI.createFolder(createFolder)
    override fun createList(createList: CreateList): Call<UserList> = misskeyAPI.createList(createList)
    override fun createMessage(messageAction: MessageAction): Call<Message> = misskeyAPI.createMessage(messageAction)
    override fun createReaction(reaction: CreateReaction): Call<Unit> = misskeyAPI.createReaction(reaction)
    override fun delete(deleteNote: DeleteNote): Call<Unit> = misskeyAPI.delete(deleteNote)
    override fun deleteFavorite(noteRequest: NoteRequest): Call<Unit> = misskeyAPI.deleteFavorite(noteRequest)
    override fun deleteList(listId: ListId): Call<Unit> = misskeyAPI.deleteList(listId)
    override fun deleteMessage(messageAction: MessageAction): Call<Unit> = misskeyAPI.deleteMessage(messageAction)
    override fun deleteReaction(deleteNote: DeleteNote): Call<Unit> = misskeyAPI.deleteReaction(deleteNote)
    override fun favorites(noteRequest: NoteRequest): Call<List<Favorite>?> = misskeyAPI.favorites(noteRequest)
    override fun featured(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.featured(noteRequest)
    open fun followers(userRequest: RequestUser): Call<List<FollowFollowerUser>> = apiDiff.followers(userRequest)
    open fun following(userRequest: RequestUser): Call<List<FollowFollowerUser>> = apiDiff.following(userRequest)
    override fun getFiles(fileRequest: RequestFile): Call<List<FileProperty>> = misskeyAPI.getFiles(fileRequest)
    override fun getFolders(folderRequest: RequestFolder): Call<List<FolderProperty>> = misskeyAPI.getFolders(folderRequest)
    override fun getMessageHistory(requestMessageHistory: RequestMessageHistory): Call<List<Message>> = misskeyAPI.getMessageHistory(requestMessageHistory)
    override fun getMessages(requestMessage: RequestMessage): Call<List<Message>> = misskeyAPI.getMessages(requestMessage)
    override fun getMeta(requestMeta: RequestMeta): Call<Meta> = misskeyAPI.getMeta(requestMeta)
    override fun globalTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.globalTimeline(noteRequest)
    override fun homeTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.homeTimeline(noteRequest)
    override fun hybridTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.hybridTimeline(noteRequest)
    override fun i(i: I): Call<User> = misskeyAPI.i(i)
    override fun localTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.localTimeline(noteRequest)
    override fun mentions(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.mentions(noteRequest)
    override fun muteUser(requestUser: RequestUser): Call<Unit> = misskeyAPI.muteUser(requestUser)
    override fun followUser(requestUser: RequestUser): Call<User> = misskeyAPI.followUser(requestUser)
    override fun myApps(i: I): Call<List<App>> = misskeyAPI.myApps(i)
    override fun noteState(noteRequest: NoteRequest): Call<State> = misskeyAPI.noteState(noteRequest)
    override fun notification(notificationRequest: NotificationRequest): Call<List<Notification>?> = misskeyAPI.notification(notificationRequest)
    override fun pullUserFromList(listUserOperation: ListUserOperation): Call<Unit> = misskeyAPI.pullUserFromList(listUserOperation)
    override fun pushUserToList(listUserOperation: ListUserOperation): Call<Unit> = misskeyAPI.pushUserToList(listUserOperation)
    override fun readMessage(messageAction: MessageAction): Call<Unit> = misskeyAPI.readMessage(messageAction)
    override fun searchByTag(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.searchByTag(noteRequest)
    override fun searchNote(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.searchNote(noteRequest)
    override fun searchUser(requestUser: RequestUser): Call<List<User>> = misskeyAPI.searchUser(requestUser)
    override fun showApp(showApp: ShowApp): Call<App> = misskeyAPI.showApp(showApp)
    override fun showList(listId: ListId): Call<UserList> = misskeyAPI.showList(listId)
    override fun showNote(requestNote: NoteRequest): Call<Note> = misskeyAPI.showNote(requestNote)
    override fun showUser(requestUser: RequestUser): Call<User> = misskeyAPI.showUser(requestUser)
    override fun signIn(signIn: SignIn): Call<I> = misskeyAPI.signIn(signIn)
    override fun unFollowUser(requestUser: RequestUser): Call<User> = misskeyAPI.unFollowUser(requestUser)
    override fun unblockUser(requestUser: RequestUser): Call<Unit> = misskeyAPI.unblockUser(requestUser)
    override fun unmuteUser(requestUser: RequestUser): Call<Unit> = misskeyAPI.unmuteUser(requestUser)
    override fun updateList(createList: UpdateList): Call<Unit> = misskeyAPI.updateList(createList)
    override fun userList(i: I): Call<List<UserList>> = misskeyAPI.userList(i)
    override fun userListTimeline(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.userListTimeline(noteRequest)
    override fun userNotes(noteRequest: NoteRequest): Call<List<Note>?> = misskeyAPI.userNotes(noteRequest)
    override fun vote(vote: Vote): Call<Unit> = misskeyAPI.vote(vote)


}