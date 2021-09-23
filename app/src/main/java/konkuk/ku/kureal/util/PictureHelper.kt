package konkuk.ku.kureal.util

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import java.util.ArrayList

class PictureHelper(context:Context) {
    private val mContext = context

    fun mergeMultiple(imageList: ArrayList<Any>): Bitmap? {
        val listBmp: ArrayList<Bitmap> = ArrayList<Bitmap>()
        if (listBmp != null) {
            //imageList에 존재하는 것 절대 경로로 변환해서 bitmap으로 변환
            for (i in imageList.indices) {
                listBmp.add(BitmapFactory.decodeFile(getFullPathFromUri(mContext,imageList[i] as Uri)))
            }
        }
        val result =
            Bitmap.createBitmap(listBmp[0].width * imageList.size, listBmp[0].height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(result)
        val paint = Paint()
        // TODO : 높이가 다른 이미지들을 붙이면 제대로 붙지 않음
        for (i in listBmp.indices) {
            canvas.drawBitmap(listBmp[i], (listBmp[i].width * (i % imageList.size)).toFloat(), (listBmp[i].height * (i / imageList.size)).toFloat(), paint)
        }
        return result
    }

    //절대경로 반환
    fun
            getFullPathFromUri(ctx: Context, fileUri: Uri?): String? {
        var fullPath: String? = null
        val column = "_data"
        var cursor: Cursor? =
            fileUri?.let { ctx.getContentResolver().query(it, null, null, null, null) }
        if (cursor != null) {
            cursor.moveToFirst()
            var document_id: String = cursor.getString(0)
            if (document_id == null) {
                for (i in 0 until cursor.getColumnCount()) {
                    if (column.equals(cursor.getColumnName(i), ignoreCase = true)) {
                        fullPath = cursor.getString(i)
                        break
                    }
                }
            }
            else {
                document_id = document_id.substring(document_id.lastIndexOf(":") + 1)
                cursor.close()
                val projection = arrayOf(column)
                try {
                    cursor = ctx.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        MediaStore.Images.Media._ID + " = ? ",
                        arrayOf(document_id),
                        null
                    )
                    if (cursor != null) {
                        cursor.moveToFirst()
                        fullPath = cursor.getString(cursor.getColumnIndexOrThrow(column))
                    }
                } finally {
                    if (cursor != null) cursor.close()
                }
            }
        }
        return fullPath
    }
}