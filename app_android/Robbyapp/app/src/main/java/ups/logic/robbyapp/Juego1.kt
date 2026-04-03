package ups.logic.robbyapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.transition.Visibility
import android.util.Log
import android.view.WindowManager
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
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ups.logic.robbyapp.bluetooth.BluetoothManager
import ups.logic.robbyapp.configuraciones.ApiService
import ups.logic.robbyapp.databinding.ActivityJuego1Binding
import java.io.ByteArrayOutputStream
import java.util.TreeMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import okhttp3.ResponseBody
import java.text.Normalizer
import kotlin.random.Random
import android.view.View

class Juego1 : AppCompatActivity() {

    //validaciones
    private var correctCount = 0
    private var incorrectCount = 0
    private var lastReceivedValue: String? = null
    private val threshold = 10
    private var ultimaVariable: String? = null

    //Configuraciones para camara
    private var cameraProvider: ProcessCameraProvider? = null

    private var binding: ActivityJuego1Binding? = null

    private val fragmento
        get() = binding!!


    private val processingThread = HandlerThread("ProcessingThread").apply { start() }
    private val processingHandler = android.os.Handler(processingThread.looper)

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmapBuffer: Bitmap
    private var camera: Camera? = null

    private lateinit var media: MediaPlayer

    private lateinit var repetirAudio: ImageButton
    private var audioActual: Int = 0

    private lateinit var animacion: LottieAnimationView
    private lateinit var fireworks: LottieAnimationView
    private lateinit var mensajeResultado: TextView

    private lateinit var volver: ImageButton
    private lateinit var imagen1: ImageView
    private lateinit var imagen2: ImageView
    //URL para conexion con la api

    val ok =  OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .build()



    val retrofit = Retrofit.Builder()
        .baseUrl(Memoria.url) //Siempre debe terminar en / la URL
        .addConverterFactory(GsonConverterFactory.create())
        .client(ok)
        .build()

    val apiService = retrofit.create(ApiService::class.java)

    private lateinit var puntaje : TextView
    private lateinit var tiempoVista: TextView
    private lateinit var tiempoVista2: TextView
    private var conteo: Int = 0
    private var tiempo: Int = 200
    private var sendFrame = false
    private var attempts: Int = 0

    var audiosAAcuatico = TreeMap<Int, String>()
    var audiosAAereo = TreeMap<Int, String>()
    var audiosATerrestre = TreeMap<Int, String>()
    var audiosTAcuatico = TreeMap<Int, String>()
    var audiosTAereo = TreeMap<Int, String>()
    var audiosTTerrestre = TreeMap<Int, String>()
    var audiosColor = TreeMap<Int, String>()
    var audiosComidaChatarra = TreeMap<Int, String>()
    var audiosEmocion = TreeMap<Int, String>()
    var audiosFruta = TreeMap<Int, String>()
    var audiosHigiene = TreeMap<Int, String>()
    var audiosInstrumentoMusical = TreeMap<Int, String>()
    var audiosJuguete = TreeMap<Int, String>()
    var audiosNumero = TreeMap<Int, String>()
    var audiosPersona = TreeMap<Int, String>()
    var audiosPrendaDeVestir = TreeMap<Int, String>()
    var audiosVerdura = TreeMap<Int, String>()

    var audios = listOf(audiosAAcuatico, audiosAAereo, audiosATerrestre, audiosTAcuatico, audiosTAereo, audiosTTerrestre, audiosColor, audiosComidaChatarra,
        audiosEmocion, audiosFruta, audiosHigiene, audiosInstrumentoMusical, audiosJuguete, audiosNumero, audiosPersona, audiosPrendaDeVestir, audiosVerdura)

    var listadoImagenes = TreeMap<Int, String>()





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJuego1Binding.inflate(layoutInflater)
        setContentView(fragmento.root)
        fireworks = findViewById(R.id.fireworks)
        mensajeResultado = findViewById(R.id.mensajeResultado)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainjuego)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        animacion = findViewById(R.id.cara)
        animacion.playAnimation()
        animacion.loop(true)

        supportActionBar?.hide()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        puntaje = findViewById(R.id.puntaje)
        tiempoVista = findViewById(R.id.tiempo)
        tiempoVista2 = findViewById(R.id.tiempo2)
        imagen1 = findViewById(R.id.imagen1)
        imagen2 = findViewById(R.id.imagen2)

        volver = findViewById(R.id.volver)

        repetirAudio = findViewById(R.id.repetirAudio)
        repetirAudio.setOnClickListener {
            if (audioActual != 0) {
                val media = MediaPlayer.create(this, audioActual)
                media.start()
            }
        }

        /////Iniciamos la camara
        cameraExecutor = Executors.newSingleThreadExecutor()


        cargarAudios()
        cargarImagenes()

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

        hideSystemUI()



        media = MediaPlayer.create(this, R.raw.vozinicial)

        media.start()


        Memoria.audioMemoria = 0

        val random = Random.nextInt(1,10)
        Memoria.seleccion = random

        if(random < 6){
            var audio = audios[Memoria.numeroCategoria]
            Memoria.audioMemoria = audio.keys.random()
            //audiosCache.add(Memoria.variable)
            android.os.Handler(Looper.getMainLooper()).postDelayed(
                {
                    audioActual = Memoria.audioMemoria
                    media = MediaPlayer.create(this, Memoria.audioMemoria)
                    Memoria.variable = audio[Memoria.audioMemoria]
                    media.start()

                    media.setOnCompletionListener {
                        tiempoVista.isVisible = true
                        tiempoVista2.isVisible = true
                        fragmento.camara.post{

                            setUpCamera()
                        }
                    }

                },
                6000
            )
        }else{
            if(Memoria.edad){
                var audio = audios[Memoria.numeroCategoria]
                Memoria.audioMemoria = audio.keys.random()
                android.os.Handler(Looper.getMainLooper()).postDelayed(
                    {
                        audioActual = R.raw.leemicara
                        media = MediaPlayer.create(this, audioActual)
                        Memoria.variable = audio[Memoria.audioMemoria]
                        media.start()

                        media.setOnCompletionListener {
                            tiempoVista.isVisible = true
                            tiempoVista2.isVisible = true
                            fragmento.camara.post{

                                setUpCamera()
                            }
                        }

                    },
                    6000
                )
            }else{
                Memoria.seleccion = 5
                var audio = audios[Memoria.numeroCategoria]
                Memoria.audioMemoria = audio.keys.random()
                //audiosCache.add(Memoria.variable)
                android.os.Handler(Looper.getMainLooper()).postDelayed(
                    {
                        audioActual = Memoria.audioMemoria
                        media = MediaPlayer.create(this, Memoria.audioMemoria)
                        Memoria.variable = audio[Memoria.audioMemoria]
                        media.start()

                        media.setOnCompletionListener {
                            tiempoVista.isVisible = true
                            tiempoVista2.isVisible = true
                            fragmento.camara.post{

                                setUpCamera()
                            }
                        }

                    },
                    6000
                )
            }
        }




        volver.setOnClickListener {

            finish()
        }
        Memoria.puntosJuego1 = 0

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

    private var finalJuego = true

    fun detects(image: Bitmap, nivel: Int) {
        var audio = audios[Memoria.numeroCategoria]
        if(!sendFrame) {
            if (nivel < 6) {
                juegoCamara(image)
            } else {
                juegoLeer(image)
            }
        }
//        else if(Memoria.puntosJuego1 == 10){
//            Handler(Looper.getMainLooper()).postDelayed(
//                {
//                    media = MediaPlayer.create(this, R.raw.diezpuntos)
//                    media.start()
//                    val duration = R.raw.diezpuntos.let { this.media.duration }
//                    val lon: Long = duration.toLong()
//                    Thread.sleep(lon)
//                },
//                10
//            )
//            Thread.sleep(5200)
        else if(audio.isEmpty() && finalJuego){

            finalJuego = false
            media = MediaPlayer.create(this, R.raw.juego1completo)
            media.start()
            //media.setOnCompletionListener {
                finish()
            //}


            BluetoothManager.sendDataToHC05("4")

        }
    }



    private fun juegoCamara(image: Bitmap) {
        //Thread.sleep(700)
        if(!sendFrame){
            val handler = android.os.Handler(Looper.getMainLooper())
            handler.post {
                tiempoVista.text = ""
                tiempoVista2.text = ""
                tiempoVista.isVisible = false
                tiempoVista2.isVisible = false
            }

            val matriz = Matrix()
            matriz.postRotate(0f)

            val outputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("image", "image.jpeg", requestBody)

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
                    val textoLimpio = body?.replace("[^a-zA-ZáéíóúÁÉÍÓÚ ]".toRegex(), "")?.lowercase()



                    // Validar la respuesta del API
                    if (textoLimpio!!.contains("naida")) {
                        return
                    }
                    val text =
                        Memoria.variable.replace("[^a-zA-ZáéíóúÁÉÍÓÚ ]".toRegex(), "")?.lowercase()
                    val textoSinTildes = Normalizer.normalize(text, Normalizer.Form.NFD)
                        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                        .replace("nia", "ña", ignoreCase = true)
                        .replace("nio", "ño", ignoreCase = true)

                    if (textoLimpio == lastReceivedValue) {
                        if (textoLimpio == textoSinTildes) {
                            correctCount++
                            incorrectCount = 0
                        } else {
                            incorrectCount++
                            correctCount = 0
                        }
                    } else {
                        resetCounters()
                        if (textoLimpio == text) {
                            correctCount++
                            incorrectCount = 0
                        } else {
                            incorrectCount++
                            correctCount = 0
                        }
                    }

                    lastReceivedValue = textoLimpio

                    if (correctCount >= threshold) {
                        BluetoothManager.sendDataToHC05("7")
                        BluetoothManager.sendDataToHC05("3")
                        println("Correcto: ${Memoria.variable}")
                        attempts = 0

                        Memoria.puntosJuego1 += 1
                        mostrarFuegos()
                        mostrarCorrecto()
                        handler.post {
                            tiempoVista.isVisible = false
                            tiempoVista2.isVisible = false
                        }
                        media = MediaPlayer.create(this, R.raw.correctolohicistebienjuegocuatro)
    //                        Handler(Looper.getMainLooper()).postDelayed(
    //                            { animacion.playAnimation() },
    //                            0
    //                        )

                        media.start()
                        Thread.sleep(2000)

                        if (!Memoria.edad) {
                            sendFrame = true
                            val audio = audios[Memoria.numeroCategoria]
                            audio.keys.remove(Memoria.audioMemoria)
                            Memoria.audioMemoria = audio.keys.random()

                            android.os.Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    audioActual = if (Memoria.seleccion < 6) {
                                    Memoria.audioMemoria
                                } else {
                                     R.raw.leemicara
                        }

                                media = MediaPlayer.create(this, audioActual)

                                    Memoria.variable = audio[Memoria.audioMemoria]
                                    media.start()
                                },
                                5000
                            )
                        } else {
                            val random = Random.nextInt(1, 10)
                            Memoria.seleccion = random
                            sendFrame = true
                            val audio = audios[Memoria.numeroCategoria]
                            audio.keys.remove(Memoria.audioMemoria)
                            Memoria.audioMemoria = audio.keys.random()

                            android.os.Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    media = if (random < 6) {
                                        
                                        MediaPlayer.create(this, Memoria.audioMemoria)
                                    } else {
                                        MediaPlayer.create(this, R.raw.leemicara)
                                    }
                                    Memoria.variable = audio[Memoria.audioMemoria]
                                    media.start()
                                },
                                5000
                            )
                        }

                        android.os.Handler(Looper.getMainLooper()).postDelayed(
                            {
                                tiempoVista.isVisible = true
                                tiempoVista2.isVisible = true
                                sendFrame = false
                            },
                            8500
                        )
                        puntaje.text = Memoria.puntosJuego1.toString()

                        Thread.sleep(5000)
                        resetCounters()
                        BluetoothManager.sendDataToHC05("6")
                    } else {
    //                        if (Memoria.variable.lowercase() == ultimaVariable) {
    //                            incorrectCount++
    //                        } else {
    //                            incorrectCount = 1
    //                        }
                        if (incorrectCount > 6) {
                            return
                        }


                        if (incorrectCount == 5) {
                            BluetoothManager.sendDataToHC05("7")
                            BluetoothManager.sendDataToHC05("2")
                            BluetoothManager.sendDataToHC05("6")
                            //lastReceivedValue = Memoria.variable.lowercase()
    //                            if(textoLimpio == lastReceivedValue){
    //                                return
    //                            }
                            attempts++
                            println("Incorrecto: Se esperaba ${Memoria.variable}, pero se recibió $textoLimpio")
                            mostrarIncorrecto()
                            media = MediaPlayer.create(this, R.raw.incorrecto)
                            android.os.Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    media.start()
                                },
                                0
                            )
                            media.setOnCompletionListener {
                                media = MediaPlayer.create(this, Memoria.audioMemoria).apply {
                                    start()
                                }
                                if (attempts == 3) {
                                    sendFrame = true
                                    correccion()
                                    attempts = 0
    //                                    return
                                }
                            }

                            //if (BluetoothManager.isConnected()) {
                            //}
                            //tiempo = 200
                            //resetCounters()
                        }
                    }


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


    private fun juegoLeer(image: Bitmap){
        //Thread.sleep(700)
        val objeto = Memoria.variable
        val handler = android.os.Handler(Looper.getMainLooper())
        handler.post{
            tiempoVista.text = objeto
            tiempoVista2.text = objeto
        }
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody =
            MultipartBody.Part.createFormData("image", "image.jpeg", requestBody)

        var call : Call<ResponseBody>?  = null
        when(Memoria.numeroCategoria){
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
            val response = call!!.execute()  // Llamada sincrónica
            if (response.isSuccessful) {
                val body = response.body()?.string()  // La respuesta ya está en el cuerpo
                val gson = Gson()
                //fragmento.obje.text = body ?: "Nada"

                val text =
                    Memoria.variable.replace("[^a-zA-ZáéíóúÁÉÍÓÚ ]".toRegex(), "")?.lowercase()
                val textoSinTildes = Normalizer.normalize(text, Normalizer.Form.NFD)
                    .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                    .replace("nia", "ña", ignoreCase = true)
                    .replace("nio", "ño", ignoreCase = true)



                if(textoSinTildes!!.contains("naida")){
                    return
                }
                    if (textoSinTildes == lastReceivedValue) {
                        if (textoSinTildes == Memoria.variable.replace("[^a-zA-ZáéíóúÁÉÍÓÚ ]".toRegex(), "")?.lowercase()) {
                            correctCount++
                            incorrectCount = 0
                        } else {
                            incorrectCount++
                            correctCount = 0
                        }
                    } else {
                        resetCounters()
                        if (textoSinTildes == Memoria.variable.replace("[^a-zA-ZáéíóúÁÉÍÓÚ ]".toRegex(), "")?.lowercase()) {
                            correctCount++
                        } else {
                            incorrectCount++
                        }
                    }

                    lastReceivedValue = textoSinTildes

                    if (correctCount >= threshold) {
                        BluetoothManager.sendDataToHC05("7")
                        BluetoothManager.sendDataToHC05("3")
                        BluetoothManager.sendDataToHC05("6")
                        println("Correcto: ${Memoria.variable}")
                        attempts = 0
                        Memoria.puntosJuego1 += 1
                        handler.post {
                            tiempoVista.isVisible = false
                            tiempoVista2.isVisible = false
                        }
                        media = MediaPlayer.create(this, R.raw.correctolohicistebienjuegocuatro)
//                        android.os.Handler(Looper.getMainLooper()).postDelayed(
//                            { animacion.playAnimation() },
//                            0
//                        )

                        media.start()
                        Thread.sleep(2000)
                        sendFrame = true
                        val audio = audios[Memoria.numeroCategoria]
                        audio.keys.remove(Memoria.audioMemoria)
                        Memoria.audioMemoria = audio.keys.random()
                        val random = Random.nextInt(1, 10)
                        Memoria.seleccion = random
                        android.os.Handler(Looper.getMainLooper()).postDelayed(
                            {
                                media = if (random < 6) {
                                    MediaPlayer.create(this, Memoria.audioMemoria)
                                } else {
                                    MediaPlayer.create(this, R.raw.leemicara)
                                }
                                Memoria.variable = audio[Memoria.audioMemoria]
                                media.start()
                            },
                            5000
                        )

                        android.os.Handler(Looper.getMainLooper()).postDelayed(
                            {
                                tiempoVista.isVisible = true
                                tiempoVista2.isVisible = true
                                sendFrame = false
                            },
                            8500
                        )
                        puntaje.text = Memoria.puntosJuego1.toString()
                        //Thread.sleep(5000)
                        resetCounters()
                    } else {


                        if (incorrectCount > 6) {
                            return
                        }



                        if (incorrectCount == 5) {
//                            if(textoLimpio == lastReceivedValue){
//                                return
//                            }
                            //lastReceivedValue = Memoria.variable.lowercase()
                            attempts++
                            println("Incorrecto: Se esperaba ${Memoria.variable}, pero se recibió $textoSinTildes")
                            BluetoothManager.sendDataToHC05("7")
                            BluetoothManager.sendDataToHC05("2")
                            BluetoothManager.sendDataToHC05("6")
                            mostrarIncorrecto()
                            media = MediaPlayer.create(this, R.raw.incorrecto)
                            android.os.Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    media.start()
                                },
                                0
                            )
                            media.setOnCompletionListener {
                                media = MediaPlayer.create(this, Memoria.audioMemoria).apply {
                                    //start()
                                }
                                if(attempts == 3){
                                    sendFrame = false
                                    correccion()
                                    attempts = 0

                                }
                            }

                            //if (BluetoothManager.isConnected()) {
                            //BluetoothManager.sendDataToHC05("2")
                            //}
                           // tiempo = 200
                            //resetCounters()
                        }
                    }



                Log.d("Retrofit", "Respuesta exitosa: $body")
            } else {
                val error = response.errorBody()?.string()
                Log.e("Retrofit", "Error en la respuesta: $error")
            }

        } catch (e: Exception) {
            Log.e("Retrofit", "Error en la solicitud: ${e.message}")
        }
    }

    private fun correccion() {
        media = MediaPlayer.create(this, R.raw.estaeslarespuesta)
        media.start()

        val audio = audios[Memoria.numeroCategoria]
        val imagen = audio[Memoria.audioMemoria]

        val clave = listadoImagenes.entries.find { it.value.lowercase() == imagen?.lowercase() }?.key

        if (clave != null) {
            imagen1.setImageResource(clave)
            imagen2.setImageResource(clave)

            imagen1.isVisible = true
            imagen2.isVisible = true

            if (tiempoVista.isVisible) {
                tiempoVista.isVisible = false
                tiempoVista2.isVisible = false
            }

            // Ocultar imagen después de 5 segundos
            Handler(Looper.getMainLooper()).postDelayed({
                imagen1.isVisible = false
                imagen2.isVisible = false

                if (!tiempoVista.isVisible) {
                    tiempoVista.isVisible = true
                    tiempoVista2.isVisible = true
                }
                sendFrame = false
                // Reproducir audio después de ocultar imagen
                media = MediaPlayer.create(this, R.raw.okey)
                media.start()
//                media.setOnCompletionListener {
//                    media = MediaPlayer.create(this, Memoria.audioMemoria).apply {
//                        if(Memoria.seleccion < 5){
//                            start()
//                        }
//                    }
//                }
            }, 8000)
        } else {
            sendFrame = false
            Toast.makeText(this, "Imagen no encontrada", Toast.LENGTH_SHORT).show()
        }
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
                listadoImagenes[R.raw.finalpera] = "Pera"
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
                listadoImagenes[R.raw.finalcortarlasunas] = "Cortar las uñas"
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
                //listadoImagenes[R.raw.finaltia] = "Tia"
                //listadoImagenes[R.raw.finaltio] = "Tio"
                listadoImagenes[R.raw.finalpapa] = "Papa"
                listadoImagenes[R.raw.finalmama] = "Mama"
                listadoImagenes[R.raw.finalhermana] = "Hermana"
                listadoImagenes[R.raw.finalhermano] = "Hermano"
                listadoImagenes[R.raw.finaldoctor] = "Doctor"
                listadoImagenes[R.raw.finaldentista] = "Dentista"
                listadoImagenes[R.raw.finalbombero] = "Bombero"
                listadoImagenes[R.raw.finalabuela] = "Abuela"
                listadoImagenes[R.raw.finalabuelo] = "Abuelo"
                listadoImagenes[R.raw.finalninia] = "Ninia"
                listadoImagenes[R.raw.finalninio] = "Ninio"

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




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

    private fun cargarAudios(){
        //audiosPictogramas[R.raw.frutas] = "Fruta"
//        audiosPictogramas[R.raw.fruta] = "Fruta"
//        audiosPictogramas[R.raw.animalesacuatico] = "Animal Acuatico"//"Animales Acuaticos"
//        //audiosPictogramas[R.raw.animalesacuaticos] = "Animal Acuatico"//"Animales Acuaticos"
//        audiosPictogramas[R.raw.animalesaereo] = "Animal Aereo"//"Animales Aereos"
//        //audiosPictogramas[R.raw.animalesaereos] = "Animal Aereo"//"Animales Aereos"
//        audiosPictogramas[R.raw.animalesterrestre] =  "Animal Terrestre"//"Animales Terrestres"
//        //audiosPictogramas[R.raw.animalesterrestres] = "Animal Terrestre"//"Animales Terrestres"
//        audiosPictogramas[R.raw.color] = "Color"
//        audiosPictogramas[R.raw.comidachatarra] = "Comida Chatarra"
//        //audiosPictogramas[R.raw.comidachatarras] = "Comida Chatarra"
//        audiosPictogramas[R.raw.emocion] = "Emocion"
//        //audiosPictogramas[R.raw.emociones] = "Emocion"
//        audiosPictogramas[R.raw.higiene] = "Higiene"
//        //audiosPictogramas[R.raw.higienes] = "Higiene"
//        audiosPictogramas[R.raw.prendasdevestir] = "Prenda de Vestir"
//        //audiosPictogramas[R.raw.prenda] = "Prenda de Vestir"
//        audiosPictogramas[R.raw.transporteaereo] = "Transporte Aereo"
//        //audiosPictogramas[R.raw.transporteaereos] = "Transporte Aereo"
//        audiosPictogramas[R.raw.transporteterrestre] = "Transporte Terrestre"
//        //audiosPictogramas[R.raw.transporteterrestres] = "Transporte Terrestre"
//        audiosPictogramas[R.raw.transporteacuatico] = "Transporte Acuatico"
//        //audiosPictogramas[R.raw.transporteacuaticos] = "Transporte Acuatico"
//        audiosPictogramas[R.raw.numero] = "Numero"
//        //audiosPictogramas[R.raw.numeros] = "Numero"
//        audiosPictogramas[R.raw.persona] = "Persona"
//        //audiosPictogramas[R.raw.personas] = "Persona"
        //audiosPictogramas[R.raw.instrumentomusical] = "Instrumento Musical"
        //audiosPictogramas[R.raw.instrumentosmusicales] = "Instrumento Musical"
        ////////////////////////// Audios especificos

        audiosAAereo[R.raw.abeja] = "Abeja"
        audiosPrendaDeVestir[R.raw.abrigo] = "Abrigo"
        audiosPersona[R.raw.abuela] = "Abuela"
        audiosPersona[R.raw.abuelo] = "Abuelo"
        audiosHigiene[R.raw.agua] = "Agua"
        audiosAAereo[R.raw.libelula] = "Libelula"
        audiosColor[R.raw.amarillo] = "Amarillo"
        audiosAAcuatico[R.raw.anguila] = "Anguila"
        audiosATerrestre[R.raw.ardilla] = "Ardilla"
        audiosATerrestre[R.raw.armadillo] = "Armadillo"
        audiosColor[R.raw.azul] = "Azul"
        audiosAAcuatico[R.raw.ballena] = "Ballena"
        audiosPersona[R.raw.bombero] = "Bombero"
        audiosColor[R.raw.blanco] = "Blanco"
        audiosVerdura[R.raw.brocoli] = "Brocoli"
        audiosVerdura[R.raw.aji] = "Aji"
        audiosAAereo[R.raw.buho] = "Buho"
        audiosPrendaDeVestir[R.raw.buso] = "Buso"
        audiosATerrestre[R.raw.caballo] = "Caballo"
        audiosAAcuatico[R.raw.caballitodemar] = "Caballito de mar"
        audiosATerrestre[R.raw.cabra] = "Cabra"
        audiosPrendaDeVestir[R.raw.calzoncillo] = "Calzoncillo"
        audiosNumero[R.raw.uno] = "Uno"
        audiosNumero[R.raw.dos] = "Dos"
        //audiosHigiene[R.raw.enjabonarelcuerpo] = "Enjabonar el cuerpo"
        audiosHigiene[R.raw.enjuagarlaboca] = "Enjuagar la boca"
        audiosNumero[R.raw.tres] = "Tres"
        audiosNumero[R.raw.cuatro] = "Cuatro"
        audiosNumero[R.raw.cinco] = "Cinco"
        audiosNumero[R.raw.seis] = "Seis"
        audiosNumero[R.raw.siete] = "Siete"
        audiosNumero[R.raw.ocho] = "Ocho"
        audiosNumero[R.raw.nueve] = "Nueve"
        audiosNumero[R.raw.diez] = "Diez"
        audiosAAcuatico[R.raw.camaron] = "Camaron"
        audiosATerrestre[R.raw.camello] = "Camello"
        audiosPrendaDeVestir[R.raw.camisa] = "Camisa"
        audiosPrendaDeVestir[R.raw.camiseta] = "Camiseta"
        audiosAAcuatico[R.raw.cangrejo] = "Cangrejo"
        audiosATerrestre[R.raw.canguro] = "Canguro"
        audiosJuguete[R.raw.canicas] = "Canicas"
        audiosPrendaDeVestir[R.raw.capucha] = "Capucha"
        audiosATerrestre[R.raw.caracol] = "Caracol"
        audiosPrendaDeVestir[R.raw.casaca] = "Casaca"
        audiosATerrestre[R.raw.cebra] = "Cebra"
        audiosColor[R.raw.celeste] = "Celeste"
        audiosATerrestre[R.raw.cerdo] = "Cerdo"
        audiosPrendaDeVestir[R.raw.chaleco] = "Chaleco"
        audiosATerrestre[R.raw.ciervo] = "Ciervo"
        audiosAAereo[R.raw.ciguenia] = "Ciguenia"
        audiosAAcuatico[R.raw.cocodrilo] = "Cocodrilo"
        audiosAAereo[R.raw.colibri] = "Colibri"
        audiosAAereo[R.raw.condor] = "Condor"
        audiosJuguete[R.raw.cubo] = "Cubo"
        audiosATerrestre[R.raw.cucaracha] = "Cucaracha"
        audiosAAereo[R.raw.cuervo] = "Cuervo"
        audiosAAcuatico[R.raw.delfin] = "Delfin"
        audiosPersona[R.raw.doctor] = "Doctor"
        audiosATerrestre[R.raw.erizo] = "Erizo"
        audiosATerrestre[R.raw.escarabajo] = "Escarabajo"
        audiosATerrestre[R.raw.escorpion] = "Escorpion"
        audiosAAcuatico[R.raw.estrellademar] = "Estrella de mar"
        audiosAAcuatico[R.raw.foca] = "Foca"
        audiosATerrestre[R.raw.gato] = "Gato"
        audiosPersona[R.raw.hermana] = "Hermana"
        audiosPersona[R.raw.hermano] = "Hermano"
        audiosAAcuatico[R.raw.hipopotamo] = "Hipopótamo"
        audiosATerrestre[R.raw.hormiga] = "Hormiga"
        audiosATerrestre[R.raw.jirafa] = "Jirafa"
        audiosHigiene[R.raw.lavarselacara] = "Lavarse la cara"
        audiosATerrestre[R.raw.leon] = "Leon"
        audiosATerrestre[R.raw.lombriz] = "Lombriz"
        audiosPersona[R.raw.mama] = "Mama"
        audiosAAcuatico[R.raw.manta] = "Manta"
        audiosPrendaDeVestir[R.raw.medias] = "Medias"
        audiosAAcuatico[R.raw.medusa] = "Medusa"
        audiosPersona[R.raw.ninia] = "Ninia"
        audiosPersona[R.raw.ninio] = "Ninio"
        audiosPersona[R.raw.dentista] = "Dentista"
        audiosAAcuatico[R.raw.orca] = "Orca"
        audiosAAcuatico[R.raw.ostra] = "Ostra"
        audiosATerrestre[R.raw.oveja] = "Oveja"
        audiosPrendaDeVestir[R.raw.pantalon] = "Pantalon"
        audiosPersona[R.raw.papa] = "Papa"
        audiosAAcuatico[R.raw.pato] = "Pato"
        audiosJuguete[R.raw.peluche] = "Peluche"
        audiosAAcuatico[R.raw.pez] = "Pez"
        audiosAAcuatico[R.raw.pinguino] = "Pinguino"
        audiosAAcuatico[R.raw.pirana] = "Pirania"
        audiosPersona[R.raw.profesora] = "Profesora"
       // audiosPersona[R.raw.psicologo] = "Psicologo"
        audiosAAcuatico[R.raw.pulpo] = "Pulpo"
        audiosAAcuatico[R.raw.rana] = "Rana"
        audiosATerrestre[R.raw.raton] = "Raton"
        audiosJuguete[R.raw.resorte] = "Resorte"
        audiosATerrestre[R.raw.rinoceronte] = "Rinoceronte"
        audiosJuguete[R.raw.robot] = "Robot"
        audiosJuguete[R.raw.pelota] = "Pelota"
        //audiosJuguete[R.raw.rosadelosvientos] = "Rosa de los vientos"
        audiosJuguete[R.raw.titere] = "Titere"
        audiosPrendaDeVestir[R.raw.saco] = "Saco"
        audiosATerrestre[R.raw.saltamontes] = "Saltamontes"
        audiosPrendaDeVestir[R.raw.tacones] = "Tacones"
        audiosATerrestre[R.raw.tarantula] = "Tarantula"
        //audiosPersona[R.raw.tia] = "Tia"
        audiosAAcuatico[R.raw.tiburon] = "Tiburon"
        audiosATerrestre[R.raw.tigre] = "Tigre"
        //audiosPersona[R.raw.tio] = "Tio"
        audiosAAcuatico[R.raw.tortuga] = "Tortuga"
        audiosJuguete[R.raw.trompo] = "Trompo"
        audiosATerrestre[R.raw.vaca] = "Vaca"
        audiosPrendaDeVestir[R.raw.vestido] = "Vestido"
        audiosJuguete[R.raw.yoyo] = "Yoyo"
        audiosPrendaDeVestir[R.raw.zapatos] = "Zapatos"
        audiosATerrestre[R.raw.zorro] = "Zorro"
        audiosAAereo[R.raw.flamenco] = "Flamenco"
        audiosAAereo[R.raw.gaviota] = "Gaviota"
        audiosATerrestre[R.raw.gorila] = "Gorila"
        audiosColor[R.raw.gris] = "Gris"
        audiosAAereo[R.raw.lechuza] = "Lechuza"
        audiosColor[R.raw.lila] = "Lila"
        audiosAAereo[R.raw.mariposa] = "Mariposa"
        audiosColor[R.raw.marron] = "Marron"
        audiosColor[R.raw.morado] = "Morado"
        audiosAAereo[R.raw.mosca] = "Mosca"
        audiosColor[R.raw.negro] = "Negro"
        audiosAAereo[R.raw.pajaro] = "Pajaro"
        audiosAAereo[R.raw.paloma] = "Paloma"
        audiosATerrestre[R.raw.pavo] = "Pavo"
        audiosAAereo[R.raw.pelicano] = "Pelicano"
        audiosATerrestre[R.raw.perro] = "Perro"
        audiosColor[R.raw.rojo] = "Rojo"
        audiosPrendaDeVestir[R.raw.ropainterior] = "Ropa interior"
        audiosColor[R.raw.rosado] = "Rosado"
        audiosPrendaDeVestir[R.raw.saco] = "Saco"
        audiosHigiene[R.raw.sonarlanariz] = "Sonar la nariz"
        audiosAAereo[R.raw.tucan] = "Tucan"
        audiosColor[R.raw.verdeclaro] = "Verde claro"
        audiosColor[R.raw.verdeoscuro] = "Verde oscuro"
        audiosJuguete[R.raw.zancos] = "Zancos"
        //audiosPrendaDeVestir[R.raw.sort] = "Sort"

        //2da mitad...
        audiosFruta[R.raw.durazno] = "Durazno"
        audiosFruta[R.raw.maracuya] = "Maracuya"
        audiosFruta[R.raw.limon] = "Limon"
        audiosFruta[R.raw.chirimoya] = "Chirimoya"
        audiosFruta[R.raw.mora] = "Mora"
        audiosFruta[R.raw.guineo] = "Guineo"
        audiosFruta[R.raw.mango] = "Mango"
        audiosFruta[R.raw.coco] = "Coco"
        audiosFruta[R.raw.cereza] = "Cereza"
        audiosFruta[R.raw.aguacate] = "Aguacate"
        audiosFruta[R.raw.papaya] = "Papaya"
        audiosFruta[R.raw.uvas] = "Uvas"
        audiosFruta[R.raw.granadilla] = "Granadilla"
        audiosFruta[R.raw.arandanos] = "Arandanos"
        audiosFruta[R.raw.manzana] = "Manzana"
        audiosFruta[R.raw.kiwi] = "Kiwi"
        audiosFruta[R.raw.fresa] = "Fresa"
        audiosFruta[R.raw.frambuesa] = "Frambuesa"
        audiosFruta[R.raw.mandarina] = "Mandarina"
        audiosFruta[R.raw.naranja] = "Naranja"
        audiosTTerrestre[R.raw.cuadron] = "Cuadron"
        audiosInstrumentoMusical[R.raw.clarinete] = "Clarinete"
        audiosTTerrestre[R.raw.bus] = "Bus"
        audiosComidaChatarra[R.raw.canguil] = "Canguil"
        audiosVerdura[R.raw.cebolla] = "Cebolla"
        audiosVerdura[R.raw.esparragos] = "Esparragos"
        audiosHigiene[R.raw.jabon] = "Jabon"
        audiosVerdura[R.raw.cebollin] = "Cebollin"
        audiosTTerrestre[R.raw.cochedecarreras] = "Coche de carreras"
        audiosTAcuatico[R.raw.remos] = "Remos"
        audiosTAcuatico[R.raw.lancha] = "Lancha"
        audiosTTerrestre[R.raw.camiondelabasura] = "Camión de la basura"
        audiosTTerrestre[R.raw.volqueta] = "Volqueta"
        audiosInstrumentoMusical[R.raw.trombon] = "Trombon"
        audiosTTerrestre[R.raw.tranvia] = "Tranvía"
        //audiosHigiene[R.raw.cepillodecabello] = "Cepillo de cabello"
        audiosTTerrestre[R.raw.tanque] = "Tanque"
        audiosHigiene[R.raw.baniar] = "Baniar"
        audiosTAcuatico[R.raw.submarino] = "Submarino"
        audiosInstrumentoMusical[R.raw.marimba] = "Marimba"
        audiosTAereo[R.raw.avion] = "Avion"
        audiosTAcuatico[R.raw.barcopirata] = "Barco pirata"
        //audiosHigiene[R.raw.cepillodecabello] = "Cepillo de cabello"
        audiosTTerrestre[R.raw.tren] = "Tren"
        audiosInstrumentoMusical[R.raw.guitarraelectrica] = "Guitarra electrica"
        audiosVerdura[R.raw.lechuga] = "Lechuga"
        audiosInstrumentoMusical[R.raw.xilofono] = "Xilofono"
        audiosTTerrestre[R.raw.autobus] = "Autobús"
        audiosInstrumentoMusical[R.raw.saxofon] = "Saxofon"
        audiosInstrumentoMusical[R.raw.bajo] = "Bajo"
        audiosHigiene[R.raw.lavarlasmanos] = "Lavar las manos"
        audiosInstrumentoMusical[R.raw.armonica] = "Armonica"
        audiosHigiene[R.raw.jabon] = "Jabon"
        audiosInstrumentoMusical[R.raw.arpa] = "Arpa"
        audiosTAereo[R.raw.helicoptero] = "Helicoptero"
        audiosTTerrestre[R.raw.metro] = "Metro"
        audiosHigiene[R.raw.secarelcuerpo] = "Secar el cuerpo"
        audiosHigiene[R.raw.peinar] = "Peinar"
        audiosEmocion[R.raw.feliz] = "Feliz"
        audiosTAcuatico[R.raw.motoacuatica] = "Moto acuatica"
        audiosHigiene[R.raw.peinar] = "Peinar"
        audiosEmocion[R.raw.enfadado] = "Enfadado"
        audiosTTerrestre[R.raw.ambulancia] = "Ambulancia"
        audiosTTerrestre[R.raw.triciclo] = "Triciclo"
        audiosInstrumentoMusical[R.raw.piano] = "Piano"
        audiosComidaChatarra[R.raw.gomitas] = "Gomitas"
        audiosInstrumentoMusical[R.raw.flauta] = "Flauta"
        audiosVerdura[R.raw.pimiento] = "Pimiento"
        audiosTTerrestre[R.raw.carruaje] = "Carruaje"
        audiosComidaChatarra[R.raw.helado] = "Helado"
        audiosVerdura[R.raw.pepino] = "Pepino"
        audiosTAereo[R.raw.paracaidas] = "Paracaidas"
        audiosInstrumentoMusical[R.raw.saxofon] = "Saxofon"
        audiosTAcuatico[R.raw.barcopesquero] = "Barco Pesquero"
        audiosHigiene[R.raw.perfumarse] = "Perfumarse"
        audiosInstrumentoMusical[R.raw.bongos] = "Bongos"
        audiosTTerrestre[R.raw.carro] = "Carro"
        audiosTTerrestre[R.raw.camiondebomberos] = "Camión de bomberos"
        audiosVerdura[R.raw.arveja] = "Arveja"
        audiosTAcuatico[R.raw.barco] = "Barco"
        audiosTAereo[R.raw.cohete] = "Cohete"
        audiosHigiene[R.raw.cortarlasunasdelospies] = "Cortar las uñas"
        audiosTTerrestre[R.raw.bicicleta] = "Bicicleta"
        audiosComidaChatarra[R.raw.gomitas] = "Gomitas"
        audiosTTerrestre[R.raw.cochedecarreras] = "Coche de carreras"
        audiosEmocion[R.raw.asustado] = "Asustado"
        audiosTAcuatico[R.raw.remos] = "Remos"
        audiosEmocion[R.raw.timido] = "Timido"
        //audiosComidaChatarra[R.raw.chocolate] = "Chocolate"
        audiosInstrumentoMusical[R.raw.flauta] = "Flauta"
        audiosTAcuatico[R.raw.canoa] = "Canoa"
        audiosTTerrestre[R.raw.moto] = "Moto"
        audiosHigiene[R.raw.camion] = "Camión"
        audiosHigiene[R.raw.cepillo] = "Cepillo"
        audiosInstrumentoMusical[R.raw.maracas] = "Maracas"
        audiosInstrumentoMusical[R.raw.tambor] = "Tambor"
        audiosEmocion[R.raw.aburrido] = "Aburrido"
        audiosTAereo[R.raw.avioneta] = "Avioneta"
        audiosTAcuatico[R.raw.yate] = "Yate"
        audiosTTerrestre[R.raw.grua] = "Grúa"
        audiosComidaChatarra[R.raw.canguil] = "Canguil"
        audiosEmocion[R.raw.feliz] = "Feliz"
        audiosVerdura[R.raw.acelga] = "Acelga"
        audiosInstrumentoMusical[R.raw.acordeon] = "Acordeon"
        audiosComidaChatarra[R.raw.hamburguesa] = "Hamburguesa"
        audiosVerdura[R.raw.apio] = "Apio"
        audiosVerdura[R.raw.tomate] = "Tomate"
        audiosTTerrestre[R.raw.busturistico] = "Bus turístico"
        audiosEmocion[R.raw.triste] = "Triste"
        audiosEmocion[R.raw.asustado] = "Asustado"
        audiosComidaChatarra[R.raw.galletadechocolate] = "Galleta de chocolate"
        audiosTAereo[R.raw.globo] = "Globo"
        audiosInstrumentoMusical[R.raw.violin] = "Violin"
        audiosTTerrestre[R.raw.carrodepolicia] = "Carro de policía"
        audiosHigiene[R.raw.pasta] = "Pasta"
        audiosTTerrestre[R.raw.taxi] = "Taxi"
        audiosInstrumentoMusical[R.raw.bateria] = "Bateria"
        audiosTAereo[R.raw.teleferico] = "Teleferico"
        audiosEmocion[R.raw.asco] = "Asco"
        //audiosHigiene[R.raw.cortaruniasdelasmanos] = "Cortar unias de las manos"
        audiosComidaChatarra[R.raw.papasfritas] = "Papas fritas"
        audiosInstrumentoMusical[R.raw.trompeta] = "Trompeta"
        audiosVerdura[R.raw.remolacha] = "Remolacha"
        audiosTTerrestre[R.raw.furgoneta] = "Furgoneta"
        audiosInstrumentoMusical[R.raw.triangulo] = "Triangulo"
        audiosInstrumentoMusical[R.raw.guitarra] = "Guitarra"
        audiosVerdura[R.raw.coliflor] = "Coliflor"
        audiosInstrumentoMusical[R.raw.platillos] = "Platillos"
        audiosComidaChatarra[R.raw.grajeas] = "Grajeas"
        audiosComidaChatarra[R.raw.pizza] = "Pizza"
        audiosTTerrestre[R.raw.monopatin] = "Monopatín"
        audiosVerdura[R.raw.berenjena] = "Berenjena"
        audiosVerdura[R.raw.espinaca] = "Espinaca"
        audiosInstrumentoMusical[R.raw.pandereta] = "Pandereta"

    }

    private fun resetCounters() {
        correctCount = 0
        incorrectCount = 0
        lastReceivedValue = null
    }


}



object Constants {
    const val TAG = "cameraX"
    const val FILE_NAME_FORMAT = "yy-MM-dd-HH-mm-ss-SSS"
    const val REQUEST_CODE_PERMISSIONS = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.INTERNET)
}
