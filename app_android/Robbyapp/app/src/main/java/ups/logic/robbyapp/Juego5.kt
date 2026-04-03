package ups.logic.robbyapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.airbnb.lottie.LottieAnimationView
import ups.logic.robbyapp.databinding.ActivityJuego1Binding
import ups.logic.robbyapp.databinding.ActivityJuego4Binding
import ups.logic.robbyapp.databinding.ActivityJuego5Binding
import java.io.File
import java.util.TreeMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Juego5 : AppCompatActivity() {

    private var cameraProvider: ProcessCameraProvider? = null

    private var binding: ActivityJuego5Binding? = null

    private val fragmento
        get() = binding!!

    private lateinit var imageCapture: ImageCapture
    private lateinit var img: ImageView

    private val handler = Handler(Looper.getMainLooper())
    private val frameInterval: Long = 5  // Intervalo en milisegundos (500ms)


    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private val processingThread = HandlerThread("ProcessingThread").apply { start() }
    private val processingHandler = android.os.Handler(processingThread.looper)

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmapBuffer: Bitmap
    private var camera: Camera? = null

    private lateinit var media: MediaPlayer
    private lateinit var imagen:ImageView
    private lateinit var volver: ImageButton

    private lateinit var animacion: LottieAnimationView

    var audiosPictogramas = TreeMap<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJuego5Binding.inflate(layoutInflater)
        setContentView(fragmento.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()

        media = MediaPlayer.create(this, R.raw.saludojuego5)
        media.start()

        imagen = findViewById(R.id.imagen)

        cameraExecutor = Executors.newSingleThreadExecutor()


        fragmento.camara.post{

            setUpCamera()
        }


        val ruta = copyRawFileToInternalStorage(this, R.raw.cara, "ruta.xml")

    //    cargarxml(ruta)


        hideSystemUI()
        volver = findViewById(R.id.volver)
        volver.setOnClickListener {
            finish()
        }
    }

    private fun allPermissionsGranted()=
        Constans.REQUIRED_PERMISSIONS.all{
            this?.let { it1 ->
                ContextCompat.checkSelfPermission(
                    it1, it
                )
            } == PackageManager.PERMISSION_GRANTED
        }


    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()

                startCamera()
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun process(bitmap: Bitmap){
        val bIn: Bitmap = bitmap.copy(bitmap.config, true)
        var lado: String = ""
        //lado = procesarImagen(bitmap, bIn, lado)
        //print(lado)
        runOnUiThread {
            binding!!.imagen.setImageBitmap(bIn) // Asegúrate de que `imageView` no sea null
        }

    }

    fun copyRawFileToInternalStorage(context: Context, rawResId: Int, fileName: String): String {
        val inputStream = context.resources.openRawResource(rawResId)
        val outputFile = File(/* parent = */ context.filesDir, /* child = */ fileName)

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return outputFile.absolutePath
    }

    fun displayBitmap(image: ImageProxy) {
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        val imageRotation = image.imageInfo.rotationDegrees
        process(bitmapBuffer)

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        //val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(fragmento.camara.display.rotation)
            .build()

        imageAnalyzer =
            ImageAnalysis.Builder()
                //.setTargetResolution(Size(640, 640))  // Resolución deseada para el análisis
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setTargetRotation(fragmento.camara.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()


                .also {
                    it.setAnalyzer(cameraExecutor){ image ->
                        if(!::bitmapBuffer.isInitialized){
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }
                        displayBitmap(image)
                    }
                }
        cameraProvider.unbindAll()


        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

            preview?.setSurfaceProvider(fragmento.camara.surfaceProvider)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //}, ContextCompat.getMainExecutor(this))
    }


    override fun onPause() {
        super.onPause()
        // Detener el envío de frames cuando la actividad esté pausada
        handler.removeCallbacksAndMessages(null)
    }




    @RequiresApi(value = 24)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())

            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
//
//    external fun procesarImagen(bitmapIn: Bitmap, bitmapOut: Bitmap, lado: String): String
//    external fun cargarxml(ruta: String)

//    companion object {
//        // Used to load the 'robbyapp' library on application startup.
//        init {
//            System.loadLibrary("robbyapp")
//        }
//    }


}

object Constans {
    const val TAG = "cameraX"
    const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
    const val REQUEST_CODE_PERMISSIONS = 123
    val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.INTERNET)
}