package ups.logic.robbyapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ups.logic.robbyapp.Memoria
import ups.logic.robbyapp.bluetooth.BluetoothManager
import ups.logic.robbyapp.configuraciones.AndroidAudioRecorder
import ups.logic.robbyapp.configuraciones.ApiService
import ups.logic.robbyapp.databinding.ActivityJuego2Binding
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.TreeMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.text.lowercase
import kotlin.time.Duration.Companion.milliseconds

class Juego2 : AppCompatActivity() {
    //Variables para validar iteraciones de frames
    private var correctCount = 0
    private var incorrectCount = 0
    private var lastReceivedValue: String? = null
    private val threshold = 6
    private var ultimaVariable: String? = null

    //Configuraciones de la camara
    private var cameraProvider: ProcessCameraProvider? = null

    private var binding: ActivityJuego2Binding? = null

    private val fragmento
        get() = binding!!

    private var seleccion: Int = 1


    private val processingThread = HandlerThread("ProcessingThread").apply { start() }
    private val processingHandler = android.os.Handler(processingThread.looper)

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmapBuffer: Bitmap
    private var camera: Camera? = null

    private lateinit var puntaje : TextView

    private lateinit var volver: ImageButton
    private lateinit var reproducirAdivinanza: Button

    private lateinit var media: MediaPlayer

    private lateinit var repetirAudio: ImageButton
    private var audioActual: Int = 0

    private lateinit var animacion: LottieAnimationView
    private lateinit var fireworks: LottieAnimationView
    private lateinit var mensajeResultado: TextView

    private var vista: Boolean = true

    private lateinit var context: Context
    val nuevo: AndroidAudioRecorder = AndroidAudioRecorder(this)

    var audiosAAcuatico = TreeMap<Int, Int>()
    var audiosAAereo = TreeMap<Int, Int>()
    var audiosATerrestre = TreeMap<Int, Int>()
    var audiosTAcuatico = TreeMap<Int, Int>()
    var audiosTAereo = TreeMap<Int, Int>()
    var audiosTTerrestre = TreeMap<Int, Int>()
    var audiosColor = TreeMap<Int, Int>()
    var audiosComidaChatarra = TreeMap<Int, Int>()
    var audiosEmocion = TreeMap<Int, Int>()
    var audiosFruta = TreeMap<Int, Int>()
    var audiosHigiene = TreeMap<Int, Int>()
    var audiosInstrumentoMusical = TreeMap<Int, Int>()
    var audiosJuguete = TreeMap<Int, Int>()
    var audiosNumero = TreeMap<Int, Int>()
    var audiosPersona = TreeMap<Int, Int>()
    var audiosPrendaDeVestir = TreeMap<Int, Int>()
    var audiosVerdura = TreeMap<Int, Int>()

    var audios = listOf(audiosAAcuatico, audiosAAereo, audiosATerrestre, audiosTAcuatico, audiosTAereo, audiosTTerrestre, audiosColor, audiosComidaChatarra,
        audiosEmocion, audiosFruta, audiosHigiene, audiosInstrumentoMusical, audiosJuguete, audiosNumero, audiosPersona, audiosPrendaDeVestir, audiosVerdura)


    val ok = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(Memoria.url) //Siempre debe terminar en / la URL
        .addConverterFactory(GsonConverterFactory.create())
        .client(ok)
        .build()

    val apiService = retrofit.create(ApiService::class.java)


    private var audioFile: File?=null
    private var sendFrame = false

    private var attempts: Int = 0


    private lateinit var imagen1: ImageView
    private lateinit var imagen2: ImageView

    var listadoImagenes = TreeMap<Int, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)


        context = this
        enableEdgeToEdge()
        binding = ActivityJuego2Binding.inflate(layoutInflater)
        setContentView(fragmento.root)
        fireworks = findViewById(R.id.fireworks)
        repetirAudio = findViewById(R.id.repetirAudio)
        mensajeResultado = findViewById(R.id.mensajeResultado)
        repetirAudio.setOnClickListener {
        if (Memoria.audioMemoriaJuego2 != 0) {
            media = MediaPlayer.create(this, Memoria.audioMemoriaJuego2)
            media.setVolume(1.5f, 1.5f)
            media.start()
        }
    }
        

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        animacion = findViewById(R.id.cara)
        animacion.playAnimation()
        animacion.loop(true)

        reproducirAdivinanza = findViewById(R.id.adivinanza)

        audioFile = File(getExternalFilesDir(null), "audio.mp4")

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        supportActionBar?.hide()

        /////Iniciamos la camara
        cameraExecutor = Executors.newSingleThreadExecutor()

        fragmento.camara.post{


        }
        if (allPermissionsGranted()) {
            //setUpCamera()
        } else {
            this.let {
                ActivityCompat.requestPermissions(
                    it,
                    Constants.REQUIRED_PERMISSIONS,
                    Constants.REQUEST_CODE_PERMISSIONS
                )
            }
        }

        cargarAdivinanzas()
        cargarImagenes()

        var random = Random.nextInt(1,10)
        Memoria.seleccion = random
//        random = 5
//        Memoria.seleccion = random
        if(3 == 3){
            var audio = audios[Memoria.numeroCategoria]
            Memoria.audioMemoriaJuego2 = audio.keys.random()
            val resourceId = audio[Memoria.audioMemoriaJuego2]
            val resourceName = resourceId?.let { this.resources.getResourceEntryName(it) }
            Memoria.memoriaAdivinanza = resourceName
            android.os.Handler(Looper.getMainLooper()).postDelayed(
                {   
                    audioActual = Memoria.audioMemoriaJuego2
                    media = MediaPlayer.create(this, Memoria.audioMemoriaJuego2)
                    media.setVolume(1.5f, 1.5f)
                    media.start()

                    Memoria.seleccion = 1
                    media.setOnCompletionListener {
                        audioActual = R.raw.muestramelarespuesta
                        media = MediaPlayer.create(this, audioActual)
                        media.setVolume(1.5f, 1.5f)
                        media.start()
                        fragmento.camara.post{
                            setUpCamera()
                        }
                    }


                },
                500
            )
        }else{
            var audio = audios[Memoria.numeroCategoria]
            Memoria.audioMemoriaJuego2 = audio.keys.random()

            val resourceId = audio[Memoria.audioMemoriaJuego2]
            val resourceName = resourceId?.let { this.resources.getResourceEntryName(it) }
            Memoria.memoriaAdivinanza = resourceName
            android.os.Handler(Looper.getMainLooper()).postDelayed(
                {   
                    audioActual = Memoria.audioMemoriaJuego2
                    media = MediaPlayer.create(this, Memoria.audioMemoriaJuego2)
                    media.setVolume(1.5f, 1.5f)
                    media.start()
                    Memoria.seleccion = 2
                    media.setOnCompletionListener {
                        audioActual = R.raw.dimelarespuesta
                        media = MediaPlayer.create(this, audioActual)
                        media.setVolume(1.5f, 1.5f)
                        media.start()

                    }

                },
                500
            )
        }

        puntaje = findViewById(R.id.puntaje)

        hideSystemUI()
        volver = findViewById(R.id.volver)

        imagen1 = findViewById(R.id.imagen12)
        imagen2 = findViewById(R.id.imagen22)

        volver.setOnClickListener {
            finish()
        }
        reproducirAdivinanza.setOnClickListener {
            audioActual = Memoria.audioMemoriaJuego2
            media = MediaPlayer.create(this, Memoria.audioMemoriaJuego2)
            media.setVolume(1.5f, 1.5f)
            media.start()
            val duracion:Int  = Memoria.audioMemoriaJuego2.let { this.media.duration }
            var lon: Long = duracion.toLong()
            //lon *=1000
            media.start()
            Thread.sleep(lon)
            media.setOnCompletionListener {
                runOnUiThread {
                    if(cameraProvider ==null){
                        fragmento.camara.post {
                            setUpCamera()
                        }
                    }
                }
            }
        }

        BluetoothManager.sendDataToHC05("3")
    }
    private fun mostrarFuegos() {
    runOnUiThread {
        fireworks.visibility = View.VISIBLE
        fireworks.bringToFront()
        fireworks.progress = 0f
        fireworks.repeatCount = 0
        fireworks.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            fireworks.cancelAnimation()
            fireworks.visibility = View.GONE
            }, 1500)
        }
    }

    fun mostrarCorrecto() {
    runOnUiThread {
        mensajeResultado.text = "¡Muy bien!"
        mensajeResultado.setTextColor(Color.parseColor("#00C853"))
        mensajeResultado.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            mensajeResultado.visibility = View.GONE
        }, 2000)
    }
}

    fun mostrarIncorrecto() {
    runOnUiThread {
        mensajeResultado.text = "Intenta otra vez"
        mensajeResultado.setTextColor(Color.RED)
        mensajeResultado.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            mensajeResultado.visibility = View.GONE
        }, 2000)
    }
}

    private fun allPermissionsGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
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


    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        //val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmento.camara.display.rotation)
            .build()

        imageAnalyzer =
            ImageAnalysis.Builder()
                //.setTargetResolution(Size(640, 640))  // Resolución deseada para el análisis
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
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
            var listado = listOf(1f,2f,3f)
            //val cameraControl = camera?.cameraControl
            //cameraControl?.setZoomRatio(2.0f)

            preview?.setSurfaceProvider(fragmento.camara.surfaceProvider)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //}, ContextCompat.getMainExecutor(this))
    }

    fun displayBitmap(image: ImageProxy) {
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        val imageRotation = image.imageInfo.rotationDegrees
        detects(bitmapBuffer, Memoria.seleccion)

    }


    fun detects(image: Bitmap, nivel: Int) {

        juegoCamara(image)

//        if(nivel == 2){
//            //juegoMicrofono()
//        }else{
//
//        }

    }

    private fun juegoCamara(image: Bitmap) {
        if(!sendFrame) {
            runOnUiThread {
                if (cameraProvider == null) {
                    setUpCamera()
                }
            }

            val matriz = Matrix()
            matriz.postRotate(0f)

            val outputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody =
                MultipartBody.Part.createFormData("image", "image.jpeg", requestBody)

            var call: Call<ResponseBody>? = null
            when (Memoria.numeroCategoria) {
                0 -> call = apiService.aacuatico(multipartBody)
                1 -> call = apiService.aaereo(multipartBody)
                2 -> call = apiService.aterrestre(multipartBody)
                3 -> call = apiService.taacuatico(multipartBody)
                4 -> call = apiService.taereo(multipartBody)
                5 -> call = apiService.tterrestre(multipartBody)
                6 -> call = apiService.colores(multipartBody)
                7 -> call = apiService.comidachatarra(multipartBody)
                8 -> call = apiService.emocion(multipartBody)
                9 -> call = apiService.fruta(multipartBody)
                10 -> call = apiService.higiene(multipartBody)
                11 -> call = apiService.instrumentomusical(multipartBody)
                12 -> call = apiService.juguete(multipartBody)
                13 -> call = apiService.numero(multipartBody)
                14 -> call = apiService.persona(multipartBody)
                15 -> call = apiService.prendadevestir(multipartBody)
                16 -> call = apiService.verdura(multipartBody)
            }

            try {
                val response = call!!.execute() // Llamada sincrónica
                if (response.isSuccessful) {
                    val body = response.body()?.string() // La respuesta ya está en el cuerpo
                    val gson = Gson()
                    val textoLimpio =
                        body?.replace("[^a-zA-ZáéíóúÁÉÍÓÚ ]".toRegex(), "")?.replace(" ", "")
                            ?.lowercase()


                    // Validar la respuesta del API
                    if (textoLimpio!!.contains("naida")) {
                        return
                    }
                    if (textoLimpio == lastReceivedValue) {
                        if (textoLimpio == Memoria.memoriaAdivinanza.replace(
                                "[^a-zA-ZáéíóúÁÉÍÓÚ ]".toRegex(),
                                ""
                            )?.lowercase()
                        ) {
                            correctCount++
                            incorrectCount = 0
                        } else {
                            incorrectCount++
                            correctCount = 0
                        }
                    } else {
                        resetCounters()
                        if (textoLimpio == Memoria.memoriaAdivinanza.replace(
                                "[^a-zA-ZáéíóúÁÉÍÓÚ ]".toRegex(),
                                ""
                            )?.lowercase()
                        ) {
                            correctCount++
                            //incorrectCount = 0
                        } else {
                            incorrectCount++
                            //correctCount = 0
                        }
                    }

                    lastReceivedValue = textoLimpio

                    if (correctCount >= threshold) {
                        runOnUiThread {
                            if (cameraProvider != null) {
                                cameraProvider?.unbindAll()
                                cameraProvider = null
                            }
                        }
                        println("Correcto: ${Memoria.memoriaAdivinanza}")
                        attempts = 0

                        BluetoothManager.sendDataToHC05("7")
                        BluetoothManager.sendDataToHC05("3")
                        Memoria.puntosJuego1 += 1
                        mostrarFuegos()
                        mostrarCorrecto()

                        media = MediaPlayer.create(this, R.raw.correctolohicistebienjuegocuatro)
                        media.setVolume(1.5f, 1.5f)
                        android.os.Handler(Looper.getMainLooper()).postDelayed(
                            { //animacion.playAnimation()
                                val audio = audios[Memoria.numeroCategoria]
                                audio.keys.remove(Memoria.audioMemoriaJuego2)
                            },
                            0
                        )

                        media.start()
                        Thread.sleep(2000)

                        val random = Random.nextInt(1, 10)

                        var audio = audios[Memoria.numeroCategoria]

                        if (audio.isEmpty()) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    media = MediaPlayer.create(this, R.raw.juego1completo)
                                    media.start()
                                    sendFrame = true
                                    media.setOnCompletionListener {
                                        finish()
                                    }
                                },
                                0
                            )
                        }


                        audio.keys.remove(Memoria.audioMemoriaJuego2)
                        Memoria.audioMemoriaJuego2 = audio.keys.random()

                        val resourceId = audio[Memoria.audioMemoriaJuego2]
                        val resourceName =
                            resourceId?.let { this.resources.getResourceEntryName(it) }
                        Memoria.memoriaAdivinanza = resourceName
                        runOnUiThread {
                            if (cameraProvider != null) {
                                cameraProvider?.unbindAll()
                                cameraProvider = null
                            }
                        }
                        android.os.Handler(Looper.getMainLooper()).postDelayed(
                            {
                                if ((3 == 2) == true) {
                                    audioActual = Memoria.audioMemoriaJuego2
                                    media = MediaPlayer.create(this, Memoria.audioMemoriaJuego2)
                                    media.setVolume(1.5f, 1.5f)

                                    val duracion: Int =
                                        Memoria.audioMemoriaJuego2.let { this.media.duration }
                                    var lon: Long = duracion.toLong()
                                    //lon *=1000
                                    media.start()
                                    Thread.sleep(lon)
                                    media.setOnCompletionListener {
                                        media = MediaPlayer.create(this, R.raw.dimelarespuesta)
                                        media.setVolume(1.5f, 1.5f)
                                        media.start()
                                        media.setOnCompletionListener {
                                            Memoria.seleccion = 2
                                            //juegoMicrofono()
                                        }
                                    }
                                } else {
                                    audioActual = Memoria.audioMemoriaJuego2
                                    media = MediaPlayer.create(this, Memoria.audioMemoriaJuego2)
                                    media.setVolume(1.5f, 1.5f)
                                    var duracion: Int =
                                        Memoria.audioMemoriaJuego2.let { this.media.duration }
                                    var lon: Long = duracion.toLong()
                                    //lon *=1000
                                    media.start()
                                    Thread.sleep(lon)
                                    media.setOnCompletionListener {
                                        media = MediaPlayer.create(this, R.raw.muestramelarespuesta)
                                        media.setVolume(1.5f, 1.5f)

                                        media.start()

                                        media.setOnCompletionListener {
                                            //setUpCamera()
                                            fragmento.camara.post {
                                                setUpCamera()
                                            }
                                            Memoria.seleccion = 1
                                        }

                                    }

                                }


                            },
                            6000
                        )


                        puntaje.text = Memoria.puntosJuego1.toString()

                        resetCounters()

                    } else {//if (incorrectCount >= threshold) {

                        if (incorrectCount > 6) {
                            return
                        }

                        if (incorrectCount == 5) {
                            //lastReceivedValue = Memoria.memoriaAdivinanza
                            //lastReceivedValue = Memoria.memoriaAdivinanza.lowercase()
                            attempts++

                            println("Incorrecto: Se esperaba ${Memoria.memoriaAdivinanza}, pero se recibió $textoLimpio")
                            media = MediaPlayer.create(this, R.raw.incorrecto)
                            mostrarIncorrecto()
                            //media.setVolume(1.5f, 1.5f)


                            media.start()

                            media.setOnCompletionListener {
//                            media = MediaPlayer.create(this, Memoria.audioMemoria).apply {
//                                start()
//                            }
                                if (attempts == 3) {
                                    correccion()

                                    sendFrame = true
                                    attempts = 0
//                                    return
                                }
                            }
//                            media.setOnCompletionListener {
//                                media = MediaPlayer.create(this, Memoria.audioMemoriaJuego2)
//                                media.setVolume(1.5f, 1.5f)
//                                media.start()
//                                val duracion:Int  = Memoria.audioMemoriaJuego2.let { this.media.duration }
//                                var lon: Long = duracion.toLong()
//                                //lon *=1000
//                                media.start()
//                                Thread.sleep(lon)
//                                media.setOnCompletionListener {
//                                    runOnUiThread {
//                                        if(cameraProvider ==null){
//                                            fragmento.camara.post {
//                                                setUpCamera()
//                                            }
//                                        }
//                                    }
//                                }
//                            }

                            //if (BluetoothManager.isConnected()) {
                            BluetoothManager.sendDataToHC05("2")
                            //}

                            //resetCounters()
                        }
                    }


                    BluetoothManager.sendDataToHC05("6")
                    Log.d("Retrofit", "Respuesta exitosa: $body")
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("Retrofit", "Error en la respuesta: $error")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "Error en la solicitud: ${e.message}")
            }
        }
    }

    fun juegoMicrofono() {
        if(vista) {
            //media.setOnCompletionListener {
            runOnUiThread {
                if (cameraProvider != null) {
                    cameraProvider?.unbindAll()
                    cameraProvider = null
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Iniciar grabación
                    val audioFile = File(getExternalFilesDir(null), "audio.mp4")
                    nuevo.start(audioFile)

                    delay(5000)

                    nuevo.stop()

                    // Verificar si el archivo existe y tiene datos antes de enviarlo
                    if (!audioFile.exists() || audioFile.length() == 0L) {
                        Log.e("JuegoMicrofono", "El archivo de audio no se grabó correctamente.")
                        return@launch
                    }

                    val requestBody = audioFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                    val multipartBody =
                        MultipartBody.Part.createFormData("file", audioFile.name, requestBody)

                    val call = apiService.process_audio(multipartBody)
                    val response = call.execute()

                    if (response.isSuccessful) {
                        val respuesta = response.body()?.string()
                        println(respuesta)
                        val textoLimpio = respuesta?.replace("[^a-zA-ZáéíóúÁÉÍÓÚ]".toRegex(), "")?.toLowerCase()
                        if (textoLimpio!!.toLowerCase()
                                .contains(Memoria.memoriaAdivinanza.lowercase())
                        ) {
                            media = MediaPlayer.create(context, R.raw.correctolohicistebienjuegocuatro)
                            media.setVolume(1.5f, 1.5f)
                            media.start()
                            BluetoothManager.sendDataToHC05("3")
                            media.setOnCompletionListener {
                                val audio = audios[Memoria.audioMemoriaJuego2]
                                audio.keys.remove(Memoria.audioMemoria)
                            }

                            val random = Random.nextInt(1, 10)

                            media.setOnCompletionListener {
                                val audio = audios[Memoria.numeroCategoria]
                                Memoria.audioMemoriaJuego2 = audio.keys.random()

                                val resourceId = audio[Memoria.audioMemoriaJuego2]
                                val resourceName =
                                    resourceId?.let { context!!.resources.getResourceEntryName(it) }
                                Memoria.memoriaAdivinanza = resourceName

                                android.os.Handler(Looper.getMainLooper()).postDelayed({
                                    media = MediaPlayer.create(context, Memoria.audioMemoriaJuego2)
                                    media.setVolume(1.5f, 1.5f)
                                    media.start()

                                    media.setOnCompletionListener {
                                        fragmento.camara.post { setUpCamera() }
                                        media = MediaPlayer.create(
                                            context,
                                            if (random < 6) R.raw.muestramelarespuesta else R.raw.dimelarespuesta
                                        )
                                        media.start()
                                        Memoria.seleccion = if (random < 6) 1 else 2
                                    }
                                }, 500)
                            }
                        } else {
                            mostrarIncorrecto()
                            media = MediaPlayer.create(context, R.raw.incorrecto)
                            
                            media.setVolume(1.5f, 1.5f)
                            media.start()
                            BluetoothManager.sendDataToHC05("2")
                            media.setOnCompletionListener {
                                media = MediaPlayer.create(context, Memoria.audioMemoriaJuego2)
                                media.setVolume(1.5f, 1.5f)
                                media.start()
                                media.setOnCompletionListener {
                                    juegoMicrofono()
                                }
                            }
                        }
                    } else {
                        Log.e("JuegoMicrofono", "Error en la respuesta del servidor.")
                        juegoMicrofono()
                    }

                } catch (e: Exception) {
                    Log.e("JuegoMicrofono", "Error en la grabación o envío: ${e.message}")
                }
            }
            //}
        }
    }


    override fun onPause() {
        super.onPause()
        nuevo.stop()
        vista = false
    }

    override fun onStop() {
        super.onStop()
        media.stop()
    }


        private fun correccion() {
            media = MediaPlayer.create(this, R.raw.estaeslarespuesta)
            media.start()

            val audio = audios[Memoria.numeroCategoria]
            var imagen = audio[Memoria.audioMemoriaJuego2]

            if (imagen == null) {
                Toast.makeText(this, "Audio no encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            val nombreRecurso: String = try {
                context.resources.getResourceEntryName(imagen).lowercase()
            } catch (e: Resources.NotFoundException) {
                Toast.makeText(this, "Nombre de recurso no encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            val clave = listadoImagenes.entries.find { it.value.lowercase() == nombreRecurso?.lowercase() }?.key
            if (clave != null) {


                imagen1.setImageResource(clave)
                imagen2.setImageResource(clave)

                imagen1.isVisible = true
                imagen2.isVisible = true




                //Thread.sleep(8000)
                // Ocultar imagen después de 5 segundos
                Handler(Looper.getMainLooper()).postDelayed({
                    imagen1.isVisible = false
                    imagen2.isVisible = false



                    sendFrame = false
                    // Reproducir audio después de ocultar imagen
                    media = MediaPlayer.create(this, R.raw.okey)
                    media.start()
                    media.setOnCompletionListener {
                        audioActual = Memoria.audioMemoriaJuego2
                        media = MediaPlayer.create(this, Memoria.audioMemoriaJuego2).apply {
                            if(Memoria.seleccion < 5){
                                start()
                            }
                        }
                    }
                }, 8000)
                //Thread.sleep(8000)
            } else {
                Toast.makeText(this, "Imagen no encontrada", Toast.LENGTH_SHORT).show()
            }
        }



    private fun resetCounters() {
        correctCount = 0
        incorrectCount = 0
        lastReceivedValue = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Los permisos no se concedieron.", Toast.LENGTH_SHORT).show()

                finish()
            }
        }
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


    private fun cargarAdivinanzas(){
        //Listado de adivinanzas
        //1ra mitad
        audiosAAereo[R.raw.adivinanzaabeja] = R.raw.abeja
        audiosPrendaDeVestir[R.raw.adivinanzaabrigo] = R.raw.abrigo
        audiosPersona[R.raw.adivinanzaabuela] = R.raw.abuela
        audiosPersona[R.raw.adivinanzaabuelo] = R.raw.abuelo
        audiosHigiene[R.raw.adivinanzaagua] = R.raw.agua
        audiosAAereo[R.raw.adivinanzaaguila] = R.raw.aguila
        audiosColor[R.raw.adivinanzaamarillo] = R.raw.amarillo
        audiosAAcuatico[R.raw.adivinanzaanguila] = R.raw.anguila
        audiosATerrestre[R.raw.adivinanzaardilla] = R.raw.ardilla
        audiosATerrestre[R.raw.adivinanzaarmadillo] = R.raw.armadillo
        audiosColor[R.raw.adivinanzaazul] = R.raw.azul
        audiosAAcuatico[R.raw.adivinanzaballena] = R.raw.ballena
        audiosPersona[R.raw.adivinanzabombero] = R.raw.bombero
        audiosColor[R.raw.adivinanzablanco] = R.raw.blanco
        audiosVerdura[R.raw.adivinanzabrocoli] = R.raw.brocoli
        audiosAAereo[R.raw.adivinanzabuho] = R.raw.buho
        audiosPrendaDeVestir[R.raw.adivinanzabuso] = R.raw.buso
        audiosATerrestre[R.raw.adivinanzacaballo] = R.raw.caballo
        audiosAAcuatico[R.raw.adivinanzacaballitodemar] = R.raw.caballitodemar
        audiosATerrestre[R.raw.adivinanzacabra] = R.raw.cabra
        audiosPrendaDeVestir[R.raw.adivinanzacalzoncillo] = R.raw.calzoncillo
        audiosNumero[R.raw.adivinanzauno] = R.raw.uno
        audiosNumero[R.raw.adivinanzados] = R.raw.dos
        // audiosHigiene[R.raw.adivinanzabaniar] = R.raw.baniar
        audiosHigiene[R.raw.adivinanzaenjuagarlaboca] = R.raw.enjuagarlaboca
        audiosNumero[R.raw.adivinanzatres] = R.raw.tres
        audiosNumero[R.raw.adivinanzacuatro] = R.raw.cuatro
        audiosNumero[R.raw.adivinanzacinco] = R.raw.cinco
        audiosNumero[R.raw.adivinanzaseis] = R.raw.seis
        audiosNumero[R.raw.adivinanzasiete] = R.raw.siete
        audiosNumero[R.raw.adivinanzaocho] = R.raw.ocho
        audiosNumero[R.raw.adivinanzanueve] = R.raw.nueve
        audiosNumero[R.raw.adivinanzadiez] = R.raw.diez
        audiosAAcuatico[R.raw.adivinanzacamaron] = R.raw.camaron
        audiosATerrestre[R.raw.adivinanzacamello] = R.raw.camello
        audiosPrendaDeVestir[R.raw.adivinanzacamisa] = R.raw.camisa
        audiosPrendaDeVestir[R.raw.adivinanzacamiseta] = R.raw.camiseta
        audiosAAcuatico[R.raw.adivinanzacangrejo] = R.raw.cangrejo
        audiosATerrestre[R.raw.adivinanzacanguro] = R.raw.canguro
        audiosJuguete[R.raw.adivinanzacanicas] = R.raw.canicas
        audiosPrendaDeVestir[R.raw.adivinanzacapucha] = R.raw.capucha
        audiosATerrestre[R.raw.adivinanzacaracol] = R.raw.caracol
        audiosPrendaDeVestir[R.raw.adivinanzacasaca] = R.raw.casaca
        audiosATerrestre[R.raw.adivinanzacebra] = R.raw.cebra
        audiosColor[R.raw.adivinanzaceleste] = R.raw.celeste
        audiosATerrestre[R.raw.adivinanzacerdo] = R.raw.cerdo
        audiosPrendaDeVestir[R.raw.adivinanzachaleco] = R.raw.chaleco
        audiosAAereo[R.raw.adivinanzaciervo] = R.raw.ciervo
        audiosAAereo[R.raw.adivinanzaciguena] = R.raw.ciguenia
        audiosAAcuatico[R.raw.adivinanzacocodrilo] = R.raw.cocodrilo
        audiosAAereo[R.raw.adivinanzacolibri] = R.raw.colibri
        audiosAAereo[R.raw.adivinanzacondor] = R.raw.condor
        audiosJuguete[R.raw.adivinanzacubo] = R.raw.cubo
        audiosATerrestre[R.raw.adivinanzacucaracha] = R.raw.cucaracha
        audiosAAereo[R.raw.adivinanzacuervo] = R.raw.cuervo
        audiosAAcuatico[R.raw.adivinanzadelfin] = R.raw.delfin
        audiosPersona[R.raw.adivinanzadoctor] = R.raw.doctor
        audiosATerrestre[R.raw.adivinanzaerizo] = R.raw.erizo
        //audiosATerrestre[R.raw.adivinanzaescarabajo] = R.raw.escarabajo
        audiosATerrestre[R.raw.adivinanzaescorpion] = R.raw.escorpion
        audiosAAcuatico[R.raw.adivinanzaestrellademar] = R.raw.estrellademar
        audiosAAcuatico[R.raw.adivinanzafoca] = R.raw.foca
        audiosATerrestre[R.raw.adivinanzagato] = R.raw.gato
        audiosPersona[R.raw.adivinanzahermana] = R.raw.hermana
        //audiosPersona[R.raw.adivinanzahermano] = R.raw.hermano
        audiosAAcuatico[R.raw.adivinanzahipopotamo] = R.raw.hipopotamo
        audiosATerrestre[R.raw.adivinanzahormiga] = R.raw.hormiga
        audiosATerrestre[R.raw.adivinanzajirafa] = R.raw.jirafa
        audiosHigiene[R.raw.adivinanzalavarselacara] = R.raw.lavarselacara
        audiosATerrestre[R.raw.adivinanzaleon] = R.raw.leon
        audiosATerrestre[R.raw.adivinanzalombriz] = R.raw.lombriz
        audiosPersona[R.raw.adivinanzamama] = R.raw.mama
        audiosAAcuatico[R.raw.adivinanzamanta] = R.raw.manta
        audiosPrendaDeVestir[R.raw.adivinanzamedias] = R.raw.medias
        audiosAAcuatico[R.raw.adivinanzamedusa] = R.raw.medusa
        audiosPersona[R.raw.adivinanzaninia] = R.raw.ninia
        audiosPersona[R.raw.adivinanzaninio] = R.raw.ninio
        audiosAAcuatico[R.raw.adivinanzaorca] = R.raw.orca
        audiosAAcuatico[R.raw.adivinanzaostra] = R.raw.ostra
        audiosATerrestre[R.raw.adivinanzaoveja] = R.raw.oveja
        audiosPrendaDeVestir[R.raw.adivinanzapantalon] = R.raw.pantalon
        audiosPersona[R.raw.adivinanzapapa] = R.raw.papa
        audiosPrendaDeVestir[R.raw.adivinanzapato] = R.raw.pato
        audiosJuguete[R.raw.adivinanzapeluche] = R.raw.peluche
        audiosAAcuatico[R.raw.adivinanzapez] = R.raw.pez
        audiosAAcuatico[R.raw.adivinanzapinguino] = R.raw.pinguino
        audiosAAcuatico[R.raw.adivinanzapirana] = R.raw.pirana
        audiosPersona[R.raw.adivinanzaprofesora] = R.raw.profesora
        //audiosPersona[R.raw.adivinanzapsicologo] = R.raw.psicologo
        audiosAAcuatico[R.raw.adivinanzapulpo] = R.raw.pulpo
        audiosAAcuatico[R.raw.adivinanzarana] = R.raw.rana
        audiosATerrestre[R.raw.adivinanzaraton] = R.raw.raton
        audiosJuguete[R.raw.adivinanzaresorte] = R.raw.resorte
        audiosATerrestre[R.raw.adivinanzarinoceronte] = R.raw.rinoceronte
        audiosJuguete[R.raw.adivinanzarobot] = R.raw.robot
        audiosPrendaDeVestir[R.raw.adivinanzasaco] = R.raw.saco
        audiosATerrestre[R.raw.adivinanzasaltamontes] = R.raw.saltamontes
        audiosPrendaDeVestir[R.raw.adivinanzatacones] = R.raw.tacones
        audiosATerrestre[R.raw.adivinanzatarantula] = R.raw.tarantula
        //audiosPersona[R.raw.adivinanzatia] = R.raw.tia
        audiosAAcuatico[R.raw.adivinanzatiburon] = R.raw.tiburon
        audiosATerrestre[R.raw.adivinanzatigre] = R.raw.tigre
        //audiosPersona[R.raw.adivinanzatio] = R.raw.tio
        audiosJuguete[R.raw.adivinanzatitere] = R.raw.titere
        audiosAAcuatico[R.raw.adivinanzatortuga] = R.raw.tortuga
        audiosJuguete[R.raw.adivinanzatrompo] = R.raw.trompo
        audiosATerrestre[R.raw.adivinanzavaca] = R.raw.vaca
        audiosPrendaDeVestir[R.raw.adivinanzavestido] = R.raw.vestido
        audiosJuguete[R.raw.adivinanzayoyo] = R.raw.yoyo
        audiosPrendaDeVestir[R.raw.adivinanzazapatos] = R.raw.zapatos
        audiosATerrestre[R.raw.adivinanzazorro] = R.raw.zorro
        audiosAAereo[R.raw.adivinanzaflamenco] = R.raw.flamenco
        audiosAAereo[R.raw.adivinanzagaviota] = R.raw.gaviota
        audiosATerrestre[R.raw.adivinanzagorila] = R.raw.gorila
        //audiosColor[R.raw.adivinanzagris] = R.raw.gris
        audiosAAereo[R.raw.adivinanzalechuza] = R.raw.lechuza
        //audiosColor[R.raw.adivinanzalila] = R.raw.lila
        audiosAAereo[R.raw.adivinanzamariposa] = R.raw.mariposa
        audiosColor[R.raw.adivinanzamarron] = R.raw.marron
        audiosColor[R.raw.adivinanzamorado] = R.raw.morado
        audiosAAereo[R.raw.adivinanzamosca] = R.raw.mosca
        audiosColor[R.raw.adivinanzanegro] = R.raw.negro
        audiosAAereo[R.raw.adivinanzapajaro] = R.raw.pajaro
        audiosAAereo[R.raw.adivinanzapaloma] = R.raw.paloma
        audiosAAereo[R.raw.adivinanzapavo] = R.raw.pavo
        audiosAAereo[R.raw.adivinanzapelicano] = R.raw.pelicano
        audiosATerrestre[R.raw.adivinanzaperro] = R.raw.perro
        audiosColor[R.raw.adivinanzarojo] = R.raw.rojo
        audiosPrendaDeVestir[R.raw.adivinanzaropainterior] = R.raw.ropainterior
        audiosColor[R.raw.adivinanzarosado] = R.raw.rosado
        audiosPrendaDeVestir[R.raw.adivinanzasaco] = R.raw.saco
        audiosHigiene[R.raw.adivinanzasonarlanariz] = R.raw.sonarlanariz
        audiosAAereo[R.raw.adivinanzatucan] = R.raw.tucan
        audiosColor[R.raw.adivinanzaverdeclaro] = R.raw.verdeclaro
        audiosColor[R.raw.adivinanzaverdeoscuro] = R.raw.verdeoscuro
        audiosJuguete[R.raw.adivinanzazancos] = R.raw.zancos
        audiosPrendaDeVestir[R.raw.adivinanzasort] = R.raw.sort

        //2da mitad...
        //audiosFruta[R.raw.adivinanzamelon] = R.raw.melon
        audiosHigiene[R.raw.adivinanzalavarlasmanos] = R.raw.lavarlasmanos
        audiosHigiene[R.raw.adivinanzajabon] = R.raw.jabon
        audiosHigiene[R.raw.adivinanzapeinar] = R.raw.peinar
        audiosInstrumentoMusical[R.raw.adivinanzapiano] = R.raw.piano
        audiosInstrumentoMusical[R.raw.adivinanzaarpa] = R.raw.arpa
        audiosFruta[R.raw.adivinanzalimon] = R.raw.limon
        audiosFruta[R.raw.adivinanzadurazno] = R.raw.durazno
        audiosInstrumentoMusical[R.raw.adivinanzasaxofon] = R.raw.saxofon
        audiosTAcuatico[R.raw.adivinanzabarcopirata] = R.raw.barcopirata
        audiosComidaChatarra[R.raw.adivinanzagomitas] = R.raw.gomitas
        audiosTTerrestre[R.raw.adivinanzacochedecarreras] = R.raw.cochedecarreras
        audiosTAcuatico[R.raw.adivinanzaremos] = R.raw.remos
        audiosTAereo[R.raw.adivinanzacohete] = R.raw.cohete
        audiosFruta[R.raw.adivinanzamandarina] = R.raw.mandarina
        audiosInstrumentoMusical[R.raw.adivinanzaflauta] = R.raw.flauta
        audiosTAcuatico[R.raw.adivinanzamotoacuatica] = R.raw.motoacuatica
        audiosHigiene[R.raw.adivinanzacepillo] = R.raw.cepillo
        //audiosHigiene[R.raw.adivinanzacepillodecabello] = R.raw.cepillodecabello
        audiosHigiene[R.raw.adivinanzasecarelcuerpo] = R.raw.secarelcuerpo
        audiosFruta[R.raw.adivinanzaframbuesa] = R.raw.frambuesa
        audiosComidaChatarra[R.raw.adivinanzacanguil] = R.raw.canguil
        audiosEmocion[R.raw.adivinanzafeliz] = R.raw.feliz
        audiosFruta[R.raw.adivinanzaarandanos] = R.raw.arandanos
        audiosEmocion[R.raw.adivinanzaasustado] = R.raw.asustado
        audiosComidaChatarra[R.raw.adivinanzagalletadechocolate] = R.raw.galletadechocolate
        audiosEmocion[R.raw.adivinanzatimido] = R.raw.timido
        //audiosFruta[R.raw.adivinanzaciruela] = R.raw.ciruela
        audiosTTerrestre[R.raw.adivinanzatranvia] = R.raw.tranvia
        audiosFruta[R.raw.adivinanzanaranja] = R.raw.naranja
        audiosEmocion[R.raw.adivinanzaaburrido] = R.raw.aburrido
        audiosTAereo[R.raw.adivinanzaglobo] = R.raw.globo
        audiosTTerrestre[R.raw.adivinanzametro] = R.raw.metro
        audiosTTerrestre[R.raw.adivinanzatriciclo] = R.raw.triciclo
        audiosFruta[R.raw.adivinanzapapaya] = R.raw.papaya
        audiosFruta[R.raw.adivinanzamora] = R.raw.mora
        audiosFruta[R.raw.adivinanzafresa] = R.raw.fresa
        audiosVerdura[R.raw.adivinanzapimiento] = R.raw.pimiento
        audiosFruta[R.raw.adivinanzagranadilla] = R.raw.granadilla
        audiosFruta[R.raw.adivinanzamaracuya] = R.raw.maracuya
        audiosFruta[R.raw.adivinanzakiwi] = R.raw.kiwi
        audiosHigiene[R.raw.adivinanzapasta] = R.raw.pasta
        audiosInstrumentoMusical[R.raw.adivinanzaacordeon] = R.raw.acordeon
        audiosVerdura[R.raw.adivinanzaacelga] = R.raw.acelga
        audiosTTerrestre[R.raw.adivinanzacamion] = R.raw.camion
        audiosVerdura[R.raw.adivinanzaapio] = R.raw.apio
        audiosTAereo[R.raw.adivinanzaavion] = R.raw.avion
        audiosInstrumentoMusical[R.raw.adivinanzaplatillos] = R.raw.platillos
        audiosInstrumentoMusical[R.raw.adivinanzapandereta] = R.raw.pandereta
        //audiosComidaChatarra[R.raw.adivinanzachocolate] = R.raw.chocolate
        //audiosHigiene[R.raw.adivinanzacortaruniasdelasmanos] = R.raw.cortaruniasdelasmanos
        audiosVerdura[R.raw.adivinanzaarveja] = R.raw.arveja
        audiosFruta[R.raw.adivinanzachirimoya] = R.raw.chirimoya
        audiosInstrumentoMusical[R.raw.adivinanzamarimba] = R.raw.marimba
        audiosTTerrestre[R.raw.adivinanzacuadron] = R.raw.cuadron
        audiosFruta[R.raw.adivinanzamanzana] = R.raw.manzana
        audiosTTerrestre[R.raw.adivinanzacarruaje] = R.raw.carruaje
        audiosEmocion[R.raw.adivinanzatriste] = R.raw.triste
        audiosVerdura[R.raw.adivinanzalechuga] = R.raw.lechuga
        audiosInstrumentoMusical[R.raw.adivinanzaviolin] = R.raw.violin
        audiosInstrumentoMusical[R.raw.adivinanzatambor] = R.raw.tambor
        audiosTTerrestre[R.raw.adivinanzacamiondebomberos] = R.raw.camiondebomberos
        audiosTAcuatico[R.raw.adivinanzacanoa] = R.raw.canoa
        audiosFruta[R.raw.adivinanzacereza] = R.raw.cereza
        audiosVerdura[R.raw.adivinanzaremolacha] = R.raw.remolacha
        audiosInstrumentoMusical[R.raw.adivinanzabateria] = R.raw.bateria
        audiosTAereo[R.raw.adivinanzaparacaidas] = R.raw.paracaidas
        audiosTTerrestre[R.raw.adivinanzafurgoneta] = R.raw.furgoneta
        audiosInstrumentoMusical[R.raw.adivinanzabongos] = R.raw.bongos
        //audiosInstrumentoMusical[R.raw.adivinanzabajo] = R.raw.bajo
        audiosVerdura[R.raw.adivinanzacebolla] = R.raw.cebolla
        audiosEmocion[R.raw.adivinanzaenfadado] = R.raw.enfadado
        audiosInstrumentoMusical[R.raw.adivinanzatrompeta] = R.raw.trompeta
        audiosComidaChatarra[R.raw.adivinanzahamburguesa] = R.raw.hamburguesa
        audiosTTerrestre[R.raw.adivinanzacarro] = R.raw.carro
        audiosTTerrestre[R.raw.adivinanzatanque] = R.raw.tanque
        audiosFruta[R.raw.adivinanzacoco] = R.raw.coco
        audiosAAereo[R.raw.adivinanzacoliflor] = R.raw.coliflor
        audiosVerdura[R.raw.adivinanzatomate] = R.raw.tomate
        audiosInstrumentoMusical[R.raw.adivinanzaclarinete] = R.raw.clarinete
        audiosTAereo[R.raw.adivinanzahelicoptero] = R.raw.helicoptero
        audiosComidaChatarra[R.raw.adivinanzahelado] = R.raw.helado
        audiosHigiene[R.raw.adivinanzacortarlasunasdelospies] = R.raw.cortarlasunasdelospies
        audiosTAcuatico[R.raw.adivinanzalancha] = R.raw.lancha
        audiosTTerrestre[R.raw.adivinanzagrua] = R.raw.grua
        audiosVerdura[R.raw.adivinanzacebollin] = R.raw.cebollin
        audiosVerdura[R.raw.adivinanzapepino] = R.raw.pepino
        audiosInstrumentoMusical[R.raw.adivinanzaguitarraelectrica] = R.raw.guitarraelectrica
        audiosInstrumentoMusical[R.raw.adivinanzatriangulo] = R.raw.triangulo
        audiosFruta[R.raw.adivinanzamango] = R.raw.mango
        audiosTTerrestre[R.raw.adivinanzamoto] = R.raw.moto
        audiosInstrumentoMusical[R.raw.adivinanzaarmonica] = R.raw.armonica
        audiosVerdura[R.raw.adivinanzaberenjena] = R.raw.berenjena
        //audiosVerdura[R.raw.adivinanzaaji] = R.raw.aji
        //audiosVerdura[R.raw.adivinanzaesparragos] = R.raw.esparragos
        audiosTTerrestre[R.raw.adivinanzaambulancia] = R.raw.ambulancia
        audiosTTerrestre[R.raw.adivinanzabicicleta] = R.raw.bicicleta
        audiosTTerrestre[R.raw.adivinanzacamiondelabasura] = R.raw.camiondelabasura
        audiosInstrumentoMusical[R.raw.adivinanzaguitarra] = R.raw.guitarra
        audiosTTerrestre[R.raw.adivinanzabusturistico] = R.raw.busturistico
        audiosTTerrestre[R.raw.adivinanzaautobus] = R.raw.autobus
        audiosInstrumentoMusical[R.raw.adivinanzamaracas] = R.raw.maracas
        audiosEmocion[R.raw.adivinanzaasco] = R.raw.asco
        audiosHigiene[R.raw.adivinanzaperfumarse] = R.raw.perfumarse
        audiosFruta[R.raw.adivinanzaguineo] = R.raw.guineo
        audiosFruta[R.raw.adivinanzauvas] = R.raw.uvas
        audiosTTerrestre[R.raw.adivinanzatren] = R.raw.tren
        //audiosFruta[R.raw.adivinanzapera] = R.raw.pera
        audiosComidaChatarra[R.raw.adivinanzapapafrita] = R.raw.papasfritas
        audiosTAereo[R.raw.adivinanzaavioneta] = R.raw.avioneta
        audiosTTerrestre[R.raw.adivinanzacarrodepolicia] = R.raw.carrodepolicia
        audiosComidaChatarra[R.raw.adivinanzapizza] = R.raw.pizza
        audiosTAereo[R.raw.adivinanzateleferico] = R.raw.teleferico
        audiosFruta[R.raw.adivinanzaaguacate] = R.raw.aguacate
        audiosTTerrestre[R.raw.adivinanzamonopatin] = R.raw.monopatin
        audiosVerdura[R.raw.adivinanzaespinaca] = R.raw.espinaca
        audiosTAcuatico[R.raw.adivinanzasubmarino] = R.raw.submarino
        audiosTAcuatico[R.raw.adivinanzabarco] = R.raw.barco
        audiosInstrumentoMusical[R.raw.adivinanzaxilofono] = R.raw.xilofono
        //audiosComidaChatarra[R.raw.adivinanzagrajeas] = R.raw.grajeas
        audiosTTerrestre[R.raw.adivinanzataxi] = R.raw.taxi
        audiosTAcuatico[R.raw.adivinanzayate] = R.raw.yate
        audiosInstrumentoMusical[R.raw.adivinanzatrombon] = R.raw.trombon
        audiosHigiene[R.raw.adivinanzabaniar] = R.raw.baniar
        audiosTAcuatico[R.raw.adivinanzabarcopesquero] = R.raw.barcopesquero
        audiosInstrumentoMusical[R.raw.adivinanzapandereta] = R.raw.pandereta



        ///Fin de adivinanzas
    }


    private fun cargarImagenes(){
        when(Memoria.numeroCategoria){
            0 ->{
                listadoImagenes[R.raw.finalfoca] = "Foca"
                listadoImagenes[R.raw.finalhipopotamo] = "Hipopótamo"
                listadoImagenes[R.raw.finalmanta] = "Manta"
                listadoImagenes[R.raw.finalcangrejo] = "Cangrejo"
                listadoImagenes[R.raw.finaldelfin] = "Delfin"
                listadoImagenes[R.raw.finalballena] = "Ballena"
                listadoImagenes[R.raw.finalcamaron] = "Camaron"
                listadoImagenes[R.raw.finalanguila] = "Anguila"
                listadoImagenes[R.raw.finalcaballitodemar] = "Caballito de mar"
                listadoImagenes[R.raw.finalcocodrilo] = "Cocodrilo"
                listadoImagenes[R.raw.finalestrellademar] = "Estrella de mar"
                listadoImagenes[R.raw.finalpez] = "Pez"
                listadoImagenes[R.raw.finaltiburon] = "Tiburon"
                listadoImagenes[R.raw.finalorca] = "Orca"
                listadoImagenes[R.raw.finalmedusa] = "Medusa"
                listadoImagenes[R.raw.finalostra] = "Ostra"
                listadoImagenes[R.raw.finalpinguino] = "Pinguino"
                listadoImagenes[R.raw.finalpirania] = "Pirania"
                listadoImagenes[R.raw.finalpulpo] = "Pulpo"
                listadoImagenes[R.raw.finalpato] = "Pato"
                listadoImagenes[R.raw.finaltortuga] = "Tortuga"
            }
            1 ->{
                listadoImagenes[R.raw.finalpaloma] = "Paloma"
                listadoImagenes[R.raw.finalmosca] = "Mosca"
                listadoImagenes[R.raw.finalmariposa] = "Mariposa"
                listadoImagenes[R.raw.finalpajaro] = "Pajaro"
                listadoImagenes[R.raw.finalabeja] = "Abeja"
                //listadoImagenes[R.raw.finalaguila] = "Aguila"
                listadoImagenes[R.raw.finaltucan] = "Tucan"
                listadoImagenes[R.raw.finalpelicano] = "Pelicano"
                listadoImagenes[R.raw.finalbuho] = "Buho"
                listadoImagenes[R.raw.finalchocolate] = "Chocolate"
                listadoImagenes[R.raw.finalciguenia] = "Ciguenia"
                listadoImagenes[R.raw.finalcolibri] = "Colibri"
                listadoImagenes[R.raw.finalcondor] = "Condor"
                listadoImagenes[R.raw.finalcuervo] = "Cuervo"
                listadoImagenes[R.raw.finalflamenco] = "Flamenco"
                listadoImagenes[R.raw.finalgaviota] = "Gaviota"
                listadoImagenes[R.raw.finallechuza] = "Lechuza"
                listadoImagenes[R.raw.finallibelula] = "Libelula"
            }
            2 ->{
                listadoImagenes[R.raw.finalrinoceronte] = "Rinoceronte"
                listadoImagenes[R.raw.finalsaltamontes] = "Saltamontes"
                listadoImagenes[R.raw.finalperro] = "Perro"
                listadoImagenes[R.raw.finalgato] = "Gato"
                listadoImagenes[R.raw.finaltarantula] = "Tarantula"
                listadoImagenes[R.raw.finalzorro] = "Zorro"
                listadoImagenes[R.raw.finalraton] = "Raton"
                listadoImagenes[R.raw.finalvaca] = "Vaca"
                listadoImagenes[R.raw.finaljirafa] = "Jirafa"
                listadoImagenes[R.raw.finaltigre] = "Tigre"
                listadoImagenes[R.raw.finalardilla] = "Ardilla"
                listadoImagenes[R.raw.finalarmadillo] = "Armadillo"
                listadoImagenes[R.raw.finalpavo] = "Pavo"
                listadoImagenes[R.raw.finaloveja] = "Oveja"
                listadoImagenes[R.raw.finallombriz] = "Lombriz"
                listadoImagenes[R.raw.finalleon] = "Leon"
                listadoImagenes[R.raw.finalhormiga] = "Hormiga"
                listadoImagenes[R.raw.finalgorila] = "Gorila"
                listadoImagenes[R.raw.finalescorpion] = "Escorpion"
                listadoImagenes[R.raw.finalcucaracha] = "Cucaracha"
                listadoImagenes[R.raw.finalerizo] = "Erizo"
                listadoImagenes[R.raw.finalcaracol] = "Caracol"
                listadoImagenes[R.raw.finalciervo] = "Ciervo"
                listadoImagenes[R.raw.finalcerdo] = "Cerdo"
                listadoImagenes[R.raw.finalcebra] = "Cebra"
                listadoImagenes[R.raw.finalcanguro] = "Canguro"
                listadoImagenes[R.raw.finalcamello] = "Camello"
                listadoImagenes[R.raw.finalcaballo] = "Caballo"
                listadoImagenes[R.raw.finalcabra] = "Cabra"
                listadoImagenes[R.raw.finalrana] = "Rana"

            }
            3 ->{
                listadoImagenes[R.raw.finalyate] = "Yate"
                listadoImagenes[R.raw.finalremos] = "Remos"
                listadoImagenes[R.raw.finalsubmarino] = "Submarino"
                listadoImagenes[R.raw.finalmotoacuatica] = "Moto acuatica"
                listadoImagenes[R.raw.finallancha] = "Lancha"
                listadoImagenes[R.raw.finalcanoa] = "Canoa"
                listadoImagenes[R.raw.finalbarco] = "Barco"
                listadoImagenes[R.raw.finalpesquero] = "Pesquero"
                listadoImagenes[R.raw.finalbarcopirata] = "Barco pirata"
            }

            4 ->{
                listadoImagenes[R.raw.finalteleferico] = "Teleferico"
                listadoImagenes[R.raw.finalparacaidas] = "Paracaidas"
                listadoImagenes[R.raw.finalhelicoptero] = "Helicoptero"
                listadoImagenes[R.raw.finalcohete] = "Cohete"
                listadoImagenes[R.raw.finalavion] = "Avion"
                listadoImagenes[R.raw.finalglobo] = "Globo"
                listadoImagenes[R.raw.finalavioneta] = "Avioneta"
            }
            5 ->{
                listadoImagenes[R.raw.finalvolqueta] = "Volqueta"
                listadoImagenes[R.raw.finaltriciclo] = "Triciclo"
                listadoImagenes[R.raw.finalmonopatin] = "Monopatin"
                listadoImagenes[R.raw.finalmotocicleta] = "Motocicleta"
                listadoImagenes[R.raw.finalmetro] = "Metro"
                listadoImagenes[R.raw.finalgrua] = "Grua"
                listadoImagenes[R.raw.finalfurgoneta] = "Furgoneta"
                listadoImagenes[R.raw.finalcuadron] = "Cuadron"
                listadoImagenes[R.raw.finalcochedecarrereas] = "Coche de carrereas"
                listadoImagenes[R.raw.finalcarro] = "Carro"
                listadoImagenes[R.raw.finalcamion] = "Camión"
                listadoImagenes[R.raw.finalautobus] = "Autobus"
                listadoImagenes[R.raw.finalbus] = "Bus"
                listadoImagenes[R.raw.finalbicicleta] = "Bicicleta"
                listadoImagenes[R.raw.finalambulancia] = "Ambulancia"
                listadoImagenes[R.raw.finaltranvia] = "Tranvia"
                listadoImagenes[R.raw.finaltren] = "Tren"
                listadoImagenes[R.raw.finaltractor] = "Tractor"
                listadoImagenes[R.raw.finaltaxi] = "Taxi"
                listadoImagenes[R.raw.finaltanque] = "Tanque"
                listadoImagenes[R.raw.finalcamiondebomberos] = "Camión de bomberos"
                listadoImagenes[R.raw.finalcamiondelabasura] = "Camión de la basura"
                listadoImagenes[R.raw.finalcarrodepolicia] = "Carro de policia"
                listadoImagenes[R.raw.finalbusturistico] = "Bus turistico"
                listadoImagenes[R.raw.finalcarruaje] = "Carruaje"
            }
            6 ->{
                listadoImagenes[R.raw.finalverdeclaro] = "Verde claro"
                listadoImagenes[R.raw.finalverdeoscuro] = "Verde oscuro"
                listadoImagenes[R.raw.finalrosado] = "Rosado"
                listadoImagenes[R.raw.finalnegro] = "Negro"
                listadoImagenes[R.raw.finalmorado] = "Morado"
                listadoImagenes[R.raw.finalblanco] = "Blanco"
                listadoImagenes[R.raw.finalanaranjado] = "Anaranjado"
                listadoImagenes[R.raw.finalamarillo] = "Amarillo"
                listadoImagenes[R.raw.finalmarron] = "Marron"
                listadoImagenes[R.raw.finalceleste] = "Celeste"
                listadoImagenes[R.raw.finalazul] = "Azul"
                listadoImagenes[R.raw.finalrojo] = "Rojo"
            }
            7 ->{
                listadoImagenes[R.raw.finalpizza] = "Pizza"
                listadoImagenes[R.raw.finalpapasfritas] = "Papas fritas"
                listadoImagenes[R.raw.finalhelado] = "Helado"
                listadoImagenes[R.raw.finalhamburguesa] = "Hamburguesa"
                listadoImagenes[R.raw.finalgomitas] = "Gomitas"
                listadoImagenes[R.raw.finalcanguil] = "Canguil"
                listadoImagenes[R.raw.finalgalletasdechocolate] = "Galletas de chocolate"

            }
            8 ->{
                listadoImagenes[R.raw.finaltriste] = "Triste"
                listadoImagenes[R.raw.finaltimido] = "Timido"
                listadoImagenes[R.raw.finalfeliz] = "Feliz"
                listadoImagenes[R.raw.finalenfadado] = "Enfadado"
                listadoImagenes[R.raw.finalasustado] = "Asustado"
                listadoImagenes[R.raw.finalaburrido] = "Aburrido"
                listadoImagenes[R.raw.finalasco] = "Asco"

            }
            9 ->{
                listadoImagenes[R.raw.finaluvas] = "Uvas"
                //listadoImagenes[R.raw.finalpera] = "Pera"
                listadoImagenes[R.raw.finalpapaya] = "Papaya"
                listadoImagenes[R.raw.finalmora] = "Mora"
                listadoImagenes[R.raw.finalnaranja] = "Naranja"
                listadoImagenes[R.raw.finalmaracuya] = "Maracuya"
                listadoImagenes[R.raw.finalmanzana] = "Manzana"
                listadoImagenes[R.raw.finalmango] = "Mango"
                listadoImagenes[R.raw.finalmandarina] = "Mandarina"
                listadoImagenes[R.raw.finallimon] = "Limon"
                listadoImagenes[R.raw.finalkiwi] = "Kiwi"
                listadoImagenes[R.raw.finalguineo] = "Guineo"
                listadoImagenes[R.raw.finalgranadilla] = "Granadilla"
                listadoImagenes[R.raw.finalframbuesa] = "Frambuesa"
                listadoImagenes[R.raw.finaldurazno] = "Durazno"
                listadoImagenes[R.raw.finalcoco] = "Coco"
                listadoImagenes[R.raw.finalchirimoya] = "Chirimoya"
                listadoImagenes[R.raw.finalcereza] = "Cereza"
                listadoImagenes[R.raw.finalaguacate] = "Aguacate"
                listadoImagenes[R.raw.finalarandanos] = "Arandanos"
                listadoImagenes[R.raw.finalfresa] = "Fresa"
            }
            10 ->{
                listadoImagenes[R.raw.finalsonarlanariz] = "Sonar la nariz"
                listadoImagenes[R.raw.finalseccarcuerpo] = "Secar cuerpo"
                listadoImagenes[R.raw.finalperfumarse] = "Perfumarse"
                listadoImagenes[R.raw.finalpeinar] = "Peinar"
                listadoImagenes[R.raw.finalpasta] = "Pasta"
                listadoImagenes[R.raw.finallavarlasmanos] = "Lavar las manos"
                listadoImagenes[R.raw.finallavarselacara] = "Lavarse la cara"
                listadoImagenes[R.raw.finaljabon] = "Jabon"
                listadoImagenes[R.raw.finalenjuagarlaboca] = "Enjuagar la boca"
                listadoImagenes[R.raw.finalcortarlasunas] = "Cortar las unas"
                listadoImagenes[R.raw.finalcepillodecabello] = "Cepillo de cabello"
                listadoImagenes[R.raw.finalcepillo] = "Cepillo"
                listadoImagenes[R.raw.finalbanar] = "Banar"
                listadoImagenes[R.raw.finalagua] = "Agua"

            }
            11 ->{
                listadoImagenes[R.raw.finalviolin] = "Violin"
                listadoImagenes[R.raw.finalxilofono] = "Xilofono"
                listadoImagenes[R.raw.finaltrompeta] = "Trompeta"
                listadoImagenes[R.raw.finaltrombon] = "Trombon"
                listadoImagenes[R.raw.finaltriangulo] = "Triangulo"
                listadoImagenes[R.raw.finaltambor] = "Tambor"
                listadoImagenes[R.raw.finalsaxofon] = "Saxofon"
                listadoImagenes[R.raw.finalplatillos] = "Platillos"
                listadoImagenes[R.raw.finalpiano] = "Piano"
                listadoImagenes[R.raw.finalpandereta] = "Pandereta"
                listadoImagenes[R.raw.finalmarimba] = "Marimba"
                listadoImagenes[R.raw.finalmaracas] = "Maracas"
                listadoImagenes[R.raw.finalguitarraelectrica] = "Guitarra electrica"
                listadoImagenes[R.raw.finalguitarra] = "Guitarra"
                listadoImagenes[R.raw.finalflauta] = "Flauta"
                listadoImagenes[R.raw.finalclarinete] = "Clarinete"
                listadoImagenes[R.raw.finalbongos] = "Bongos"
                listadoImagenes[R.raw.finalbateria] = "Bateria"
                listadoImagenes[R.raw.finalarpa] = "Arpa"
                listadoImagenes[R.raw.finalacordeon] = "Acordeon"
                listadoImagenes[R.raw.finalarmonica] = "Armonica"
            }
            12 ->{
                listadoImagenes[R.raw.finalyoyo] = "Yoyo"
                listadoImagenes[R.raw.finaltrompo] = "Trompo"
                listadoImagenes[R.raw.finaltitere] = "Titere"
                listadoImagenes[R.raw.finalrosadelosvientos] = "Rosa de los vientos"
                listadoImagenes[R.raw.finalresorte] = "Resorte"
                listadoImagenes[R.raw.finalpeluche] = "Peluche"
                listadoImagenes[R.raw.finalpelota] = "Pelota"
                listadoImagenes[R.raw.finalcanicas] = "Canicas"
                listadoImagenes[R.raw.finalcubo] = "Cubo"
                listadoImagenes[R.raw.finalrobot] = "Robot"
                listadoImagenes[R.raw.finalzancos] = "Zancos"

            }
            13 ->{
                listadoImagenes[R.raw.finalcinco] = "5"
                listadoImagenes[R.raw.finalcincos] = "5"
                listadoImagenes[R.raw.finalcuatro] = "4"
                listadoImagenes[R.raw.finalcuatros] = "4"
                listadoImagenes[R.raw.finaldiez] = "10"
                listadoImagenes[R.raw.finaldos] = "2"
                listadoImagenes[R.raw.finaldoss] = "2"
                listadoImagenes[R.raw.finalnueve] = "9"
                listadoImagenes[R.raw.finalnueves] = "9"
                listadoImagenes[R.raw.finalocho] = "8"
                listadoImagenes[R.raw.finalochos] = "8"
                listadoImagenes[R.raw.finalseis] = "6"
                listadoImagenes[R.raw.finalseiss] = "6"
                listadoImagenes[R.raw.finalsiete] = "7"
                listadoImagenes[R.raw.finalsietes] = "7"
                listadoImagenes[R.raw.finaltres] = "3"
                listadoImagenes[R.raw.finaltress] = "3"
                listadoImagenes[R.raw.finaluno] = "1"
                listadoImagenes[R.raw.finalunos] = "1"

            }
            14 ->{
                listadoImagenes[R.raw.finalprofesora] = "Profesora"
                //listadoImagenes[R.raw.finalpsicologo] = "Psicologo"
                listadoImagenes[R.raw.finaltia] = "Tia"
                listadoImagenes[R.raw.finaltio] = "Tio"
                listadoImagenes[R.raw.finalpapa] = "Papa"
                listadoImagenes[R.raw.finalmama] = "Mama"
                listadoImagenes[R.raw.finalhermana] = "Hermana"
                listadoImagenes[R.raw.finalhermano] = "Hermano"
                listadoImagenes[R.raw.finaldoctor] = "Doctor"
                listadoImagenes[R.raw.finaldentista] = "Dentista"
                listadoImagenes[R.raw.finalbombero] = "Bombero"
                listadoImagenes[R.raw.finalabuela] = "Abuela"
                listadoImagenes[R.raw.finalabuelo] = "Abuelo"
                listadoImagenes[R.raw.finalninia] = "Nina"
                listadoImagenes[R.raw.finalninio] = "Nino"

            }
            15 ->{
                listadoImagenes[R.raw.finaltacones] = "Tacones"
                listadoImagenes[R.raw.finalvestido] = "Vestido"
                listadoImagenes[R.raw.finalzapatos] = "Zapatos"
                listadoImagenes[R.raw.finalshort] = "Short"
                listadoImagenes[R.raw.finalsaco] = "Saco"
                listadoImagenes[R.raw.finalropainterior] = "Ropa interior"
                listadoImagenes[R.raw.finalpantalon] = "Pantalon"
                listadoImagenes[R.raw.finalmedias] = "Medias"
                listadoImagenes[R.raw.finalchaleco] = "Chaleco"
                listadoImagenes[R.raw.finalcamisa] = "Camisa"
                listadoImagenes[R.raw.finalcalzoncillo] = "Calzoncillo"
                listadoImagenes[R.raw.finalbuso] = "Buso"
                listadoImagenes[R.raw.finalbata] = "Bata"
                listadoImagenes[R.raw.finalabrigo] = "Abrigo"
                listadoImagenes[R.raw.finalcamisetas] = "Camiseta"
                listadoImagenes[R.raw.finalcapucha] = "Capucha"
                listadoImagenes[R.raw.finalcasaca] = "Casaca"

            }
            16 ->{

                listadoImagenes[R.raw.finalacelga] = "Acelga"
                listadoImagenes[R.raw.finalaji] = "Aji"
                listadoImagenes[R.raw.finalapio] = "Apio"
                listadoImagenes[R.raw.finalarveja] = "Arveja"
                listadoImagenes[R.raw.finalberenjena] = "Berenjena"
                listadoImagenes[R.raw.finalbrocoli] = "Brocoli"
                listadoImagenes[R.raw.finalcebolla] = "Cebolla"
                listadoImagenes[R.raw.finalcebollin] = "Cebollin"
                listadoImagenes[R.raw.finalcoliflor] = "Coliflor"
                listadoImagenes[R.raw.finalesparragos] = "Esparragos"
                listadoImagenes[R.raw.finalespinaca] = "Espinaca"
                listadoImagenes[R.raw.finallechuga] = "Lechuga"
                listadoImagenes[R.raw.finalpepino] = "Pepino"
                listadoImagenes[R.raw.finalpimiento] = "Pimiento"
                listadoImagenes[R.raw.finalremolacha] = "Remolacha"
                listadoImagenes[R.raw.finaltomate] = "Tomate"
            }
        }

    }




}