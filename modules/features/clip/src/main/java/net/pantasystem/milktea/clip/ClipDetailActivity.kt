package net.pantasystem.milktea.clip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_navigation.ClipDetailNavigation
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.clip.ClipId
import javax.inject.Inject

@AndroidEntryPoint
class ClipDetailActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    @Inject
    internal lateinit var pageableFragmentFactory: PageableFragmentFactory

    private val clipId: ClipId by lazy {
        ClipId(
            requireNotNull(intent.getLongExtra(ClipDetailNavigationImpl.EXTRA_ACCOUNT_ID, 0)),
            requireNotNull(intent.getStringExtra(ClipDetailNavigationImpl.EXTRA_CLIP_ID))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContent {
            MdcTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(id = R.string.clip))
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "navigate up"
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    AndroidView(
                        modifier = Modifier.padding(paddingValues),
                        factory = {
                            FrameLayout(it).apply {
                                id = R.id.container
                            }
                        },
                        update = { frameLayout ->
                            val fragment = pageableFragmentFactory.create(
                                clipId.accountId,
                                Pageable.ClipNotes(clipId.clipId)
                            )
                            val transaction = supportFragmentManager.beginTransaction()
                            transaction.replace(frameLayout.id, fragment)
                            transaction.commit()
                        }
                    )
                }
            }
        }
    }
}


class ClipDetailNavigationImpl @Inject constructor(
    val activity: Activity,
) : ClipDetailNavigation {
    companion object {
        const val EXTRA_ACCOUNT_ID = "ClipDetailNavigationImpl.EXTRA_ACCOUNT_ID"
        const val EXTRA_CLIP_ID = "ClipDetailNavigationImpl.EXTRA_CLIP_ID"
    }

    override fun newIntent(args: ClipId): Intent {
        return Intent(activity, ClipDetailActivity::class.java).apply {
            putExtra(EXTRA_ACCOUNT_ID, args.accountId)
            putExtra(EXTRA_CLIP_ID, args.clipId)
        }
    }
}