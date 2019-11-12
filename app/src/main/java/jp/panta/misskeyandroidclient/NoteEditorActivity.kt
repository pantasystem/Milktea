package jp.panta.misskeyandroidclient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.databinding.ActivityNoteEditorBinding
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.NoteEditorViewModelFactory
import kotlinx.android.synthetic.main.activity_note_editor.*
import java.io.File

class NoteEditorActivity : AppCompatActivity() {

    companion object{
        const val SELECT_DRIVE_FILE_REQUEST_CODE = 114
        const val SELECT_LOCAL_FILE_REQUEST_CODE = 514
        const val READ_STORAGE_PERMISSION_REQUEST_CODE = 1919
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
            showDriveFileSelector()
        }

        selectFileFromLocal.setOnClickListener {
            showFileManager()
        }
    }

    private fun showFileManager(){
        if(checkPermission()){
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, SELECT_LOCAL_FILE_REQUEST_CODE)
        }else{
            requestPermission()
        }

    }

    private fun showDriveFileSelector(){
        val selectedSize = mViewModel?.totalImageCount?.value?: 0
        val selected = mViewModel?.driveImages?.value
        val selectableMaxSize = 4 - selectedSize
        val intent = Intent(this, DriveActivity::class.java)
            .putExtra(DriveActivity.EXTRA_INT_SELECTABLE_FILE_MAX_SIZE, selectableMaxSize)
        if(selected != null){
            intent.putExtra(DriveActivity.EXTRA_FILE_PROPERTY_LIST_SELECTED_FILE, ArrayList<FileProperty>(selected))
        }
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
            SELECT_LOCAL_FILE_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    Log.d("NoteEditorActivity", "選択した")

                    val uri = data?.data
                    if(uri != null){
                        val file = if(uri.toString().startsWith("content://media")){
                            getMediaFile(data)
                        }else{
                            getDocumentFile(data)
                        }
                        Log.d("NoteEditorActivity", "fileは有効なのか？:${file?.exists()}")
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
        }
    }

    private fun getDocumentFile(data: Intent): File {
        val strDocId = DocumentsContract.getDocumentId(data.data)

        val strSplittedDocId = strDocId.split(":")
        Log.d("EditNoteActivity", "strSplittedDocId $strSplittedDocId")
        val strId = strSplittedDocId[strSplittedDocId.size - 1]

        val crsCursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns.DATA),
            "_id=?",
            arrayOf(strId),
            null
        )
        crsCursor?.moveToFirst()
        val filePath = crsCursor?.getString(0)
        crsCursor?.close()
        Log.d("EditNoteActivity", "filePath $filePath")
        return File(filePath)
    }


    private fun getMediaFile(data: Intent): File?{
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(data.data!!, projection, null, null, null, null)

        val path: String?
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(0)
            }else{
                path = null
            }
            cursor.close()

        }else{
            path = null
        }
        return if(path == null) null else File(path)
    }

}
