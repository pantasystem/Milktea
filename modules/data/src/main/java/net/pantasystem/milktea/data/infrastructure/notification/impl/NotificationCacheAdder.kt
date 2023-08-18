package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.data.converters.NotificationDTOEntityConverter
import net.pantasystem.milktea.data.converters.TootDTOEntityConverter
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.data.infrastructure.toModel
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.markers.MarkerRepository
import net.pantasystem.milktea.model.markers.MarkerType
import net.pantasystem.milktea.model.note.NoteRelationGetter
import net.pantasystem.milktea.model.notification.NotificationDataSource
import net.pantasystem.milktea.model.notification.NotificationRelation
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCacheAdder @Inject constructor(
    private val userDataSource: UserDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val noteRelationGetter: NoteRelationGetter,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val groupDataSource: GroupDataSource,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    private val notificationDTOEntityConverter: NotificationDTOEntityConverter,
    private val markerRepository: MarkerRepository,
    private val tootDTOEntityConverter: TootDTOEntityConverter,
) {
    suspend fun addAndConvert(account: Account, notificationDTO: NotificationDTO, skipExists: Boolean = false): NotificationRelation {
        val user = notificationDTO.user?.let {
            userDTOEntityConverter.convert(account, it)
        }
        if (user != null) {
            if (!skipExists || userDataSource.get(user.id).isFailure) {
                userDataSource.add(user)
            }
        }
        val noteRelation = notificationDTO.note?.let{
            noteRelationGetter.get(noteDataSourceAdder.addNoteDtoToDataSource(account, it, skipExists))
        }
        val notification = notificationDTOEntityConverter.convert(notificationDTO, account)

        if (!skipExists || notificationDataSource.get(notification.id).isFailure) {
            notificationDataSource.add(notification)
        }

        notificationDTO.invitation?.group?.toGroup(account.accountId)?.let { group ->
            if (!skipExists || groupDataSource.find(group.id).isFailure) {
                groupDataSource.add(group)
            }
        }
        return NotificationRelation(
            notification,
            user,
            noteRelation?.getOrNull()
        )
    }

    suspend fun addConvert(account: Account, mstNotificationDTO: MstNotificationDTO, skipExists: Boolean = false): NotificationRelation {
        val lastReadId = markerRepository.find(account.accountId, listOf(MarkerType.Notifications)).map {
            it.notifications?.lastReadId ?: ""
        }.getOrElse {
            ""
        }
        val user = mstNotificationDTO.account.toModel(account)
        val noteRelation = mstNotificationDTO.status?.let {
            tootDTOEntityConverter.convert(it, account)
        }?.let {
            noteRelationGetter.get(it)
        }

        val notification = mstNotificationDTO.toModel(account, isRead = lastReadId >= mstNotificationDTO.id)
        mstNotificationDTO.status?.let {
            noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, it, skipExists)
        }

        if (!skipExists || notificationDataSource.get(notification.id).isFailure) {
            notificationDataSource.add(notification)
        }

        return NotificationRelation(
            notification = notification,
            user = user,
            note = noteRelation?.getOrNull(),
        )
    }
}