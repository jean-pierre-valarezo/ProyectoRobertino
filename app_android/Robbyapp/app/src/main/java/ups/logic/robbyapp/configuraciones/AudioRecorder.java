package ups.logic.robbyapp.configuraciones;

import java.io.File;

public interface AudioRecorder {
    public void start(File output);
    public void stop();
}
