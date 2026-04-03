package ups.logic.robbyapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import okhttp3.RequestBody;
import retrofit2.converter.gson.GsonConverterFactory;
import ups.logic.robbyapp.configuraciones.ApiService;
import ups.logic.robbyapp.bluetooth.BluetoothManager;
import android.graphics.Matrix;


public class AreaDibujo extends View {

    private Handler handler = new Handler();
    private Runnable action;

    float posx = 0, posy = 0;
    Path path;
    Paint paint;
    List<Path> paths;
    List<Paint> paints;
    public static AreaDibujo area;
    public TreeMap<Integer, String> caracter;

    List<Integer> keys;
    List<String> palabras;

    int numeroRandom;
    int randomKey;
    String randomVocal;

    Random random;

    MediaPlayer media;
    Context contexto;
    List<String> pictogramas = new ArrayList<>();

    TreeMap<String, String> mapaResultados = new TreeMap<>();
    public static Juego4 actividadJuego4;
    int attempts = 0;

    public AreaDibujo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paths = new ArrayList<>();
        paints = new ArrayList<>();
        area = this;
        agregarAudios();

        contexto = context;
        random = new Random();
        new Handler(Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    @Override
                    public void run() {

                        keys = new ArrayList<>(caracter.keySet());

                        numeroRandom = random.nextInt(keys.size());

                        randomKey = keys.get(numeroRandom);
                        Memoria.audioJuego4 = randomKey;
                        Memoria.numeroOLetraJuego4 = caracter.get(randomKey);

                        media = MediaPlayer.create(context, randomKey);

                        media.start();
                    }
                },
                5500
        );

        //for(String pic: )

    }

    void cargar(){
        pictogramas.add("Abrigo");
        pictogramas.add("Abuela");
        pictogramas.add("Abuelo");
        pictogramas.add("Agua");
        pictogramas.add("Aguila");
        pictogramas.add("Amarillo");
        pictogramas.add("Anguila");
        pictogramas.add("Ardilla");
        pictogramas.add("Armadillo");
        pictogramas.add("Azul");
        pictogramas.add("Ballena");
        pictogramas.add("Bombero");
        pictogramas.add("Blanco");
        pictogramas.add("Brocoli");
        pictogramas.add("Buho");
        pictogramas.add("Buso");
        pictogramas.add("Caballo");
        pictogramas.add("Caballito de mar");
        pictogramas.add("Cabra");
        pictogramas.add("Calzoncillo");
        pictogramas.add("Uno");
        pictogramas.add("Dos");
        pictogramas.add("Enjabonar el cuerpo");
        pictogramas.add("Enjuagar la boca");
        pictogramas.add("Tres");
        pictogramas.add("Cuatro");
        pictogramas.add("Cinco");
        pictogramas.add("Seis");
        pictogramas.add("Siete");
        pictogramas.add("Ocho");
        pictogramas.add("Nueve");
        pictogramas.add("Diez");
        pictogramas.add("Camaron");
        pictogramas.add("Camello");
        pictogramas.add("Camisa");
        pictogramas.add("Camiseta");
        pictogramas.add("Cangrejo");
        pictogramas.add("Canguro");
        pictogramas.add("Canicas");
        pictogramas.add("Capucha");
        pictogramas.add("Caracol");
        pictogramas.add("Casaca");
        pictogramas.add("Cebra");
        pictogramas.add("Celeste");
        pictogramas.add("Cerdo");
        pictogramas.add("Chaleco");
        pictogramas.add("Ciervo");
        pictogramas.add("Ciguenia");
        pictogramas.add("Cocodrilo");
        pictogramas.add("Colibri");
        pictogramas.add("Condor");
        pictogramas.add("Cubo");
        pictogramas.add("Cucaracha");
        pictogramas.add("Cuervo");
        pictogramas.add("Delfin");
        pictogramas.add("Doctor");
        pictogramas.add("Erizo");
        pictogramas.add("Escarabajo");
        pictogramas.add("Escorpion");
        pictogramas.add("Estrella de mar");
        pictogramas.add("Foca");
        pictogramas.add("Gato");
        pictogramas.add("Hermana");
        pictogramas.add("Hipopotamo");
        pictogramas.add("Hormiga");
        pictogramas.add("Jirafa");
        pictogramas.add("Lavarse la cara");
        pictogramas.add("Leon");
        pictogramas.add("Lombriz");
        pictogramas.add("Mama");
        pictogramas.add("Manta");
        pictogramas.add("Medias");
        pictogramas.add("Medusa");
        pictogramas.add("Ninia");
        pictogramas.add("Ninio");
        pictogramas.add("Orca");
        pictogramas.add("Ostra");
        pictogramas.add("Oveja");
        pictogramas.add("Pantalon");
        pictogramas.add("Papa");
        pictogramas.add("Pato");
        pictogramas.add("Peluche");
        pictogramas.add("Pez");
        pictogramas.add("Pinguino");
        pictogramas.add("Pirana");
        pictogramas.add("Profesora");
        pictogramas.add("Psicologo");
        pictogramas.add("Pulpo");
        pictogramas.add("Rana");
        pictogramas.add("Raton");
        pictogramas.add("Resorte");
        pictogramas.add("Rinoceronte");
        pictogramas.add("Robot");
        pictogramas.add("Saco");
        pictogramas.add("Saltamontes");
        pictogramas.add("Tacones");
        pictogramas.add("Tarantula");
        pictogramas.add("Tia");
        pictogramas.add("Tiburon");
        pictogramas.add("Tigre");
        pictogramas.add("Tio");
        pictogramas.add("Titere");
        pictogramas.add("Tortuga");
        pictogramas.add("Trompo");
        pictogramas.add("Vaca");
        pictogramas.add("Vestido");
        pictogramas.add("Yoyo");
        pictogramas.add("Zapatos");
        pictogramas.add("Zorro");
        pictogramas.add("Flamenco");
        pictogramas.add("Gaviota");
        pictogramas.add("Gorila");
        pictogramas.add("Gris");
        pictogramas.add("Lechuza");
        pictogramas.add("Lila");
        pictogramas.add("Mariposa");
        pictogramas.add("Marron");
        pictogramas.add("Morado");
        pictogramas.add("Mosca");
        pictogramas.add("Negro");
        pictogramas.add("Pajaro");
        pictogramas.add("Paloma");
        pictogramas.add("Pavo");
        pictogramas.add("Pelicano");
        pictogramas.add("Perro");
        pictogramas.add("Rojo");
        pictogramas.add("Ropa interior");
        pictogramas.add("Rosado");
        pictogramas.add("Sonar la nariz");
        pictogramas.add("Tucan");
        pictogramas.add("Verde claro");
        pictogramas.add("Verde oscuro");
        pictogramas.add("Zancos");
        pictogramas.add("Sort");
        pictogramas.add("Durazno");
        pictogramas.add("Melon");
        pictogramas.add("Cuadron");
        pictogramas.add("Clarinete");
        pictogramas.add("Bus");
        pictogramas.add("Canguil");
        pictogramas.add("Cebolla");
        pictogramas.add("Jabon");
        pictogramas.add("Mora");
        pictogramas.add("Cebollin");
        pictogramas.add("Coche de carreras");
        pictogramas.add("Remos");
        pictogramas.add("Camion de la basura");
        pictogramas.add("Trombon");
        pictogramas.add("Tranvia");
        pictogramas.add("Cepillo de cabello");
        pictogramas.add("Tanque");
        pictogramas.add("Baniar");
        pictogramas.add("Submarino");
        pictogramas.add("Marimba");
        pictogramas.add("Avion");
        pictogramas.add("Barco pirata");
        pictogramas.add("Tren");
        pictogramas.add("Guitarra electrica");
        pictogramas.add("Maracuya");
        pictogramas.add("Lechuga");
        pictogramas.add("Xilofono");
        pictogramas.add("Ciruela");
        pictogramas.add("Lima");
        pictogramas.add("Lavar las manos");
        pictogramas.add("Chirimoya");
        pictogramas.add("Autobus");
        pictogramas.add("Saxofon");
        pictogramas.add("Bajo");
        pictogramas.add("Armonica");
        pictogramas.add("Arpa");
        pictogramas.add("Helicoptero");
        pictogramas.add("Galleta de chocolate");
        pictogramas.add("Frambuesa");
        pictogramas.add("Metro");
        pictogramas.add("Secar el cuerpo");
        pictogramas.add("Peinar");
        pictogramas.add("Feliz");
        pictogramas.add("Moto acuatica");
        pictogramas.add("Fresa");
        pictogramas.add("Enfadado");
        pictogramas.add("Ambulancia");
        pictogramas.add("Triciclo");
        pictogramas.add("Mandarina");
        pictogramas.add("Piano");
        pictogramas.add("Gomitas");
        pictogramas.add("Flauta");
        pictogramas.add("Pimiento");
        pictogramas.add("Carruaje");
        pictogramas.add("Helado");
        pictogramas.add("Kiwi");
        pictogramas.add("Pepino");
        pictogramas.add("Paracaidas");
        pictogramas.add("Barco pesquero");
        pictogramas.add("Perfumarse");
        pictogramas.add("Bongos");
        pictogramas.add("Carro");
        pictogramas.add("Manzana");
        pictogramas.add("Camion de bomberos");
        pictogramas.add("Cepillo");
        pictogramas.add("Arveja");
        pictogramas.add("Barco");
        pictogramas.add("Cohete");
        pictogramas.add("Cortarlas uñas de los pies");
        pictogramas.add("Bicicleta");
        pictogramas.add("Asustado");
        pictogramas.add("Timido");
        pictogramas.add("Chocolate");
        pictogramas.add("Motoacuatica");
        pictogramas.add("Canoa");
        pictogramas.add("Moto");
        pictogramas.add("Arandanos");
        pictogramas.add("Camion");
        pictogramas.add("Maracas");
        pictogramas.add("Tambor");
        pictogramas.add("Aburrido");
        pictogramas.add("Avioneta");
        pictogramas.add("Lancha");
        pictogramas.add("Yate");
        pictogramas.add("Naranja");
        pictogramas.add("Grua");
        pictogramas.add("Acelga");
        pictogramas.add("Granadilla");
        pictogramas.add("Acordeon");
        pictogramas.add("Hamburguesa");
        pictogramas.add("Uvas");
        pictogramas.add("Apio");
        pictogramas.add("Tomate");
        pictogramas.add("Busturistico");
        pictogramas.add("Triste");
        pictogramas.add("Papaya");
        pictogramas.add("Globo");
        pictogramas.add("Violin");
        pictogramas.add("Carro de policia");
        pictogramas.add("Pasta");
        pictogramas.add("Taxi");
        pictogramas.add("Aguacate");
        pictogramas.add("Bateria");
        pictogramas.add("Teleferico");
        pictogramas.add("Asco");
        pictogramas.add("Cortar uñas de las manos");
        pictogramas.add("Papafrita");
        pictogramas.add("Platillos");
        pictogramas.add("Pandereta");
        pictogramas.add("Trompeta");
        pictogramas.add("Pera");
        pictogramas.add("Cereza");
        pictogramas.add("Remolacha");
        pictogramas.add("Coco");
        pictogramas.add("Furgoneta");
        pictogramas.add("Triangulo");
        pictogramas.add("Guitarra");
        pictogramas.add("Coliflor");
        pictogramas.add("Grajeas");
        pictogramas.add("Pizza");
        pictogramas.add("Monopatin");
        pictogramas.add("Guineo");
        pictogramas.add("Mango");
        pictogramas.add("Berenjena");
        pictogramas.add("Espinaca");


    }

    

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int i = 0;
        for(Path trazo : paths){
            canvas.drawPath(trazo, paints.get(i++));
        }
    }

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .build();

    // Create Retrofit instance
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Memoria.url) // Always end URL with "/"
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

    // Return the API service
    ApiService apiService = retrofit.create(ApiService.class);



    int contadorVeces = 1;
    Boolean complete = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        posx = event.getX();
        posy = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //invalidate();
                paint = new Paint();
                paint.setStrokeWidth(120.0f);
                paint.setAntiAlias(true);

                Random random = new Random();
                int rojo = random.nextInt(255);
                int verde = random.nextInt(255);
                int azul = random.nextInt(255);

               // paint.setARGB(255,95,95,95);

                paint.setARGB(255,118,206,255);
                paint.setStyle(Paint.Style.STROKE);
                paints.add(paint);
                path = new Path();
                path.moveTo(posx, posy);
                paths.add(path);
                break;
            case MotionEvent.ACTION_MOVE:
                int puntosHistoricos = event.getHistorySize();
                for(int i = 0; i < puntosHistoricos; i++){
                    path.lineTo(event.getHistoricalX(i), event.getHistoricalY(i));
                }
                break;
            case MotionEvent.ACTION_UP:
                // path.lineTo(posx, posy);
                if(contadorVeces == 1){
                    contadorVeces++;
                    action = new Runnable() {
                        @Override
                        public void run() {
                            sendBitmap();
                            contadorVeces = 1;
                        }
                    };
                    handler.postDelayed(action, 2500);
                }
                //break;
        }
        invalidate();
        return true;
    }


    public void sendBitmap(){
        if (actividadJuego4 != null) {
    actividadJuego4.mostrarLoading();
    }
    Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(Color.WHITE);

    // Dibujar los trazos en negro solo para la IA
    for (Path trazo : paths) {
        Paint negro = new Paint();
        negro.setStrokeWidth(110.0f);
        negro.setAntiAlias(true);
        negro.setARGB(255, 0, 0, 0);
        negro.setStyle(Paint.Style.STROKE);
        negro.setStrokeCap(Paint.Cap.ROUND);
        negro.setStrokeJoin(Paint.Join.ROUND);

        canvas.drawPath(trazo, negro);
    }

    Bitmap bitmapRecortado = recortarBitmapAlContenido(bitmap);
    Bitmap bitmapFinal = centrarYRedimensionar(bitmapRecortado, 224);

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 100, output);



    byte[] byteArray = output.toByteArray();

    RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
    MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("image", "image.jpeg", requestBody);

    Handler mano = new Handler(Looper.getMainLooper());
    mano.postDelayed(new Runnable() {
        @Override
        public void run() {
            clean();
        }
    }, 2800);

    Call<ResponseBody> resp;

    switch (Memoria.esc){
        case 1: resp = apiService.process_numero(multipartBody); break;
        case 2: resp = apiService.process_letra(multipartBody); break;
        case 3: resp = apiService.process_vocal(multipartBody); break;
        default: apiService.process_numero(multipartBody); return;
    }

    try{
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = resp.execute();
                    if (response.isSuccessful() && response.body() != null) {
                        ResponseBody responseBody = (ResponseBody) response.body();
                        String bodyString = responseBody.string().trim();

                        String respuesta = bodyString
                                .replace("\"", "")
                                .replace("\n", "")
                                .replace("\r", "")
                                .trim()
                                .toLowerCase();

                        String esperado = Memoria.numeroOLetraJuego4
                                .trim()
                                .toLowerCase();

                        Log.e("Respuesta: ", bodyString);
                        Log.e("Respuesta limpia: ", respuesta);
                        Log.e("Esperado: ", esperado);

                        if (respuesta.contains("naida")) {
                            media = MediaPlayer.create(contexto, R.raw.incorrectoescribebien);
                            media.start();

                            if (actividadJuego4 != null) {
                                actividadJuego4.mostrarIncorrecto();
                                actividadJuego4.ocultarLoading();
                            }

                            media.setOnCompletionListener(
                                    mediaPlayer -> {
                                        if(!Memoria.inicioJuego4){
                                            media = MediaPlayer.create(contexto, Memoria.audioJuego4);
                                            media.start();
                                        }
                                    }
                            );
                            return;
                        }

                       boolean acierto;

                            if (Memoria.esc == 3) {
                                String vocalDetectada = extraerVocal(respuesta);
                                Log.e("Vocal detectada: ", vocalDetectada);
                                acierto = vocalDetectada.equals(esperado);
                            } else {
                                    acierto = (esNumero(respuesta) && respuesta.equals(esperado)) || respuesta.equals(esperado);
                                }

                            if (acierto) {

                            media = MediaPlayer.create(contexto, R.raw.correctolohicistebienjuegocuatro);
                            media.start();

                            if (actividadJuego4 != null) {
                                actividadJuego4.mostrarFuegos();
                                actividadJuego4.mostrarCorrecto();
                                actividadJuego4.ocultarLoading();

                            }

                            attempts = 0;
                            BluetoothManager.INSTANCE.sendDataToHC05("7");
                            BluetoothManager.INSTANCE.sendDataToHC05("3");
                            caracter.remove(Memoria.audioJuego4);

                            if(!Memoria.inicioJuego4 && !caracter.isEmpty()){
                                new Handler(Looper.getMainLooper()).postDelayed(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                keys = new ArrayList<>(caracter.keySet());
                                                numeroRandom = random.nextInt(keys.size());
                                                randomKey = keys.get(numeroRandom);
                                                Memoria.audioJuego4 = randomKey;
                                                Memoria.numeroOLetraJuego4 = caracter.get(randomKey);
                                                media = MediaPlayer.create(contexto, randomKey);
                                                media.start();
                                            }
                                        },
                                        4000
                                );
                            } else {
                                Memoria.inicioJuego4 = true;
                                media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        if(!complete){
                                            media = MediaPlayer.create(contexto, R.raw.completalapalabra);
                                            media.start();
                                            complete = true;
                                            Memoria.inicioJuego4 = true;
                                        } else {
                                            media = MediaPlayer.create(contexto, R.raw.siguientepalabra);
                                            media.start();
                                            Memoria.inicioJuego4 = true;
                                        }

                                        palabras = new ArrayList<>(Memoria.palabras.keySet());
                                        numeroRandom = random.nextInt(palabras.size());
                                        randomVocal = palabras.get(numeroRandom);
                                        Memoria.letra = randomVocal;
                                        Memoria.numeroOLetraJuego4 = Memoria.palabras.get(randomVocal);
                                        actividadJuego4.palabra.setText(Memoria.letra);
                                    }
                                });
                            }

                            BluetoothManager.INSTANCE.sendDataToHC05("6");

                        } 
                        else {
                            attempts++;

                            if (actividadJuego4 != null) {
                                actividadJuego4.mostrarIncorrecto();
                                actividadJuego4.ocultarLoading();
                            }


                            BluetoothManager.INSTANCE.sendDataToHC05("7");
                            BluetoothManager.INSTANCE.sendDataToHC05("2");

                            if(attempts == 3){
                                media = MediaPlayer.create(contexto, R.raw.maestracorrige);
                                media.start();
                                attempts = 0;
                            } else {
                                if(Memoria.esc == 1){
                                    media = MediaPlayer.create(contexto, R.raw.numeroincorrecto);
                                    media.start();
                                } else if(Memoria.esc == 2){
                                    media = MediaPlayer.create(contexto, R.raw.letraincorrecta);
                                    media.start();
                                } else {
                                    media = MediaPlayer.create(contexto, R.raw.vocalincorrecta);
                                    media.start();
                                }
                            }

                            media.setOnCompletionListener(
                                    mediaPlayer -> {
                                        if(!Memoria.inicioJuego4){
                                            media = MediaPlayer.create(contexto, Memoria.audioJuego4);
                                            media.start();
                                        }
                                    }
                            );
                            BluetoothManager.INSTANCE.sendDataToHC05("6");
                        }

                    } else {
                        Log.e("API", "Error en la respuesta: " + response.message());

                        if (actividadJuego4 != null) {
                            actividadJuego4.mostrarIncorrecto();
                            actividadJuego4.ocultarLoading();
                        }

                        media = MediaPlayer.create(contexto, R.raw.hubounerror);
                        media.start();
                    }
                } catch (Exception e) {
                    Log.e("API", "Fallo en la conexión: " + e.getMessage());

                    if (actividadJuego4 != null) {
                        actividadJuego4.mostrarIncorrecto();
                        actividadJuego4.ocultarLoading();
                    }

                    media = MediaPlayer.create(contexto, R.raw.hubounerror);
                    media.start();
                }
            }
        }).start();

    } catch (Exception e){
        Log.e("Error", e.getMessage());
    }
}  

    private Bitmap recortarBitmapAlContenido(Bitmap original) {
    int width = original.getWidth();
    int height = original.getHeight();

    int minX = width;
    int minY = height;
    int maxX = -1;
    int maxY = -1;

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int pixel = original.getPixel(x, y);

            if (pixel != Color.WHITE) {
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }
    }

    if (maxX == -1 || maxY == -1) {
        return original;
    }

    int padding = 30;

    minX = Math.max(minX - padding, 0);
    minY = Math.max(minY - padding, 0);
    maxX = Math.min(maxX + padding, width - 1);
    maxY = Math.min(maxY + padding, height - 1);

    return Bitmap.createBitmap(
            original,
            minX,
            minY,
            maxX - minX + 1,
            maxY - minY + 1
    );
}


    private Bitmap centrarYRedimensionar(Bitmap original, int size) {
    Bitmap fondo = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(fondo);
    canvas.drawColor(Color.WHITE);

    int width = original.getWidth();
    int height = original.getHeight();

    float scale = Math.min((float)(size - 40) / width, (float)(size - 40) / height);

    int nuevoAncho = Math.round(width * scale);
    int nuevoAlto = Math.round(height * scale);

    Bitmap escalado = Bitmap.createScaledBitmap(original, nuevoAncho, nuevoAlto, true);

    int left = (size - nuevoAncho) / 2;
    int top = (size - nuevoAlto) / 2;

    canvas.drawBitmap(escalado, left, top, null);

    return fondo;
}


    public static boolean esNumero(String valor) {
        try {
            Integer.parseInt(valor); // Intenta convertirlo a un número entero
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


   public static String extraerVocal(String valor) {
    if (valor == null) return "";

    String limpio = valor
            .replace("\"", "")
            .replace("\n", "")
            .replace("\r", "")
            .trim()
            .toLowerCase();

        if (limpio.equals("a") || limpio.contains(" a ")) return "a";
        if (limpio.equals("e") || limpio.contains(" e ")) return "e";
        if (limpio.equals("i") || limpio.contains(" i ")) return "i";
        if (limpio.equals("o") || limpio.contains(" o ")) return "o";
        if (limpio.equals("u") || limpio.contains(" u ")) return "u";

    return limpio;
    }


    public void agregarAudios(){
        caracter = new TreeMap<>();
//        numeros.put(R.raw.escribeelnumerouno, "1");
//        numeros.put(R.raw.escribeelnumerodos, "2");
//        numeros.put(R.raw.escribeelnumerotres, "3");
//        numeros.put(R.raw.escribeelnumerocuatro, "4");
//        numeros.put(R.raw.escribeelnumerocinco, "5");
//        numeros.put(R.raw.escribeelnumeroseis, "6");
//        numeros.put(R.raw.escribeelnumerosiete, "7");
//        numeros.put(R.raw.escribeelnumeroocho, "8");
//        numeros.put(R.raw.escribeelnumeronueve, "9");
        switch (Memoria.esc){
            case 1: {
                caracter.put(R.raw.escribeelnumerouno, "1");
                caracter.put(R.raw.escribeelnumerodos, "2");
                caracter.put(R.raw.escribeelnumerotres, "3");
                caracter.put(R.raw.escribeelnumerocuatro, "4");
                caracter.put(R.raw.escribeelnumerocinco, "5");
                caracter.put(R.raw.escribeelnumeroseis, "6");
                caracter.put(R.raw.escribeelnumerosiete, "7");
                caracter.put(R.raw.escribeelnumeroocho, "8");
                caracter.put(R.raw.escribeelnumeronueve, "9");
            }
                break;
                /* 
            case 2: {
                
                caracter.put(R.raw.escribelaletrab, "B");
               caracter.put(R.raw.escribelaletrac, "C");
               caracter.put(R.raw.escribelaletrad, "D");
               caracter.put(R.raw.escribelaletraf, "F");
              caracter.put(R.raw.escribelaletrag, "G");
              caracter.put(R.raw.escribelaletrah, "H");
              caracter.put(R.raw.escribelaletraj, "J");
              caracter.put(R.raw.escribelaletrak, "K");
               caracter.put(R.raw.escribelaletral, "L");
               caracter.put(R.raw.escribelaletram, "M");
              caracter.put(R.raw.escribelaletran, "N");
              caracter.put(R.raw.escribelaletrap, "P");
              caracter.put(R.raw.escribelaletraq, "Q");
              caracter.put(R.raw.escribelaletrar, "R");
              caracter.put(R.raw.escribelaletras, "S");
                caracter.put(R.raw.escribelaletrat, "T");
                caracter.put(R.raw.escribelaletrav, "V");
                caracter.put(R.raw.escribelaletraw, "W");
              caracter.put(R.raw.escribelaletrax, "X");
               caracter.put(R.raw.escribelaletray, "Y");
               caracter.put(R.raw.escribelaletraz, "Z");
            }
               
                break;
                */
            
            case 3:
            { caracter.put(R.raw.escribelavocala, "A");
                caracter.put(R.raw.escribelavocale, "E");
                caracter.put(R.raw.escribelavocali, "I");
                caracter.put(R.raw.escribelavocalo, "O");
                caracter.put(R.raw.escribelavocalu, "U"); }
                break;
        }
        

    }




    public void clean(){
        paths.clear();
        paints.clear();
        invalidate();

    }
}