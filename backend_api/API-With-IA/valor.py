import re
import random

# Diccionario final
resultado = {}

# Función para reemplazar una vocal aleatoria por _
def reemplazar_vocal(palabra):
    vocales = 'aeiouAEIOU'
    indices = [i for i, c in enumerate(palabra) if c in vocales]
    
    if not indices:
        return palabra, ""  # No hay vocales

    indice = random.choice(indices)
    vocal = palabra[indice]
    modificada = palabra[:indice] + "_" + palabra[indice+1:]
    return modificada, vocal.lower()

# Leer el archivo
with open("./output_sin_repetidos.txt", "r", encoding="utf-8") as f:
    for linea in f:
        match = re.search(r'pictogramas\.add\("([^"]+)"\);', linea)
        if match:
            nombre = match.group(1)
            clave, vocal = reemplazar_vocal(nombre)
            if vocal:  # Asegura que haya al menos una vocal
                resultado[clave] = vocal

# Mostrar resultado
print(resultado)
