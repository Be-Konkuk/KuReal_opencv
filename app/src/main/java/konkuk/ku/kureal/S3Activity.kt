package konkuk.ku.kureal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import konkuk.ku.kureal.databinding.ActivityMainBinding
import konkuk.ku.kureal.util.GalleryHelper
import konkuk.ku.kureal.util.PermissionHelper
import konkuk.ku.kureal.util.PictureHelper
import java.io.File


class S3Activity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding ?: error("View를 참조하기 위해 binding이 초기화되지 않았습니다.")
    //권한 요청
    private var permissionHelper: PermissionHelper? = null
    private var galleryHelper: GalleryHelper? = null
    private var pictureHelper: PictureHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionHelper = PermissionHelper(this) //권한 요청
        galleryHelper = GalleryHelper(this) //사진 촬영 및 저장
        pictureHelper = PictureHelper(this) //사진 합치기 및 절대경로 변환
        goCheckPermissions()

        binding.btnCamera.setOnClickListener {
            //버튼 클릭 시 이미지 불러와서 S3 전송
            chooseImg()
        }
    }

    private fun goCheckPermissions() {
        if (!permissionHelper!!.checkPermissions()) { //권한 요청 거부
            permissionHelper!!.requestPermission()
        }
    }

    /**
     * 갤러리에서 이미지 선택하기 */
    private fun chooseImg() {
        var shareIntent = Intent()
        shareIntent.action = "android.intent.action.GET_CONTENT"
        shareIntent.type = "image/*"
        //shareIntent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true) //다중선택
        shareIntent = Intent.createChooser(shareIntent, null as CharSequence?)
        this.chooseActivityLauncher.launch(shareIntent)
    }

    /**
     * 선택 후 activity */
    private val chooseActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
            result: ActivityResult ->
        //content://com.android.providers.media.documents/document/image%3A30 형태로 저장.
        //꺼낼 때는 getFullPathFromUri(requireContext(),imageList[n]) 사용
//        var imageList = ArrayList<Any>() //URI가 들어 갈 배열
//        imageList = galleryHelper.galleryChoosePic(result)
//        if(imageList.size > 0){
//            showSpherePanorama(imageList) //파노라마 뷰 설정
//        }
        var imageUriList = galleryHelper?.galleryChoosePic(result)
        var file = File(pictureHelper?.getFullPathFromUri(this, imageUriList?.get(0) as Uri))

        Log.d("S3ACTIVITY",file.name)

        uploadWithTransferUtilty(file.getName(), file); // file.getName()으로 파일 이름 가져옴
    }

    fun uploadWithTransferUtilty(fileName: String?, file: File?) {
        val awsCredentials: AWSCredentials =
            BasicAWSCredentials("${BuildConfig.aws_accesskey}", "${BuildConfig.aws_secret_accesskey}") // IAM 생성하며 받은 것 입력
        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_NORTHEAST_2))

        //val s3Client = AmazonS3Client(credentialsProvider)
        val transferUtility =
            TransferUtility.builder().s3Client(s3Client).context(this.applicationContext).build()
        TransferNetworkLossHandler.getInstance(this.applicationContext)
        val uploadObserver = transferUtility.upload(
            "kureal/photo",
            fileName,
            file
        ) // (bucket api, file이름, file객체)

        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                Log.d("MYTAG", "onStateChanged: " + id + ", " + state.toString());
                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (current.toDouble() / total * 100.0).toInt()
                Log.d("MYTAG", "UPLOAD - - ID:$id, percent done = $done")
            }

            override fun onError(id: Int, ex: Exception) {
                Log.d("MYTAG", "UPLOAD ERROR - - ID: $id - - EX:$ex")
            }
        })
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }
}