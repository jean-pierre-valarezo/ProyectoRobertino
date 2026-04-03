package ups.logic.robbyapp;

import static ups.logic.robbyapp.configuraciones.SecureStorage.getSecureString;

import android.content.Context;

import java.util.TreeMap;

public class Memoria {
    public static String url;

    public static void inicializarUrl(Context context) {
    String savedUrl = getSecureString(context, "url");

    if (savedUrl == null) {
        url = "";
        return;
    }

    savedUrl = savedUrl.trim();

    if (savedUrl.isEmpty()) {
        url = "";
        return;
    }

    if (!savedUrl.endsWith("/")) {
        savedUrl = savedUrl + "/";
    }

    url = savedUrl;
    }
    
    public static Boolean edad = false;
    public static int numeroJuego = 0;

    //Juego 1
    public static int numeroCategoria = 0;
    public static String variable;
    public static int audioMemoria = 0;
    public static int adivinanza = 0;
    public static int puntosJuego1 = 0;
    public static int seleccion = 0;
    //Juego 2
    public static int songJuego2 = 0;
    public static int audioMemoriaJuego2 = 0;
    public static String memoriaAdivinanza;

    //Juego 3
    public static String imagen;
    public static int imagenMemoria = 0;
    public static int puntosJuego3 = 0;
    public static Boolean palabraOFrase = false;
    //Juego 4

    public static int esc;
    public static int  audioJuego4;
    public static String numeroOLetraJuego4;
    public static String letra = "";
    public static Boolean inicioJuego4 = false;
    public static TreeMap<String, String> palabras;
    //Juego 5
    public static int silbOLett;

}
