package net.pantasystem.milktea.auth

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.auth.viewmodel.SignUpViewModel
import net.pantasystem.milktea.common.ui.ApplyTheme
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    @Inject
    internal lateinit var applyTheme: ApplyTheme

    private val signUpViewModel by viewModels<SignUpViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        setContent {
            MdcTheme {
                val uiState by signUpViewModel.uiState.collectAsState()
                val keyword by signUpViewModel.keyword.collectAsState()
                SignUpScreen(
                    uiState = uiState,
                    instanceDomain = keyword,
                    onInputKeyword = signUpViewModel::onInputKeyword,
                    onNextButtonClicked = {

                    }
                )
            }
        }
    }
}