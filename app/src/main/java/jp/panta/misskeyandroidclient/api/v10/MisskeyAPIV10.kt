package jp.panta.misskeyandroidclient.api.v10

import jp.panta.misskeyandroidclient.api.notes.*
import jp.panta.misskeyandroidclient.api.notification.NotificationDTO
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.auth.custom.CreateApp
import jp.panta.misskeyandroidclient.model.auth.custom.ShowApp
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.model.drive.*
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.hashtag.HashTag
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.model.list.*
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.messaging.MessageAction
import jp.panta.misskeyandroidclient.api.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.poll.Vote
import jp.panta.misskeyandroidclient.api.notification.NotificationRequest
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import retrofit2.Call

open class MisskeyAPIV10(val misskey: MisskeyAPI, val diff: MisskeyAPIV10Diff) : MisskeyAPI{
    override fun blockUser(requestUser: RequestUser) = misskey.blockUser(requestUser)

    override fun children(noteRequest: NoteRequest) = misskey.children(noteRequest)

    override fun conversation(noteRequest: NoteRequest) = misskey.conversation(noteRequest)

    override fun create(createNote: CreateNote): Call<CreateNote.Response> = misskey.create(createNote)
    override fun createApp(createApp: CreateApp) = misskey.createApp(createApp)

    override fun createFavorite(noteRequest: NoteRequest) = misskey.createFavorite(noteRequest)

    override fun createMessage(messageAction: MessageAction) = misskey.createMessage(messageAction)

    override fun createReaction(reaction: CreateReaction) = misskey.createReaction(reaction)

    override fun mentions(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.mentions(noteRequest)

    override fun delete(deleteNote: DeleteNote) = misskey.delete(deleteNote)

    override fun deleteFavorite(noteRequest: NoteRequest): Call<Unit> = misskey.deleteFavorite(noteRequest)

    override fun deleteMessage(messageAction: MessageAction): Call<Unit> = misskey.deleteMessage(messageAction)

    override fun deleteReaction(deleteNote: DeleteNote): Call<Unit> = misskey.deleteReaction(deleteNote)

    override fun favorites(noteRequest: NoteRequest): Call<List<Favorite>?> = misskey.favorites(noteRequest)

    override fun featured(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.featured(noteRequest)

    override fun followUser(requestUser: RequestUser): Call<UserDTO> = misskey.followUser(requestUser)

    open fun following(followFollower: RequestFollowFollower): Call<FollowFollowerUsers> = diff.following(followFollower)

    open fun followers(followFollower: RequestFollowFollower): Call<FollowFollowerUsers> = diff.followers(followFollower)

    override fun getFiles(fileRequest: RequestFile): Call<List<FileProperty>> = misskey.getFiles(fileRequest)

    override fun getFolders(folderRequest: RequestFolder): Call<List<FolderProperty>> = misskey.getFolders(folderRequest)

    override fun createFolder(createFolder: CreateFolder): Call<Unit> = misskey.createFolder(createFolder)

    override fun getMessageHistory(requestMessageHistory: RequestMessageHistory): Call<List<MessageDTO>> = misskey.getMessageHistory(requestMessageHistory)

    override fun getMessages(requestMessage: RequestMessage): Call<List<MessageDTO>> = misskey.getMessages(requestMessage)

    override fun getMeta(requestMeta: RequestMeta): Call<Meta> = misskey.getMeta(requestMeta)

    override fun globalTimeline(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.globalTimeline(noteRequest)

    override fun homeTimeline(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.homeTimeline(noteRequest)

    override fun hybridTimeline(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.hybridTimeline(noteRequest)

    override fun i(i: I): Call<UserDTO> = misskey.i(i)

    override fun localTimeline(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.localTimeline(noteRequest)

    override fun muteUser(requestUser: RequestUser): Call<Unit> = misskey.muteUser(requestUser)

    override fun myApps(i: I): Call<List<App>> = misskey.myApps(i)

    override fun noteState(noteRequest: NoteRequest): Call<State> = misskey.noteState(noteRequest)

    override fun notification(notificationRequest: NotificationRequest): Call<List<NotificationDTO>?> = misskey.notification(notificationRequest)

    override fun readMessage(messageAction: MessageAction): Call<Unit> = misskey.readMessage(messageAction)

    override fun searchByTag(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.searchByTag(noteRequest)

    override fun searchNote(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.searchNote(noteRequest)

    override fun showApp(showApp: ShowApp): Call<App> = misskey.showApp(showApp)

    override fun showNote(requestNote: NoteRequest): Call<NoteDTO> = misskey.showNote(requestNote)

    override fun showUser(requestUser: RequestUser): Call<UserDTO> = misskey.showUser(requestUser)

    override fun searchUser(requestUser: RequestUser): Call<List<UserDTO>> = misskey.searchUser(requestUser)

    override fun signIn(signIn: SignIn): Call<I> = misskey.signIn(signIn)

    override fun unFollowUser(requestUser: RequestUser): Call<UserDTO> = misskey.unFollowUser(requestUser)

    override fun unblockUser(requestUser: RequestUser): Call<Unit> = misskey.unblockUser(requestUser)

    override fun unmuteUser(requestUser: RequestUser): Call<Unit> = misskey.unmuteUser(requestUser)

    override fun userNotes(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.userNotes(noteRequest)

    override fun vote(vote: Vote): Call<Unit> = misskey.vote(vote)

    override fun userListTimeline(noteRequest: NoteRequest): Call<List<NoteDTO>?> = misskey.userListTimeline(noteRequest)

    override fun createList(createList: CreateList): Call<UserList> = misskey.createList(createList)

    override fun deleteList(listId: ListId): Call<Unit> = misskey.deleteList(listId)

    override fun showList(listId: ListId): Call<UserList> = misskey.showList(listId)

    override fun updateList(createList: UpdateList): Call<Unit> = misskey.updateList(createList)

    override fun userList(i: I): Call<List<UserList>> = misskey.userList(i)

    override fun pullUserFromList(listUserOperation: ListUserOperation): Call<Unit> = misskey.pullUserFromList(listUserOperation)

    override fun pushUserToList(listUserOperation: ListUserOperation): Call<Unit> = misskey.pushUserToList(listUserOperation)

    override fun unrenote(deleteNote: DeleteNote): Call<Unit> = misskey.unrenote(deleteNote)

    override fun getHashTagList(requestHashTagList: RequestHashTagList): Call<List<HashTag>> = misskey.getHashTagList(requestHashTagList)

    override fun getUsers(requestUser: RequestUser): Call<List<UserDTO>> = misskey.getUsers(requestUser)
}