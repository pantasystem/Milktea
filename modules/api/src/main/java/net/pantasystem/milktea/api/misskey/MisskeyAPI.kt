package net.pantasystem.milktea.api.misskey

import net.pantasystem.milktea.api.misskey.ap.ApResolveRequest
import net.pantasystem.milktea.api.misskey.ap.ApResolveResult
import net.pantasystem.milktea.api.misskey.app.CreateApp
import net.pantasystem.milktea.api.misskey.auth.App
import net.pantasystem.milktea.api.misskey.clip.*
import net.pantasystem.milktea.api.misskey.drive.*
import net.pantasystem.milktea.api.misskey.favorite.Favorite
import net.pantasystem.milktea.api.misskey.hashtag.RequestHashTagList
import net.pantasystem.milktea.api.misskey.hashtag.SearchHashtagRequest
import net.pantasystem.milktea.api.misskey.list.*
import net.pantasystem.milktea.api.misskey.messaging.MessageAction
import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.api.misskey.messaging.RequestMessage
import net.pantasystem.milktea.api.misskey.notes.*
import net.pantasystem.milktea.api.misskey.notes.favorite.CreateFavorite
import net.pantasystem.milktea.api.misskey.notes.favorite.DeleteFavorite
import net.pantasystem.milktea.api.misskey.notes.mute.ToggleThreadMuteRequest
import net.pantasystem.milktea.api.misskey.notes.reaction.ReactionHistoryDTO
import net.pantasystem.milktea.api.misskey.notes.reaction.RequestReactionHistoryDTO
import net.pantasystem.milktea.api.misskey.notes.translation.Translate
import net.pantasystem.milktea.api.misskey.notes.translation.TranslationResult
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationRequest
import net.pantasystem.milktea.api.misskey.register.Subscription
import net.pantasystem.milktea.api.misskey.register.UnSubscription
import net.pantasystem.milktea.api.misskey.register.WebClientBaseRequest
import net.pantasystem.milktea.api.misskey.register.WebClientRegistries
import net.pantasystem.milktea.api.misskey.users.*
import net.pantasystem.milktea.api.misskey.users.report.ReportDTO
import net.pantasystem.milktea.api.misskey.v13.EmojisResponse
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.hashtag.HashTag
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.RequestMeta
import net.pantasystem.milktea.model.messaging.RequestMessageHistory
import net.pantasystem.milktea.model.notes.poll.Vote
import net.pantasystem.milktea.model.sw.register.SubscriptionState
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAPI {


    @POST("api/app/create")
    suspend fun createApp(@Body createApp: CreateApp): Response<App>


    @POST("api/blocking/create")
    suspend fun blockUser(@Body requestUser: RequestUser): Response<Unit>

    @POST("api/blocking/delete")
    suspend fun unblockUser(@Body requestUser: RequestUser): Response<Unit>


    @POST("api/i")
    suspend fun i(@Body i: I): Response<UserDTO>

    //users
    @POST("api/users")
    suspend fun getUsers(@Body requestUser: RequestUser): Response<List<UserDTO>>

    @POST("api/users/show")
    suspend fun showUser(@Body requestUser: RequestUser): Response<UserDTO>

    @POST("api/users/show")
    suspend fun showUsers(@Body requestUser: RequestUser): Response<List<UserDTO>>

    @POST("api/users/search")
    suspend fun searchUser(@Body requestUser: RequestUser): Response<List<UserDTO>>

    @POST("api/users/lists/list")
    suspend fun userList(@Body i: I): Response<List<UserListDTO>>

    @POST("api/users/lists/show")
    suspend fun showList(@Body listId: ListId): Response<UserListDTO>

    @POST("api/users/lists/create")
    suspend fun createList(@Body createList: CreateList): Response<UserListDTO>

    @POST("api/lists/delete")
    suspend fun deleteList(@Body listId: ListId): Response<Unit>

    @POST("api/users/lists/update")
    suspend fun updateList(@Body createList: UpdateList): Response<Unit>

    @POST("api/users/lists/push")
    suspend fun pushUserToList(@Body listUserOperation: ListUserOperation): Response<Unit>

    @POST("api/users/lists/pull")
    suspend fun pullUserFromList(@Body listUserOperation: ListUserOperation): Response<Unit>

    @POST("api/following/delete")
    suspend fun unFollowUser(@Body requestUser: RequestUser): Response<UserDTO>

    @POST("api/following/create")
    suspend fun followUser(@Body requestUser: RequestUser): Response<UserDTO>

    @POST("api/following/requests/accept")
    suspend fun acceptFollowRequest(@Body followRequest: AcceptFollowRequest) : Response<Unit>

    @POST("api/following/requests/reject")
    suspend fun rejectFollowRequest(@Body rejectFollowRequest: RejectFollowRequest) : Response<Unit>
    //account
    @POST("api/i/favorites")
    suspend fun favorites(@Body noteRequest: NoteRequest): Response<List<Favorite>?>

    @POST("api/notes/favorites/create")
    suspend fun createFavorite(@Body noteRequest: CreateFavorite): Response<Unit>

    @POST("api/notes/favorites/delete")
    suspend fun deleteFavorite(@Body noteRequest: DeleteFavorite): Response<Unit>

    @POST("api/i/notifications")
    suspend fun notification(@Body notificationRequest: NotificationRequest): Response<List<NotificationDTO>?>

    @POST("api/notes/create")
    suspend fun create(@Body createNote: CreateNote): Response<CreateNote.Response>

    @POST("api/notes/delete")
    suspend fun delete(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/reactions")
    suspend fun reactions(@Body body: RequestReactionHistoryDTO): Response<List<ReactionHistoryDTO>>

    @POST("api/notes/reactions/create")
    suspend fun createReaction(@Body reaction: CreateReactionDTO): Response<Unit>
    @POST("api/notes/reactions/delete")
    suspend fun deleteReaction(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/unrenote")
    suspend fun unrenote(@Body deleteNote: DeleteNote): Response<Unit>

    @POST("api/notes/search")
    suspend fun searchNote(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/state")
    suspend fun noteState(@Body noteRequest: NoteRequest): Response<NoteStateResponse>

    @POST("api/notes/show")
    suspend fun showNote(@Body requestNote: NoteRequest): Response<NoteDTO>

    @POST("api/notes/children")
    suspend fun children(@Body noteRequest: NoteRequest): Response<List<NoteDTO>>

    @POST("api/notes/conversation")
    suspend fun conversation(@Body noteRequest: NoteRequest): Response<List<NoteDTO>>

    @POST("api/notes/featured")
    suspend fun featured(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    //timeline
    @POST("api/notes/timeline")
    suspend fun homeTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>


    @POST("api/notes/hybrid-timeline")
    suspend fun hybridTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/local-timeline")
    suspend fun localTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/global-timeline")
    suspend fun globalTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/polls/vote")
    suspend fun vote(@Body vote: Vote) : Response<Unit>

    @POST("api/notes/search-by-tag")
    suspend fun searchByTag(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/user-list-timeline")
    suspend fun userListTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>
    //user
    @POST("api/users/notes")
    suspend fun userNotes(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/mentions")
    suspend fun mentions(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    @POST("api/notes/recommended-timeline")
    suspend fun getCalckeyRecommendedTimeline(@Body noteRequest: NoteRequest): Response<List<NoteDTO>?>

    //drive
    @POST("api/drive/files")
    suspend fun getFiles(@Body fileRequest: RequestFile): Response<List<FilePropertyDTO>>

    @POST("api/drive/files/update")
    suspend fun updateFile(@Body updateFileRequest: UpdateFileDTO): Response<FilePropertyDTO>

    @POST("api/drive/files/delete")
    suspend fun deleteFile(@Body req: DeleteFileDTO): Response<Unit>

    @POST("api/drive/files/show")
    suspend fun showFile(@Body req: ShowFile) : Response<FilePropertyDTO>

    @POST("api/drive/folders")
    suspend fun getFolders(@Body folderRequest: RequestFolder): Response<List<Directory>>

    @POST("api/drive/folders/create")
    suspend fun createFolder(@Body createFolder: CreateFolder): Response<Directory>


    //meta
    @POST("api/meta")
    suspend fun getMeta(@Body requestMeta: RequestMeta): Response<Meta>


    //message
    @POST("api/messaging/history")
    suspend fun getMessageHistory(@Body requestMessageHistory: RequestMessageHistory): Response<List<MessageDTO>>

    @POST("api/messaging/messages")
    suspend fun getMessages(@Body requestMessage: RequestMessage): Response<List<MessageDTO>>

    @POST("api/messaging/messages/create")
    suspend fun createMessage(@Body messageAction: MessageAction): Response<MessageDTO>

    @POST("api/messaging/messages/delete")
    suspend fun deleteMessage(@Body messageAction: MessageAction): Response<Unit>

    @POST("api/messaging/messages/read")
    suspend fun readMessage(@Body messageAction: MessageAction): Response<Unit>

    @POST("api/mute/create")
    suspend fun muteUser(@Body createMuteRequest: CreateMuteUserRequest): Response<Unit>

    @POST("api/mute/delete")
    suspend fun unmuteUser(@Body requestUser: RequestUser): Response<Unit>

    @POST("api/hashtags/list")
    suspend fun getHashTagList(@Body requestHashTagList: RequestHashTagList): Response<List<HashTag>>

    @POST("api/sw/register")
    suspend fun swRegister(@Body subscription: Subscription) : Response<SubscriptionState>

    @POST("api/sw/unregister")
    suspend fun swUnRegister(@Body unSub: UnSubscription) : Response<Unit>

    @POST("api/following/requests/cancel")
    suspend fun cancelFollowRequest(@Body req: CancelFollow) : Response<UserDTO>

    @POST("api/notes/renotes")
    suspend fun renotes(@Body req: FindRenotes) : Response<List<NoteDTO>>

    @POST("api/notes/translate")
    suspend fun translate(@Body req: Translate) : Response<TranslationResult>

    @POST("api/users/report-abuse")
    suspend fun report(@Body req: ReportDTO) : Response<Unit>

    @POST("api/ap/show")
    suspend fun resolve(@Body req: ApResolveRequest) : Response<ApResolveResult>

    @POST("api/notes/thread-muting/create")
    suspend fun createThreadMute(@Body req: ToggleThreadMuteRequest) : Response<Unit>

    @POST("api/notes/thread-muting/delete")
    suspend fun deleteThreadMute(@Body req: ToggleThreadMuteRequest) : Response<Unit>

    @POST("api/hashtags/search")
    suspend fun searchHashtag(@Body req: SearchHashtagRequest) : Response<List<String>>

    @POST("api/emojis")
    suspend fun getEmojis(@Body req: EmptyRequest) : Response<EmojisResponse>

    @POST("api/i/registry/get-all")
    suspend fun getReactionsFromGetAll(@Body req: WebClientBaseRequest): Response<WebClientRegistries>

    @POST("api/clips/create")
    suspend fun createClip(@Body req: CreateClipRequest): Response<ClipDTO>

    @POST("api/clips/update")
    suspend fun updateClip(@Body req: UpdateClipRequest): Response<ClipDTO>

    @POST("api/clips/delete")
    suspend fun deleteClip(@Body req: DeleteClipRequest): Response<Unit>

    @POST("api/users/clips")
    suspend fun findByUsersClip(@Body req: FindUsersClipRequest): Response<List<ClipDTO>>

    @POST("api/notes/clips")
    suspend fun findByNotesClip(@Body req: FindNotesClip): Response<List<ClipDTO>>


    @POST("api/clips/add-note")
    suspend fun addNoteToClip(@Body req: AddNoteToClipRequest): Response<Unit>

    @POST("api/clips/remove-note")
    suspend fun removeNoteToClip(@Body req: RemoveNoteToClipRequest): Response<Unit>

    @POST("api/clips/show")
    suspend fun showClip(@Body req: ShowClipRequest): Response<ClipDTO>

    @POST("api/clips/list")
    suspend fun findMyClips(@Body req: I): Response<List<ClipDTO>>

    @POST("api/clips/notes")
    suspend fun getClipNotes(@Body req: NoteRequest): Response<List<NoteDTO>?>
}