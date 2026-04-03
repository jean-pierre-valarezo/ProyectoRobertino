package ups.logic.robbyapp

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.Visibility
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ups.logic.robbyapp.bluetooth.BluetoothManager
import ups.logic.robbyapp.configuraciones.AndroidAudioRecorder
import ups.logic.robbyapp.configuraciones.ApiService
import ups.logic.robbyapp.databinding.ActivityJuego3Binding
import java.io.File
import java.text.Normalizer
import java.util.TreeMap
import java.util.concurrent.TimeUnit

class Juego3 : AppCompatActivity() {


    private var binding: ActivityJuego3Binding? = null

    private val fragmento
        get() = binding!!


    private val processingThread = HandlerThread("ProcessingThread").apply { start() }
    private val processingHandler = android.os.Handler(processingThread.looper)

    val ok = OkHttpClient.Builder()
        .connectTimeout(55, TimeUnit.SECONDS)   // conexión
        .readTimeout(55, TimeUnit.SECONDS)      // lectura de respuesta
        .writeTimeout(55, TimeUnit.SECONDS)     // envío de datos
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(Memoria.url) //Siempre debe terminar en / la URL
        .addConverterFactory(GsonConverterFactory.create())
        .client(ok)
        .build()

    val apiService = retrofit.create(ApiService::class.java)



    private lateinit var media: MediaPlayer
    private var audioActual: Int = 0
    private lateinit var repetirAudio: ImageButton


    private var audioFile: File? = null

    private lateinit var animacion: LottieAnimationView
    private lateinit var fireworks: LottieAnimationView
    private lateinit var mensajeResultado: TextView
    private lateinit var imagen: ImageView
    private lateinit var volver: ImageButton
    private lateinit var grabar: Button
    private lateinit var oracion: TextView
    private lateinit var puntos: TextView


    var listadoImagenes = TreeMap<Int, String>()
    var listadoCorrecciones = TreeMap<Int, String>()

    val nuevo: AndroidAudioRecorder = AndroidAudioRecorder(this)
    var contexto: Context? = null

    var estado = false
    var fraseHablada = ""

    var attempts = 0;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Memoria.palabraOFrase = false
        binding = ActivityJuego3Binding.inflate(layoutInflater)
        setContentView(fragmento.root)
        fireworks = findViewById(R.id.fireworks)
        mensajeResultado = findViewById(R.id.mensajeResultado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cargarImagenes()
        audioFile = File(getExternalFilesDir(null), "audio.mp4")

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        animacion = findViewById(R.id.cara)
        animacion.playAnimation()
        animacion.loop(true)


        supportActionBar?.hide()

        imagen = findViewById(R.id.imagen)
        oracion = findViewById(R.id.oracion)
        puntos = findViewById(R.id.puntosjuego3)
        grabar = findViewById(R.id.grabar)
        audioActual = R.raw.saludojuego3
        media = MediaPlayer.create(this,R.raw.saludojuego3)
        media.start()


        contexto = this


        hideSystemUI()

        volver = findViewById(R.id.volver)
        repetirAudio = findViewById(R.id.repetirAudio)

        repetirAudio.setOnClickListener {
            if (audioActual != 0) {
                val media = MediaPlayer.create(this, audioActual)
                media.start()
            }
        }
        
        volver.setOnClickListener {
            finish()
        }

        media.setOnCompletionListener {
            GlobalScope.launch(Dispatchers.IO){
                if(bandera) {
                    withContext(Dispatchers.Main) {
                        var imagenRandom = listadoImagenes.keys.random()
                        imagen.setImageResource(imagenRandom)
                        Memoria.imagen = listadoImagenes[imagenRandom]
                        listadoImagenes.keys.remove(imagenRandom)
                    }
                    bandera = false
                }

                withContext(Dispatchers.Main) {
                    audioActual = R.raw.queesestaimagen
                    media = MediaPlayer.create(contexto, R.raw.queesestaimagen)
                    media.start()

                    media.setOnCompletionListener {
                        animaUI()

                    }
                }
            }
        }
        grabar.setOnClickListener {
            if(estado){
                nuevo.stop()
                grabar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5ba2c8"))
                grabar.text = "Grabar"
                estado = false
                if(!Memoria.palabraOFrase){
                    iniciarJuego()
                    //Memoria.palabraOFrase = true
                }else {
                    emitir(fraseHablada)
                    //Memoria.palabraOFrase = false
                }
            }else{
                nuevo.start(audioFile)
                grabar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#fd5555"))
                grabar.text = "Detener"
                estado = true
            }
        }
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
        mensajeResultado.setTextColor(android.graphics.Color.parseColor("#00C853"))
        mensajeResultado.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            mensajeResultado.visibility = View.GONE
        }, 2000)
    }
}

fun mostrarIncorrecto() {
    runOnUiThread {
        mensajeResultado.text = "Intenta otra vez"
        mensajeResultado.setTextColor(android.graphics.Color.RED)
        mensajeResultado.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            mensajeResultado.visibility = View.GONE
        }, 2000)
    }
}



    fun animaUI(){
        imagen.visibility = View.VISIBLE
        oracion.visibility = View.INVISIBLE
        oracion.text = ""
        val cara = findViewById<LottieAnimationView>(R.id.cara)
        val dibujo = findViewById<ImageView>(R.id.imagen)

        val parent = cara.parent as ConstraintLayout
        val screenWidth = parent.width // Get total screen width

        val startWidthCara = cara.width
        val endWidthCara = screenWidth / 2

        val startWidthDibujo = dibujo.width
        val endWidthDibujo = screenWidth / 2

        // Animate width change
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000 // 1 second animation
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float

                // Update cara width
                val paramsCara = cara.layoutParams as ConstraintLayout.LayoutParams
                paramsCara.width = (startWidthCara + (endWidthCara - startWidthCara) * progress).toInt()
                cara.layoutParams = paramsCara

                // Update dibujo width
                val paramsDibujo = dibujo.layoutParams as ConstraintLayout.LayoutParams
                paramsDibujo.width = (startWidthDibujo + (endWidthDibujo - startWidthDibujo) * progress).toInt()
                dibujo.layoutParams = paramsDibujo
            }
        }
        animator.start()
    }

    fun animaOracionUI(){
        val cara = findViewById<LottieAnimationView>(R.id.cara)
        val dibujo = findViewById<TextView>(R.id.oracion)

        val parent = cara.parent as ConstraintLayout
        val screenWidth = parent.width // Get total screen width

        val startWidthCara = cara.width
        val endWidthCara = screenWidth / 2

        val startWidthDibujo = dibujo.width
        val endWidthDibujo = screenWidth / 2

        // Animate width change
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000 // 1 second animation
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float

                // Update cara width
                val paramsCara = cara.layoutParams as ConstraintLayout.LayoutParams
                paramsCara.width = (startWidthCara + (endWidthCara - startWidthCara) * progress).toInt()
                cara.layoutParams = paramsCara

                // Update dibujo width
                val paramsDibujo = dibujo.layoutParams as ConstraintLayout.LayoutParams
                paramsDibujo.width = (startWidthDibujo + (endWidthDibujo - startWidthDibujo) * progress).toInt()
                dibujo.layoutParams = paramsDibujo
            }
        }
        animator.start()
    }

    private var bandera = true
    private var vista: Boolean = true

    fun iniciarJuego() {
        if (!vista) return

        val requestBody = audioFile?.asRequestBody("video/mp4".toMediaTypeOrNull())
        if (requestBody == null) {
            Log.e("API", "Archivo de audio nulo")
            return
        }

        val multipartBody = MultipartBody.Part.createFormData("file", audioFile?.name, requestBody)
        val call = apiService.process_audio(multipartBody)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val respuesta = response.body()?.string()
                    val resp = respuesta?.replace("nio", "ño")?.replace("nia", "ña")
                    Log.d("API", "Respuesta: $resp")

                    runOnUiThread {
                        val cara = findViewById<LottieAnimationView>(R.id.cara)
                        val dibujo = findViewById<ImageView>(R.id.imagen)
                        val parent = cara.parent as ConstraintLayout
                        val screenWidth = parent.width

                        val startWidthCara = cara.width
                        val startWidthDibujo = dibujo.width
                        val endWidthCara = screenWidth
                        val endWidthDibujo = 0

                        if (resp?.lowercase()?.contains(Memoria.imagen.lowercase()) == true) {
                            media = MediaPlayer.create(contexto, R.raw.correctolohicistebienjuegocuatro)
                            media.start()
                            mostrarFuegos()
                            mostrarCorrecto()
                            attempts = 0
                            //BluetoothManager.sendDataToHC05("7")
                            BluetoothManager.sendDataToHC05("3")
                            bandera = true

                            val restoreAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                                duration = 1000
                                addUpdateListener { animation ->
                                    val progress = animation.animatedValue as Float

                                    val paramsCara = cara.layoutParams as ConstraintLayout.LayoutParams
                                    paramsCara.width =
                                        (startWidthCara + (endWidthCara - startWidthCara) * progress).toInt()
                                    cara.layoutParams = paramsCara

                                    val paramsDibujo = dibujo.layoutParams as ConstraintLayout.LayoutParams
                                    paramsDibujo.width =
                                        (startWidthDibujo + (endWidthDibujo - startWidthDibujo) * progress).toInt()
                                    dibujo.layoutParams = paramsDibujo
                                }
                            }
                            restoreAnimator.start()
                            ollama()
                            Memoria.palabraOFrase = true
                        } else {
                            attempts++
                            if (attempts == 4) {
                                media = MediaPlayer.create(contexto, R.raw.maestracorrige2)
                                attempts = 0
                            } else {
                                media = MediaPlayer.create(contexto, R.raw.vuelveaintentarlo)
                                mostrarIncorrecto()
                            }
                            media.start()

                            //BluetoothManager.sendDataToHC05("7")
                            BluetoothManager.sendDataToHC05("2")
                            Memoria.palabraOFrase = false
                        }
                    }

                } else {
                    runOnUiThread {
                        media = MediaPlayer.create(contexto, R.raw.hubounerror)
                        media.start()
                        Memoria.palabraOFrase = false
                    }
                }
                BluetoothManager.sendDataToHC05("6")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API", "Error de conexión: ${t.message}")
                runOnUiThread {
                    media = MediaPlayer.create(contexto, R.raw.hubounerror)
                    media.start()
                    Memoria.palabraOFrase = false
                }
            }
        })
    }



    private fun ollama() {
        media.setOnCompletionListener {
            CoroutineScope(Dispatchers.IO).launch {
                val prompt = PromptRequest("${Memoria.imagen.lowercase()}.")
                val call = apiService.sendPrompt(prompt)

                try {
                    val response = call.execute()
                    if (response.isSuccessful) {
                        val respueta = response.body()?.response

                        runOnUiThread {
                            oracion.text = respueta
                            audioActual = R.raw.leeenvozalta
                            media = MediaPlayer.create(contexto, R.raw.leeenvozalta)
                            media.start()

                            media.setOnCompletionListener {
                                imagen.visibility = View.INVISIBLE
                                oracion.visibility = View.VISIBLE
                                animaOracionUI()

                                val textoLimpio = respueta
                                    ?.replace("[^\\wáéíóúÁÉÍÓÚñÑ ]".toRegex(), "")
                                    ?.lowercase()

                                val textoSinTildes = Normalizer.normalize(textoLimpio, Normalizer.Form.NFD)
                                    .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                                    .replace("ña", "nia", ignoreCase = true)
                                    .replace("ño", "nio", ignoreCase = true)

                                fraseHablada = textoSinTildes
                            }
                        }
                    }else{

                    }
                } catch (e: Exception) {
                    Log.e("OLLAMA", "Error: ${e.message}")
                }
            }
        }
    }


    private fun normalizarTexto(texto: String): String {
    return Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .replace("[^a-zA-Z0-9 ]".toRegex(), "")
        .lowercase()
        .trim()
    }

private fun porcentajeCoincidencia(textoEsperado: String, textoReconocido: String): Double {
    val esperado = normalizarTexto(textoEsperado).split("\\s+".toRegex()).filter { it.isNotBlank() }
    val reconocido = normalizarTexto(textoReconocido).split("\\s+".toRegex()).filter { it.isNotBlank() }

    if (esperado.isEmpty()) return 0.0

    val coincidencias = esperado.count { palabra ->
        reconocido.contains(palabra)
    }

    return coincidencias.toDouble() / esperado.size.toDouble()
    }

    private fun emitir(oracion: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val requestBody = audioFile?.asRequestBody("video/mp4".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", audioFile?.name, requestBody!!)

            val call = apiService.process_audio(multipartBody)

            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val respuesta = response.body()?.string()
                    val resp = respuesta
                        ?.replace("nio", "no")
                        ?.replace("nia", "na")
                        ?.lowercase()
                        ?: ""

                        val esperado = oracion ?: ""
                        val porcentaje = porcentajeCoincidencia(esperado, resp)

                    if (porcentaje >= 0.6) {
                        attempts = 0
                        media = MediaPlayer.create(contexto, R.raw.correctomuybien)
                        media.start()

                        //BluetoothManager.sendDataToHC05("7")
                        BluetoothManager.sendDataToHC05("3")

                        runOnUiThread {
                            val cara = findViewById<LottieAnimationView>(R.id.cara)
                            val dibujo = findViewById<ImageView>(R.id.imagen)
                            val parent = cara.parent as ConstraintLayout
                            val screenWidth = parent.width

                            val startWidthCara = cara.width
                            val startWidthDibujo = dibujo.width
                            val endWidthCara = screenWidth
                            val endWidthDibujo = 0

                            val restoreAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                                duration = 1000
                                addUpdateListener { animation ->
                                    val progress = animation.animatedValue as Float

                                    val paramsCara = cara.layoutParams as ConstraintLayout.LayoutParams
                                    paramsCara.width = (startWidthCara + (endWidthCara - startWidthCara) * progress).toInt()
                                    cara.layoutParams = paramsCara

                                    val paramsDibujo = dibujo.layoutParams as ConstraintLayout.LayoutParams
                                    paramsDibujo.width = (startWidthDibujo + (endWidthDibujo - startWidthDibujo) * progress).toInt()
                                    dibujo.layoutParams = paramsDibujo
                                }
                            }
                            restoreAnimator.start()

                            Memoria.puntosJuego3 += 1
                            puntos.text = Memoria.puntosJuego3.toString()
                        }

                        Memoria.palabraOFrase = false

                        media.setOnCompletionListener {
                            Thread.sleep(1000)
                            GlobalScope.launch(Dispatchers.IO) {
                                if (bandera) {
                                    runOnUiThread {
                                        val imagenRandom = listadoImagenes.keys.random()
                                        imagen.setImageResource(imagenRandom)
                                        Memoria.imagen = listadoImagenes[imagenRandom]!!
                                        listadoImagenes.remove(imagenRandom)
                                    }
                                    bandera = false
                                }

                                runOnUiThread {
                                    media = MediaPlayer.create(contexto, R.raw.queesestaimagen)
                                    media.start()
                                    media.setOnCompletionListener {
                                        animaUI()
                                    }
                                }
                            }
                        }

                    } else {
                        attempts++
                        media = if (attempts == 4) {
                            attempts = 0
                            MediaPlayer.create(contexto, R.raw.maestracorrige3)
                        } else {
                            mostrarIncorrecto()
                            MediaPlayer.create(contexto, R.raw.nopvuelveaintentarlo)
                            
                        }
                        media.start()

                        //BluetoothManager.sendDataToHC05("7")
                        BluetoothManager.sendDataToHC05("2")

                        Memoria.palabraOFrase = true
                    }

                    BluetoothManager.sendDataToHC05("6")
                }
            } catch (e: Exception) {
                Log.e("EMITIR", "Error: ${e.message}")
            }
        }
    }


    override fun onPause() {
        super.onPause()
        vista = false
    }

    override fun onStop() {
        super.onStop()
        media.stop()
    }

    private fun cargarImagenes(){
        when(Memoria.numeroCategoria){
            0 ->{
                listadoImagenes[R.raw.finalfoca] = "Foca"
                listadoImagenes[R.raw.finalhipopotamo] = "Hipopotamo"
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
                listadoImagenes[R.raw.finalcamion] = "Camion"
                listadoImagenes[R.raw.finalautobus] = "Autobus"
                listadoImagenes[R.raw.finalbus] = "Bus"
                listadoImagenes[R.raw.finalbicicleta] = "Bicicleta"
                listadoImagenes[R.raw.finalambulancia] = "Ambulancia"
                listadoImagenes[R.raw.finaltranvia] = "Tranvia"
                listadoImagenes[R.raw.finaltren] = "Tren"
                listadoImagenes[R.raw.finaltractor] = "Tractor"
                listadoImagenes[R.raw.finaltaxi] = "Taxi"
                listadoImagenes[R.raw.finaltanque] = "Tanque"
                listadoImagenes[R.raw.finalcamiondebomberos] = "Camion de bomberos"
                listadoImagenes[R.raw.finalcamiondelabasura] = "Camion de la basura"
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
                listadoImagenes[R.raw.finalcanguil] = "Palomitas de maiz"
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
                listadoImagenes[R.raw.finalpasta] = "Pasta de dientes"
                listadoImagenes[R.raw.finallavarlasmanos] = "Lavar las manos"
                listadoImagenes[R.raw.finallavarselacara] = "Lavarse la cara"
                listadoImagenes[R.raw.finaljabon] = "Jabon"
                listadoImagenes[R.raw.finalenjuagarlaboca] = "Enjuagar la boca"
                listadoImagenes[R.raw.finalcortarlasunas] = "Cortar las unias"
                listadoImagenes[R.raw.finalcepillodecabello] = "Cepillo de cabello"
                listadoImagenes[R.raw.finalcepillo] = "Cepillo de dientes"
                listadoImagenes[R.raw.finalbanar] = "Baniar"
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
                listadoImagenes[R.raw.finalpsicologo] = "Psicologo"
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





    @RequiresApi(value = 24)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window,
            window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())

            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

data class PromptRequest(val prompt: String)
data class ResponseText(val response: String)
