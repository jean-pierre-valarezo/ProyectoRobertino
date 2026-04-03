package ups.logic.robbyapp.configuraciones

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {

    private var recorder: MediaRecorder? = null

    private fun createRecorder():MediaRecorder{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            MediaRecorder(context)
        }else MediaRecorder()
    }

    override fun start(output: File?) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(output?.absolutePath)

            prepare()
            start()

            recorder = this
        }
    }

    override fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}

//class AndroidAudioPlayer(
//    private val context: Context
//) : AudioPlayer{
//
//    private var player: MediaPlayer?= null
//
//
//    override fun playFile(file: File) {
//        MediaPlayer.create(context, file.toUri()).apply {
//            player = this
//            start()
//        }
//    }
//
//    override fun stop() {
//        player?.stop()
//        player?.release()
//        player = null
//    }
//
//}