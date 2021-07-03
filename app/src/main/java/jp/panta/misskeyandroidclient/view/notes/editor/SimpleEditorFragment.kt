package jp.panta.misskeyandroidclient.view.notes.editor

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.*
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentSimpleEditorBinding
import jp.panta.misskeyandroidclient.api.drive.FilePropertyDTO
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.file.toFile
import jp.panta.misskeyandroidclient.util.listview.applyFlexBoxLayout
import jp.panta.misskeyandroidclient.view.account.AccountSwitchingDialog
import jp.panta.misskeyandroidclient.view.emojis.CustomEmojiPickerDialog
import jp.panta.misskeyandroidclient.view.text.CustomEmojiCompleteAdapter
import jp.panta.misskeyandroidclient.view.text.CustomEmojiTokenizer
import jp.panta.misskeyandroidclient.view.users.UserChipListAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.account.AccountViewModel
import jp.panta.misskeyandroidclient.viewmodel.emojis.EmojiSelectionViewModel
import jp.panta.misskeyandroidclient.viewmodel.file.FileListener
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SelectedUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

interface SimpleEditor{

    val isShowEditorMenu: MutableLiveData<Boolean>
    fun goToNormalEditor()

    fun closeMenu()
    fun openMenu()
}

class SimpleEditorFragment : Fragment(R.layout.fragment_simple_editor), FileListener, SimpleEditor {

    companion object{
        const val SELECT_DRIVE_FILE_REQUEST_CODE = 1141
        const val SELECT_LOCAL_FILE_REQUEST_CODE = 5142
        const val READ_STORAGE_PERMISSION_REQUEST_CODE = 191
        const val SELECT_USER_REQUEST_CODE = 814
        const val SELECT_MENTION_TO_USER_REQUEST_CODE = 939

        private const val CONFIRM_SAVE_AS_DRAFT_OR_DELETE = "confirm_save_as_draft_or_delete"
    }

    var mViewModel: NoteEditorViewModel? = null
    private val mBinding: FragmentSimpleEditorBinding by dataBinding()

    override val isShowEditorMenu: MutableLiveData<Boolean> = MutableLiveData(false)

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        mBinding.simpleEditor = this

        val miApplication = requireContext().applicationContext as MiApplication
        mBinding.lifecycleOwner = this

        val userChipAdapter = UserChipListAdapter(viewLifecycleOwner)
        mBinding.addressUsersView.adapter = userChipAdapter
        mBinding.addressUsersView.applyFlexBoxLayout(requireContext())


        val accountViewModel = ViewModelProvider(this, AccountViewModel.Factory(miApplication))[AccountViewModel::class.java]
        mBinding.accountViewModel = accountViewModel
        accountViewModel.switchAccount.observe(this) {
            AccountSwitchingDialog().show(childFragmentManager, "tag")
        }
        accountViewModel.showProfile.observe(this) {
            val intent = UserDetailActivity.newInstance(requireContext(), userId = User.Id(it.accountId, it.remoteId))
            intent.putActivity(Activities.ACTIVITY_IN_APP)

            startActivity(intent)
        }

        miApplication.getCurrentInstanceMeta()?.emojis?.let{ emojis ->
            mBinding.inputMainText.setAdapter(
                CustomEmojiCompleteAdapter(
                    emojis,
                    requireContext()
                )
            )
            mBinding.inputMainText.setTokenizer(CustomEmojiTokenizer())

            mBinding.inputCw.setAdapter(
                CustomEmojiCompleteAdapter(
                    emojis,
                    requireContext()
                )
            )
            mBinding.inputCw.setTokenizer(CustomEmojiTokenizer())
        }

        val factory = NoteEditorViewModelFactory(miApplication, replyToNoteId = null, quoteToNoteId = null, draftNote = null)
        val viewModel = ViewModelProvider(requireActivity(), factory)[NoteEditorViewModel::class.java]
        mViewModel = viewModel

        mBinding.noteEditorViewModel = viewModel

        val simpleImagePreviewAdapter = SimpleImagePreviewAdapter(this)
        mBinding.imageListPreview.adapter = simpleImagePreviewAdapter
        mBinding.imageListPreview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        viewModel.files.observe(viewLifecycleOwner) { list ->
            simpleImagePreviewAdapter.submitList(list)
        }
        viewModel.poll.observe(viewLifecycleOwner) { poll ->
            if (poll == null) {
                removePollFragment()
            } else {
                setPollFragment()
            }
        }


        viewModel.isPost.observe(viewLifecycleOwner) {
            viewModel.clear()
        }

        viewModel.showVisibilitySelectionEvent.observe(viewLifecycleOwner) {
            Log.d("NoteEditorActivity", "公開範囲を設定しようとしています")
            val dialog = VisibilitySelectionDialog()
            dialog.show(childFragmentManager, "NoteEditor")
        }

        viewModel.address.observe(viewLifecycleOwner) {
            userChipAdapter.submitList(it)
        }

        viewModel.showPollTimePicker.observe(this) {
            PollTimePickerDialog().show(childFragmentManager, "TimePicker")
        }

        viewModel.showPollDatePicker.observe(this) {
            PollDatePickerDialog().show(childFragmentManager, "DatePicker")
        }



        mBinding.selectFileFromDrive.setOnClickListener {
            showDriveFileSelector()
        }

        mBinding.selectFileFromLocal.setOnClickListener {
            showFileManager()
        }

        mBinding.addAddress.setOnClickListener {
            startSearchAndSelectUser()
        }

        mBinding.mentionButton.setOnClickListener {
            startMentionToSearchAndSelectUser()
        }

        mBinding.showEmojisButton.setOnClickListener {
            CustomEmojiPickerDialog().show(childFragmentManager, "Editor")
        }


        mBinding.postButton.setOnClickListener {
            viewModel.post()
        }

        val emojiSelectionViewModel = ViewModelProvider(requireActivity())[EmojiSelectionViewModel::class.java]
        emojiSelectionViewModel.selectedEmojiName.observe(viewLifecycleOwner, (::onSelect))
        emojiSelectionViewModel.selectedEmoji.observe(viewLifecycleOwner, (::onSelect))

    }

    private fun onSelect(emoji: Emoji) {
        val pos = mBinding.inputMainText.selectionEnd
        mViewModel?.addEmoji(emoji, pos)?.let{ newPos ->
            mBinding.inputMainText.setText(mViewModel?.text?.value?: "")
            mBinding.inputMainText.setSelection(newPos)
            Log.d("NoteEditorActivity", "入力されたデータ:${mBinding.inputMainText.text}")
        }
    }

    private fun onSelect(emoji: String) {
        val pos = mBinding.inputMainText.selectionEnd
        mViewModel?.addEmoji(emoji, pos)?.let{ newPos ->
            mBinding.inputMainText.setText(mViewModel?.text?.value?: "")
            mBinding.inputMainText.setSelection(newPos)
        }
    }

    private fun setPollFragment(){
        val ft = childFragmentManager.beginTransaction()
        ft.replace(R.id.edit_poll, PollEditorFragment(), "pollFragment")
        ft.commit()
    }

    private fun removePollFragment(){
        val fragment = childFragmentManager.findFragmentByTag("pollFragment")
        if(fragment != null){
            val ft = childFragmentManager.beginTransaction()
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
        val intent = Intent(requireContext(), DriveActivity::class.java)
            .putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, selectableMaxSize)

        registerForOpenDriveActivityResult.launch(intent)
    }

    private fun checkPermission(): Boolean{
        val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission(){
        //val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if(! checkPermission()){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun startSearchAndSelectUser(){
        val selectedUserIds = mViewModel?.address?.value?.mapNotNull {
            it.userId?: it.user.value?.id
        } ?: emptyList()

        val intent = SearchAndSelectUserActivity.newIntent(requireContext(), selectedUserIds = selectedUserIds)
        startActivityForResult(intent, SELECT_USER_REQUEST_CODE)
    }

    private fun startMentionToSearchAndSelectUser(){
        val intent = Intent(requireContext(), SearchAndSelectUserActivity::class.java)
        startActivityForResult(intent, SELECT_MENTION_TO_USER_REQUEST_CODE)
    }

    override fun onSelect(file: File?) {
        file?.let{
            val intent = Intent(requireContext(), MediaActivity::class.java)
            intent.putExtra(MediaActivity.EXTRA_FILE, file)
            startActivity(intent)
        }

    }

    override fun onDetach(file: File?) {
        file?.let{
            mViewModel?.removeFileNoteEditorData(file)
        }
    }

    override fun closeMenu() {
        isShowEditorMenu.value = false
    }
    override fun openMenu() {
        isShowEditorMenu.value = true
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun goToNormalEditor() {
        mViewModel?.toDraftNote()?.let{
            val intent = NoteEditorActivity.newBundle(requireContext(), draftNote = it)
            startActivity(intent)
            mViewModel?.clear()
        }

    }

    private val registerForOpenDriveActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            val selectedFilePropertyIds = (result.data?.getSerializableExtra(DriveActivity.EXTRA_SELECTED_FILE_PROPERTY_IDS) as List<*>).map {
                it as FileProperty.Id
            }
            mViewModel?.addFilePropertyFromIds(selectedFilePropertyIds)
        }

    }
    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode and 0xffff){

            SELECT_LOCAL_FILE_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    Log.d("NoteEditorActivity", "選択した")

                    val uri = data?.data
                    if(uri != null){
                        val size = mViewModel?.fileTotal()

                        if(size != null && size < 4){
                            mViewModel?.add(uri.toFile(requireContext()))
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
                    Toast.makeText(requireContext(), "ストレージへのアクセスを許可しないとファイルを読み込めないぽよ", Toast.LENGTH_LONG).show()
                }
            }
            SELECT_USER_REQUEST_CODE ->{
                if(resultCode == RESULT_OK && data != null){
                    val changedDiff = data.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? SelectedUserViewModel.ChangedDiffResult
                    val added = changedDiff?.added
                    val removed = changedDiff?.removed
                    if(added != null && removed != null){
                        mViewModel?.setAddress(added, removed)
                    }
                }
            }
            SELECT_MENTION_TO_USER_REQUEST_CODE ->{
                if(resultCode == RESULT_OK && data != null){
                    val changedDiff = data.getSerializableExtra(SearchAndSelectUserActivity.EXTRA_SELECTED_USER_CHANGED_DIFF) as? SelectedUserViewModel.ChangedDiffResult
                    val users = changedDiff?.selectedUsers?: emptyList()
                    val pos = mBinding.inputMainText.selectionEnd

                    mViewModel?.addMentionUsers(users, pos)?.let{ newPos ->
                        mBinding.inputMainText.setText(mViewModel?.text?.value?: "")
                        mBinding.inputMainText.setSelection(newPos)
                    }
                }
            }
        }
    }


 

}