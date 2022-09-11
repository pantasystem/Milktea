package net.pantasystem.milktea.data.gettters

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Getters @Inject constructor(
    val notificationRelationGetter: NotificationRelationGetter,
)