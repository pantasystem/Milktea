package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.data.converters.NotificationDTOEntityConverter
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.data.infrastructure.toModel
import net.pantasystem.milktea.data.infrastructure.toNote
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.notes.NoteRelationGetter
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
    private val nodeInfoRepository: NodeInfoRepository,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    private val notificationDTOEntityConverter: NotificationDTOEntityConverter,
) {
    suspend fun addAndConvert(account: Account, notificationDTO: NotificationDTO): NotificationRelation {
        val user = notificationDTO.user?.let {
            userDTOEntityConverter.convert(account, it)
        }
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        if (user != null) {
            userDataSource.add(user)
        }
        val noteRelation = notificationDTO.note?.let{
            noteRelationGetter.get(noteDataSourceAdder.addNoteDtoToDataSource(account, it))
        }
        val notification = notificationDTOEntityConverter.convert(notificationDTO, account, nodeInfo)
        notificationDataSource.add(notification)
        notificationDTO.invitation?.group?.toGroup(account.accountId)?.let { group ->
            groupDataSource.add(group)
        }
        return NotificationRelation(
            notification,
            user,
            noteRelation?.getOrNull()
        )
    }

    suspend fun addConvert(account: Account, mstNotificationDTO: MstNotificationDTO): NotificationRelation {
        val user = mstNotificationDTO.account.toModel(account)
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        val noteRelation = mstNotificationDTO.status?.toNote(account, nodeInfo)?.let {
            noteRelationGetter.get(it)
        }
        val notification = mstNotificationDTO.toModel(account, isRead = true)
        notificationDataSource.add(notification)
        return NotificationRelation(
            notification = notification,
            user = user,
            note = noteRelation?.getOrNull(),
        )
    }
}