package jp.panta.misskeyandroidclient.ui.notes.view.editor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.composethemeadapter.MdcTheme
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.*
import jp.panta.misskeyandroidclient.databinding.FragmentNoteEditorBinding
import jp.panta.misskeyandroidclient.databinding.ViewNoteEditorToolbarBinding
import jp.panta.misskeyandroidclient.ui.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.ui.account.viewmodel.AccountViewModel
import jp.panta.misskeyandroidclient.ui.confirm.ConfirmDialog
import jp.panta.misskeyandroidclient.ui.emojis.CustomEmojiPickerDialog
import jp.panta.misskeyandroidclient.ui.emojis.viewmodel.EmojiSelection
import jp.panta.misskeyandroidclient.ui.emojis.viewmodel.EmojiSelectionViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.ui.text.CustomEmojiCompleteAdapter
import jp.panta.misskeyandroidclient.ui.text.CustomEmojiTokenizer
import jp.panta.misskeyandroidclient.ui.users.UserChipListAdapter
import jp.panta.misskeyandroidclient.ui.users.viewmodel.selectable.SelectedUserViewModel
import jp.panta.misskeyandroidclient.util.listview.applyFlexBoxLayout
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_compose.FilePreviewTarget
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.DriveNavigationArgs
import net.pantasystem.milktea.common_navigation.EXTRA_SELECTED_FILE_PROPERTY_IDS
import net.pantasystem.milktea.data.infrastructure.confirm.ConfirmCommand
import net.pantasystem.milktea.data.infrastructure.confirm.ResultType
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.drive.toAppFile
import net.pantasystem.milktea.media.MediaActivity
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.file.toFile
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

@AndroidEntryPoint
class NoteEditorFragment : Fragment(R.layout.fragment_note_editor), EmojiSelection {

    companion object {
        private const val EXTRA_REPLY_TO_NOTE_ID = "EXTRA_REPLY_TO_NOTE_ID"
        private const val EXTRA_QUOTE_TO_NOTE_ID = "EXTRA_QUOTE_TO_NOTE_ID"
        private const val EXTRA_DRAFT_NOTE_ID = "EXTRA_DRAFT_NOTE"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"

        private const val CONFIRM_SAVE_AS_DRAFT_OR_DELETE = "confirm_save_as_draft_or_delete"
        private const val EXTRA_MENTIONS = "EXTRA_MENTIONS"
        private const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"
        private const val EXTRA_TEXT = "EXTRA_TEXT"
        
        fun newInstance(
            replyTo: Note.Id? = null,
            quoteTo: Note.Id? = null,
            draftNoteId: Long? = null,
            mentions: List<String>? = null,
            channelId: Channel.Id? = null,
            text: String? = null,
        ): NoteEditorFragment {
            return NoteEditorFragment().apply { 
                arguments = Bundle().apply { 
                    if (replyTo != null) {
                        putString(EXTRA_REPLY_TO_NOTE_ID, replyTo.noteId)
                        putLong(EXTRA_ACCOUNT_ID, replyTo.accountId)
                    }
                    if (quoteTo != null) {
                        putString(EXTRA_QUOTE_TO_NOTE_ID, quoteTo.noteId)
                        putLong(EXTRA_ACCOUNT_ID, quoteTo.accountId)
                    }
                    if (draftNoteId != null) {
                        putLong(EXTRA_DRAFT_NOTE_ID, draftNoteId)
                    }
                    
                    if (mentions != null) {
                        putStringArray(EXTRA_MENTIONS, mentions.toTypedArray())
                    }
                    
                    if (channelId != null) {
                        putString(EXTRA_CHANNEL_ID, channelId.channelId)
                        putLong(EXTRA_ACCOUNT_ID, channelId.accountId)
                    }
                    if (text != null) {
                        putString(EXTRA_TEXT, text)
                    }
                }
            }
        }
    }
    private val binding: FragmentNoteEditorBinding by dataBinding()
    
    private val noteEditorViewModel: NoteEditorViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()
    private val emojiSelectionViewModel: EmojiSelectionViewModel by activityViewModels()

    @Inject internal lateinit var accountStore: AccountStore
    @Inject internal lateinit var metaRepository: MetaRepository
    @Inject internal lateinit var filePropertyDataSource: FilePropertyDataSource
    @Inject internal lateinit var filePropertyRepository: DriveFileRepository
    @Inject internal lateinit var settingStore: SettingStore
    @Inject internal lateinit var driveNavigation: net.pantasystem.milktea.common_navigation.DriveNavigation
    @Inject internal lateinit var authorizationNavigation: AuthorizationNavigation

    internal lateinit var confirmViewModel: ConfirmViewModel

    private val accountId: Long? by lazy(LazyThreadSafetyMode.NONE) {
        if (requireArguments().getLong(EXTRA_ACCOUNT_ID, -1) == -1L) null else requireArguments().getLong(
            EXTRA_ACCOUNT_ID,
            -1
        )
    }
    private val replyToNoteId by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(EXTRA_REPLY_TO_NOTE_ID)?.let {
            requireNotNull(accountId)
            Note.Id(accountId!!, it)
        }
    }
    private val quoteToNoteId by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(EXTRA_QUOTE_TO_NOTE_ID)?.let {
            requireNotNull(accountId)
            Note.Id(accountId!!, it)
        }
    }

    private val channelId by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(EXTRA_CHANNEL_ID)?.let {
            requireNotNull(accountId)
            Channel.Id(accountId!!, it)
        }
    }

    private val draftNoteId by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getLong(EXTRA_DRAFT_NOTE_ID, - 1).let {
            if (it == -1L) null else it
        }
    }

    private val mentions by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getStringArray(EXTRA_MENTIONS)?.let {
            Log.d("NoteEditorActivity", "mentions:${it.toList()}")
            it.toList()
        }
    }

    private val text by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(EXTRA_TEXT, null)
    }
    

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModels()

        val toolbarBase = getToolbarBase()
        val noteEditorToolbar = DataBindingUtil.inflate<ViewNoteEditorToolbarBinding>(
            LayoutInflater.from(requireContext()),
            R.layout.view_note_editor_toolbar,
            toolbarBase,
            true
        )
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.noteEditorToolbar)

        noteEditorToolbar.actionUpButton.setOnClickListener {
            finishOrConfirmSaveAsDraftOrDelete()
        }
        noteEditorToolbar.lifecycleOwner = viewLifecycleOwner


        confirmViewModel = ViewModelProvider(requireActivity())[ConfirmViewModel::class.java]

        val userChipAdapter = UserChipListAdapter(viewLifecycleOwner)
        binding.addressUsersView.adapter = userChipAdapter
        binding.addressUsersView.applyFlexBoxLayout(requireContext())


        binding.accountViewModel = accountViewModel
        noteEditorToolbar.accountViewModel = accountViewModel
        noteEditorToolbar.viewModel = noteEditorViewModel
        accountViewModel.switchAccount.observe(viewLifecycleOwner) {
            AccountSwitchingDialog().show(childFragmentManager, "tag")
        }
        accountViewModel.showProfile.observe(viewLifecycleOwner) {
            val intent =
                UserDetailActivity.newInstance(requireActivity(), userId = User.Id(it.accountId, it.remoteId))

            intent.putActivity(Activities.ACTIVITY_IN_APP)


            startActivity(intent)
        }

        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
            metaRepository.observe(it.instanceDomain)
        }.mapNotNull {
            it?.emojis
        }.distinctUntilChanged().onEach { emojis ->
            binding.inputMain.setAdapter(
                CustomEmojiCompleteAdapter(
                    emojis,
                    requireContext()
                )
            )
            binding.inputMain.setTokenizer(CustomEmojiTokenizer())

            binding.cw.setAdapter(
                CustomEmojiCompleteAdapter(
                    emojis,
                    requireContext()
                )
            )
            binding.cw.setTokenizer(CustomEmojiTokenizer())
        }.launchIn(lifecycleScope)

        if (!text.isNullOrBlank() && savedInstanceState == null) {
            noteEditorViewModel.changeText(text)
        }
        noteEditorViewModel.setReplyTo(replyToNoteId)
        noteEditorViewModel.setRenoteTo(quoteToNoteId)
        noteEditorViewModel.setChannelId(channelId)
        if (draftNoteId != null) {
            noteEditorViewModel.setDraftNoteId(draftNoteId!!)
        }

        binding.filePreview.apply {
            setContent {
                MdcTheme {
                    NoteFilePreview(
                        noteEditorViewModel = noteEditorViewModel,
                        fileRepository = filePropertyRepository,
                        dataSource = filePropertyDataSource,
                        onShow = {
                            val file = when (it) {
                                is FilePreviewTarget.Remote -> {
                                    it.fileProperty.toFile()
                                }
                                is FilePreviewTarget.Local -> {
                                    it.file.toFile()
                                }
                            }
                            val intent = MediaActivity.newInstance(
                                requireActivity(),
                                listOf(file),
                                0
                            )
                            requireActivity().startActivity(intent)
                        }
                    )
                }

            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                noteEditorViewModel.poll.distinctUntilChangedBy {
                    it == null
                }.collect { poll ->
                    if (poll == null) {
                        removePollFragment()
                    } else {
                        setPollFragment()
                    }
                }
            }
        }

        binding.cw.addTextChangedListener { e ->
            noteEditorViewModel.setCw(e?.toString())
        }

        binding.inputMain.addTextChangedListener { e ->
            Log.d("NoteEditorActivity", "text changed:$e")
            noteEditorViewModel.setText((e?.toString() ?: ""))
        }

        noteEditorViewModel.state.onEach {
            if (it.textCursorPos != null && it.text != null) {
                binding.inputMain.setText(it.text ?: "")
                binding.inputMain.setSelection(it.textCursorPos ?: 0)
            }
        }.launchIn(lifecycleScope)

        noteEditorViewModel.isPost.observe(viewLifecycleOwner) {
            if (it) {
                noteEditorToolbar.postButton.isEnabled = false
                requireActivity().finish()
            }
        }

        noteEditorViewModel.showVisibilitySelectionEvent.observe(viewLifecycleOwner) {
            Log.d("NoteEditorActivity", "公開範囲を設定しようとしています")
            val dialog = VisibilitySelectionDialog()
            dialog.show(childFragmentManager, "NoteEditor")
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                noteEditorViewModel.address.collect {
                    userChipAdapter.submitList(it)
                }
            }
        }

        noteEditorViewModel.showPollTimePicker.observe(viewLifecycleOwner) {
            PollTimePickerDialog().show(childFragmentManager, "TimePicker")
        }

        noteEditorViewModel.showPollDatePicker.observe(viewLifecycleOwner) {
            PollDatePickerDialog().show(childFragmentManager, "DatePicker")
        }

        emojiSelectionViewModel.selectedEmoji.observe(viewLifecycleOwner) {
            onSelect(it)
        }

        emojiSelectionViewModel.selectedEmojiName.observe(viewLifecycleOwner) {
            onSelect(it)
        }


        binding.selectFileFromDrive.setOnClickListener {
            showDriveFileSelector()
        }

        binding.selectFileFromLocal.setOnClickListener {
            showFileManager()
        }

        binding.addAddress.setOnClickListener {
            startSearchAndSelectUser()
        }

        binding.mentionButton.setOnClickListener {
            startMentionToSearchAndSelectUser()
        }

        binding.showEmojisButton.setOnClickListener {
            CustomEmojiPickerDialog().show(childFragmentManager, "Editor")
        }

        binding.reservationAtPickDateButton.setOnClickListener {
            ReservationPostDatePickerDialog().show(childFragmentManager, "Pick date")
        }

        binding.reservationAtPickTimeButton.setOnClickListener {
            ReservationPostTimePickerDialog().show(childFragmentManager, "Pick time")
        }


        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountStore.state.collect {
                    if (it.isUnauthorized) {
                        requireActivity().finish()
                        startActivity(
                            authorizationNavigation.newIntent(Unit)
                        )
                    }
                }
            }
        }


        confirmViewModel.confirmedEvent.observe(viewLifecycleOwner) {
            when (it.eventType) {
                CONFIRM_SAVE_AS_DRAFT_OR_DELETE -> {
                    if (it.resultType == ResultType.POSITIVE) {
                        noteEditorViewModel.saveDraft()
                    } else {
                        requireActivity().finish()
                    }
                }
            }
        }

        confirmViewModel.confirmEvent.observe(viewLifecycleOwner) {
            ConfirmDialog().show(childFragmentManager, "confirm")
        }

        noteEditorViewModel.isSaveNoteAsDraft.observe(viewLifecycleOwner) {
            Handler(Looper.getMainLooper()).post {
                if (it == null) {
                    Toast.makeText(requireContext(), "下書きに失敗しました", Toast.LENGTH_LONG).show()
                } else {
                    upTo()
                }
            }


        }
        if (mentions != null && savedInstanceState == null) {
            addMentionUserNames(mentions!!)
        }

        binding.inputMain.requestFocus()
        
        
    }


    override fun onSelect(emoji: Emoji) {
        val pos =  binding.inputMain.selectionEnd
        noteEditorViewModel.addEmoji(emoji, pos).let { newPos ->
            binding.inputMain.setText(noteEditorViewModel.text.value ?: "")
            binding.inputMain.setSelection(newPos)
            Log.d("NoteEditorActivity", "入力されたデータ:${binding.inputMain.text}")
        }
    }

    override fun onSelect(emoji: String) {
        val pos = binding.inputMain.selectionEnd
        noteEditorViewModel.addEmoji(emoji, pos).let { newPos ->
            binding.inputMain.setText(noteEditorViewModel.text.value ?: "")
            binding.inputMain.setSelection(newPos)
        }
    }

    private fun bindViewModels() {
        binding.viewModel = noteEditorViewModel
        binding.accountViewModel = accountViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        
    }

    private fun setPollFragment() {
        val ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.edit_poll, PollEditorFragment(), "pollFragment")
        ft.commit()
    }

    private fun removePollFragment() {
        val fragment = childFragmentManager.findFragmentByTag("pollFragment")
        if (fragment != null) {
            val ft = childFragmentManager.beginTransaction()
            ft.remove(fragment)
            ft.commit()
        }
    }
    /**
     * 設定をもとにToolbarを表示するベースとなるViewGroupを非表示・表示＆取得をしている
     */
    private fun getToolbarBase(): ViewGroup {
        return if (settingStore.isPostButtonAtTheBottom) {
            binding.noteEditorToolbar.visibility = View.GONE
            binding.bottomToolbarBase.visibility = View.VISIBLE
            binding.bottomToolbarBase
        } else {
            binding.bottomToolbarBase.visibility = View.GONE
            binding.bottomToolbarBase.visibility = View.VISIBLE
            binding.noteEditorToolbar
        }
    }

    private fun addMentionUserNames(userNames: List<String>) {
        val pos = binding.inputMain.selectionEnd
        noteEditorViewModel.addMentionUserNames(userNames, pos).let { newPos ->
            Log.d(
                "NoteEditorActivity",
                "text:${noteEditorViewModel.state.value.text}, stateText:${noteEditorViewModel.state.value.text}"
            )
            binding.inputMain.setText(noteEditorViewModel.state.value.text ?: "")
            binding.inputMain.setSelection(newPos)
        }
    }

    private fun showDriveFileSelector() {
        val selectedSize = noteEditorViewModel.state.value.totalFilesCount
        //Directoryは既に選択済みのファイルの数も含めてしまうので選択済みの数も合わせる
        val selectableMaxSize = noteEditorViewModel.maxFileCount.value - selectedSize
        val intent = driveNavigation.newIntent(DriveNavigationArgs(
            selectableFileMaxSize = selectableMaxSize,
            accountId = accountStore.currentAccountId,
        ))

        intent.action = Intent.ACTION_OPEN_DOCUMENT
        openDriveActivityResult.launch(intent)
    }

    private fun checkPermission(): Boolean {
        val permissionCheck =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (!checkPermission()) {
            requestReadStoragePermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun showFileManager() {
        if (checkPermission()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            openLocalStorageResult.launch(intent)
        } else {
            requestPermission()
        }

    }

    private fun startSearchAndSelectUser() {
        val selectedUserIds = noteEditorViewModel.address.value.mapNotNull {
            it.userId
        }

        val intent = SearchAndSelectUserActivity.newIntent(requireActivity(), selectedUserIds = selectedUserIds)

        selectUserResult.launch(intent)
    }


    private fun startMentionToSearchAndSelectUser() {
        val intent = Intent(requireActivity(), SearchAndSelectUserActivity::class.java)
        selectMentionToUserResult.launch(intent)
    }

    private fun finishOrConfirmSaveAsDraftOrDelete() {
        if (noteEditorViewModel.canSaveDraft()) {
            confirmViewModel.confirmEvent.event = ConfirmCommand(
                getString(R.string.save_draft),
                getString(R.string.save_the_note_as_a_draft),
                eventType = CONFIRM_SAVE_AS_DRAFT_OR_DELETE,
                args = "",
                positiveButtonText = getString(R.string.save),
                negativeButtonText = getString(R.string.delete)

            )
        } else {
            upTo()
        }
    }

    private fun upTo() {
        if (text.isNullOrEmpty()) {
            requireActivity().finish()
        } else {
            val upIntent = Intent(requireActivity(), MainActivity::class.java)
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (requireActivity().shouldUpRecreateTask(upIntent)) {
                TaskStackBuilder.create(requireActivity())
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities()
                requireActivity().finish()
            } else {
                requireActivity().navigateUpTo(upIntent)
            }
        }
    }


    private val openDriveActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val ids =
                (result?.data?.getSerializableExtra(EXTRA_SELECTED_FILE_PROPERTY_IDS) as List<*>?)?.mapNotNull {
                    it as? FileProperty.Id
                }
            Log.d("NoteEditorActivity", "result:${ids}")
            val size = noteEditorViewModel.fileTotal()

            if (ids != null && ids.isNotEmpty() && size + ids.size <= noteEditorViewModel.maxFileCount.value) {
                noteEditorViewModel.addFilePropertyFromIds(ids)
            }
        }

    private val openLocalStorageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            val uri = result?.data?.data
            if (uri != null) {
                val size = noteEditorViewModel.fileTotal()

                if (size > noteEditorViewModel.maxFileCount.value) {
                    Log.d("NoteEditorActivity", "失敗しました")
                } else {
                    noteEditorViewModel.add(uri.toAppFile(requireContext()))
                    Log.d("NoteEditorActivity", "成功しました")
                }

            }
        }

    private val requestReadStoragePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                showFileManager()
            } else {
                Toast.makeText(requireContext(), "ストレージへのアクセスを許可しないとファイルを読み込めないぽよ", Toast.LENGTH_LONG).show()
            }
        }

    private val selectUserResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val changed =
                    result.data?.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? SelectedUserViewModel.ChangedDiffResult
                if (changed != null) {
                    noteEditorViewModel.setAddress(changed.added, changed.removed)
                }
            }
        }

    private val selectMentionToUserResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val changed =
                    result.data?.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? SelectedUserViewModel.ChangedDiffResult

                if (changed != null) {
                    addMentionUserNames(changed.selectedUserNames)
                }

            }
        }


}