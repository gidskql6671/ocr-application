package com.example.ocr_application

import android.Manifest
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ocr_application.databinding.ActivityMainBinding
import com.googlecode.tesseract.android.TessBaseAPI
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.*
import kotlin.math.max
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {

    private val language = "eng"
    private lateinit var tessBaseAPI: TessBaseAPI

    lateinit var button: Button
    lateinit var imageView: ImageView
    lateinit var surfaceView: CameraSurfaceView
    lateinit var textView: TextView

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //권한 체크
        TedPermission.create()
            .setPermissionListener(object: PermissionListener {
                override fun onPermissionGranted() {
                    logic()
                }
                override fun onPermissionDenied(deniedPermissions: MutableList<String>) {
                    Toast.makeText(this@MainActivity,
                        "권한을 허가해주세요.",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })
            .setRationaleMessage("카메라 권한이 필요합니다.")
            .setDeniedMessage("카메라 권한을 거부하셨습니다.")
            .setPermissions(Manifest.permission.CAMERA)
            .check();

    }

    override fun onDestroy() {
        super.onDestroy()

        tessBaseAPI.recycle()
    }

    private fun logic() {
        imageView = binding.imageView
        surfaceView = binding.surfaceView
        textView = binding.textView

        button = binding.button.also {
            it.setOnClickListener { capture() }
        }

        tessBaseAPI = TessBaseAPI()
        Log.i("help12", "version : ${tessBaseAPI.version}")
        val dir = "$filesDir/tesseract"
        if (checkLanguageFile("$dir/tessdata")) {
            tessBaseAPI.init(dir, language, TessBaseAPI.OEM_LSTM_ONLY)
        }
    }

    private fun capture() {
        surfaceView.capture { bytes, camera ->
            var originBitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            val src = Mat()
            val bmp32: Bitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true)
            Utils.bitmapToMat(bmp32, src)

            // 흑백 사진으로 전환
            val graySrc = Mat()
            Imgproc.cvtColor(src, graySrc, Imgproc.COLOR_BGR2GRAY)

            // 이진화
            val binarySrc = Mat()
            Imgproc.threshold(graySrc, binarySrc, 0.0, 255.0, Imgproc.THRESH_OTSU)

            // 윤곽선 찾기
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(
                binarySrc,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE
            )

            // 가장 면적이 큰 윤곽선 찾기
            var biggestContour: MatOfPoint? = null
            var biggestContourArea: Double = 0.0
            for (contour in contours) {
                val area = Imgproc.contourArea(contour)
                if (area > biggestContourArea) {
                    biggestContour = contour
                    biggestContourArea = area
                }
            }

            if (biggestContour == null) {
                throw IllegalArgumentException("No Contour")
            }

            // 너무 작아도 안됨
            if (biggestContourArea < 400) {
                throw IllegalArgumentException("too small")
            }

            val candidate2f = MatOfPoint2f(*biggestContour.toArray())
            val approxCandidate = MatOfPoint2f()
            Imgproc.approxPolyDP(
                candidate2f,
                approxCandidate,
                Imgproc.arcLength(candidate2f, true) * 0.02,
                true
            )

//            // 사각형 판별
//            if (approxCandidate.rows() != 4) {
//                throw java.lang.IllegalArgumentException("It's not rectangle")
//            }
//
//            // 컨벡스(볼록한 도형)인지 판별
//            if (!Imgproc.isContourConvex(MatOfPoint(*approxCandidate.toArray()))) {
//                throw java.lang.IllegalArgumentException("It's not convex")
//            }

            // 좌상단부터 시계 반대 방향으로 정점을 정렬한다.
            val points = arrayListOf(
                Point(approxCandidate.get(0, 0)[0], approxCandidate.get(0, 0)[1]),
                Point(approxCandidate.get(1, 0)[0], approxCandidate.get(1, 0)[1]),
                Point(approxCandidate.get(2, 0)[0], approxCandidate.get(2, 0)[1]),
                Point(approxCandidate.get(3, 0)[0], approxCandidate.get(3, 0)[1]),
            )
            points.sortBy { it.x } // x좌표 기준으로 먼저 정렬

            if (points[0].y > points[1].y) {
                val temp = points[0]
                points[0] = points[1]
                points[1] = temp
            }

            if (points[2].y < points[3].y) {
                val temp = points[2]
                points[2] = points[3]
                points[3] = temp
            }

            // 원본 영상 내 정점들
            val srcQuad = MatOfPoint2f().apply { fromList(points) }

            val maxSize = calculateMaxWidthHeight(
                tl = points[0],
                bl = points[1],
                br = points[2],
                tr = points[3]
            )
            val dw = maxSize.width
            val dh = dw * maxSize.height/maxSize.width
            val dstQuad = MatOfPoint2f(
                Point(0.0, 0.0),
                Point(0.0, dh),
                Point(dw, dh),
                Point(dw, 0.0)
            )

            // 투시변환 매트릭스 구하기
            val perspectiveTransform = Imgproc.getPerspectiveTransform(srcQuad, dstQuad)

            // 투시변환 된 결과 영상 얻기
            val dst = Mat()
            Imgproc.warpPerspective(src, dst, perspectiveTransform, Size(dw, dh))

            var bitmapResult = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(dst, bitmapResult)

            bitmapResult = getRotatedBitmap(bitmapResult, 90)
            imageView.setImageBitmap(bitmapResult)

            originBitmap = getRotatedBitmap(originBitmap, 90)
            imageView.setImageBitmap(originBitmap)

            button.isEnabled = false
            button.text = "텍스트 인식중..."

            tessBaseAPI.setImage(bitmapResult)
            val result = tessBaseAPI.utF8Text

            Log.i("Help!!!!","utF8Text :\n$result")

            textView.text = result
            button.isEnabled = true
            button.text = "텍스트 인식"

            camera.startPreview()
        }
    }

    // 사각형 꼭짓점 정보로 사각형 최대 사이즈 구하기
    // 평면상 두 점 사이의 거리는 직각삼각형의 빗변길이 구하기와 동일
    private fun calculateMaxWidthHeight(
        tl:Point,
        tr:Point,
        br:Point,
        bl:Point,
    ):Size{
        // Calculate width
        val widthA = sqrt((tl.x - tr.x) * (tl.x - tr.x) + (tl.y - tr.y) * (tl.y - tr.y))
        val widthB = sqrt((bl.x - br.x) * (bl.x - br.x) + (bl.y - br.y) * (bl.y - br.y))
        val maxWidth = max(widthA, widthB)

        // Calculate height
        val heightA = sqrt((tl.x - bl.x) * (tl.x - bl.x) + (tl.y - bl.y) * (tl.y - bl.y))
        val heightB = sqrt((tr.x - br.x) * (tr.x - br.x) + (tr.y - br.y) * (tr.y - br.y))
        val maxHeight = max(heightA, heightB)
        return Size(maxWidth, maxHeight)
    }

    @Synchronized
    fun getRotatedBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        var bitmap = bitmap
        if (degrees != 0 && bitmap != null) {
            val m = Matrix()
            m.setRotate(degrees.toFloat(), bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
            try {
                val b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                if (bitmap != b2) {
                    bitmap = b2
                }
            } catch (ex: OutOfMemoryError) {
                ex.printStackTrace()
            }
        }
        return bitmap
    }

    private fun checkLanguageFile(dir: String): Boolean {
        val file = File(dir)
        if (!file.exists() && file.mkdirs()) {
            createFiles(dir)
        } else if (file.exists()) {
            val filePath = "$dir/$language.traineddata"
            val langDataFile = File(filePath)
            if (!langDataFile.exists()) {
                createFiles(dir)
            }
        }
        return true
    }

    private fun createFiles(dir: String) {
        val assetMgr: AssetManager = this.assets

        try {
            val inputStream: InputStream = assetMgr.open("$language.traineddata")
            val destFile = "$dir/$language.traineddata"

            val outputStream: OutputStream = FileOutputStream(destFile)
            val buffer = ByteArray(1024)
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }

            inputStream.close()
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    external fun stringFromJNI(): String

    companion object {
        init {
            System.loadLibrary("ocr_application")
            System.loadLibrary("opencv_java4")
        }
    }
}