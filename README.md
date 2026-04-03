# 🤖 Robertiño v3.0

Robot educativo/terapéutico basado en inteligencia artificial, visión por computador y reconocimiento de voz, integrado con una aplicación Android y un robot físico controlado por Bluetooth.

---

##  Descripción del proyecto

Robertiño es un sistema educativo interactivo que permite a los niños aprender mediante juegos como:

- 🔍 Búsqueda  
- ❓ Adivinanzas  
- 📖 Lectura  
- ✍️ Escritura  

El sistema utiliza inteligencia artificial para reconocer imágenes, interpretar voz y generar respuestas educativas.

El celular funciona como la **cabeza del robot**, donde se ejecuta la app y se controla todo el sistema.

---

##  Arquitectura del sistema

```text
Celular (App Android)
        ↓ WiFi (HTTP)
Backend IA (Python - Flask)
        ↓ respuesta
Celular
        ↓ Bluetooth
Robot (ESP32 / Arduino + HC-05)
```

📌 El celular actúa como puente entre el backend y el robot.

---

##  Tecnologías utilizadas

### Aplicación Android
- Kotlin
- Android Studio
- CameraX
- Retrofit
- MediaPlayer
- Lottie (animaciones)
- Bluetooth

### Backend
- Python
- Flask
- ONNX Runtime
- OpenCV
- YOLO (detección de personas)
- Vosk (voz a texto)
- LLM (modelo tipo Gemma)

### Robot
- Arduino / ESP32
- Bluetooth HC-05

---

##  Instalación del backend

### 1. Crear entorno virtual

```bash
python -m venv venv
```

### 2. Activar entorno

```bash
venv\Scripts\activate
```

### 3. Instalar dependencias

```bash
pip install -r requirements.txt
```

### 4. Ejecutar backend

```bash
python app.py
```

Si todo está correcto, aparecerá:

```text
Running on http://0.0.0.0:5000
```

---

##  Uso del entorno virtual

En caso de:

- clonar el proyecto  
- borrar el entorno virtual  
- usar otra máquina  

solo ejecutar:

```bash
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
```

---

##  Conexión del sistema

### 🔹 App ↔ Backend

- Conexión por WiFi mediante HTTP  
- Ejemplo:

```text
http://192.168.1.10:5000/
```

Flujo:

```text
Celular → WiFi → Backend → Respuesta
```

---

### 🔹 App ↔ Robot

- Conexión mediante Bluetooth  
- Dispositivo: **Robertino**  
- Comunicación serial mediante módulo HC-05  

---

##  Pruebas del backend

Se pueden probar endpoints con herramientas como:

- Bruno  
- Postman  

Ejemplo:

```text
http://10.131.186.2:5000/word
```

---

## ⚠️ Observaciones del proyecto

- No utiliza base de datos (manejo en memoria)  
- No usa gráficos 3D (para compatibilidad con dispositivos de bajos recursos)  
- La app es nativa en Android (no usa Godot)  
- El backend actúa como el “cerebro” del sistema  
- El robot solo ejecuta comandos físicos  

---

##  Cambios realizados (versión 3.0)

### Backend
- Corrección de dependencias faltantes  
- Creación de `requirements.txt`  
- Corrección de procesamiento de imágenes (RGB vs escala de grises)  
- Optimización de YOLO (640 → 320)  
- Exportación optimizada a ONNX  

### App Android
- Corrección de errores de navegación  
- Mejora de estabilidad  
- Validación de conexión a internet  
- Compatibilidad con Android 5.0 y gama media  
- Corrección de errores en almacenamiento de IP  

### Experiencia de usuario
- Animaciones (fade, carga, fuegos artificiales)  
- Vibración táctil  
- Efectos en botones  
- Mensajes visuales  
- Botón para repetir audio  
- Rotación de pantalla  

### Diseño
- Nuevo logotipo  
- Splash screen  
- Nuevas fuentes  
- Actualización a versión **3.0**  

---

##  Funcionamiento del sistema

1. El usuario interactúa con la app  
2. La app captura imagen o audio  
3. Envía datos al backend  
4. El backend procesa con IA  
5. Devuelve una respuesta  
6. La app interpreta el resultado  
7. Envía comandos por Bluetooth  
8. El robot ejecuta movimientos  

---

##  Conclusión

El sistema implementa una arquitectura donde:

- El backend procesa la IA  
- El celular conecta todo  
- El robot ejecuta acciones  

Se logró una versión estable, optimizada y funcional (**v3.0**).



---


## Modelos y archivos pesados

Los modelos de IA y otros archivos pesados no están incluidos en este repositorio.

Descarga:
[]


