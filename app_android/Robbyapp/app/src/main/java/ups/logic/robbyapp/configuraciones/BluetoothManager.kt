package ups.logic.robbyapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.util.*

object BluetoothManager {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var hc05Device: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID Serial

    /**
     * Conectar al HC-05
     */
    @SuppressLint("MissingPermission")
    fun connectToHC05(): Boolean {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.e("BluetoothManager", "Bluetooth no disponible o desactivado")
            return false
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            if (device.name == "Robertino") {
                hc05Device = device
                return@forEach
            }
        }

        if (hc05Device == null) {
            Log.e("BluetoothManager", "Robertiño no encontrado")
            return false
        }

        return try {
            bluetoothSocket = hc05Device!!.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket!!.connect()
            outputStream = bluetoothSocket!!.outputStream
            Log.d("BluetoothManager", "Conectado a Robertiño")
            true
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error al conectar con Robertiño", e)
            false
        }
    }

    /**
     * Enviar datos al HC-05
     */
    fun sendDataToHC05(data: String): Boolean {
        return try {
            outputStream?.write(data.toByteArray())
            Log.d("BluetoothManager", "Enviado: $data")
            true
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error al enviar datos", e)
            false
        }
    }

    /**
     * Cerrar conexión
     */
    fun closeConnection() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            outputStream = null
            Log.d("BluetoothManager", "Conexión cerrada")
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error al cerrar conexión", e)
        }
    }

    /**
     * Verificar si está conectado
     */
    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }
}
