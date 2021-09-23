package konkuk.ku.kureal.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHelper (context:Context){
    private val mContext = context
    private val permissionList : MutableList<String> = mutableListOf()

    companion object{
        // Permisisons
        val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val PERMISSIONS_REQUEST = 100
    }

    //허용 받아야할 권한이 남았는지 체크
    public fun checkPermissions(): Boolean {

        //배열로 선언한 권한 중 허용하지 않은 배열이 있는지 체크
        for(permission in PERMISSIONS){
            val result = ContextCompat.checkSelfPermission(mContext, permission)
            if(result != PackageManager.PERMISSION_GRANTED){
                permissionList.add(permission)
            }
        }
        if(permissionList.isNotEmpty()){
            return false
        }
        return true
    }

    //권한 허용 요청
    public fun requestPermission(){
        ActivityCompat.requestPermissions(mContext as Activity, permissionList.toTypedArray(), PERMISSIONS_REQUEST)
    }

    //권한 요청에 대한 결과 처리
    public fun permissionResult(requestCode: Int, permissions:Array<out String>, grantResults:IntArray) : Boolean {
        if(requestCode == PERMISSIONS_REQUEST && (grantResults.size >0)){
            for (g in grantResults){
                //하나라도 거부했으면 false 리턴
                if(g == -1) return false
            }
        }
        return true
    }

}