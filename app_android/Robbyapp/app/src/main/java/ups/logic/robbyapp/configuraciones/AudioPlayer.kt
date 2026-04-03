package ups.logic.robbyapp.configuraciones

import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
}