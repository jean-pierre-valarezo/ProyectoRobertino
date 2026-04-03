import 'dart:convert';
import 'dart:typed_data';

import 'package:control_robertino/Repository.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:socket_io_client/socket_io_client.dart';
import 'package:lottie/lottie.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Controles de Robertiño',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
            seedColor: const Color.fromARGB(255, 31, 94, 175)),
        useMaterial3: true,
      ),
      routes: {
        '/': (context) => MyHomePage(title: 'Flutter Demo Home Page'),
        '/ollama': (context) => ChatOllama()
      },
      debugShowCheckedModeBanner: false,
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  int valor = 0;

  late Socket socket;
  final ValueNotifier<String> palabraNotifier = ValueNotifier('');
  final ValueNotifier<Uint8List?> imagenNotifier = ValueNotifier(null);

  static const platform = MethodChannel('ip.channel/ifconfig');
  @override
  void initState() {
    super.initState();
    activateAPI();


    _controller = AnimationController(vsync: this);
  
    socket = io('http://localhost:5000', <String, dynamic>{
      'transports': ['websocket'],
      'autoConnect': true,
    });

    socket.onConnect((_) {
      print('Conectado al servidor WebSocket');
    });

    socket.on('response', (data) {
      try {
        if (data.containsKey('data') && data['data'] != null) {
          palabraNotifier.value = data['data'];
          imagenNotifier.value = null;
        } else if (data.containsKey('image') && data['image'] != null) {
          palabraNotifier.value = '';
          imagenNotifier.value = base64Decode(data['image']);
        }
      } catch (e) {
        print('Error al decodificar o actualizar estado: $e');
      }
    });
  }

  @override
  void dispose() {
    socket.dispose();
    _controller.dispose();
    super.dispose();
  }

  final List<Widget> _pages = [
    MyHomePage(title: '',),
    ChatOllama(),
  ];

  @override
  Widget build(BuildContext context) {
    // Tamaño fijo para la animación Lottie
    const double lottieHeight = 360;
    const double respuestaHeight = 300;

    return Scaffold(
      bottomNavigationBar: BottomNavigationBar(onTap: (int index) {
        setState(() {
          valor = index;
        });
      }, items: [
        BottomNavigationBarItem(icon: Icon(Icons.home), label: "Inicio"),
        BottomNavigationBarItem(icon: Icon(Icons.chat), label: "Chat"),

      ]),
      appBar: AppBar(
        actions: [
          IconButton(
            onPressed: () {
              Navigator.push(
                  context, MaterialPageRoute(builder: (context) => const Config()));
            },
            icon: const Icon(Icons.settings),
          )
        ],
      ),
      backgroundColor: Colors.white,
      
      body: valor == 0 ? SingleChildScrollView(
        child: Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              const Text(
                'ROBERTIÑO',
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1.2,
                  color: Color(0xFF047BCA),
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 20),

              // Contenedor fijo para la animación Lottie
              SizedBox(
                height: lottieHeight,
                child: Lottie.asset(
                  'assets/menucara.json',
                  controller: _controller,
                  fit: BoxFit.contain,
                  frameRate: FrameRate(60),
                  animate: true,
                  onLoaded: (composition) {
                    _controller.duration = composition.duration;
                    _controller.repeat();
                  },
                ),
              ),

              const SizedBox(height: 30),

              const Text(
                'Respuesta del niño:',
                style: TextStyle(
                  fontSize: 18,
                  color: Colors.grey,
                ),
              ),

              const SizedBox(height: 10),

              // Contenedor fijo para la respuesta (imagen o texto)
              SizedBox(
                height: respuestaHeight,
                child: ValueListenableBuilder<String>(
                  valueListenable: palabraNotifier,
                  builder: (context, palabra, _) {
                    return ValueListenableBuilder<Uint8List?>(
                      valueListenable: imagenNotifier,
                      builder: (context, imagen, _) {
                        return RespuestaWidget(
                          palabra: palabra,
                          imageDecoded: imagen,
                        );
                      },
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
      ) : ChatOllama()
    );
  }

  void activateAPI() async {
    try {
      final ip = await platform.invokeMethod<String?>('prenderAPI', {'name': 'bob'});

      debugPrint(ip);
      
    } on PlatformException catch (e) {
      debugPrint(e.message);
    }
  }
}

class Config extends StatefulWidget {
  const Config({super.key});

  @override
  State<Config> createState() => _ConfigState();
}

class _ConfigState extends State<Config> {
  static const platform = MethodChannel('ip.channel/ifconfig');

  String? ipAddress;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios),
          color: Colors.black,
          onPressed: () {
            Navigator.pop(context);
          },
        ),
      ),
      body: Center(
        child: Column(
          children: [
            const SizedBox(height: 20),
            const Text(
              'Ajustes',
              style: TextStyle(fontSize: 50, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 30),
            const Text('Dirección de computador'),
            ElevatedButton(
              onPressed: getIPAddress,
              child: const Text('Extraer data'),
            ),
            const SizedBox(height: 10),
            Text('Dirección de maquina: $ipAddress'),
          ],
        ),
      ),
    );
  }

  Future<void> getIPAddress() async {
    try {
      final ip = await platform.invokeMethod<String?>('getIp', {'name': 'bob'});

      setState(() {
        ipAddress = ip;
      });
    } on PlatformException catch (e) {
      debugPrint(e.message);
    }
  }
}

class RespuestaWidget extends StatelessWidget {
  final String palabra;
  final Uint8List? imageDecoded;

  const RespuestaWidget(
      {super.key, required this.palabra, required this.imageDecoded});

  @override
  Widget build(BuildContext context) {
    if (imageDecoded != null) {
      return Image.memory(
        imageDecoded!,
        fit: BoxFit.contain,
        width: double.infinity,
        height: double.infinity,
      );
    } else {
      return Center(
        child: Text(
          palabra.toUpperCase(),
          style: const TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.w600,
            color: Colors.black87,
          ),
          textAlign: TextAlign.center,
        ),
      );
    }
  }
}


class ChatOllama extends StatefulWidget {
  
  const ChatOllama({super.key});                

  @override
  State<ChatOllama> createState() => _ChatOllamaState();
}

class _ChatOllamaState extends State<ChatOllama> {
  String texto = '';
  TextEditingController commentController = TextEditingController();
  FocusNode myFocusNode = FocusNode();
  ScrollController scrollController = ScrollController();

  List<String> listadoChat = <String>[];

  @override
  void initState() {
    super.initState();
    myFocusNode.requestFocus();
  }

  @override
  void dispose() {
    myFocusNode.dispose();
    commentController.dispose();
    scrollController.dispose();
    super.dispose();
  }

  void scrollToBottom() {
    Future.delayed(Duration(milliseconds: 300), () {
      scrollController.animateTo(
        scrollController.position.maxScrollExtent,
        duration: Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    });
  }

  Widget chatBubble(String text, bool isUser) {
    return Align(
      alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: EdgeInsets.symmetric(vertical: 6, horizontal: 12),
        padding: EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: isUser ? Colors.grey.shade300: Colors.blueAccent ,
          borderRadius: BorderRadius.only(
            topLeft: Radius.circular(16),
            topRight: Radius.circular(16),
            bottomLeft: isUser ? Radius.circular(16) : Radius.circular(0),
            bottomRight: isUser ? Radius.circular(0) : Radius.circular(16),
          ),
        ),
        child: Text(
          text,
          style: TextStyle(
            color: isUser ? Colors.black87 : Colors.white,
            fontSize: 16,
          ),
        ),
      ),
    );
  }

  Widget chatList() {
    return ListView.builder(
      controller: scrollController,
      padding: EdgeInsets.only(top: 16, bottom: 80),
      itemCount: listadoChat.length,
      itemBuilder: (context, index) {
        final isUser = index % 2 == 0;
        return chatBubble(listadoChat[index], isUser);
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color(0xfff1f1f1),
      body: RawKeyboardListener(
        focusNode: myFocusNode,
        onKey: (RawKeyEvent event) {
          if (event is RawKeyDownEvent &&
              event.logicalKey == LogicalKeyboardKey.backspace) {
            final text = commentController.text;
            if (text.isNotEmpty) {
              setState(() {
                commentController.text = text.substring(0, text.length - 1);
                commentController.selection = TextSelection.fromPosition(
                  TextPosition(offset: commentController.text.length),
                );
              });
            }
          }
        },
        child: SafeArea(
          child: chatList(),
        ),
      ),
      bottomNavigationBar: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 6),
        child: Row(
          children: [
            Expanded(
              child: TextField(
                focusNode: myFocusNode,
                controller: commentController,
                decoration: InputDecoration(
                  hintText: 'Pídele algo a Gemma :)',
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(30),
                  ),
                  contentPadding:
                      EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                ),
              ),
            ),
            SizedBox(width: 8),
            CircleAvatar(
              backgroundColor: Colors.blueAccent,
              child: IconButton(
                icon: Icon(Icons.send, color: Colors.white),
                onPressed: () async {
                  final texto = commentController.text.trim();
                  if (texto.isEmpty) return;

                  setState(() {
                    listadoChat.add(texto);
                    commentController.clear();
                  });
                  scrollToBottom();

                  final repo = Repository();
                  String response = await repo.ollama(texto);

                  setState(() {
                    listadoChat.add(response); 
                  });
                  scrollToBottom();
                },
              ),
            )
          ],
        ),
      ),
    );
  }
}
