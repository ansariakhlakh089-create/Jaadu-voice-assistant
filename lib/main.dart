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
  final SpeechToText _speech = SpeechToText();

  bool _isListening = false;
  bool _speechReady = false;

  String _heardText = 'मैं आपकी बात सुनने के लिए तैयार हूँ।';

  @override
  void initState() {
    super.initState();
    _initializeSpeech();
  }

  Future<void> _initializeSpeech() async {
    final available = await _speech.initialize(
      onError: (error) {
        setState(() {
          _heardText = 'आवाज़ पहचानने में समस्या हुई।';
        });
      },
    );

    setState(() {
      _speechReady = available;
    });
  }

  Future<void> _startListening() async {
    if (!_speechReady) {
      await _initializeSpeech();
    }

    if (!_speechReady) {
      setState(() {
        _heardText = 'Speech recognition उपलब्ध नहीं है।';
      });
      return;
    }

    setState(() {
      _isListening = true;
      _heardText = 'मैं सुन रहा हूँ...';
    });

    await _speech.listen(
      localeId: 'hi_IN',
      onResult: (result) {
        setState(() {
          _heardText = result.recognizedWords;
        });

        if (result.finalResult) {
          _handleCommand(result.recognizedWords);
        }
      },
    );
  }

  Future<void> _stopListening() async {
    await _speech.stop();

    setState(() {
      _isListening = false;
    });
  }

  Future<void> _handleCommand(String command) async {
    final text = command.toLowerCase().trim();

    if (text.contains('youtube') ||
        text.contains('यूट्यूब')) {
      await _openUrl('https://www.youtube.com');
      return;
    }

    if (text.contains('instagram') ||
        text.contains('इंस्टाग्राम') ||
        text.contains('रील')) {
      await _openUrl('https://www.instagram.com');
      return;
    }

    if (text.contains('गाना') ||
        text.contains('music') ||
        text.contains('म्यूजिक')) {
      await _openUrl('https://music.youtube.com');
      return;
    }

    setState(() {
      _heardText = 'मैंने सुना: $command\n\nयह command अभी नहीं जोड़ी गई है।';
    });
  }

  Future<void> _openUrl(String url) async {
    final uri = Uri.parse(url);

    final success = await launchUrl(
      uri,
      mode: LaunchMode.externalApplication,
    );

    if (!success) {
      setState(() {
        _heardText = 'यह ऐप/लिंक नहीं खुल पाया।';
      });
    }
  }

  @override
  void dispose() {
    _speech.stop();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'जादू',
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
          ),
        ),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Column(
          children: [
            const SizedBox(height: 50),

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
                _heardText,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 18,
                  color: Colors.white70,
                ),
              ),
            ),

            const Spacer(),

            GestureDetector(
              onTap: _isListening
                  ? _stopListening
                  : _startListening,
              child: Container(
                width: 100,
                height: 100,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: _isListening
                      ? Colors.red
                      : Colors.deepPurple,
                  boxShadow: [
                    BoxShadow(
                      color: Colors.deepPurple.withOpacity(0.4),
                      blurRadius: 25,
                      spreadRadius: 5,
                    ),
                  ],
                ),
                child: Icon(
                  _isListening
                      ? Icons.stop
                      : Icons.mic,
                  size: 45,
                  color: Colors.white,
                ),
              ),
            ),

            const SizedBox(height: 25),

            Text(
              _isListening
                  ? 'बोलिए...'
                  : 'बोलने के लिए माइक दबाएँ',
              style: const TextStyle(
                fontSize: 16,
                color: Colors.white60,
              ),
            ),

            const SizedBox(height: 50),
          ],
        ),
      ),
    );
  }
}
