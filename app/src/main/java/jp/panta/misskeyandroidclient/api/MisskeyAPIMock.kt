package jp.panta.misskeyandroidclient.api

import jp.panta.misskeyandroidclient.api.app.CreateApp
import jp.panta.misskeyandroidclient.api.drive.*
import jp.panta.misskeyandroidclient.api.list.*
import jp.panta.misskeyandroidclient.api.messaging.MessageAction
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.messaging.RequestMessage
import jp.panta.misskeyandroidclient.api.notes.*
import jp.panta.misskeyandroidclient.api.notes.reaction.ReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.notes.reaction.RequestReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.notes.translation.Translate
import jp.panta.misskeyandroidclient.api.notes.translation.TranslationResult
import jp.panta.misskeyandroidclient.api.notification.NotificationDTO
import jp.panta.misskeyandroidclient.api.notification.NotificationRequest
import jp.panta.misskeyandroidclient.api.sw.register.Subscription
import jp.panta.misskeyandroidclient.api.sw.register.SubscriptionState
import jp.panta.misskeyandroidclient.api.sw.register.UnSubscription
import jp.panta.misskeyandroidclient.api.users.*
import jp.panta.misskeyandroidclient.api.users.report.ReportDTO
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.drive.Directory
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.hashtag.HashTag
import jp.panta.misskeyandroidclient.model.hashtag.RequestHashTagList
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.RequestMeta
import jp.panta.misskeyandroidclient.model.messaging.RequestMessageHistory
import jp.panta.misskeyandroidclient.model.notes.poll.Vote
import kotlinx.datetime.Clock
import retrofit2.Response
import retrofit2.mock.BehaviorDelegate

class MisskeyAPIMock(
    private val delegate: BehaviorDelegate<MisskeyAPI>
) : MisskeyAPI {


    override suspend fun createApp(createApp: CreateApp): Response<App> {
        return delegate.returningResponse(App(
            name = createApp.name
        )).createApp(createApp)
    }

    override suspend fun blockUser(requestUser: RequestUser): Response<Unit> {
        return delegate.returningResponse(Unit).blockUser(requestUser)
    }

    override suspend fun unblockUser(requestUser: RequestUser): Response<Unit> {
        return delegate.returningResponse(Unit).unblockUser(requestUser)
    }

    override suspend fun i(i: I): Response<UserDTO> {
        return delegate.returningResponse(UserDTO(
            userName = "me",
            id = "me"
        )).i(i)
    }

    override suspend fun getUsers(requestUser: RequestUser): Response<List<UserDTO>> {
        return delegate.returningResponse(emptyList<UserDTO>()).getUsers(requestUser)
    }

    override suspend fun showUser(requestUser: RequestUser): Response<UserDTO> {
        return delegate.returningResponse(UserDTO(id = requestUser.userId ?: "", userName = ""))
            .showUser(requestUser)
    }

    override suspend fun searchUser(requestUser: RequestUser): Response<List<UserDTO>> {
        return delegate.returningResponse(emptyList<UserDTO>()).searchUser(requestUser)

    }

    override suspend fun userList(i: I): Response<List<UserListDTO>> {
        return delegate.returningResponse(emptyList<UserListDTO>()).userList(i)
    }

    override suspend fun showList(listId: ListId): Response<UserListDTO> {
        return delegate.returningResponse(UserListDTO(id = "listId", createdAt = Clock.System.now(), name = "userList", userIds = emptyList()))
            .showList(listId)
    }

    override suspend fun createList(createList: CreateList): Response<UserListDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteList(listId: ListId): Response<Unit> {
        return delegate.returningResponse(Unit).deleteList(listId)
    }

    override suspend fun updateList(createList: UpdateList): Response<Unit> {
        return delegate.returningResponse(Unit).updateList(createList)
    }

    override suspend fun pushUserToList(listUserOperation: ListUserOperation): Response<Unit> {
        return delegate.returningResponse(Unit).pushUserToList(listUserOperation)
    }

    override suspend fun pullUserFromList(listUserOperation: ListUserOperation): Response<Unit> {
        return delegate.returningResponse(Unit).pullUserFromList(listUserOperation)
    }

    override suspend fun unFollowUser(requestUser: RequestUser): Response<UserDTO> {
        return delegate.returningResponse(UserDTO("id", "username"))
            .unFollowUser(requestUser)
    }

    override suspend fun followUser(requestUser: RequestUser): Response<UserDTO> {
        return delegate.returningResponse(UserDTO("id", "username"))
            .unFollowUser(requestUser)
    }

    override suspend fun acceptFollowRequest(followRequest: AcceptFollowRequest): Response<Unit> {
        return delegate.returningResponse(Unit).acceptFollowRequest(followRequest)
    }

    override suspend fun rejectFollowRequest(rejectFollowRequest: RejectFollowRequest): Response<Unit> {
        return delegate.returningResponse(Unit).rejectFollowRequest(rejectFollowRequest)
    }

    override suspend fun favorites(noteRequest: NoteRequest): Response<List<Favorite>?> {
        return delegate.returningResponse(emptyList<Favorite>()).favorites(noteRequest)
    }

    override suspend fun createFavorite(noteRequest: NoteRequest): Response<Unit> {
        return delegate.returningResponse(Unit).createFavorite(noteRequest)
    }

    override suspend fun deleteFavorite(noteRequest: NoteRequest): Response<Unit> {
        return delegate.returningResponse(Unit).deleteFavorite(noteRequest)
    }

    override suspend fun notification(notificationRequest: NotificationRequest): Response<List<NotificationDTO>?> {
        return delegate.returningResponse(emptyList<NotificationDTO>())
            .notification(notificationRequest)
    }

    override suspend fun create(createNote: CreateNote): Response<CreateNote.Response> {
        TODO()
    }

    override suspend fun delete(deleteNote: DeleteNote): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun reactions(body: RequestReactionHistoryDTO): Response<List<ReactionHistoryDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun createReaction(reaction: CreateReaction): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReaction(deleteNote: DeleteNote): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun unrenote(deleteNote: DeleteNote): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun searchNote(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun noteState(noteRequest: NoteRequest): Response<State> {
        TODO("Not yet implemented")
    }

    override suspend fun showNote(requestNote: NoteRequest): Response<NoteDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun children(noteRequest: NoteRequest): Response<List<NoteDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun conversation(noteRequest: NoteRequest): Response<List<NoteDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun featured(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun homeTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun hybridTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun localTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun globalTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun vote(vote: Vote): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun searchByTag(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun userListTimeline(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun userNotes(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun mentions(noteRequest: NoteRequest): Response<List<NoteDTO>?> {
        TODO("Not yet implemented")
    }

    override suspend fun getFiles(fileRequest: RequestFile): Response<List<FilePropertyDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateFile(updateFileRequest: UpdateFileDTO): Response<FilePropertyDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFile(req: DeleteFileDTO): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun showFile(req: ShowFile): Response<FilePropertyDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun getFolders(folderRequest: RequestFolder): Response<List<Directory>> {
        TODO("Not yet implemented")
    }

    override suspend fun createFolder(createFolder: CreateFolder): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getMeta(requestMeta: RequestMeta): Response<Meta> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessageHistory(requestMessageHistory: RequestMessageHistory): Response<List<MessageDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessages(requestMessage: RequestMessage): Response<List<MessageDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun createMessage(messageAction: MessageAction): Response<MessageDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(messageAction: MessageAction): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun readMessage(messageAction: MessageAction): Response<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun muteUser(requestUser: RequestUser): Response<Unit> {
        return delegate.returningResponse(Unit).muteUser(requestUser)
    }

    override suspend fun unmuteUser(requestUser: RequestUser): Response<Unit> {
        return delegate.returningResponse(Unit).unmuteUser(requestUser)
    }

    override suspend fun getHashTagList(requestHashTagList: RequestHashTagList): Response<List<HashTag>> {
        TODO("Not yet implemented")
    }

    override suspend fun swRegister(subscription: Subscription): Response<SubscriptionState> {
        TODO("Not yet implemented")
    }

    override suspend fun swUnRegister(unSub: UnSubscription): Response<Unit> {
        return delegate.returningResponse(Unit).swUnRegister(unSub)
    }

    override suspend fun cancelFollowRequest(req: CancelFollow): Response<UserDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun renotes(req: FindRenotes): Response<List<NoteDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun translate(req: Translate): Response<TranslationResult> {
        TODO("Not yet implemented")
    }

    override suspend fun report(req: ReportDTO): Response<Unit> {
        return delegate.returningResponse(Unit).report(req)
    }

}