package net.pantasystem.milktea.common_android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUtil @Inject constructor(
    @ApplicationContext val context: Context
){
    fun makeNotificationManager(
        id: String,
        name: String,
        description: String
    ): NotificationManager {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(id) == null) {
                val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
                channel.description = description
                notificationManager.createNotificationChannel(channel)
            }
        }
        return notificationManager

    }
}