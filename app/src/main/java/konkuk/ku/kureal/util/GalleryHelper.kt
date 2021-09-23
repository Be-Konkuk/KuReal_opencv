package konkuk.ku.kureal.util

import android.content.Context
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class GalleryHelper (context:Context){

    var mContext = context
    lateinit var currentPhotoPath: String

    val photoFile: File? = try {
        createImageFile()
    } catch (ex: IOException) {
        // Error occurred while creating the File
        Log.d("test", "error: $ex")
        null
    }

    //tmp 사진 파일 생성
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Log.d("test", "currentPhotoPath : $currentPhotoPath")
        }
    }

    //갤러리에 CurrentPhotoPath에 존재하는 사진 저장하기
    fun galleryAddPic() {
        Log.d("test", "currentPhotoPath2 : $currentPhotoPath")

        val file = File(currentPhotoPath)
        MediaScannerConnection.scanFile(mContext, arrayOf(file.toString()), null, null)
    }

    //갤러리에서 사진 선택
    fun galleryChoosePic(result: ActivityResult) :ArrayList<Any> {
        val imageList = ArrayList<Any>() //URI가 들어 갈 배열

        //한 개 고름
        if (result?.getData()?.clipData == null) {
            Log.i("1. single choice", result?.getData()?.data.toString())
            var source: ImageDecoder.Source? = null
            val imageBitmap = result?.getData()?.data.let { uri ->
                if (uri != null) {
                    imageList.add(uri)
                    source = ImageDecoder.createSource(mContext.contentResolver, uri)
                }
            }
        }

        //다중 선택 시
        else{
            val clipData = result?.getData()?.clipData
            Log.i("clipdata", clipData!!.itemCount.toString())

            if (clipData.itemCount > 10) {
                Toast.makeText(mContext, "사진은 10개까지 선택가능 합니다.", Toast.LENGTH_SHORT).show()
            }
            //한 개 고름
            else if (clipData.itemCount == 1) {
                Log.i("2. clipdata choice", clipData.getItemAt(0).uri.toString())
                Log.i("2. single choice", clipData.getItemAt(0).uri.path!!)
                imageList.add(clipData.getItemAt(0).uri)
            }
            //1~10개 고름
            else if (clipData.itemCount in 2..9) {
                var i = 0
                while (i < clipData.itemCount) {
                    Log.i("3. single choice", clipData.getItemAt(i).uri.toString())
                    imageList.add(clipData.getItemAt(i).uri)
                    i++
                }
            }
        }
        return imageList
    }
}