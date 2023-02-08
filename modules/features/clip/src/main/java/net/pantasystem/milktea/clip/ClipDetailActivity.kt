package net.pantasystem.milktea.clip

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import javax.inject.Inject

@AndroidEntryPoint
class ClipDetailActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContent {
            MdcTheme {

            }
        }
    }
}