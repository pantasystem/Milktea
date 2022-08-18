package net.pantasystem.milktea.common_android.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat


object PermissionUtil {

    @RequiresApi(33)
    fun getReadMediaPermissions(): List<String> {
        return listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    }

    fun checkReadStoragePermission(context: Context): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= 33) {
            getReadMediaPermissions().all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            val permissionCheck =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            permissionCheck == PackageManager.PERMISSION_GRANTED
        }
        return permissions
    }
}