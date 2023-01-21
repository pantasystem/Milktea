package net.pantasystem.milktea.data.infrastructure.notification.impl

import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.data.infrastructure.toNotification
import net.pantasystem.milktea.data.infrastructure.toUser
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
) {
    suspend fun addAndConvert(account: Account, notificationDTO: NotificationDTO): NotificationRelation {
        val user = notificationDTO.user?.toUser(account, false)
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull()
        if (user != null) {
            userDataSource.add(user)
        }
        val noteRelation = notificationDTO.note?.let{
            noteRelationGetter.get(noteDataSourceAdder.addNoteDtoToDataSource(account, it))
        }
        val notification = notificationDTO.toNotification(account, nodeInfo)
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
}