
import eventlet
eventlet.monkey_patch()

from flask import Flask, render_template, Response, stream_with_context, Request, request, jsonify
from io import BytesIO

import numpy as np
import requests
import time


import json
import base64
##Librerias para trabajar con procesamiento de imagenes y modelos de IA 
from io import BytesIO
import io
from PIL import Image
#from tensorflow.keras.preprocessing import image
#from ultralytics import YOLO
import os
#import tensorflow as tf
#import tflite_runtime.interpreter as tflite
#from ultralytics import YOLO

import cv2
#import tensorflow.lite as tflite
from google.cloud import speech
import moviepy.editor as mp

##Librerias para trabajar con audio
#import pyaudio
import wave
import numpy as np
from pydub import AudioSegment
from scipy.ndimage import maximum_filter1d
#import ffmpeg
import onnxruntime as ort
from flask_socketio import SocketIO, emit


app = Flask(__name__)
# IP Address
#_URL = 'http://192.168.221.62'
_URL = 'http://192.168.10.1'
# Default Streaming Port
_PORT = '8000'
# Default streaming route
_ST = '/stream'
SEP = ':'

stream_url = ''.join([_URL,SEP,_PORT,_ST])
#cocoNames = ['Color', 'ComidaChatarra', 'Fruta', 'Verdura', 'Emocion', 'Higiene', 'InstrumentoMusical', 'Juguete', 'Lugar', 'Acuático', 'Terrestre', 'Aéreo', 'Número', 'Persona', 'Prenda de Vestir']



socketio = SocketIO(app, cors_allowed_origins="*", async_mode='threading')

@socketio.on('connect')
def test_connect():
    print("Flutter conectado")
    emit('mensaje_flutter', {'data': 'Bienvenido Flutter'})



cocoNames = {
    'acuatico_animal': 'Animal Acuatico',
    'acuatico_transporte': 'Transporte Acuatico',
    'aereo_animal': 'Animal Aereo',
    'aereo_transporte': 'Transporte Aereo',
    'color': 'Color',
    'comida_chatarra': 'Comida Chatarra',
    'emocion': 'Emocion',
    'fruta': 'Fruta',
    'higiene': 'Higiene',
    'instrumento_musical': 'Instrumento Musical',
    'juguete': 'Juguete',
    'numero': 'Numero',
    'persona': 'Persona',
    'prenda_de_vestir': 'Prenda de Vestir',
    'terrestre_animal': 'Animal Terrestre',
    'terrestre_transporte': 'Transporte Terrestre',
    'verdura': 'Verdura'
}

letras = {0: '.ipynb_checkpoints', 1: 'b', 2: 'c', 3: 'd', 4: 'f',
    5: 'g', 6: 'h', 7: 'j', 8: 'k', 9: 'l',
    10: 'm', 11: 'n', 12: 'p', 13: 'q', 14: 'r',
    15: 's', 16: 't', 17: 'v', 18: 'w', 19: 'x',
    20: 'y', 21: 'z'}

vocales = {0: 'a', 1: 'e', 2: 'i', 3: 'o', 4: 'u'}

numerosesc = {0: '0', 1: '1', 2: '2', 3: '3', 4: '4', 5: '5', 6: '6', 7: '7', 8: '8', 9: '9'}
#/home/jorgelituma/Escritorio/Tesis/best_float16.tflite



dataset1 = [
    "Agua", "Aguacate", "Amarillo", "Anaranjado", "Arandanos",
    "Azul", "Baniar", "Blanco", "Canguil", "Celeste", "Cepillo",
    "Cepillo_de_cabello", "Cereza", "Chirimoya", "Chocolate",
    "Coco", "Cortar_las_unias", "Durazno", "Enjuagar_la_boca",
    "Frambuesa", "Fresa", "Galletas_de_chocolate", "Gomitas",
    "Granadilla", "Guinea", "Hamburguesa", "Helado", "Jabon",
    "Kiwi", "Lavar_las_manos", "Lavarse_la_cara", "Limon",
    "Mandarina", "Mango", "Manzana", "Maracuya", "Marron",
    "Mora", "Morado", "Naranja", "Negro", "Papas_fritas",
    "Papaya", "Pasta", "Peinar", "Pera", "Performarse",
    "Pizza", "Rojo", "Rosado", "Secar_cuerpo", "Sonar_la_nariz",
    "Uvas", "Verde_claro", "Verde_oscuro"
]

dataset2 = [
    "Aburrido", "Acelga", "Acordeon", "Aji", "Apio", "Armonica",
    "Arpa", "Arveja", "Asco", "Asustado", "Barco", "barco_pirata",
    "Bateria", "Berenjena", "Bongos", "Brocoli", "Canoa",
    "Cebolla", "Cebollín", "Clarinete", "Coliflor", "Enfadado",
    "Esparragos", "Espinaca", "Feliz", "Flauta", "Guitarra",
    "Guitarra_electrica", "Lancha", "Lechuga", "Maracas", "Marimba",
    "Moto_acualica", "Pandereta", "Pepino", "Pesquero","Piano",
    "Pimiento", "Platillos", "Remolacha", "Remos", "Saxofon",
    "Submarino", "Tambor", "Timido", "Tomate", "Triangulo",
    "Triste", "Trombon", "Trompeta", "Ver", "Violin", "Xilofono", "Yate"
]

dataset3 = [
    "Abeja", "Aguila", "Ambulancia", "Autobus", "Bicicleta",
    "Bubo", "Bus", "Bus turístico", "Camion", "Camion_de_bomberos",
    "Camion_de_la_basura", "Canicas", "Carro", "Carro_de_policia",
    "Cartuaje", "Ciguenia", "Coche_de_carreras", "Colibri",
    "Condor", "Cuadron", "Cubo", "Cuervo", "Furgoneta", "Gaviota",
    "Hamenco", "Lachuza", "Libelula", "Mariposa", "Metro",
    "Mosca", "Moto", "Pajaro", "Paloma", "Patinete electrico",
    "Pelicano", "Pelota", "Peluche", "Resorte", "Robot", "Rosa_de_los_vientos",
    "Tanque", "Taxi", "Titere", "Tractor", "Tranvía", "Tren",
    "Tricido", "Trompo", "Tucan", "Ver", "Volqueta"
]

dataset4 = [
    "Abuela", "Abuelo", "Anguila", "Ballena", "Bombero",
    "Caballito_de_mar", "Camaron", "Cangrejo", "Cinco",
    "CINCO-", "Cocodrilo", "Cuatro", "CUATRO-", "Delfín",
    "Dentista", "Diez", "Doctor", "Dos", "Dos_", "Estrella_de_mar",
    "Foca", "Hermana", "Hermano", "Hipopotamo", "Mama", "Medusa",
    "Monta", "Ninia", "Ninio", "Nueve", "NUEVE", "Ocho", "OCHO-",
    "Orca", "Ostra", "Papa", "Pato", "Pez", "Pinguino", "Pirania",
    "Profesora", "Psicologo", "Pulpo", "Rana", "Seis", "Seis_",
    "Siete", "SIETE-", "Tia", "Tiburon"
]

dataset5 = [
    "Abrigo", "Ardilla", "Armadillo", "Avion", "Avioneta",
    "Bata", "Buso", "Caballo", "Cabra", "Calzoncillo", "Camello",
    "Camisa", "Camisetas", "Canguro", "Capucha", "Caracol",
    "Casaca", "Cebra", "Cerdo", "Chaleco", "Ciervo", "Cohete",
    "Cucaracha", "Erizo", "Escarabajo", "Escorpion", "Gato",
    "Globo", "Gorila", "Helicoptero", "Hormiga", "Jirafa", "Leon",
    "Lombriz", "Medias", "Oveja", "Pantalon", "Paracaídas", "Pavo",
    "Perro", "Raton", "Rinoceronte", "Ropa_interior", "Saco",
    "Saltamontes", "Short", "tacones", "Tarantula", "Teleferico",
    "Tigre", "Vaca", "Vestido", "Zapatos", "Zorro"
]

#clas = YOLO('/home/jorgelituma/Escritorio/Tesis/api_yolo_flask/modelonumeros.pt')
onnx_model_path_transporte_acuatico = "./clasificadores/ModeloAcuaticoTransporteFinalFinal.onnx"
session_transporte_acuatico = ort.InferenceSession(onnx_model_path_transporte_acuatico)
model_transporte_terrestre = "./clasificadores/ModeloTerrestreTransporteFinal.onnx"
session_transporte_terrestre = ort.InferenceSession(model_transporte_terrestre)
model_transporte_aereo = "./clasificadores/ModeloAereoTransporteFinal.onnx"
session_transporte_aereo = ort.InferenceSession(model_transporte_aereo)
#model_animal_acuatico = "./clasificadores/ModeloAcuaticoAnimalF.onnx"
model_animal_acuatico = "./clasificadores/ModeloAcuaticoAnimalFinal.onnx"
session_animal_acuatico = ort.InferenceSession(model_animal_acuatico)
model_animal_aereo = "./clasificadores/ModeloAereoAnimalFinalFinal.onnx"
session_animal_aereo = ort.InferenceSession(model_animal_aereo)
model_animal_terrestre = "./clasificadores/ModeloTerrestreAnimalFinal.onnx"
session_animal_terrester = ort.InferenceSession(model_animal_terrestre)
model_comida_chatarra = "./clasificadores/ModeloComidaChatarraFinalFinal.onnx"
session_comida_chatarra = ort.InferenceSession(model_comida_chatarra)
model_frutas = "./clasificadores/ModeloFrutasFinalFinal.onnx"
session_frutas = ort.InferenceSession(model_frutas)
model_personas = "./clasificadores/ModeloPersonaFinal.onnx"
session_personas = ort.InferenceSession(model_personas)
model_emociones = "./clasificadores/ModeloEmocionFinal.onnx"
session_emociones = ort.InferenceSession(model_emociones)
model_higiene = "./clasificadores/ModeloHigieneFinal.onnx"
session_higiene = ort.InferenceSession(model_higiene)
model_verduras = "./clasificadores/ModeloVerduraFinal.onnx"
session_verduras = ort.InferenceSession(model_verduras)
model_numeros = "./clasificadores/ModeloNumeroFinalSoloNumeros.onnx"
session_numeros = ort.InferenceSession(model_numeros)
model_colores = "./clasificadores/ModeloColoresFormas.onnx"
session_colores = ort.InferenceSession(model_colores)
#model_prendas_vestir = "./clasificadores/ModeloPrendaDeVestirF64.onnx"
model_prendas_vestir = "./clasificadores/ModeloPrendaDeVestirFinalFinal.onnx"
session_prendas_vestir = ort.InferenceSession(model_prendas_vestir)
model_instrumentos_musicales = "./clasificadores/ModeloInstrumentoMusicalFinal.onnx"
session_instrumentos_musicales = ort.InferenceSession(model_instrumentos_musicales)
model_juguetes = "./clasificadores/ModeloJugueteF.onnx"
session_juguetes = ort.InferenceSession(model_juguetes)

sesion_base = ort.InferenceSession("./clasificadores/base.onnx")
session_persona = ort.InferenceSession("./clasificadores/yolo11n.onnx")
#session_persona = ort.InferenceSession("./clasificadores/yolo11n_320.onnx")

#######
session_letras = ort.InferenceSession("./modelos/letras.onnx")
session_numero = ort.InferenceSession("./modelos/numeros.onnx")
session_vocales = ort.InferenceSession("./modelos/ModeloVocalFinalFinal.onnx")
# clasificador4 = ort.InferenceSession("./datasets/modelo4.onnx")
# clasificador5 = ort.InferenceSession("./datasets/modelo5.onnx")
######

#### 17 MODELOS INDIVIDUALES DEL DATASET
# modeloAAcuatico = ort.InferenteSession('./clasificadores/')
# modeloAAereo = ort.InferenteSession('./clasificadores/')
# modeloATerrestre = ort.InferenteSession('./clasificadores/')
# modeloTAcuatico = ort.InferenteSession('./clasificadores/')
# modeloTAereo = ort.InferenteSession('./clasificadores/')
# modeloTTerrestre = ort.InferenteSession('./clasificadores/')
# modeloColor = ort.InferenteSession('./clasificadores/')
# modeloComidaChatarra = ort.InferenteSession('./clasificadores/')
# modeloEmocino = ort.InferenteSession('./clasificadores/')
# modeloFruta = ort.InferenteSession('./clasificadores/')
# modeloHigiene = ort.InferenteSession('./clasificadores/')
# modeloInstrumentoMusical = ort.InferenteSession('./clasificadores/')
# modeloJuguete = ort.InferenteSession('./clasificadores/')
# modeloNumero = ort.InferenteSession('./clasificadores/')
# modeloPersona = ort.InferenteSession('./clasificadores/')
# modeloPrendaDeVestir = ort.InferenteSession('./clasificadores/')
# modeloVerdura = ort.InferenteSession('./clasificadores/')

########################################  
colores_list = {0: 'amarillo', 1: 'anaranjado', 2: 'azul', 3: 'blanco', 4: 'celeste', 5: 'marron', 6: 'morado', 7: 'negro', 8: 'rojo', 9: 'rosado', 10: 'verde claro', 11: 'verde oscuro'}

comida_chatarra = {0: 'Canguil' ,1: 'Galletadechocolate', 2: 'Gomitas', 3: 'Hamburguesa', 4: 'Helado', 5: 'Papas fritas', 6: 'Pizza'}

animales_acuaticos = {0: 'Anguila', 1: 'Ballena', 2: 'Caballito de mar', 3: 'Camaron', 4: 'Cangrejo', 5: 'Cocodrilo', 6: 'Delfin', 7: 'Estrella de mar', 8: 'Foca', 9: 'Hipopotamo', 10: 'Manta', 11: 'Medusa', 12: 'Orca', 13: 'Ostra', 14: 'Pato', 15: 'Pez', 16: 'Pinguino', 17: 'Pirana', 18: 'Pulpo', 19: 'Rana', 20: 'Tiburon', 21: 'Tortuga'}

animales_aereos = {0: 'Abeja', 1: 'Buho', 2: 'Ciguenia', 3: 'Colibri', 4: 'Condor', 5: 'Cuervo', 6: 'Flamenco', 7: 'Gaviota', 8: 'Lechuza', 9: 'Libelula', 10: 'Mariposa', 11: 'Mosca', 12: 'Pajaro', 13: 'Paloma', 14: 'Pelicano', 15: 'Tucan'}

transporte_acuatico = {0: 'Barco', 1: 'Canoa', 2: 'Lancha', 3: 'Moto acuatica', 4: 'Barco Pesquero', 5: 'Remos', 6: 'Submarino', 7: 'Yate', 8: 'Barco pirata'}

transporte_aereo = {0: 'Avion', 1: 'Avioneta', 2: 'Cohete', 3: 'Globo', 4: 'Helicoptero', 5: 'Paracaidas', 6: 'Teleferico'}

emociones = {0: 'Aburrido', 1: 'Asco', 2: 'Asustado', 3: 'Enfadado', 4: 'Feliz', 5: 'Timido', 6: 'Triste'}

frutas = {0: 'Aguacate', 1: 'Arandanos', 2: 'Cereza', 3: 'Chirimoya', 4: 'Coco', 5: 'Durazno', 6: 'Frambuesa', 7: 'Fresa', 8: 'Granadilla', 9: 'Guineo', 10: 'Kiwi', 11: 'Limon', 12: 'Mandarina', 13: 'Mango', 14: 'Manzana', 15: 'Maracuya', 16: 'Mora', 17: 'Naranja', 18: 'Papaya', 19: 'Pera', 20: 'Uvas'}

higiene_list = {0: 'Agua', 1: 'Baniar', 2: 'Cepillo', 3: 'Cepillo de cabello', 4: 'Cortar_las_unas_de_los_pies', 5: 'Enjuagar_la_boca', 6: 'Jabon', 7: 'Lavar_las_manos', 8: 'Lavarse_la_cara', 9: 'Pasta', 10: 'Peinar', 11: 'Perfumarse', 12: 'Secar_el_cuerpo', 13: 'Sonar_la_nariz'}

instrumento_musical = {0: 'Acordeon', 1: 'Armonica', 2: 'Arpa', 3: 'Bateria', 4: 'Bongos', 5: 'Clarinete', 6: 'Flauta', 7: 'Guitarra', 8: 'Guitarra_electrica', 9: 'Maracas', 10: 'Marimba', 11: 'Pandereta', 12: 'Piano', 13: 'Platillos', 14: 'Saxofon', 15: 'Tambor', 16: 'Triangulo', 17: 'Trombon', 18: 'Trompeta', 19: 'Violin', 20: 'Xilofono'}

juguetes = {0: 'Canicas', 1: 'Cubo', 2: 'Pelota', 3: 'Peluche', 4: 'Resorte', 5: 'Robot', 6: 'Rosa_de_los_vientos', 7: 'Titere', 8: 'Trompo', 9: 'Yoyo', 10: 'Zancos'}

numeros = {0: 'Cinco', 1: 'Cuatro', 2: 'Dos', 3: 'Diez', 4: 'Ocho', 5: 'Nueve', 6: 'Seis', 7: 'Siete', 8: 'Tres', 9: 'Uno'}

personas = {0: 'Abuela', 1: 'Abuelo', 2: 'Bombero', 3: 'Dentista', 4: 'Doctor', 5: 'Hermana', 6: 'Hermano', 7: 'Mama', 8: 'Ninia', 9: 'Ninio', 10: 'Papa', 11: 'Profesora', 12: 'Psicologo', 13: 'Tia', 14: 'Tio'}

prenda_de_vestir = {0: 'Abrigo', 1: 'Buso', 2: 'Calzoncillo', 3: 'Camisa', 4: 'Camisetas', 5: 'Capucha', 6: 'Casaca', 7: 'Chaleco', 8: 'Medias', 9: 'Pantalon', 10: 'Ropa_interior', 11: 'Saco', 12: 'Vestido', 13: 'Zapatos', 14: 'tacones'}

animales_terrestres = {0: 'Ardilla', 1: 'Armadillo', 2: 'Caballo', 3: 'Cabra', 4: 'Camello', 5: 'Canguro', 6: 'Caracol', 7: 'Cebra', 8: 'Cerdo', 9: 'Ciervo', 10: 'Cucaracha', 11: 'Erizo', 12: 'Escarabajo', 13: 'Escorpion', 14: 'Gato', 15: 'Gorila', 16: 'Hormiga', 17: 'Jirafa', 18: 'Leon', 19: 'Lombriz', 20: 'Oveja', 21: 'Pavo', 22: 'Perro', 23: 'Raton', 24: 'Rinoceronte', 25: 'Saltamontes', 26: 'Tarantula', 27: 'Tigre', 28: 'Vaca', 29: 'Zorro'}

transporte_terrestre = {0: 'Ambulancia', 1: 'Autobus', 2: 'Bicicleta', 3: 'Bus', 4: 'Bus turistico', 5: 'Camion', 6: 'Camion de bomberos', 7: 'Camion de la basura', 8: 'Carro', 9: 'Carro de policia', 10: 'Carruaje', 11: 'Coche de carreras', 12: 'Cuadron', 13: 'Furgoneta', 14: 'Grua', 15: 'Metro', 16: 'Moto', 17: 'Monopatin', 18: 'Tanque', 19: 'Taxi', 20: 'Tractor', 21: 'Tranvia', 22: 'Tren', 23: 'Triciclo', 24: 'Volqueta'}

verduras = {0: 'Acelga', 1: 'Aji', 2: 'Apio', 3: 'Arveja', 4: 'Berenjena', 5: 'Brocoli', 6: 'Cebolla', 7: 'Cebollin', 8: 'Coliflor', 9: 'Esparragos', 10: 'Espinaca', 11: 'Lechuga', 12: 'Pepino', 13: 'Pimiento', 14: 'Remolacha', 15: 'Tomate'}


coco_names = [
    "person","bicycle","car","motorcycle","airplane","bus","train","truck","boat","traffic light","fire hydrant","stop sign","parking meter","bench","bird","cat","dog","horse","sheep","cow","elephant","bear","zebra","giraffe","backpack","umbrella","handbag","tie","suitcase","frisbee","skis","snowboard","sports ball","kite","baseball bat","baseball glove","skateboard","surfboard","tennis racket","bottle","wine glass","cup","fork","knife","spoon","bowl","banana","apple","sandwich","orange","broccoli","carrot","hot dog","pizza","donut","cake","chair","couch","potted plant","bed","dining table","toilet","tv","laptop","mouse",    "remote",    "keyboard",    "cell phone","microwave","oven",
    "toaster","sink","refrigerator","book","clock","vase","scissors","teddy bear","hair drier","toothbrush",]

@app.route('/process_img', methods=['POST'])
def process_img():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(original_image).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension

            # Realizar inferencia con ONNX Runtime
            inputs = {session_transporte_acuatico.get_inputs()[0].name: image_array}
            outputs = session_transporte_acuatico.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            #print(f"Resultados: {results}")
            print("Llegando")
            if best_conf >= 0.7:  # Solo consideramos clases con confianza mayor a 0.5
                best_class = transporte_acuatico.get(best_idx, "Desconocido")  # Obtiene el nombre de la clase correspondiente
                #category_label = cocoNames.get(best_class, best_class)

                print(f"Clase Predicha: {best_class} - {category_label} con probabilidad: {best_conf}")

            # Enviar la respuesta en formato JSON
            return jsonify(category_label if category_label else "No se pudo clasificar")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405


@app.route('/process_img_especifica', methods=['POST'])
def process_img_especifica():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (224, 224)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            size_cl = (640, 640)
            #size_cl = (320, 320)
            image_resized = original_image.resize(input_size)
            img_res = original_image.resize(size_cl)
            image_array = np.array(image_resized).astype(np.float32)
            img_array_cl = np.array(img_res).astype(np.float32) / 255.0

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension

            
            img_array_cl = img_array_cl.transpose(2, 0, 1)  # Convertir a formato CxHxW
            img_array_cl = np.expand_dims(img_array_cl, axis=0)  # Añadir batch dimension

            

            # Realizar inferencia con ONNX Runtime
            inputs = {session.get_inputs()[0].name: image_array}
            outputs = session.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            print(best_conf)
            if best_conf >=0.7:
                best_class = list(cocoNames.keys())[best_idx]  # Obtiene el nombre de la clase correspondiente
                category_label = cocoNames.get(best_class, best_class)
                print(category_label)
                if category_label == 'Color' or category_label == 'Comida Chatarra' or category_label == 'Fruta' or category_label == 'Higiene':
                    print("Clasificador 1")
                    inputs = {clasificador1.get_inputs()[0].name: img_array_cl}
                    outputs = clasificador1.run(None, inputs)

                    # Extraer el resultado (la salida es de forma [1, 17])
                    results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
                    best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
                    best_conf = results[0][best_idx]  # Confianza de la clase más probable

                    if results is not None:
                        best_idx = np.argmax(results)
                        best_conf = results[0][best_idx]
                        print(best_conf)

                        if best_conf >= 0.7:
                            print("Es mayor o igual a 0.5")
                            #best_class = resultado[0].names[best_idx]  # Nombre de la mejor clase
                            category_label = dataset1[best_idx] if best_idx < len(dataset1) else "No se"
                            print(category_label)
                        

                elif category_label == 'Verdura' or category_label == 'Emocion' or category_label == 'Instrumento Musical' or category_label == 'Transporte Acuatico':
                    print("Clasificador 2")
                    inputs = {clasificador2.get_inputs()[0].name: img_array_cl}
                    outputs = clasificador2.run(None, inputs)

                    # Extraer el resultado (la salida es de forma [1, 17])
                    results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
                    best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
                    best_conf = results[0][best_idx]  # Confianza de la clase más probable

                    if results is not None:
                        best_idx = np.argmax(results)
                        best_conf = results[0][best_idx]  # Confianza de la mejor clase
                        print(best_conf)
                        if best_conf >= 0.7:
                            print("Es mayor o igual a 0.5")
                            #best_class = resultado[0].names[best_idx]  # Nombre de la mejor clase
                            category_label = dataset2[best_idx] if best_idx < len(dataset2) else "No se"
                            print(category_label)

                elif category_label == 'Juguete' or category_label == 'Transporte Terrestre' or category_label == 'Animal Aereo':
                    print("Clasificador 3")
                    inputs = {clasificador3.get_inputs()[0].name: img_array_cl}
                    outputs = clasificador3.run(None, inputs)

                    # Extraer el resultado (la salida es de forma [1, 17])
                    results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
                    best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
                    best_conf = results[0][best_idx]  # Confianza de la clase más probable

                    if results is not None:
                        best_idx = np.argmax(results)
                        best_conf = results[0][best_idx]
                        print(best_conf)

                        if best_conf >= 0.7:
                            print("Es mayor o igual a 0.5")
                            #best_class = resultado[0].names[best_idx]  # Nombre de la mejor clase
                            category_label =  dataset3[best_idx] if best_idx < len(dataset3) else "No se"
                            print(category_label)

                elif category_label == 'Persona' or category_label == 'Numero' or category_label == 'Animal Acuatico':
                    print("Clasificador 4")
                    inputs = {clasificador4.get_inputs()[0].name: img_array_cl}
                    outputs = clasificador4.run(None, inputs)

                    # Extraer el resultado (la salida es de forma [1, 17])
                    results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
                    best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
                    best_conf = results[0][best_idx]  # Confianza de la clase más probable

                    if results is not None:
                        best_idx = np.argmax(results)
                        best_conf = results[0][best_idx]
                        print(best_conf)

                        if best_conf >= 0.7:
                            print("Es mayor o igual a 0.5")
                            #best_class = resultado[0].names[best_idx]  # Nombre de la mejor clase
                            category_label =  dataset4[best_idx] if best_idx < len(dataset4) else "No se"
                            print(category_label)

                elif category_label == 'Transporte Aereo' or category_label == 'Prenda de Vestir' or category_label == 'Animal Terrestre':
                    print("Clasificador 5")
                    inputs = {clasificador5.get_inputs()[0].name: img_array_cl}
                    outputs = clasificador5.run(None, inputs)

                    # Extraer el resultado (la salida es de forma [1, 17])
                    results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
                    best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
                    best_conf = results[0][best_idx]  # Confianza de la clase más probable

                    if results is not None:
                        best_idx = np.argmax(results)
                        best_conf = results[0][best_idx]
                        print(best_conf)

                        if best_conf >= 0.7:
                            print("Es mayor o igual a 0.5")
                            #best_class = resultado[0].names[best_idx]  # Nombre de la mejor clase
                            category_label =  dataset5[best_idx] if best_idx < len(dataset5) else "No se"
                            print(category_label)


            print(category_label)
            # Enviar la respuesta en formato JSON
            return jsonify(category_label if category_label else "Naida")


        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

####Metodos individuales con modelos individuales del dataset
@app.route('/aacuaticos', methods=['POST'])
def aacuaticos():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension

            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            # Transponer salida para obtener detecciones como [8400, 84]
            detections = outputs_persona[0]  # (1, 84, 8400)
            detections = np.transpose(detections, (0, 2, 1))  # [1, 8400, 84]
            detections = detections[0]  # [8400, 84]

            persona_detectada = False

            for det in detections:
                scores = det[4:]  # scores para cada clase
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                #print(class_id)
                #print(class_conf)
                if class_conf < 0.8:
                    continue  # filtrar predicciones de baja confianza

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.5")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            ##############################################################################
            # Realizar inferencia con ONNX Runtime
            inputs = {session_animal_acuatico.get_inputs()[0].name: image_array}
            outputs = session_animal_acuatico.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            #print(f"Resultados: {results}")
            print("Llegando")
            if best_conf >= 0.5:  # Solo consideramos clases con confianza mayor a 0.5
                best_class = animales_acuaticos.get(best_idx, "Desconocido")  # Obtiene el nombre de la clase correspondiente
                #category_label = cocoNames.get(best_class, best_class)

                print(f"Clase Predicha: {best_class} - {category_label} con probabilidad: {best_conf}")

            # Enviar la respuesta en formato JSON
            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            print("Error detallado:", str(e))
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/aaereos', methods=['POST'])
def aaereos():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})

            # Guardar la imagen localmente
            
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            print("ëtapa 1")
            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            # Transponer salida para obtener detecciones como [8400, 84]
            detections = outputs_persona[0]  # (1, 84, 8400)
            detections = np.transpose(detections, (0, 2, 1))  # [1, 8400, 84]
            detections = detections[0]  # [8400, 84]

            persona_detectada = False

            for det in detections:
                scores = det[4:]  # scores para cada clase
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                #print(class_id)
                #print(class_conf)
                if class_conf < 0.8:
                    continue  # filtrar predicciones de baja confianza

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print(class_conf)
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            # Realizar inferencia con ONNX Runtime
            inputs = {session_animal_aereo.get_inputs()[0].name: image_array}
            outputs = session_animal_aereo.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            #print(f"Resultados: {results}")
            print("Llegando")
            if best_conf >= 0.8:  # Solo consideramos clases con confianza mayor a 0.5
                best_class = animales_aereos.get(best_idx, "Desconocido")  # Obtiene el nombre de la clase correspondiente
                #category_label = cocoNames.get(best_class, best_class)

                print(f"Clase Predicha: {best_class} - {category_label} con probabilidad: {best_conf}")

            # Enviar la respuesta en formato JSON
            return jsonify(best_class if best_class else "Naida")
        except Exception as e:
            print(str(e))
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/aterrestres', methods=['POST'])
def aterrestres():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})

            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension

            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            # Transponer salida para obtener detecciones como [8400, 84]
            detections = outputs_persona[0]  # (1, 84, 8400)
            detections = np.transpose(detections, (0, 2, 1))  # [1, 8400, 84]
            detections = detections[0]  # [8400, 84]

            persona_detectada = False

            for det in detections:
                scores = det[4:]  # scores para cada clase
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                #print(class_id)
                #print(class_conf)
                if class_conf < 0.6:
                    continue  # filtrar predicciones de baja confianza

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.5")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            # Realizar inferencia con ONNX Runtime
            inputs = {session_animal_terrester.get_inputs()[0].name: image_array}
            outputs = session_animal_terrester.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            #print(f"Resultados: {results}")
            print("Llegando")
            if best_conf >= 0.7:  # Solo consideramos clases con confianza mayor a 0.5
                best_class = animales_terrestres.get(best_idx, "Desconocido")  # Obtiene el nombre de la clase correspondiente
                #category_label = cocoNames.get(best_class, best_class)

                print(f"Clase Predicha: {best_class} - {category_label} con probabilidad: {best_conf}")

            # Enviar la respuesta en formato JSON
            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/taacuaticos', methods=['POST'])
def tacuaticos():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            # Transponer salida para obtener detecciones como [8400, 84]
            detections = outputs_persona[0]  # (1, 84, 8400)
            detections = np.transpose(detections, (0, 2, 1))  # [1, 8400, 84]
            detections = detections[0]  # [8400, 84]

            persona_detectada = False

            for det in detections:
                scores = det[4:]  # scores para cada clase
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                #print(class_id)
                #print(class_conf)
                if class_conf < 0.8:
                    continue  # filtrar predicciones de baja confianza

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.5")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            # Realizar inferencia con ONNX Runtime
            inputs = {session_transporte_acuatico.get_inputs()[0].name: image_array}
            outputs = session_transporte_acuatico.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            #print(f"Resultados: {results}")
            print("Llegando")
            if best_conf >= 0.7:  # Solo consideramos clases con confianza mayor a 0.5
                best_class = transporte_acuatico.get(best_idx, "Desconocido")  # Obtiene el nombre de la clase correspondiente
                #category_label = cocoNames.get(best_class, best_class)

                print(f"Clase Predicha: {best_class} - {category_label} con probabilidad: {best_conf}")

            # Enviar la respuesta en formato JSON
            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/taereos', methods=['POST'])
def taereos():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension

            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.7:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            #---- AQUI VAMOS A DETECTAR YA LOS MODELOS DE TRASPORTE AEREOS ------    

            # Realizar inferencia con ONNX Runtime
            inputs = {session_transporte_aereo.get_inputs()[0].name: image_array}
            outputs = session_transporte_aereo.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.7:
                best_class = transporte_aereo.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/tterrestres', methods=['POST'])
def tterrestres():
    if request.method == 'POST':
        try:
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()
            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})

            # Guardar imagen original
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Imagen para OpenCV y PIL
            image_np = cv2.imdecode(np.frombuffer(image_bytes, np.uint8), cv2.IMREAD_COLOR)
            original_image = Image.fromarray(cv2.cvtColor(image_np, cv2.COLOR_BGR2RGB))
            original_width, original_height = original_image.size

            # Preprocesamiento común para ambos modelos
            input_size = (640, 640)
            #input_size = (320, 320)
            image_resized = cv2.resize(image_np, input_size)
            
            image_input = image_resized.astype(np.float32) / 255.0
            image_input = np.transpose(image_input, (2, 0, 1))  # CxHxW
            image_input = np.expand_dims(image_input, axis=0)   # 1x3x640x640

            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_input}
            outputs_persona = session_persona.run(None, inputs_persona)
            # Transponer salida para obtener detecciones como [8400, 84]
            detections = outputs_persona[0]  # (1, 84, 8400)
            detections = np.transpose(detections, (0, 2, 1))  # [1, 8400, 84]
            detections = detections[0]  # [8400, 84]

            persona_detectada = False

            for det in detections:
                scores = det[4:]  # scores para cada clase
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                #print(class_id)
                #print(class_conf)
                if class_conf < 0.8:
                    continue  # filtrar predicciones de baja confianza

                if class_id == 0:
                    persona_detectada = True

                # Decodificar bbox
                # center_x, center_y, width, height = det[:4]
                # x1 = int((center_x - width / 2) / 640 * original_width)
                # y1 = int((center_y - height / 2) / 640 * original_height)
                # x2 = int((center_x + width / 2) / 640 * original_width)
                # y2 = int((center_y + height / 2) / 640 * original_height)

                # label = coco_names[class_id] if class_id < len(coco_names) else str(class_id)
                # color = (0, 255, 0) if class_id == 0 else (255, 0, 0)

                # cv2.rectangle(image_np, (x1, y1), (x2, y2), color, 2)
                # cv2.putText(image_np, f"{label} {class_conf:.2f}", (x1, max(y1 - 10, 0)),
                #             cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

            # Guardar imagen final con boxes
            #cv2.imwrite("resultado_persona.jpg", cv2.resize(image_np, (original_width, original_height)))

            # Retornar según detección
            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.5")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            # --------- ETAPA 2: CLASIFICACIÓN DE TRANSPORTE ---------
            inputs = {session_transporte_terrestre.get_inputs()[0].name: image_input}
            outputs = session_transporte_terrestre.run(None, inputs)

            results = outputs[0]
            best_idx = int(np.argmax(results))
            best_conf = float(results[0][best_idx])

            best_class = None
            if best_conf >= 0.8:
                best_class = transporte_terrestre.get(best_idx, "Desconocido")
                print(f"Clase detectada: {best_class} con confianza {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            import traceback
            traceback.print_exc()
            return jsonify({"error": f"Error interno: {str(e)}"}), 500

    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/color', methods=['POST'])
def color():
    return

@app.route('/comidachatarra', methods=['POST'])
def comidachatarra():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.65:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            # ----- AQUI YA VA LA CLASIFICACION DEL MODELO -------

            # Realizar inferencia con ONNX Runtime
            inputs = {session_comida_chatarra.get_inputs()[0].name: image_array}
            outputs = session_comida_chatarra.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.85:
                best_class = comida_chatarra.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/emocion', methods=['POST'])
def emocion():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.8:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            # -- AQUI VA LA CLASIFICACION CON EL MODELO -----

            # Realizar inferencia con ONNX Runtime
            inputs = {session_emociones.get_inputs()[0].name: image_array}
            outputs = session_emociones.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.7:
                best_class = emociones.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/fruta', methods=['POST'])
def fruta():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.6:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")


            # ---- AQUI EMPIEZA LA CLASIFICACION CON EL MODELO ----


            # Realizar inferencia con ONNX Runtime
            inputs = {session_frutas.get_inputs()[0].name: image_array}
            outputs = session_frutas.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            print("Llegando")
            if best_conf >= 0.5:
                best_class = frutas.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/higiene', methods=['POST'])
def higiene():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.6:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            # ---- AQUI EMPIEZA LA CLASIFICACION CON EL MODELO --- 

            # Realizar inferencia con ONNX Runtime
            inputs = {session_higiene.get_inputs()[0].name: image_array}
            outputs = session_higiene.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.7:
                best_class = higiene_list.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/instrumentomusical', methods=['POST'])
def instrumentomusical():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.5:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            
            # -----AQUI COMIENZA LA CLASIFICACION CON EL MODELO DE NO DETECTAR UNA PEROSNA ---

            # Realizar inferencia con ONNX Runtime
            inputs = {session_instrumentos_musicales.get_inputs()[0].name: image_array}
            outputs = session_instrumentos_musicales.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.7:
                best_class = instrumento_musical.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/juguete', methods=['POST'])
def juguete():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.8:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            
            # ----AQUI LA CLASIFICACION CON EL MODELO DE JUGUETES---

            # Realizar inferencia con ONNX Runtime
            inputs = {session_juguetes.get_inputs()[0].name: image_array}
            outputs = session_juguetes.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.7:
                best_class = juguetes.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/numero', methods=['POST'])
def numero():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.8:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            # --AQUI LA CALSIFICACION CON EL MODELO DE NUMERO----


            # Realizar inferencia con ONNX Runtime
            inputs = {session_numeros.get_inputs()[0].name: image_array}
            outputs = session_numeros.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.7:
                best_class = numeros.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/persona', methods=['POST'])
def persona():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.7:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")


            # AQUI LA CLASIFICACION CON EL MODELO DE PERSONAS *-----


            # Realizar inferencia con ONNX Runtime
            inputs = {session_personas.get_inputs()[0].name: image_array}
            outputs = session_personas.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.7:
                best_class = personas.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/prendadevestir', methods=['POST'])
def prendadevestir():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.8:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            
            #AQUI VA LA CLASIFICACION CON EL MODELO DE PREDAS DE VESTIR


            # Realizar inferencia con ONNX Runtime
            inputs = {session_prendas_vestir.get_inputs()[0].name: image_array}
            outputs = session_prendas_vestir.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.85:
                best_class = prenda_de_vestir.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/verdura', methods=['POST'])
def verdura():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.5:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")

            ## AQUI VA LA CALSIFICAICON CON EL MODELO DE VERDURA

            # Realizar inferencia con ONNX Runtime
            inputs = {session_verduras.get_inputs()[0].name: image_array}
            outputs = session_verduras.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.6:
                best_class = verduras.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405
    
@app.route('/colores', methods=['POST'])
def colores():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("image.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            # Preprocesamiento para ONNX (ajustar según las necesidades de tu modelo)
            input_size = (640, 640)  # Ajusta esto según las dimensiones de entrada de tu modelo ONNX
            #input_size = (320, 320)
            image_resized = original_image.resize(input_size)
            image_array = np.array(image_resized).astype(np.float32)

            # Normalización de la imagen (ajustar según las necesidades de tu modelo)
            image_array = (image_array / 255.0 - 0.5) * 2.0  # Esto depende de cómo fue entrenado el modelo
            image_array = np.transpose(image_array, (2, 0, 1))  # Convertir a formato CxHxW
            image_array = np.expand_dims(image_array, axis=0)  # Añadir batch dimension


            # --------- ETAPA 1: DETECCIÓN DE PERSONAS CON YOLOv8 ONNX ---------
            inputs_persona = {session_persona.get_inputs()[0].name: image_array}
            outputs_persona = session_persona.run(None, inputs_persona)
            detections = outputs_persona[0]
            detections = np.transpose(detections, (0, 2, 1))
            detections = detections[0]

            persona_detectada = False

            for det in detections:
                scores = det[4:]
                class_id = int(np.argmax(scores))
                class_conf = float(scores[class_id])
                if class_conf < 0.8:
                    continue

                if class_id == 0:
                    persona_detectada = True

            if persona_detectada:
                print("🧍‍♂ PERSONA DETECTADA con confianza > 0.8")
                return jsonify("Naida")
            else:
                print("NO SE DETECTÓ PERSONA — se permite clasificación")
            

            ##AQUI EL MODELO DE CLASIFICACION ENTRA EN ACCION

            # Realizar inferencia con ONNX Runtime
            inputs = {session_colores.get_inputs()[0].name: image_array}
            outputs = session_colores.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None

            print("Llegando")
            if best_conf >= 0.7:
                best_class = colores_list.get(best_idx, "Desconocido")
                print(f"Clase Predicha: {best_class} con probabilidad: {best_conf}")

            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405
####Metodos individuales con modelos individuales del dataset


# Función para preprocesar la imagen
def preprocess_image(image_bytes):
    # Convertir bytes de imagen a un array numpy
    nparr = np.frombuffer(image_bytes, np.uint8)
    # Convertir el array numpy a una imagen de OpenCV
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    # Verificar si la imagen se cargó correctamente
    if image is None:
        raise ValueError("Error al cargar la imagen. Verifica que los bytes de la imagen son correctos.")

    # Verificar las dimensiones de la imagen
    if image.shape[0] == 0 or image.shape[1] == 0:
        raise ValueError("La imagen tiene dimensiones inválidas.")

    # Convertir la imagen a escala de grises
    #gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Aplicar un threshold (umbral)
    _, thresholded_image = cv2.threshold(image, 128, 255, cv2.THRESH_BINARY)

    # Invertir los colores de la imagen
    image_inverted = cv2.bitwise_not(thresholded_image)

    # Redimensionar la imagen al tamaño esperado por el modelo (32x32)
    #image_resized = cv2.resize(image_inverted, (32, 32))
    image_resized = cv2.resize(image_inverted, (224, 224))

    cv2.imwrite("imagens.png", image_resized)
    # Normalizar la imagen
    image_normalized = image_resized / 255.0
    # Convertir la imagen a flotante y añadir una dimensión para el batch
    image_expanded = np.expand_dims(image_normalized, axis=0).astype(np.float32)
    return image_expanded

def preprocess_Letra(image_bytes):
    # Convertir bytes de imagen a un array numpy
    nparr = np.frombuffer(image_bytes, np.uint8)
    # Convertir el array numpy a una imagen de OpenCV
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    # Verificar si la imagen se cargó correctamente
    if image is None:
        raise ValueError("Error al cargar la imagen. Verifica que los bytes de la imagen son correctos.")

    # Verificar las dimensiones de la imagen
    if image.shape[0] == 0 or image.shape[1] == 0:
        raise ValueError("La imagen tiene dimensiones inválidas.")

    # Convertir la imagen a escala de grises
    #gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Aplicar un threshold (umbral)
    _, thresholded_image = cv2.threshold(image, 128, 255, cv2.THRESH_BINARY)

    # Invertir los colores de la imagen
    image_resized = cv2.resize(thresholded_image, (32, 32))

    cv2.imwrite("letra.png", image_resized)
    # Normalizar la imagen
    image_normalized = (image_resized / 255.0 - 0.5) * 2.0  
    image_normalized = np.transpose(image_normalized, (2, 0, 1))

    # Convertir la imagen a flotante y añadir una dimensión para el batch
    image_expanded = np.expand_dims(image_normalized, axis=0).astype(np.float32)
    
    return image_expanded 



def preprocess_vocal(image_bytes):
    nparr = np.frombuffer(image_bytes, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if image is None or image.shape[0] == 0 or image.shape[1] == 0:
        raise ValueError("Error al cargar la imagen.")

    # Escala de grises
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Fondo blanco, letra negra -> invertimos para resaltar el trazo
    _, thresholded = cv2.threshold(gray, 180, 255, cv2.THRESH_BINARY_INV)

    # Redimensionar
    image_resized = cv2.resize(thresholded, (32, 32))

    cv2.imwrite("vocal.png", image_resized)

    # Normalización
    image_normalized = image_resized.astype(np.float32) / 255.0

    # Convertir a 3 canales repitiendo la misma imagen
    image_3ch = np.stack([image_normalized, image_normalized, image_normalized], axis=0)  # [3, 32, 32]

    

    # Añadir canal y batch: [1, 1, 32, 32]
    #image_expanded = np.expand_dims(image_normalized, axis=0)   # [1, 32, 32]
    #image_expanded = np.expand_dims(image_expanded, axis=1)     # [1, 1, 32, 32]

    image_expanded = np.expand_dims(image_3ch, axis=0)  # [1, 3, 32, 32]


    return image_expanded.astype(np.float32)

def preprocess_number(image_bytes):
    # Convertir bytes a una imagen con OpenCV (BGR)
    nparr = np.frombuffer(image_bytes, np.uint8)
    image = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if image is None or image.shape[0] == 0 or image.shape[1] == 0:
        raise ValueError("Error al cargar la imagen.")

    # Convertir de BGR a RGB porque OpenCV carga en BGR por defecto
    #image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    _, thresholded_image = cv2.threshold(image, 128, 255, cv2.THRESH_BINARY_INV)

    # Redimensionar a 32x32 como espera el modelo
    image_resized = cv2.resize(thresholded_image, (32, 32))

    # Normalizar a rango [0, 1]
    cv2.imwrite("numero.png", image_resized)
 
    image_normalized = image_resized.astype(np.float32) / 255.0

    # Reordenar a [C, H, W]
    image_transposed = np.transpose(image_normalized, (2, 0, 1))

    # Añadir dimensión de batch: [1, 3, 32, 32]
    image_batched = np.expand_dims(image_transposed, axis=0)

    return image_batched.astype(np.float32)


@app.route('/process_letra', methods=['POST'])
def process_letra():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("imagen.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            print("Llegando")
            image_array = preprocess_Letra(image_bytes)
            print("Des")

            # Realizar inferencia con ONNX Runtime
            inputs = {session_letras.get_inputs()[0].name: image_array}
            outputs = session_letras.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            print("Vector de salida vocales:", results)
            print("Indice predicho:", best_idx)
            print("Mapa vocales:", vocales)
            
            

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            #print(f"Resultados: {results}")
           
            print("Llegando")
            if best_conf >= 0.5:  # Solo consideramos clases con confianza mayor a 0.5
                best_class = letras.get(best_idx, "Desconocido")  # Obtiene el nombre de la clase correspondiente
                #category_label = cocoNames.get(best_class, best_class)

                print(f"Clase Predicha: {best_class} - {category_label} con probabilidad: {best_conf}")

            # Enviar la respuesta en formato JSON
            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            print(str(e))
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

@app.route('/process_vocal', methods=['POST'])
def process_vocal():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("imagen.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            print("Llegando")
            image_array = preprocess_vocal(image_bytes)
            print("Des")


            

            # Realizar inferencia con ONNX Runtime
            inputs = {session_vocales.get_inputs()[0].name: image_array}
            outputs = session_vocales.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            #print(f"Resultados: {results}")
            print("Llegando")
            if best_conf >= 0.5:  # Solo consideramos clases con confianza mayor a 0.5
                best_class = vocales.get(best_idx, "Desconocido")  # Obtiene el nombre de la clase correspondiente
                #category_label = cocoNames.get(best_class, best_class)

                print(f"Clase Predicha: {best_class} - {category_label} con probabilidad: {best_conf}")

            # Enviar la respuesta en formato JSON
            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            print(str(e))
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405
    
@app.route('/process_numero', methods=['POST'])
def process_numero():
    if request.method == 'POST':
        try:
            # Verificar si el archivo fue enviado
            if 'image' not in request.files:
                return jsonify({"error": "No se envió la imagen"}), 400

            # Leer la imagen desde el archivo recibido
            uploaded_file = request.files['image']
            image_bytes = uploaded_file.read()

            image_base64 = base64.b64encode(image_bytes).decode('utf-8')

            socketio.emit('response', {'image': f"{image_base64}"})
            # Guardar la imagen localmente
            with open("imagen.jpeg", "wb") as file:
                file.write(image_bytes)

            # Abrir la imagen y preprocesarla
            original_image = Image.open(BytesIO(image_bytes)).convert("RGB")
            original_width, original_height = original_image.size

            print("Llegando")
            image_array = preprocess_number(image_bytes)
            print("Des")

            # Realizar inferencia con ONNX Runtime
            inputs = {session_numero.get_inputs()[0].name: image_array}
            outputs = session_numero.run(None, inputs)

            # Extraer el resultado (la salida es de forma [1, 17])
            results = outputs[0]  # Esto depende de la salida de tu modelo ONNX
            best_idx = np.argmax(results)  # Índice de la clase con mayor probabilidad
            best_conf = results[0][best_idx]  # Confianza de la clase más probable

            # Lógica para obtener el nombre de la clase usando cocoNames
            best_class = None
            category_label = None
            #print(f"Resultados: {results}")
            print("Llegando")
            if best_conf >= 0.5:  # Solo consideramos clases con confianza mayor a 0.5
                best_class = numerosesc.get(best_idx, "Desconocido")  # Obtiene el nombre de la clase correspondiente
                #category_label = cocoNames.get(best_class, best_class)

                print(f"Clase Predicha: {best_class} - {category_label} con probabilidad: {best_conf}")

            # Enviar la respuesta en formato JSON
            return jsonify(best_class if best_class else "Naida")

        except Exception as e:
            print(str(e))
            return jsonify({"error": f"Error interno: {str(e)}"}), 500
    else:
        return jsonify({"error": "Método no permitido"}), 405

######################################################################################################################
######################################################################################################################
######################################################################################################################
######################################################################################################################
######################################################################################################################
######################################################################################################################

from vosk import Model, KaldiRecognizer
import unicodedata

VOSK_MODEL_PATH = "./model/vosk-model-small-es-0.42"
vosk_model = Model(VOSK_MODEL_PATH)

CHANNELS = 1  # Mono
RATE = 44100
CHUNK = 1024
SILENCE_THRESHOLD = 500

# ----------------------------
# Conversión y preprocesado
# ----------------------------

def convertir_a_mono(input_audio):
    audio = AudioSegment.from_wav(input_audio)
    audiomono = audio.set_channels(1)
    audiomono = audiomono.set_frame_rate(RATE)
    audiomono.export("./audios/audio-un-canal.wav", format="wav")
    print("Convertido a un canal y guardado en ./audios/audio-un-canal.wav")

def convertir_mp4_a_wav(mp4_path):
    """Convierte un archivo .mp4 a .wav con Linear16"""
    clip = mp.AudioFileClip(mp4_path)
    wav_path = mp4_path.replace(".mp4", ".wav")
    clip.write_audiofile(wav_path, codec="pcm_s16le", fps=RATE)
    return wav_path

# ----------------------------
# Transcripción con Vosk
# ----------------------------

def transcribir_audio_vosk(audio_path):
    """Transcribe un archivo WAV usando Vosk"""
    wf = wave.open(audio_path, "rb")
    if wf.getnchannels() != 1 or wf.getframerate() != RATE:
        raise ValueError("El audio debe estar en mono y a 44100 Hz")

    rec = KaldiRecognizer(vosk_model, wf.getframerate())
    rec.SetWords(True)

    texto_final = ""
    while True:
        data = wf.readframes(CHUNK)
        if len(data) == 0:
            break
        if rec.AcceptWaveform(data):
            result = json.loads(rec.Result())
            texto_final += " " + result.get("text", "")
    # Resultado final
    result = json.loads(rec.FinalResult())
    texto_final += " " + result.get("text", "")

    return texto_final.strip()

# ----------------------------
# Utilidades
# ----------------------------

def quitar_tildes(texto):
    texto = texto.replace("ñ", "ni").replace("Ñ", "Ni")
    return ''.join(
        c for c in unicodedata.normalize('NFD', texto)
        if unicodedata.category(c) != 'Mn'
    )

def detectar_sonido_y_recortar(input_file, output_file, silence_threshold=SILENCE_THRESHOLD):
    """Recorta el audio eliminando partes silenciosas"""
    audio = AudioSegment.from_wav(input_file)
    samples = np.array(audio.get_array_of_samples())
    window_size = int(RATE * 0.05)
    amplitudes = maximum_filter1d(abs(samples), size=window_size)
    sonido_indices = np.where(amplitudes > silence_threshold)[0]

    if len(sonido_indices) == 0:
        print("No se detectó sonido.")
        return

    start_time = (sonido_indices[0] / RATE) * 1000
    end_time = (sonido_indices[-1] / RATE) * 1000
    audio_recortado = audio[start_time:end_time]
    audio_recortado.export(output_file, format="wav")
    print(f"Audio recortado guardado en: {output_file}")

# ----------------------------
# Endpoint Flask
# ----------------------------

@app.route('/upload_audio', methods=['POST'])
def upload_audio():
    if "file" not in request.files:
        return jsonify({"error": "No se encontró archivo"}), 400

    file = request.files["file"]
    file_path = f"./audios/{file.filename}"
    file.save(file_path)

    wav_path = convertir_mp4_a_wav(file_path)

    convertir_a_mono(wav_path)
    file_path_mono = "./audios/audio-un-canal.wav"
    output_path = file_path_mono.replace(".wav", "_recortado.wav")

    detectar_sonido_y_recortar(file_path_mono, output_path)

    texto = transcribir_audio_vosk(output_path)
    textoNuevo = quitar_tildes(texto)

    print("Texto final:", textoNuevo)
    try:
        socketio.emit('response', {'data': f"{textoNuevo}"})
    except Exception as e:
        print("Error enviando por SocketIO:", e)

    return jsonify({"text": textoNuevo})







########################################################################
##Metodo para consumir modelo LLM
API_URL = "http://127.0.0.1:11434/api/generate"

@app.route('/ollama', methods=['POST'])
def generate_text():
    user_input = request.json.get("prompt", "")
    print(user_input)

    if not user_input:
        return jsonify({"error": "Se requiere un prompt"}), 400

    # Configurar los datos a enviar al modelo
    prompt = "Responde solo con una oración, sin explicaciones: " + user_input

    payload = {
        #"model": "gemma3:1b",
        "model": "gemma:2b",
        # "messages": [
        # #     {
        # #     "role": "system",
        # # "content": "Eres un asistente que siempre responde con una sola oración con la palabra que te de, clara y directa, sin rodeos ni explicaciones extensas."}, 
        # {"role": "user", "content": prompt}]
       "prompt": prompt,
       "stream": False
    }

    # Hacer la solicitud al servidor de la API en modo streaming
    # response = requests.post(API_URL, json=payload, stream=True)

    # if response.status_code != 200:
    #     return jsonify({"error": "Fallo en la solicitud al modelo"}), 500

    # print("Recibiendo respuesta en streaming...")

    # final_text = ""
    # for line in response.iter_lines():
    #     if line:
    #         try:
    #             decoded_line = line.decode("utf-8")
    #             #print(f" Recibido: {decoded_line}")  # <-- Debugging

    #             json_data = json.loads(decoded_line)

    #             # Obtener el contenido del mensaje
    #             content = json_data.get("message", {}).get("content", "")

    #             if content:
    #                 final_text += content

    #             # Si "done" es True, terminamos
    #             if json_data.get("done", False):
    #                 break
    #         except json.JSONDecodeError as e:
    #             print(f"Error decodificando JSON: {e}")
    #             continue
    #         print(final_text)
    # return jsonify({"response": final_text.strip()})
    
    try:
        response = requests.post(API_URL, json=payload)
        response.raise_for_status()
        data = response.json()

        # Cortar respuesta en la primera oración si se extiende
        respuesta = data.get("response", "").strip()
        if "." in respuesta:
            respuesta = respuesta.split(".")[0] + "."

        return jsonify({"response": respuesta.strip()})

    except requests.exceptions.RequestException as e:
        print(f"Error al conectar con la API: {e}")
        return jsonify({"error": "Error al contactar con Ollama"}), 500

##prompt

##respondeme unicamente con una oracion simple para que un niño de 5 y 6 años de edad lea, una simple que no tenga mas de 10 palabras, las oraciones deben ser diferentes todo el tiempo, recuerda las ultimas palabras que me hayas respondido para evitar oraciones repetidas.

#import regex as re

palabras_incompletas = {'Abrig_': 'o', '_buela': 'a', 'Abu_lo': 'e', 'Agu_': 'a', 'Aguil_': 'a', 'Amar_llo': 'i', 'Anguil_': 'a', 'Ardill_': 'a', 'Armad_llo': 'i', '_zul': 'a', 'Ballen_': 'a', 'Bomb_ro': 'e', 'Blanc_': 'o', 'Br_coli': 'o', 'B_ho': 'u', 'B_so': 'u', 'Cab_llo': 'a', 'Caballit_ de mar': 'o', 'Cabr_': 'a', 'Calzoncill_': 'o', 'Un_': 'o', 'D_s': 'o', 'Enjab_nar el cuerpo': 'o', '_njuagar la boca': 'e', 'Tr_s': 'e', 'Cuatr_': 'o', 'C_nco': 'i', 'Se_s': 'i', 'S_ete': 'i', 'Och_': 'o', 'Nu_ve': 'e', 'Di_z': 'e', 'C_maron': 'a', 'C_mello': 'a', 'C_misa': 'a', 'Camis_ta': 'e', 'Cangr_jo': 'e', 'C_nguro': 'a', 'C_nicas': 'a', 'Capuch_': 'a', 'Carac_l': 'o', 'C_saca': 'a', 'Cebr_': 'a', 'Cel_ste': 'e', 'Cerd_': 'o', 'Chalec_': 'o', 'Cierv_': 'o', 'Cigu_nia': 'e', 'Coc_drilo': 'o', 'Col_bri': 'i', 'Cond_r': 'o', 'C_bo': 'u', 'Cucar_cha': 'a', 'Cuerv_': 'o', 'D_lfin': 'e', 'Doct_r': 'o', '_rizo': 'e', '_scarabajo': 'e', 'Escorp_on': 'i', 'Estr_lla de mar': 'e', 'Foc_': 'a', 'G_to': 'a', 'Herman_': 'a', 'H_popotamo': 'i', 'Hormig_': 'a', 'Jiraf_': 'a', 'Lavars_ la cara': 'e', 'Le_n': 'o', 'L_mbriz': 'o', 'Mam_': 'a', 'Mant_': 'a', 'Medi_s': 'a', 'Medus_': 'a', 'Nini_': 'a', 'N_nio': 'i', '_rca': 'o', 'Ostr_': 'a', '_veja': 'o', 'Pant_lon': 'a', 'P_pa': 'a', 'Pel_che': 'u', 'P_z': 'e', 'Pinguin_': 'o', 'Pirañ_': 'a', 'Profes_ra': 'o', 'Ps_cologo': 'i', 'Pulp_': 'o', 'R_na': 'a', 'Rat_n': 'o', 'Resort_': 'e', 'Rinoc_ronte': 'e', 'Rob_t': 'o', 'Sac_': 'o', 'Saltamont_s': 'e', 'Tac_nes': 'o', 'T_rantula': 'a', 'Ti_': 'a', 'Tib_ron': 'u', 'Tigr_': 'e', 'T_o': 'i', 'Titer_': 'e', 'Tortug_': 'a', 'Tromp_': 'o', 'V_ca': 'a', 'Vestid_': 'o', 'Y_yo': 'o', 'Zapat_s': 'o', 'Z_rro': 'o', 'Flamenc_': 'o', 'G_viota': 'a', 'Gor_la': 'i', 'Gr_s': 'i', 'Lechuz_': 'a', 'L_la': 'i', 'Mar_posa': 'i', 'M_rron': 'a', 'Mor_do': 'a', 'Mosc_': 'a', 'Negr_': 'o', 'P_jaro': 'a', 'Palom_': 'a', 'P_vo': 'a', 'P_licano': 'e', 'Perr_': 'o', 'Roj_': 'o', 'Ropa inter_or': 'i', 'Rosad_': 'o', 'Sonar la n_riz': 'a', 'T_can': 'u', 'Verde clar_': 'o', 'Verd_ oscuro': 'e', 'Zanc_s': 'o', 'Dur_zno': 'a', 'Mel_n': 'o', 'C_adron': 'u', 'Clarin_te': 'e', 'B_s': 'u', 'C_nguil': 'a', 'C_bolla': 'e', 'Jab_n': 'o', 'Mor_': 'a', 'C_bollin': 'e', 'Coche d_ carreras': 'e', 'R_mos': 'e', 'Camion de la b_sura': 'a', 'Tr_mbon': 'o', 'Tranv_a': 'i', 'Cepill_ de cabello': 'o', 'T_nque': 'a', 'B_niar': 'a', 'Submar_no': 'i', 'M_rimba': 'a', '_vion': 'a', 'Barco p_rata': 'i', 'Tr_n': 'e', 'Guitarra electric_': 'a', 'M_racuya': 'a', 'Lechug_': 'a', 'Xilofon_': 'o', 'Ciruel_': 'a', 'L_ma': 'i', 'Lav_r las manos': 'a', 'Chir_moya': 'i', 'Autob_s': 'u', 'Sax_fon': 'o', 'B_jo': 'a', '_rmonica': 'a', '_rpa': 'a', 'Helic_ptero': 'o', 'Gall_ta de chocolate': 'e', 'Framb_esa': 'u', 'M_tro': 'e', 'Secar el cuerp_': 'o', 'Pein_r': 'a', 'Fel_z': 'i', 'Moto _cuatica': 'a', 'Fr_sa': 'e', 'Enfadad_': 'o', 'Amb_lancia': 'u', 'Tr_ciclo': 'i', 'Mandar_na': 'i', 'Pi_no': 'a', 'Gomit_s': 'a', 'Flaut_': 'a', 'Pim_ento': 'i', 'Carru_je': 'a', 'Helad_': 'o', 'Kiw_': 'i', 'Pepin_': 'o', 'Par_caidas': 'a', 'Barco p_squero': 'e', 'P_rfumarse': 'e', 'B_ngos': 'o', 'C_rro': 'a', 'Manzan_': 'a', 'Camion de bomb_ros': 'e', 'Cepill_': 'o', '_rveja': 'a', 'B_rco': 'a', 'Coh_te': 'e', 'Cortarlas uñas de l_s pies': 'o', 'Bic_cleta': 'i', '_sustado': 'a', 'Timid_': 'o', 'Ch_colate': 'o', 'Motoacuatic_': 'a', 'Cano_': 'a', 'Mot_': 'o', '_randanos': 'a', 'C_mion': 'a', 'M_racas': 'a', 'Tamb_r': 'o', '_burrido': 'a', 'Avion_ta': 'e', 'L_ncha': 'a', 'Yat_': 'e', 'Naranj_': 'a', 'Gr_a': 'u', '_celga': 'a', 'Gran_dilla': 'a', 'Acorde_n': 'o', 'Hamburg_esa': 'u', '_vas': 'u', '_pio': 'a', 'T_mate': 'o', 'B_sturistico': 'u', 'Tr_ste': 'i', 'P_paya': 'a', 'Glob_': 'o', 'V_olin': 'i', 'C_rro de policia': 'a', 'P_sta': 'a', 'Tax_': 'i', 'Ag_acate': 'u', 'B_teria': 'a', 'Telefer_co': 'i', '_sco': 'a', 'Cort_r uñas de las manos': 'a', 'Papafrit_': 'a', 'Plat_llos': 'i', 'Pand_reta': 'e', 'Tromp_ta': 'e', 'P_ra': 'e', 'Cerez_': 'a', 'Remol_cha': 'a', 'C_co': 'o', 'Furgon_ta': 'e', 'Tr_angulo': 'i', 'G_itarra': 'u', 'Colifl_r': 'o', 'Graj_as': 'e', 'P_zza': 'i', 'Monop_tin': 'a', 'Guine_': 'o', 'Mang_': 'o', 'Berenjen_': 'a', '_spinaca': 'e'}


@app.route('/word', methods=['GET'])
def generate_word():

    if palabras_incompletas:
        return jsonify({
            "diccionario": palabras_incompletas
        }), 200
    else:
        return jsonify({
            "error": "Palabra no encontrada en el diccionario"
        }), 404


if __name__ == "__main__":
    #app.run(host='0.0.0.0', port=5000,debug=True)
    socketio.run(app, host='0.0.0.0', port=5000,debug=True)

 



# import os

# # Directorio donde se encuentran las imágenes
# directorio = "../../../../../../../AndroidStudioProjects/Robbyapp/app/src/main/res/raw/"

# # Extensiones de imagen permitidas
# extensiones_permitidas = {".png", ".jpg", ".jpeg", ".bmp", ".gif"}

# # Lista de imágenes procesadas
# listadoImagenes = {}

# # Recorrer archivos en el directorio
# for archivo in os.listdir(directorio):
#     nombre, extension = os.path.splitext(archivo)  # Separar nombre y extensión

#     # Verificar si el archivo es una imagen y comienza con "final"
#     if nombre.startswith("final") and extension.lower() in extensiones_permitidas:
#         clave = nombre[5:]  # Extrae el texto después de "final"
#         listadoImagenes[clave] = f"R.raw.{nombre}"

# # Imprimir el resultado en el formato deseado
# for clave, valor in listadoImagenes.items():
#     print(f'listadoImagenes["{clave}"] = {valor}')
