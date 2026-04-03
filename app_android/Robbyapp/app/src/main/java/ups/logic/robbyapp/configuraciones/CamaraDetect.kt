package ups.logic.robbyapp.configuraciones

class CameraDetect {
    private var expectedValue: String? = null
    private var lastReceivedValue: String? = null
    private var correctCount = 0
    private var incorrectCount = 0
    private val threshold = 10

    fun setExpectedValue(value: String) {
        expectedValue = value
        resetCounters()
    }

    private fun resetCounters() {
        correctCount = 0
        incorrectCount = 0
        lastReceivedValue = null
    }

    fun processResponse(receivedValue: String) {
        if (receivedValue == lastReceivedValue) {
            // Si la respuesta es la misma que la última recibida, aumentamos los contadores
            if (receivedValue == expectedValue) {
                correctCount++
                incorrectCount = 0  // Reiniciar el incorrecto porque sigue correcto
            } else {
                incorrectCount++
                correctCount = 0  // Reiniciar el correcto porque es incorrecto
            }
        } else {
            // Si la respuesta cambia, reseteamos los contadores
            resetCounters()
            if (receivedValue == expectedValue) {
                correctCount++
            } else {
                incorrectCount++
            }
        }

        lastReceivedValue = receivedValue

        // Verificamos si se alcanzó el umbral
        when {
            correctCount >= threshold -> {
                //println("Correcto: $expectedValue")
                resetCounters()
            }
            incorrectCount >= threshold -> {

                println("Incorrecto: Se esperaba $expectedValue, pero se recibió $receivedValue")
                resetCounters()
            }
        }
    }
}
