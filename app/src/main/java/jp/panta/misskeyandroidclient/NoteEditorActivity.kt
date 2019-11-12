package jp.panta.misskeyandroidclient

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import jp.panta.misskeyandroidclient.databinding.ActivityNoteEditorBinding
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModelFactory
import kotlinx.android.synthetic.main.activity_note_editor.*

class NoteEditorActivity : AppCompatActivity() {

    companion object{
        const val SELECT_DRIVE_FILE_REQUEST_CODE = 114
    }
    private var mViewModel: NoteEditorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)
        setSupportActionBar(note_editor_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val binding = DataBindingUtil.setContentView<ActivityNoteEditorBinding>(this, R.layout.activity_note_editor)

        //binding.viewModel
        binding.lifecycleOwner = this

        val miApplication = applicationContext as MiApplication
        miApplication.currentConnectionInstanceLiveData.observe(this, Observer {
            val factory = NoteEditorViewModelFactory(it, miApplication)
            val viewModel = ViewModelProvider(this, factory)[NoteEditorViewModel::class.java]
            mViewModel = viewModel
            binding.viewModel = viewModel
        })

        selectFileFromDrive.setOnClickListener {
            val selectedSize = mViewModel?.totalImageCount?.value?: 0
            val selected = mViewModel?.driveImages?.value
            val selectableMaxSize = 4 - selectedSize
            val intent = Intent(this, DriveActivity::class.java)
                .putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, selectableMaxSize)
            if(selected != null){
                intent.putExtra(DriveActivity.EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE, ArrayList(selected))
            }
            startActivityForResult(intent, SELECT_DRIVE_FILE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            SELECT_DRIVE_FILE_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    val files = (data?.getSerializableExtra(DriveActivity.EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE) as List<*>?)?.map{
                        it as FileProperty
                    }
                    mViewModel?.driveImages?.postValue(files)
                }
            }
        }
    }
}
