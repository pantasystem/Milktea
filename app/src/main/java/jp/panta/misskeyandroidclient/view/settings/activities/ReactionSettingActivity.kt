package jp.panta.misskeyandroidclient.view.settings.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ActivityReactionSettingBinding
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.view.notes.editor.CustomEmojiCompleteAdapter
import jp.panta.misskeyandroidclient.view.notes.editor.CustomEmojiTokenizer
import jp.panta.misskeyandroidclient.view.text.CustomEmojiDecorator
import jp.panta.misskeyandroidclient.viewmodel.setting.reaction.ReactionPickerSettingViewModel

class ReactionSettingActivity : AppCompatActivity() {

    private lateinit var mCustomEmojiDecorator: CustomEmojiDecorator
    private var mEmojis: List<Emoji> = emptyList()
    private var mReactionPickerSettingViewModel: ReactionPickerSettingViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_reaction_setting)
        val binding = DataBindingUtil.setContentView<ActivityReactionSettingBinding>(this, R.layout.activity_reaction_setting)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.reactionSettingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val miApplication = applicationContext as MiApplication
        mCustomEmojiDecorator = CustomEmojiDecorator()
        miApplication.currentAccount.observe(this, Observer {
            mEmojis = miApplication.getCurrentInstanceMeta()?.emojis?: emptyList()
            mReactionPickerSettingViewModel = ViewModelProvider(this, ReactionPickerSettingViewModel.Factory(it, miApplication))[ReactionPickerSettingViewModel::class.java]
            binding.reactionPickerSettingViewModel = mReactionPickerSettingViewModel!!
        })

        val emojis = miApplication.getCurrentInstanceMeta()?.emojis?.map{
            ":${it.name}:"
        }?: emptyList()
        val customEmojiAutoCompleteAdapter = CustomEmojiCompleteAdapter(emojis, this)
        binding.reactionSettingField.setAdapter(customEmojiAutoCompleteAdapter)
        binding.reactionSettingField.setTokenizer(CustomEmojiTokenizer())


    }

    override fun onStop(){
        super.onStop()
        mReactionPickerSettingViewModel?.save()
    }
}
