package jp.panta.misskeyandroidclient.api.v10

import jp.panta.misskeyandroidclient.api.list.*
import jp.panta.misskeyandroidclient.api.notes.*
import jp.panta.misskeyandroidclient.api.notification.NotificationDTO
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.api.app.CreateApp
import jp.panta.misskeyandroidclient.api.app.ShowApp
import jp.panta.misskeyandroidclient.api.drive.*
import jp.panta.misskeyandroidclient.model.auth.signin.SignIn
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.hashtag.HashTag
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.messaging.MessageAction
import jp.panta.misskeyandroidclient.api.messaging.RequestMessage
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.RequestMeta
import jp.panta.misskeyandroidclient.model.notes.poll.Vote
import jp.panta.misskeyandroidclient.api.notification.NotificationRequest
import jp.panta.misskeyandroidclient.api.notes.reaction.ReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.notes.reaction.RequestReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.notes.translation.Translate
import jp.panta.misskeyandroidclient.api.notes.translation.TranslationResult
import jp.panta.misskeyandroidclient.api.sw.register.Subscription
import jp.panta.misskeyandroidclient.api.sw.register.SubscriptionState
import jp.panta.misskeyandroidclient.api.users.*
import jp.panta.misskeyandroidclient.api.users.report.ReportDTO
import jp.panta.misskeyandroidclient.model.drive.Directory
import retrofit2.Response
import retrofit2.http.Body

open class MisskeyAPIV10(val misskey: MisskeyAPI, val diff: MisskeyAPIV10Diff) : MisskeyAPI {
    override suspend fun blockUser(requestUser: RequestUser) = misskey.blockUser(requestUser)

    override suspend fun children(noteRequest: NoteRequest) = misskey.children(noteRequest)

    override suspend fun conversation(noteRequest: NoteRequest) = misskey.conversation(noteRequest)

    override suspend fun create(createNote: CreateNote): Response<CreateNote.Response> = misskey.create(createNote)
    override suspend fun createApp(createApp: CreateApp) = misskey.createApp(createApp)

    override suspend fun createFavorite(noteRequest: NoteRequest) = misskey.createFavorite(noteRequest)

    override suspend fun createMessage(messageAction: MessageAction) = misskey.createMessage(messageAction)

    override suspend fun createReaction(reaction: CreateReaction) = misskey.createReaction(reaction)

    override suspend fun mentions(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.mentions(noteRequest)

    override suspend fun delete(deleteNote: DeleteNote) = misskey.delete(deleteNote)

    override suspend fun deleteFavorite(noteRequest: NoteRequest): Response<Unit> = misskey.deleteFavorite(noteRequest)

    override suspend fun deleteMessage(messageAction: MessageAction): Response<Unit> = misskey.deleteMessage(messageAction)

    override suspend fun deleteReaction(deleteNote: DeleteNote): Response<Unit> = misskey.deleteReaction(deleteNote)

    override suspend fun favorites(noteRequest: NoteRequest): Response<List<Favorite>?> = misskey.favorites(noteRequest)

    override suspend fun featured(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.featured(noteRequest)

    override suspend fun followUser(requestUser: RequestUser): Response<UserDTO> = misskey.followUser(requestUser)

    override suspend fun acceptFollowRequest(followRequest: AcceptFollowRequest): Response<Unit> = misskey.acceptFollowRequest(followRequest)

    open suspend fun following(followFollower: RequestFollowFollower): Response<FollowFollowerUsers> = diff.following(followFollower)

    open suspend fun followers(followFollower: RequestFollowFollower): Response<FollowFollowerUsers> = diff.followers(followFollower)

    override suspend fun rejectFollowRequest(rejectFollowRequest: RejectFollowRequest): Response<Unit> = misskey.rejectFollowRequest(rejectFollowRequest)

    override suspend fun getFiles(fileRequest: RequestFile): Response<List<FilePropertyDTO>> = misskey.getFiles(fileRequest)

    override suspend fun getFolders(folderRequest: RequestFolder): Response<List<Directory>> = misskey.getFolders(folderRequest)

    override suspend fun createFolder(createFolder: CreateFolder): Response<Unit> = misskey.createFolder(createFolder)

    override suspend fun getMessageHistory(requestMessageHistory: RequestMessageHistory): Response<List<MessageDTO>> = misskey.getMessageHistory(requestMessageHistory)

    override suspend fun getMessages(requestMessage: RequestMessage): Response<List<MessageDTO>> = misskey.getMessages(requestMessage)

    override suspend fun getMeta(requestMeta: RequestMeta): Response<Meta> = misskey.getMeta(requestMeta)

    override suspend fun globalTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.globalTimeline(noteRequest)

    override suspend fun homeTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.homeTimeline(noteRequest)

    override suspend fun hybridTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.hybridTimeline(noteRequest)

    override suspend fun i(i: I): Response<UserDTO> = misskey.i(i)

    override suspend fun localTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.localTimeline(noteRequest)

    override suspend fun muteUser(requestUser: RequestUser): Response<Unit> = misskey.muteUser(requestUser)

    override suspend fun myApps(i: I): Response<List<App>> = misskey.myApps(i)

    override suspend fun noteState(noteRequest: NoteRequest): Response<State> = misskey.noteState(noteRequest)

    override suspend fun notification(notificationRequest: NotificationRequest): Response<List<NotificationDTO>?> = misskey.notification(notificationRequest)

    override suspend fun readMessage(messageAction: MessageAction): Response<Unit> = misskey.readMessage(messageAction)

    override suspend fun searchByTag(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.searchByTag(noteRequest)

    override suspend fun searchNote(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.searchNote(noteRequest)

    override suspend fun showApp(showApp: ShowApp): Response<App> = misskey.showApp(showApp)

    override suspend fun showNote(requestNote: NoteRequest): Response<NoteDTO> = misskey.showNote(requestNote)

    override suspend fun showUser(requestUser: RequestUser): Response<UserDTO> = misskey.showUser(requestUser)

    override suspend fun searchUser(requestUser: RequestUser): Response<List<UserDTO>> = misskey.searchUser(requestUser)

    override suspend fun signIn(signIn: SignIn): Response<I> = misskey.signIn(signIn)

    override suspend fun unFollowUser(requestUser: RequestUser): Response<UserDTO> = misskey.unFollowUser(requestUser)

    override suspend fun unblockUser(requestUser: RequestUser): Response<Unit> = misskey.unblockUser(requestUser)

    override suspend fun unmuteUser(requestUser: RequestUser): Response<Unit> = misskey.unmuteUser(requestUser)

    override suspend fun userNotes(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.userNotes(noteRequest)

    override suspend fun vote(vote: Vote): Response<Unit> = misskey.vote(vote)

    override suspend fun userListTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> = misskey.userListTimeline(noteRequest)

    override suspend fun createList(createList: CreateList): Response<UserListDTO> = misskey.createList(createList)

    override suspend fun deleteList(listId: ListId): Response<Unit> = misskey.deleteList(listId)

    override suspend fun showList(listId: ListId): Response<UserListDTO> = misskey.showList(listId)

    override suspend fun updateList(createList: UpdateList): Response<Unit> = misskey.updateList(createList)

    override suspend fun userList(i: I): Response<List<UserListDTO>> = misskey.userList(i)

    override suspend fun pullUserFromList(listUserOperation: ListUserOperation): Response<Unit> = misskey.pullUserFromList(listUserOperation)

    override suspend fun pushUserToList(listUserOperation: ListUserOperation): Response<Unit> = misskey.pushUserToList(listUserOperation)

    override suspend fun unrenote(deleteNote: DeleteNote): Response<Unit> = misskey.unrenote(deleteNote)

    override suspend fun getHashTagList(requestHashTagList: RequestHashTagList): Response<List<HashTag>> = misskey.getHashTagList(requestHashTagList)

    override suspend fun getUsers(requestUser: RequestUser): Response<List<UserDTO>> = misskey.getUsers(requestUser)

    override suspend fun reactions(body: RequestReactionHistoryDTO): Response<List<ReactionHistoryDTO>> = misskey.reactions(body)

    override suspend fun swRegister(subscription: Subscription): Response<SubscriptionState> = misskey.swRegister(subscription)

    override suspend fun cancelFollowRequest(@Body req: CancelFollow) : Response<UserDTO> = misskey.cancelFollowRequest(req)

    override suspend fun renotes(req: FindRenotes) : Response<List<NoteDTO>> = misskey.renotes(req)

    override suspend fun translate(req: Translate): Response<TranslationResult> = misskey.translate(req)
    override suspend fun report(req: ReportDTO): Response<Unit> = misskey.report(req)
}