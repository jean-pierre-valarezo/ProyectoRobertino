package ups.logic.robbyapp

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import ups.logic.robbyapp.bluetooth.BluetoothManager
import ups.logic.robbyapp.databinding.ActivityJuego4Binding
import ups.logic.robbyapp.databinding.ActivitySyllablesBinding
import java.util.TreeMap
import kotlin.random.Random
import android.os.Handler
import android.os.Looper
import android.view.View
import android.speech.tts.TextToSpeech
import java.util.Locale
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator


class ActivitySyllables : AppCompatActivity() {


    private lateinit var volver: ImageButton
    private var binding: ActivitySyllablesBinding? = null

    private lateinit var palabraTexto: TextView
    private lateinit var botones: List<Button>

    private lateinit var media: MediaPlayer

    private lateinit var caracter: TreeMap<Int, String>
    private lateinit var letraCorrecta: String
    private lateinit var animacion: LottieAnimationView
    private lateinit var fireworks: LottieAnimationView

    private lateinit var mensajeResultado: TextView

    private lateinit var repetirAudio: ImageButton
    private var audioActual: Int = 0

    private var palabras = listOf(
        Pair("granadilla", "na"),
        Pair("granadilla", "di"),
        Pair("granadilla", "lla"),
        Pair("perro", "pe"),
        Pair("perro", "rro"),
        Pair("zapato", "za"),
        Pair("zapato", "pa"),
        Pair("zapato", "to"),
        Pair("manzana", "man"),
        Pair("manzana", "za"),
        Pair("manzana", "na"),
        Pair("tomate", "to"),
        Pair("tomate", "ma"),
        Pair("tomate", "te"),
        Pair("caballo", "ca"),
        Pair("caballo", "ba"),
        Pair("caballo", "llo"),
        Pair("camisa", "ca"),
        Pair("camisa", "mi"),
        Pair("camisa", "sa"),
        Pair("banana", "ba"),
        Pair("banana", "na"),
        Pair("banana", "na"), // Repetido a propósito para práctica
        Pair("jirafa", "ji"),
        Pair("jirafa", "ra"),
        Pair("jirafa", "fa"),
        Pair("cocodrilo", "co"),
        Pair("cocodrilo", "dri"),
        Pair("cocodrilo", "lo"),
        Pair("limonada", "li"),
        Pair("limonada", "mo"),
        Pair("limonada", "na"),
        Pair("camello", "ca"),
        Pair("camello", "me"),
        Pair("camello", "llo"),
        Pair("hipopotamo", "hipo"),
        Pair("hipopotamo", "po"),
        Pair("hipopotamo", "ta"),
        Pair("pirana", "pi"),
        Pair("pirana", "ra"),
        Pair("pirana", "ña"),
        Pair("tortuga", "tor"),
        Pair("tortuga", "tu"),
        Pair("tortuga", "ga"),
        Pair("mariposa", "ma"),
        Pair("mariposa", "ri"),
        Pair("mariposa", "po"),
        Pair("peluche", "pe"),
        Pair("peluche", "lu"),
        Pair("peluche", "che"),
        Pair("pantera", "pan"),
        Pair("pantera", "te"),
        Pair("pantera", "ra"),
        Pair("flamenco", "fla"),
        Pair("flamenco", "men"),
        Pair("flamenco", "co"),
        Pair("caracol", "ca"),
        Pair("caracol", "ra"),
        Pair("caracol", "col"),
        Pair("rosado", "ro"),
        Pair("rosado", "sa"),
        Pair("rosado", "do"),
        Pair("brocoli", "bro"),
        Pair("brocoli", "co"),
        Pair("brocoli", "li"),
        Pair("pajaro", "pa"),
        Pair("pajaro", "ja"),
        Pair("pajaro", "ro"),
        Pair("guitarra", "gui"),
        Pair("guitarra", "ta"),
        Pair("guitarra", "rra"),
        Pair("fresa", "fre"),
        Pair("fresa", "sa"),
        Pair("silla", "si"),
        Pair("silla", "lla"),

        Pair("flamenco", "fla"),
        Pair("flamenco", "men"),
        Pair("flamenco", "co"),

        Pair("pelota", "pe"),
        Pair("pelota", "lo"),
        Pair("pelota", "ta"),

        Pair("camion", "ca"),
        Pair("camion", "mion"),
        Pair("camion", "on"),

        Pair("rosado", "ro"),
        Pair("rosado", "sa"),
        Pair("rosado", "do"),

        Pair("pantera", "pan"),
        Pair("pantera", "te"),
        Pair("pantera", "ra"),

        Pair("tortuga", "tor"),
        Pair("tortuga", "tu"),
        Pair("tortuga", "ga"),

        Pair("cebollin", "ce"),
        Pair("cebollin", "bo"),
        Pair("cebollin", "llin"),

        Pair("tomate", "to"),
        Pair("tomate", "ma"),
        Pair("tomate", "te"),

        Pair("brocoli", "bro"),
        Pair("brocoli", "co"),
        Pair("brocoli", "li"),

        Pair("caracol", "ca"),
        Pair("caracol", "ra"),
        Pair("caracol", "col"),

        Pair("pajaro", "pa"),
        Pair("pajaro", "ja"),
        Pair("pajaro", "ro"),

        Pair("canguro", "can"),
        Pair("canguro", "gu"),
        Pair("canguro", "ro"),

        Pair("trompeta", "trom"),
        Pair("trompeta", "pe"),
        Pair("trompeta", "ta"),

        Pair("mariposa", "ma"),
        Pair("mariposa", "ri"),
        Pair("mariposa", "po"),
        Pair("mariposa", "sa"),

        Pair("chirimoya", "chi"),
        Pair("chirimoya", "mo"),
        Pair("chirimoya", "ya"),

        Pair("abeja", "a"),
        Pair("abeja", "be"),
        Pair("abeja", "ja"),

        Pair("cigueña", "ci"),
        Pair("cigueña", "gue"),
        Pair("cigueña", "ña"),

        Pair("armonica", "ar"),
        Pair("armonica", "mo"),
        Pair("armonica", "ca"),

        Pair("bateria", "ba"),
        Pair("bateria", "te"),
        Pair("bateria", "ria"),

        Pair("salchicha", "sal"),
        Pair("salchicha", "chi"),
        Pair("salchicha", "cha"),

        Pair("langosta", "lan"),
        Pair("langosta", "gos"),
        Pair("langosta", "ta"),

        Pair("jirafa", "ji"),
        Pair("jirafa", "ra"),
        Pair("jirafa", "fa"),

        Pair("caballito", "ca"),
        Pair("caballito", "ba"),
        Pair("caballito", "llito"),

        Pair("cerveza", "cer"),
        Pair("cerveza", "ve"),
        Pair("cerveza", "za")
    )


    private var intentosFallidos = 0
    private var palabraActual = Pair("", "")
    private val random = Random



    private fun vibrarError() {
    val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(200)
    }
}
    private val fragmento
        get() = binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySyllablesBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_syllables)
        setContentView(fragmento.root)
        repetirAudio = findViewById(R.id.repetirAudio)
        mensajeResultado = findViewById(R.id.mensajeResultado)

        repetirAudio.setOnClickListener {
    if (audioActual != 0) {
        media = MediaPlayer.create(this, audioActual)
        media.start()
        }
    }   
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        supportActionBar?.hide()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        hideSystemUI()

        palabraTexto = findViewById(R.id.palabraTexto)
        botones = listOf(findViewById(R.id.opcion1), findViewById(R.id.opcion2), findViewById(R.id.opcion3), findViewById(R.id.opcion4))

        animacion = findViewById(R.id.cara)
        fireworks = findViewById(R.id.fireworks) 
        animacion.playAnimation()
        animacion.loop(true)

        volver = findViewById(R.id.volver)
        volver.setOnClickListener {
            finish()
        }
        loadAudios()
        if(Memoria.silbOLett == 1){
            media = MediaPlayer.create(this, R.raw.silabafaltante)
            media.start()
            audioActual = R.raw.silabafaltante

            siguientePalabra()
        }else{
            palabraTexto.isVisible = false
            media = MediaPlayer.create(this, R.raw.seleccionaletra)
            media.start()
            media.setOnCompletionListener {
                jugarConLetras()
            }
        }
    }

    private fun mostrarFuegos() {
    runOnUiThread {
        fireworks.cancelAnimation()
        fireworks.visibility = View.VISIBLE
        fireworks.alpha = 1f
        fireworks.progress = 0f
        fireworks.repeatCount = 0
        fireworks.bringToFront()
        fireworks.translationZ = 100f
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

    private fun siguientePalabra() {
        intentosFallidos = 0
        palabraActual = palabras[random.nextInt(palabras.size)]
        val (palabra, silabaCorrecta) = palabraActual

        // Oculta la sílaba en el texto
        val textoMostrar = palabra.replace(silabaCorrecta, "__", ignoreCase = true)
        palabraTexto.text = textoMostrar

        // Generar opciones
        val opciones = mutableListOf(silabaCorrecta)
        while (opciones.size < 4) {
            val silabaAleatoria = ('a'..'z').random().toString() + ('a'..'z').random()
            if (silabaAleatoria !in opciones) opciones.add(silabaAleatoria)
        }
        opciones.shuffle()

        botones.forEachIndexed { index, button ->
            button.text = opciones[index]
            button.setOnClickListener {
                verificarRespuesta(opciones[index])
                
            }
        }
    }


    private fun verificarRespuesta(seleccion: String) {
        val correcta = palabraActual.second
        if (seleccion.equals(correcta, ignoreCase = true)) {
            media = MediaPlayer.create(this, R.raw.correctolohicistebienjuegocuatro)
            media.start()
            mostrarFuegos()

            BluetoothManager.sendDataToHC05("7")
            BluetoothManager.sendDataToHC05("3") //correcto

            //Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show()

            media.setOnCompletionListener {
                siguientePalabra()
                BluetoothManager.sendDataToHC05("6");
            }
        } else {
            vibrarError()
            media = MediaPlayer.create(this, R.raw.vuelveaintentarlo)
            media.start()

            BluetoothManager.sendDataToHC05("7");
            BluetoothManager.sendDataToHC05("2") // incorrecto

            intentosFallidos++
            if (intentosFallidos >= 3) {

                media.setOnCompletionListener {
                    siguientePalabra()
                    BluetoothManager.sendDataToHC05("6");
                }
            } else {
                Toast.makeText(this, "Incorrecto. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarLetra(seleccion: String) {
        if (seleccion.equals(letraCorrecta, ignoreCase = true)) {
            media = MediaPlayer.create(this, R.raw.correctolohicistebienjuegocuatro)
            media.start()
            mostrarFuegos()
            BluetoothManager.sendDataToHC05("7");
            BluetoothManager.sendDataToHC05("3") // correcto

            media.setOnCompletionListener {
                jugarConLetras()
                BluetoothManager.sendDataToHC05("6");
            }
        } else {
            vibrarError()
            BluetoothManager.sendDataToHC05("7");
            BluetoothManager.sendDataToHC05("2");
            media = MediaPlayer.create(this, R.raw.vuelveaintentarlo)
            media.start()
            intentosFallidos++
            if (intentosFallidos >= 2) {
                jugarConLetras()
                BluetoothManager.sendDataToHC05("6");
            }
        }
    }


    private fun jugarConLetras() {
    intentosFallidos = 0

    val claves = caracter.keys.toList()
    val audioCorrecto = claves.random()
    
    audioActual = audioCorrecto
    letraCorrecta = caracter[audioCorrecto] ?: return

    media = MediaPlayer.create(this, audioCorrecto)
    media.start()

    val opciones = mutableSetOf(letraCorrecta)
    while (opciones.size < 4) {
        val letraAleatoria = caracter.values.random()
        opciones.add(letraAleatoria)
    }

    val opcionesMezcladas = opciones.shuffled().take(4)

    botones.forEachIndexed { index, button ->
        button.text = opcionesMezcladas[index]
        button.setOnClickListener {
            verificarLetra(opcionesMezcladas[index])
        }
    }
}


    fun loadAudios(){
        caracter = TreeMap()
        caracter[R.raw.seleccionalaletrab] = "B"
        caracter[R.raw.seleccionalaletrac] = "C"
        caracter[R.raw.seleccionalaletrad] = "D"
        caracter[R.raw.seleccionalaletraf] = "F"
        caracter[R.raw.seleccionalaletrag] = "G"
        caracter[R.raw.seleccionalaletrah] = "H"
        caracter[R.raw.seleccionalaletraj] = "J"
        caracter[R.raw.seleccionalaletrak] = "K"
        caracter[R.raw.seleccionalaletral] = "L"
        caracter[R.raw.seleccionalaletram] = "M"
        caracter[R.raw.seleccionalaletran] = "N"
        caracter[R.raw.seleccionalaletrap] = "P"
        caracter[R.raw.seleccionalaletraq] = "Q"
        caracter[R.raw.seleccionalaletrar] = "R"
        caracter[R.raw.seleccionalaletras] = "S"
        caracter[R.raw.seleccionalaletrat] = "T"
        caracter[R.raw.seleccionalaletrav] = "V"
        caracter[R.raw.seleccionalaletraw] = "W"
        caracter[R.raw.seleccionalaletrax] = "X"
        caracter[R.raw.seleccionalaletray] = "Y"
        caracter[R.raw.seleccionalaletraz] = "Z"

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



