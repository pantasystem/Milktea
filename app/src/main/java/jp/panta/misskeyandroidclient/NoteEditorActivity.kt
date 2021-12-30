package jp.panta.misskeyandroidclient

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.composethemeadapter.MdcTheme
import jp.panta.misskeyandroidclient.databinding.ActivityNoteEditorBinding
import jp.panta.misskeyandroidclient.databinding.ViewNoteEditorToolbarBinding
import jp.panta.misskeyandroidclient.model.confirm.ConfirmCommand
import jp.panta.misskeyandroidclient.model.confirm.ResultType
import jp.panta.misskeyandroidclient.model.core.ConnectionStatus
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.file.toFile
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.ui.components.FilePreviewTarget
import jp.panta.misskeyandroidclient.ui.notes.editor.NoteFilePreview
import jp.panta.misskeyandroidclient.util.file.toAppFile
import jp.panta.misskeyandroidclient.util.listview.applyFlexBoxLayout
import jp.panta.misskeyandroidclient.view.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.view.confirm.ConfirmDialog
import jp.panta.misskeyandroidclient.view.emojis.CustomEmojiPickerDialog
import jp.panta.misskeyandroidclient.view.notes.editor.*
import jp.panta.misskeyandroidclient.view.text.CustomEmojiCompleteAdapter
import jp.panta.misskeyandroidclient.view.text.CustomEmojiTokenizer
import jp.panta.misskeyandroidclient.view.users.UserChipListAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.viewmodel.emojis.EmojiSelection
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SelectedUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

class NoteEditorActivity : AppCompatActivity(), EmojiSelection {

    companion object{
        private const val EXTRA_REPLY_TO_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_REPLY_TO_NOTE_ID"
        private const val EXTRA_QUOTE_TO_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_QUOTE_TO_NOTE_ID"
        private const val EXTRA_DRAFT_NOTE = "jp.panta.misskeyandroidclient.EXTRA_DRAFT_NOTE"
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ACCOUNT_ID"

        private const val CONFIRM_SAVE_AS_DRAFT_OR_DELETE = "confirm_save_as_draft_or_delete"

        fun newBundle(context: Context, replyTo: Note.Id? = null, quoteTo: Note.Id? = null, draftNote: DraftNote? = null): Intent {
            return Intent(context, NoteEditorActivity::class.java).apply {
                replyTo?.let{
                    putExtra(EXTRA_REPLY_TO_NOTE_ID, replyTo.noteId)
                    putExtra(EXTRA_ACCOUNT_ID, replyTo.accountId)
                }

                quoteTo?.let {
                    putExtra(EXTRA_QUOTE_TO_NOTE_ID, quoteTo.noteId)
                    putExtra(EXTRA_ACCOUNT_ID, quoteTo.accountId)
                }


                draftNote?.let {
                    putExtra(EXTRA_DRAFT_NOTE, it)
                }

            }
        }
    }
    private lateinit var mViewModel: NoteEditorViewModel

    private lateinit var mBinding: ActivityNoteEditorBinding

    private lateinit var mConfirmViewModel: ConfirmViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_note_editor)
        val binding = DataBindingUtil.setContentView<ActivityNoteEditorBinding>(this, R.layout.activity_note_editor)
        mBinding = binding

        setSupportActionBar(mBinding.noteEditorToolbar)


        var text: String? = null
        if(intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true){
            text = intent.getStringExtra(Intent.EXTRA_TEXT)
        }


        val miApplication = applicationContext as MiApplication

        val toolbarBase = getToolbarBase()
        val noteEditorToolbar = DataBindingUtil.inflate<ViewNoteEditorToolbarBinding>(
            LayoutInflater.from(this),
            R.layout.view_note_editor_toolbar,
            toolbarBase,
            true
        )

        //binding.viewModel
        binding.lifecycleOwner = this

        val accountId: Long? = if(intent.getLongExtra(EXTRA_ACCOUNT_ID, - 1) == - 1L) null else intent.getLongExtra(EXTRA_ACCOUNT_ID, -1)
        val replyToNoteId = intent.getStringExtra(EXTRA_REPLY_TO_NOTE_ID)?.let{
            requireNotNull(accountId)
            Note.Id(accountId, it)
        }
        val quoteToNoteId = intent.getStringExtra(EXTRA_QUOTE_TO_NOTE_ID)?.let{
            requireNotNull(accountId)
            Note.Id(accountId, it)
        }




        val draftNote: DraftNote? = intent.getSerializableExtra(EXTRA_DRAFT_NOTE) as? DraftNote?


        noteEditorToolbar.actionUpButton.setOnClickListener {
            finishOrConfirmSaveAsDraftOrDelete()
        }

        mConfirmViewModel = ViewModelProvider(this)[ConfirmViewModel::class.java]

        val userChipAdapter = UserChipListAdapter(this)
        binding.addressUsersView.adapter = userChipAdapter
        binding.addressUsersView.applyFlexBoxLayout(this)


        val accountViewModel = ViewModelProvider(this, AccountViewModel.Factory(miApplication))[AccountViewModel::class.java]
        binding.accountViewModel = accountViewModel
        noteEditorToolbar.accountViewModel = accountViewModel
        accountViewModel.switchAccount.observe(this) {
            AccountSwitchingDialog().show(supportFragmentManager, "tag")
        }
        accountViewModel.showProfile.observe(this) {
            val intent = UserDetailActivity.newInstance(this, userId = User.Id(it.accountId, it.remoteId))

            intent.putActivity(Activities.ACTIVITY_IN_APP)


            startActivity(intent)
        }

        miApplication.getCurrentAccount().filterNotNull().flatMapLatest {
            miApplication.getMetaRepository().observe(it.instanceDomain)
        }.mapNotNull {
            it?.emojis
        }.distinctUntilChanged().onEach { emojis ->
            binding.inputMain.setAdapter(
                CustomEmojiCompleteAdapter(
                    emojis,
                    this
                )
            )
            binding.inputMain.setTokenizer(CustomEmojiTokenizer())

            binding.cw.setAdapter(
                CustomEmojiCompleteAdapter(
                    emojis,
                    this
                )
            )
            binding.cw.setTokenizer(CustomEmojiTokenizer())
        }.launchIn(lifecycleScope)

        val factory = NoteEditorViewModelFactory(miApplication, replyToNoteId = replyToNoteId, quoteToNoteId = quoteToNoteId, draftNote = draftNote)
        val viewModel = ViewModelProvider(this, factory)[NoteEditorViewModel::class.java]
        mViewModel = viewModel
        if(!text.isNullOrBlank()){
            viewModel.changeText(text)
        }
        binding.viewModel = viewModel
        noteEditorToolbar.viewModel = viewModel
        noteEditorToolbar.lifecycleOwner = this

        binding.filePreview.apply {
            setContent {
                MdcTheme {
                    NoteFilePreview(
                        noteEditorViewModel = viewModel,
                        fileRepository = miApplication.getDriveFileRepository(),
                        dataSource = miApplication.getFilePropertyDataSource(),
                        onShow = {
                            val file = when(it) {
                                is FilePreviewTarget.Remote -> {
                                    it.fileProperty.toFile()
                                }
                                is FilePreviewTarget.Local -> {
                                    it.file.toFile()
                                }
                            }
                            val intent = MediaActivity.newInstance(
                                this@NoteEditorActivity,
                                listOf(file),
                                0
                            )
                            this@NoteEditorActivity.startActivity(intent)
                        }
                    )
                }

            }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.poll.distinctUntilChangedBy {
                it == null
            }.collect { poll ->
                if(poll == null) {
                    removePollFragment()
                }else{
                    setPollFragment()
                }
            }
        }

        mBinding.cw.addTextChangedListener { e ->
            viewModel.setCw(e?.toString())
        }

        mBinding.inputMain.addTextChangedListener { e ->
            viewModel.setText((e?.toString()?: ""))
        }


        viewModel.isPost.observe(this) {
            if(it) {
                noteEditorToolbar.postButton.isEnabled = false
                finish()
            }
        }

        viewModel.showVisibilitySelectionEvent.observe(this) {
            Log.d("NoteEditorActivity", "公開範囲を設定しようとしています")
            val dialog = VisibilitySelectionDialog()
            dialog.show(supportFragmentManager, "NoteEditor")
        }

        lifecycleScope.launchWhenResumed {
            viewModel.address.collect {
                userChipAdapter.submitList(it)
            }
        }

        viewModel.showPollTimePicker.observe(this) {
            PollTimePickerDialog().show(supportFragmentManager, "TimePicker")
        }

        viewModel.showPollDatePicker.observe(this) {
            PollDatePickerDialog().show(supportFragmentManager, "DatePicker")
        }



        mBinding.selectFileFromDrive.setOnClickListener {
            showDriveFileSelector()
        }

        mBinding.selectFileFromLocal.setOnClickListener {
            showFileManager()
        }

        binding.addAddress.setOnClickListener {
            startSearchAndSelectUser()
        }

        binding.mentionButton.setOnClickListener {
            startMentionToSearchAndSelectUser()
        }

        binding.showEmojisButton.setOnClickListener {
            CustomEmojiPickerDialog().show(supportFragmentManager, "Editor")
        }

        (applicationContext as? MiApplication)?.connectionStatus?.observe(this) { status ->
            when (status) {
                ConnectionStatus.SUCCESS -> Log.d("MainActivity", "成功")
                ConnectionStatus.ACCOUNT_ERROR -> {
                    finish()
                    startActivity(Intent(this, AuthorizationActivity::class.java))
                }
                ConnectionStatus.NETWORK_ERROR -> {
                    Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                }
                else -> Log.d("MainActivity", "not initialized")
            }
        }

        mConfirmViewModel.confirmedEvent.observe(this) {
            when (it.eventType) {
                CONFIRM_SAVE_AS_DRAFT_OR_DELETE -> {
                    if (it.resultType == ResultType.POSITIVE) {
                        mViewModel.saveDraft()
                    } else {
                        finish()
                    }
                }
            }
        }

        mConfirmViewModel.confirmEvent.observe( this) {
            ConfirmDialog().show(supportFragmentManager, "confirm")
        }

        mViewModel.isSaveNoteAsDraft.observe(this) {
            runOnUiThread {
                if (it == null) {
                    Toast.makeText(this, "下書きに失敗しました", Toast.LENGTH_LONG).show()
                } else {
                    upTo()
                }
            }

        }
        binding.inputMain.requestFocus()
    }

    override fun onSelect(emoji: Emoji) {
        val pos = mBinding.inputMain.selectionEnd
        mViewModel.addEmoji(emoji, pos).let{ newPos ->
            mBinding.inputMain.setText(mViewModel.text.value?: "")
            mBinding.inputMain.setSelection(newPos)
            Log.d("NoteEditorActivity", "入力されたデータ:${mBinding.inputMain.text}")
        }
    }

    override fun onSelect(emoji: String) {
        val pos = mBinding.inputMain.selectionEnd
        mViewModel.addEmoji(emoji, pos).let{ newPos ->
            mBinding.inputMain.setText(mViewModel.text.value?: "")
            mBinding.inputMain.setSelection(newPos)
        }
    }

    private fun setPollFragment(){
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.edit_poll, PollEditorFragment(), "pollFragment")
        ft.commit()
    }

    private fun removePollFragment(){
        val fragment = supportFragmentManager.findFragmentByTag("pollFragment")
        if(fragment != null){
            val ft = supportFragmentManager.beginTransaction()
            ft.remove(fragment)
            ft.commit()
        }
    }

    /**
     * 設定をもとにToolbarを表示するベースとなるViewGroupを非表示・表示＆取得をしている
     */
    private fun getToolbarBase() : ViewGroup {
        val miCore = applicationContext as MiCore
        return if(miCore.getSettingStore().isPostButtonAtTheBottom){
            mBinding.noteEditorToolbar.visibility = View.GONE
            mBinding.bottomToolbarBase.visibility = View.VISIBLE
            mBinding.bottomToolbarBase
        }else{
            mBinding.bottomToolbarBase.visibility = View.GONE
            mBinding.bottomToolbarBase.visibility = View.VISIBLE
            mBinding.noteEditorToolbar
        }
    }

    private fun showFileManager(){
        if(checkPermission()){
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            openLocalStorageResult.launch(intent)
        }else{
            requestPermission()
        }

    }

    private fun showDriveFileSelector(){
        val selectedSize = mViewModel.state.value.totalFilesCount

        val miCore = applicationContext as MiCore
        //Directoryは既に選択済みのファイルの数も含めてしまうので選択済みの数も合わせる
        val selectableMaxSize = 4 - selectedSize
        Log.d("", "選択済みのサイズ:$selectedSize")
        val intent = Intent(this, DriveActivity::class.java)
            .putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, selectableMaxSize)
            .putExtra(DriveActivity.EXTRA_ACCOUNT_ID, miCore.getCurrentAccount().value?.accountId)
        intent.action = Intent.ACTION_OPEN_DOCUMENT
        openDriveActivityResult.launch(intent)
    }

    private fun checkPermission(): Boolean{
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission(){
        //val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if(! checkPermission()){
            requestReadStoragePermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun startSearchAndSelectUser(){
        val selectedUserIds = mViewModel.address.value.mapNotNull{
            it.userId
        }

        val intent  = SearchAndSelectUserActivity.newIntent(this, selectedUserIds = selectedUserIds)

        selectUserResult.launch(intent)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun startMentionToSearchAndSelectUser(){
        val intent = Intent(this, SearchAndSelectUserActivity::class.java)
        selectMentionToUserResult.launch(intent)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun finishOrConfirmSaveAsDraftOrDelete(){
        if(mViewModel.canSaveDraft()){
            mConfirmViewModel.confirmEvent.event = ConfirmCommand(
                getString(R.string.save_draft),
                getString(R.string.save_the_note_as_a_draft),
                eventType = CONFIRM_SAVE_AS_DRAFT_OR_DELETE ,
                args = "",
                positiveButtonText = getString(R.string.save),
                negativeButtonText = getString(R.string.delete)

            )
        }else{
            upTo()
        }
    }

    private fun upTo(){
        if(intent.getStringExtra(Intent.EXTRA_TEXT).isNullOrEmpty()){
            finish()
        }else{
            val upIntent = Intent(this, MainActivity::class.java)
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if(shouldUpRecreateTask(upIntent)){
                TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities()
                finish()
            }else{
                navigateUpTo(upIntent)
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onBackPressed() {

        finishOrConfirmSaveAsDraftOrDelete()
    }

    private val openDriveActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val ids = (result?.data?.getSerializableExtra(DriveActivity.EXTRA_SELECTED_FILE_PROPERTY_IDS)  as List<*>? )?.mapNotNull {
            it as? FileProperty.Id
        }
        Log.d("NoteEditorActivity", "result:${ids}")
        val size = mViewModel.fileTotal()

        if(ids != null && ids.isNotEmpty() && size + ids.size <= 4) {
            mViewModel.addFilePropertyFromIds(ids)
        }
    }

    private val openLocalStorageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        val uri = result?.data?.data
        if(uri != null){
            val size = mViewModel.fileTotal()

            if(size > 4){
                Log.d("NoteEditorActivity", "失敗しました")
            }else{
                mViewModel.add(uri.toAppFile(this))
                Log.d("NoteEditorActivity", "成功しました")
            }

        }
    }

    private val requestReadStoragePermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if(it){
            showFileManager()
        }else{
            Toast.makeText(this, "ストレージへのアクセスを許可しないとファイルを読み込めないぽよ", Toast.LENGTH_LONG).show()
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val selectUserResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK && result.data != null){
            val changed = result.data?.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? SelectedUserViewModel.ChangedDiffResult
            if(changed != null) {
                mViewModel.setAddress(changed.added, changed.removed)
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val selectMentionToUserResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK && result.data != null){
            val changed = result.data?.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? SelectedUserViewModel.ChangedDiffResult

            if(changed != null){
                val pos = mBinding.inputMain.selectionEnd
                mViewModel.addMentionUsers(changed.selectedUsers, pos).let { newPos ->
                    Log.d("NoteEditorActivity", "text:${mViewModel.text.value}, stateText:${mViewModel.state.value.text}")
                    mBinding.inputMain.setText(mViewModel.text.value?: "")
                    mBinding.inputMain.setSelection(newPos)
                }
            }

        }
    }





}
