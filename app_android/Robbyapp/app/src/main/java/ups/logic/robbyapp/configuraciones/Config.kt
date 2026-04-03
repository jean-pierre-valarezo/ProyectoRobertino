package ups.logic.robbyapp.configuraciones

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.intellij.lang.annotations.Pattern
import ups.logic.robbyapp.Memoria
import ups.logic.robbyapp.R
import ups.logic.robbyapp.bluetooth.BluetoothManager
import ups.logic.robbyapp.configuraciones.SecureStorage.saveSecureString
import ups.logic.robbyapp.databinding.ActivityConfigBinding
import java.io.OutputStream
import ups.logic.robbyapp.configuraciones.SecureStorage.getSecureString

class Config : AppCompatActivity() {

    private var binding: ActivityConfigBinding? = null

    private val REQUEST_ENABLE_BT = 1
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var hc05Device: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private lateinit var salirGuardar: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityConfigBinding.inflate(layoutInflater)

        setContentView(R.layout.activity_config)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.hide()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        hideSystemUI()

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val btnSendData = findViewById<Button>(R.id.btnSendData)
        salirGuardar = findViewById(R.id.guardar)
        val ip = findViewById<EditText>(R.id.ipCajon)

    val savedUrl = getSecureString(this, "url")
    if (!savedUrl.isNullOrEmpty()) {
        val ipGuardada = savedUrl
            .replace("http://", "")
            .replace(":5000/", "")
            .replace(":5000", "")
            .trim()

        ip.setText(ipGuardada)
    }


        btnConnect.setOnClickListener {
            if (BluetoothManager.connectToHC05()) {
                Toast.makeText(this, "Conectado a Robertiño", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se pudo conectar a Robertiño", Toast.LENGTH_SHORT).show()
            }
        }

        val regex = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\$"



        btnSendData.setOnClickListener {
            if (BluetoothManager.isConnected()) {
                //BluetoothManager.sendDataToHC05("4")
                BluetoothManager.sendDataToHC05("3")
                Toast.makeText(this, "Movimiento Enviado  :)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Robertiño no conectado", Toast.LENGTH_SHORT).show()
            }
        }


       salirGuardar.setOnClickListener {
    BluetoothManager.sendDataToHC05("6")

    val patten = java.util.regex.Pattern.compile(regex)
    val ipText = ip.text.toString().trim()

        if (ipText.isNotEmpty()) {
            if (patten.matcher(ipText).matches()) {
                val url = "http://$ipText:5000/"
                Memoria.url = url
                saveSecureString(this, "url", url)
                Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "La dirección IP está mal escrita", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Ingrese una dirección IP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(24)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(
            window,
            window.decorView.findViewById(android.R.id.content)
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }


}
