import 'dart:convert' as convert;

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;


class Repository {
  final String url = 'http://127.0.0.1:11434/api/chat';


  Future<String> ollama(String prompt) async {
  final client = http.Client();

  final request = http.Request(
    'POST',
    Uri.parse('http://localhost:11434/api/chat'),
  );

  request.headers['Content-Type'] = 'application/json';
  request.body = convert.jsonEncode({
    "model": "gemma3:1b",
    "stream": true,
    "messages": [
      {"role": "user", "content": prompt}
    ]
  });

  final streamedResponse = await client.send(request);

  if (streamedResponse.statusCode != 200) {
    return 'Error al conectar con la API: ${streamedResponse.statusCode}';
  }

  String finalText = '';

  await streamedResponse.stream
      .transform(convert.utf8.decoder)
      .transform(const convert.LineSplitter())
      .forEach((line) {
    try {
      final jsonLine = convert.jsonDecode(line);

      final content = jsonLine['message']?['content'] ?? '';
      final done = jsonLine['done'] ?? false;

      if (content.isNotEmpty) {
        finalText += content;
       // print(content); // Mostrar en tiempo real (opcional)
      }

    } catch (e) {
      print('Error decodificando línea: $e');
    }
  });

  client.close();
  return finalText.trim();
}

  Map<String, dynamic> coomp(String dd){
    return convert.jsonDecode(dd);
  }

}

