package net.pantasystem.milktea.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import javax.inject.Inject

@AndroidEntryPoint
class JoinMilkteaActivity : AppCompatActivity() {

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                JoinMilkteaScreen(
                    onCreateAccountButtonClicked = {
                        startActivity(
                            Intent(this, SignUpActivity::class.java)
                        )
                    },
                    onLoginButtonClicked = {
                        startActivity(authorizationNavigation.newIntent(AuthorizationArgs.New))
                    }
                )
            }
        }
    }
}