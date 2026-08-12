import 'package:flutter/material.dart';
import 'package:speech_to_text/speech_to_text.dart';
import 'package:url_launcher/url_launcher.dart';

void main() {
  runApp(const JaaduApp());
}

class JaaduApp extends StatelessWidget {
  const JaaduApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Jaadu',
      theme: ThemeData(
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF101218),
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.deepPurple,
          brightness: Brightness.dark,
        ),
      ),
      home: const JaaduHome(),
    );
  }
}

class JaaduHome extends StatefulWidget {
  const JaaduHome({super.key});

  @override
  State<JaaduHome> createState() => _JaaduHomeState();
}

class _JaaduHomeState extends State<JaaduHome> {
  final SpeechToText speech = SpeechToText();

  bool isListening = false;
  bool speechAvailable = false;

  String heardText = 'नमस्ते! मैं जादू हूँ।';

  @override
  void initState() {
    super.initState();
    initializeSpeech();
  }

  Future<void> initializeSpeech() async {
    final available = await speech.initialize(
      onError: (error) {
        if (!mounted) return;

        setState(() {
          heardText = 'Speech recognition में समस्या हुई।';
          isListening = false;
        });
      },
    );

    if (!mounted) return;

    setState(() {
      speechAvailable = available;
    });
  }

  Future<void> startListening() async {
    if (!speechAvailable) {
      await initializeSpeech();
    }

    if (!speechAvailable) {
      setState(() {
        heardText = 'Speech Recognition उपलब्ध नहीं है।';
      });
      return;
    }

    setState(() {
      isListening = true;
      heardText = 'मैं सुन रहा हूँ...';
    });

    await speech.listen(
      localeId: 'hi_IN',
      listenFor: const Duration(seconds: 10),
      pauseFor: const Duration(seconds: 3),
      onResult: (result) {
        if (!mounted) return;

        setState(() {
          heardText = result.recognizedWords;
        });

        if (result.finalResult) {
          handleCommand(result.recognizedWords);
        }
      },
    );
  }

  Future<void> stopListening() async {
    await speech.stop();

    if (!mounted) return;

    setState(() {
      isListening = false;
    });
  }

  Future<void> handleCommand(String command) async {
    final text = command.toLowerCase().trim();

    // YouTube
    if (containsAny(text, [
      'youtube',
      'यूट्यूब',
      'यूट्यूब खोलो',
    ])) {
      await openUrl('https://www.youtube.com');
      return;
    }

    // Instagram
    if (containsAny(text, [
      'instagram',
      'इंस्टाग्राम',
      'इंस्टा',
      'रील',
    ])) {
      await openUrl('https://www.instagram.com');
      return;
    }

    // WhatsApp
    if (containsAny(text, [
      'whatsapp',
      'व्हाट्सएप',
      'वाट्सएप',
    ])) {
      await openUrl('https://wa.me/');
      return;
    }

    // Chrome
    if (containsAny(text, [
      'chrome',
      'क्रोम',
      'गूगल क्रोम',
    ])) {
      await openUrl('https://www.google.com');
      return;
    }

    // Google Maps
    if (containsAny(text, [
      'maps',
      'map',
      'मैप',
      'मैप्स',
      'गूगल मैप',
      'गूगल मैप्स',
    ])) {
      await openUrl('https://maps.google.com');
      return;
    }

    // Music
    if (containsAny(text, [
      'music',
      'म्यूजिक',
      'गाना',
      'गाने',
      'संगीत',
    ])) {
      await openUrl('https://music.youtube.com');
      return;
    }

    // Camera
    if (containsAny(text, [
      'camera',
      'कैमरा',
    ])) {
      await openUrl('camera:');
      return;
    }

    // Gallery / Photos
    if (containsAny(text, [
      'gallery',
      'गैलरी',
      'photos',
      'फोटो',
      'तस्वीर',
    ])) {
      await openUrl('content://media/internal/images/media');
      return;
    }

    // Phone / Dialer
    if (containsAny(text, [
      'phone',
      'फोन',
      'डायलर',
      'कॉल',
      'call',
    ])) {
      await openUrl('tel:');
      return;
    }

    // Settings
    if (containsAny(text, [
      'settings',
      'setting',
      'सेटिंग',
      'सेटिंग्स',
    ])) {
      await openUrl('app-settings:');
      return;
    }

    if (!mounted) return;

    setState(() {
      heardText =
          'मैंने सुना:\n"$command"\n\n'
          'यह command अभी मेरे पास उपलब्ध नहीं है।';
    });
  }

  bool containsAny(String text, List<String> words) {
    for (final word in words) {
      if (text.contains(word)) {
        return true;
      }
    }

    return false;
  }

  Future<void> openUrl(String url) async {
    try {
      final uri = Uri.parse(url);

      final opened = await launchUrl(
        uri,
        mode: LaunchMode.externalApplication,
      );

      if (!opened && mounted) {
        setState(() {
          heardText = 'यह app या service नहीं खुल पाई।';
        });
      }
    } catch (e) {
      if (!mounted) return;

      setState(() {
        heardText = 'Command चलाने में समस्या हुई।';
      });
    }
  }

  @override
  void dispose() {
    speech.stop();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'जादू',
          style: TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 24,
          ),
        ),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Column(
          children: [
            const SizedBox(height: 55),

            const Icon(
              Icons.auto_awesome,
              size: 80,
              color: Colors.deepPurpleAccent,
            ),

            const SizedBox(height: 25),

            const Text(
              'नमस्ते! मैं जादू हूँ',
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 15),

            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 30),
              child: Text(
                heardText,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 18,
                  color: Colors.white70,
                ),
              ),
            ),

            const Spacer(),

            GestureDetector(
              onTap: isListening ? stopListening : startListening,
              child: Container(
                width: 105,
                height: 105,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: isListening
                      ? Colors.red
                      : Colors.deepPurple,
                  boxShadow: [
                    BoxShadow(
                      color: Colors.deepPurple.withOpacity(0.35),
                      blurRadius: 25,
                      spreadRadius: 5,
                    ),
                  ],
                ),
                child: Icon(
                  isListening ? Icons.stop : Icons.mic,
                  size: 48,
                  color: Colors.white,
                ),
              ),
            ),

            const SizedBox(height: 25),

            Text(
              isListening ? 'बोलिए...' : 'माइक दबाकर बोलें',
              style: const TextStyle(
                fontSize: 16,
                color: Colors.white60,
              ),
            ),

            const SizedBox(height: 55),
          ],
        ),
      ),
    );
  }
}
      
