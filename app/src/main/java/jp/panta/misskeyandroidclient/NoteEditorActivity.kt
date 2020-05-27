package jp.panta.misskeyandroidclient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.*
import jp.panta.misskeyandroidclient.databinding.ActivityNoteEditorBinding
import jp.panta.misskeyandroidclient.model.core.ConnectionStatus
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.view.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.view.emojis.CustomEmojiPickerDialog
import jp.panta.misskeyandroidclient.view.notes.editor.*
import jp.panta.misskeyandroidclient.view.text.CustomEmojiCompleteAdapter
import jp.panta.misskeyandroidclient.view.text.CustomEmojiTokenizer
import jp.panta.misskeyandroidclient.view.users.UserChipListAdapter
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel
import jp.panta.misskeyandroidclient.viewmodel.emojis.EmojiSelection
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModelFactory
import kotlinx.android.synthetic.main.activity_note_editor.*

class NoteEditorActivity : AppCompatActivity(), EmojiSelection {

    companion object{
        const val EXTRA_REPLY_TO_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_REPLY_TO_NOTE_ID"
        const val EXTRA_QUOTE_TO_NOTE_ID = "jp.panta.misskeyandroidclient.EXTRA_QUOTE_TO_NOTE_ID"
        const val EXTRA_NOTE = "jp.panta.misskeyandroidclient.EXTRA_NOTE"
        const val SELECT_DRIVE_FILE_REQUEST_CODE = 114
        const val SELECT_LOCAL_FILE_REQUEST_CODE = 514
        const val READ_STORAGE_PERMISSION_REQUEST_CODE = 1919
        const val SELECT_USER_REQUEST_CODE = 810
        const val SELECT_MENTION_TO_USER_REQUEST_CODE = 931
    }
    private var mViewModel: NoteEditorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_note_editor)
        setSupportActionBar(note_editor_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val binding = DataBindingUtil.setContentView<ActivityNoteEditorBinding>(this, R.layout.activity_note_editor)

        //binding.viewModel
        binding.lifecycleOwner = this

        val replyToNoteId: String? = intent.getStringExtra(EXTRA_REPLY_TO_NOTE_ID)
        val quoteToNoteId: String? = intent.getStringExtra(EXTRA_QUOTE_TO_NOTE_ID)

        val note: Note? = intent.getSerializableExtra(EXTRA_NOTE) as Note?


        binding.imageListPreview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.actionUpButton.setOnClickListener {
            finish()
        }

        val userChipAdapter = UserChipListAdapter(this)
        binding.addressUsersView.adapter = userChipAdapter
        val flexBoxLayoutManager = FlexboxLayoutManager(this)
        flexBoxLayoutManager.flexDirection = FlexDirection.ROW
        flexBoxLayoutManager.flexWrap = FlexWrap.WRAP
        flexBoxLayoutManager.justifyContent = JustifyContent.FLEX_START
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        binding.addressUsersView.layoutManager = flexBoxLayoutManager

        val miApplication = applicationContext as MiApplication

        val accountViewModel = ViewModelProvider(this, AccountViewModel.Factory(miApplication))[AccountViewModel::class.java]
        binding.accountViewModel = accountViewModel
        accountViewModel.switchAccount.observe(this, Observer {
            AccountSwitchingDialog().show(supportFragmentManager, "tag")
        })
        accountViewModel.showProfile.observe(this, Observer {
            val intent = Intent(this, UserDetailActivity::class.java)
            intent.putExtra(UserDetailActivity.EXTRA_USER_ID, it)
            startActivity(intent)
        })

        miApplication.getCurrentInstanceMeta()?.emojis?.map{
            ":${it.name}:"
        }?.let{ emojis ->
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
        }

        val factory = NoteEditorViewModelFactory(miApplication, replyToNoteId = replyToNoteId, quoteToNoteId = quoteToNoteId, note = note)
        val viewModel = ViewModelProvider(this, factory)[NoteEditorViewModel::class.java]
        mViewModel = viewModel
        binding.viewModel = viewModel
        val simpleImagePreviewAdapter = SimpleImagePreviewAdapter(viewModel)
        binding.imageListPreview.adapter = simpleImagePreviewAdapter

        viewModel.editorFiles.observe(this, Observer{list ->
            simpleImagePreviewAdapter.submitList(list)
        })
        viewModel.poll.observe(this, Observer { poll ->
            if(poll == null){
                removePollFragment()
            }else{
                setPollFragment()
            }
        })

        viewModel.noteTask.observe(this, Observer{postNote->
            Log.d("NoteEditorActivity", "$postNote")
            val intent = Intent(this, PostNoteService::class.java)
            intent.putExtra(PostNoteService.EXTRA_NOTE_TASK, postNote)
            startService(intent)
            finish()
        })

        viewModel.showVisibilitySelectionEvent.observe(this, Observer {
            Log.d("NoteEditorActivity", "公開範囲を設定しようとしています")
            val dialog = VisibilitySelectionDialog()
            dialog.show(supportFragmentManager, "NoteEditor")
        })

        viewModel.address.observe(this, Observer{
            userChipAdapter.submitList(it)
        })

        viewModel.showPollTimePicker.observe(this, Observer{
            PollTimePickerDialog().show(supportFragmentManager, "TimePicker")
        })

        viewModel.showPollDatePicker.observe(this, Observer {
            PollDatePickerDialog().show(supportFragmentManager, "DatePicker")
        })

        viewModel.showPreviewFileEvent.observe(this, Observer{ file ->
            file.fileProperty?.let{ fp ->
                startActivity(MediaActivity.newIntent(this, fp))
            }
            file.uploadFile?.getUri()?.let{ uri ->
                startActivity(MediaActivity.newIntent(this, uri))
            }
        })

        selectFileFromDrive.setOnClickListener {
            showDriveFileSelector()
        }

        selectFileFromLocal.setOnClickListener {
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

        (applicationContext as? MiApplication)?.connectionStatus?.observe(this, Observer{ status ->
            when(status){
                ConnectionStatus.SUCCESS -> Log.d("MainActivity", "成功")
                ConnectionStatus.ACCOUNT_ERROR ->{
                    finish()
                    startActivity(Intent(this, AppAuthActivity::class.java))
                }
                ConnectionStatus.NETWORK_ERROR ->{
                    Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                }
                else -> Log.d("MainActivity", "not initialized")
            }
        })

        binding.inputMain.requestFocus()
    }

    override fun onSelect(emoji: Emoji) {
        mViewModel?.addEmoji(emoji)
    }

    override fun onSelect(emoji: String) {
        mViewModel?.addEmoji(emoji)
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

    private fun showFileManager(){
        if(checkPermission()){
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, SELECT_LOCAL_FILE_REQUEST_CODE)
        }else{
            requestPermission()
        }

    }

    private fun showDriveFileSelector(){
        val selectedSize = mViewModel?.totalImageCount?.value?: 0

        //Directoryは既に選択済みのファイルの数も含めてしまうので選択済みの数も合わせる
        val selectableMaxSize = 4 - selectedSize
        Log.d("", "選択済みのサイズ:$selectedSize")
        val intent = Intent(this, DriveActivity::class.java)
            .putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, selectableMaxSize)

        startActivityForResult(intent, SELECT_DRIVE_FILE_REQUEST_CODE)
    }

    private fun checkPermission(): Boolean{
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission(){
        //val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if(! checkPermission()){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    private fun startSearchAndSelectUser(){
        val selectedUserIds = mViewModel?.address?.value?.map{
            it.userId
        }?.toTypedArray()?: emptyArray()

        val intent = Intent(this, SearchAndSelectUserActivity::class.java)
        intent.putExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_IDS, selectedUserIds)
        startActivityForResult(intent, SELECT_USER_REQUEST_CODE)
    }

    private fun startMentionToSearchAndSelectUser(){
        val intent = Intent(this, SearchAndSelectUserActivity::class.java)
        startActivityForResult(intent, SELECT_MENTION_TO_USER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            SELECT_DRIVE_FILE_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    val files = (data?.getSerializableExtra(DriveActivity.EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE) as List<*>?)?.map{
                        it as FileProperty
                    }
                    //mViewModel?.driveFiles?.postValue(files)
                    if(files != null){
                        val exFiles = mViewModel?.driveFiles()
                        val addFiles = files.filter{out ->
                            exFiles?.firstOrNull {
                                it == out
                            } == null
                        }
                        mViewModel?.addAllFileProperty(addFiles)
                    }
                }
            }
            SELECT_LOCAL_FILE_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    Log.d("NoteEditorActivity", "選択した")

                    val uri = data?.data
                    if(uri != null){
                        val size = mViewModel?.fileTotal()

                        if(size != null && size < 4){
                            mViewModel?.add(uri)
                            Log.d("NoteEditorActivity", "成功しました")
                        }else{
                            Log.d("NoteEditorActivity", "失敗しました")
                        }

                    }

                }
            }
            READ_STORAGE_PERMISSION_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    showFileManager()
                }else{
                    Toast.makeText(this, "ストレージへのアクセスを許可しないとファイルを読み込めないぽよ", Toast.LENGTH_LONG).show()
                }
            }
            SELECT_USER_REQUEST_CODE ->{
                if(resultCode == RESULT_OK && data != null){
                    val added = data.getStringArrayExtra(SearchAndSelectUserActivity.EXTRA_ADDED_USER_IDS)
                    val removed = data.getStringArrayExtra(SearchAndSelectUserActivity.EXTRA_REMOVED_USER_IDS)
                    if(added != null && removed != null){
                        mViewModel?.setAddress(added, removed)
                    }
                }
            }
            SELECT_MENTION_TO_USER_REQUEST_CODE ->{
                if(resultCode == RESULT_OK && data != null){
                    val users = (data.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USERS) as ArrayList<*>).mapNotNull {
                        it as? User
                    }
                    mViewModel?.addMentionUsers(users)
                }
            }
        }
    }



}
