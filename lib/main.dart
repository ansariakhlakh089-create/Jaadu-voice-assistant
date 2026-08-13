import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:speech_to_text/speech_to_text.dart';

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
  static const MethodChannel nativeChannel =
      MethodChannel('jaadu/native');

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

  // ============================================================
  // MAIN VOICE COMMAND PROCESSOR
  // ============================================================

  Future<void> handleCommand(String command) async {
    final text = command.toLowerCase().trim();

    // ----------------------------------------------------------
    // 1. CAMERA
    // ----------------------------------------------------------

    if (containsAny(text, [
      'camera',
      'कैमरा',
      'कैमरा खोलो',
      'कैमरा चालू करो',
    ])) {
      await callNative('openCamera');
      return;
    }

    // ----------------------------------------------------------
    // 2. GALLERY
    // ----------------------------------------------------------

    if (containsAny(text, [
      'gallery',
      'गैलरी',
      'photos',
      'फोटो',
      'फोटोज',
      'तस्वीर',
      'तस्वीरें',
    ])) {
      await callNative('openGallery');
      return;
    }

    // ----------------------------------------------------------
    // 3. PHONE / DIALER
    // ----------------------------------------------------------

    if (containsAny(text, [
      'dialer',
      'डायलर',
      'फोन खोलो',
      'फोन चालू करो',
      'phone',
    ])) {
      await callNative('openPhone');
      return;
    }

    // ----------------------------------------------------------
    // 4. CONTACT CALL
    // ----------------------------------------------------------

    if (text.contains('को कॉल करो') ||
        text.contains('को फोन करो') ||
        text.contains('को कॉल लगाओ') ||
        text.contains('को फोन लगाओ') ||
        text.contains('call करो') ||
        text.contains('call लगाओ')) {
      String contactName = text;

      contactName = contactName
          .replaceAll('को कॉल करो', '')
          .replaceAll('को फोन करो', '')
          .replaceAll('को कॉल लगाओ', '')
          .replaceAll('को फोन लगाओ', '')
          .replaceAll('call करो', '')
          .replaceAll('call लगाओ', '')
          .trim();

      if (contactName.isNotEmpty) {
        await callContact(contactName);
        return;
      }
    }

    // ----------------------------------------------------------
    // 5. SETTINGS
    // ----------------------------------------------------------

    if (containsAny(text, [
      'settings',
      'setting',
      'सेटिंग',
      'सेटिंग्स',
      'सेटिंग खोलो',
    ])) {
      await callNative('openSettings');
      return;
    }

    // ----------------------------------------------------------
    // 6. WIFI
    // ----------------------------------------------------------

    if (containsAny(text, [
      'wifi',
      'wi-fi',
      'वाईफाई',
      'वाई फाई',
      'वाई-फाई',
      'वाईफाई खोलो',
      'wifi खोलो',
      'wifi चालू करो',
      'wifi on करो',
    ])) {
      await callNative('openWifi');
      return;
    }

    // ----------------------------------------------------------
    // 7. BLUETOOTH
    // ----------------------------------------------------------

    if (containsAny(text, [
      'bluetooth',
      'ब्लूटूथ',
      'ब्लूटूथ खोलो',
      'bluetooth चालू करो',
      'bluetooth on करो',
    ])) {
      await callNative('openBluetooth');
      return;
    }

    // ----------------------------------------------------------
    // 8. LOCATION
    // ----------------------------------------------------------

    if (containsAny(text, [
      'location',
      'लोकेशन',
      'स्थान',
      'लोकेशन खोलो',
      'लोकेशन चालू करो',
      'location on करो',
    ])) {
      await callNative('openLocation');
      return;
    }

    // ----------------------------------------------------------
    // 9. YOUTUBE
    // ----------------------------------------------------------

    if (containsAny(text, [
      'youtube',
      'यूट्यूब',
      'यूट्यूब खोलो',
    ])) {
      await openUrl('https://www.youtube.com');
      return;
    }

    // ----------------------------------------------------------
    // 10. INSTAGRAM
    // ----------------------------------------------------------

    if (containsAny(text, [
      'instagram',
      'इंस्टाग्राम',
      'इंस्टा',
      'रील',
    ])) {
      await openUrl('https://www.instagram.com');
      return;
    }

    // ----------------------------------------------------------
    // 11. WHATSAPP
    // ----------------------------------------------------------

    if (containsAny(text, [
      'whatsapp',
      'व्हाट्सएप',
      'वाट्सएप',
    ])) {
      await openInstalledApp('whatsapp');
      return;
    }

    // ----------------------------------------------------------
    // 12. CHROME
    // ----------------------------------------------------------

    if (containsAny(text, [
      'chrome',
      'क्रोम',
      'गूगल क्रोम',
    ])) {
      await openInstalledApp('chrome');
      return;
    }

    // ----------------------------------------------------------
    // 13. MAPS
    // ----------------------------------------------------------

    if (containsAny(text, [
      'maps',
      'map',
      'मैप',
      'मैप्स',
      'गूगल मैप',
      'गूगल मैप्स',
    ])) {
      await openInstalledApp('maps');
      return;
    }

    // ----------------------------------------------------------
    // 14. VOLUME UP
    // ----------------------------------------------------------

    if (containsAny(text, [
      'volume बढ़ाओ',
      'वॉल्यूम बढ़ाओ',
      'आवाज बढ़ाओ',
      'आवाज़ बढ़ाओ',
      'sound बढ़ाओ',
      'आवाज तेज करो',
      'आवाज़ तेज करो',
      'वॉल्यूम तेज करो',
    ])) {
      await callNative('volumeUp');
      return;
    }

    // ----------------------------------------------------------
    // 15. VOLUME DOWN
    // ----------------------------------------------------------

    if (containsAny(text, [
      'volume कम करो',
      'वॉल्यूम कम करो',
      'आवाज कम करो',
      'आवाज़ कम करो',
      'sound कम करो',
      'आवाज धीमी करो',
      'आवाज़ धीमी करो',
    ])) {
      await callNative('volumeDown');
      return;
    }

    // ----------------------------------------------------------
    // 16. MUTE
    // ----------------------------------------------------------

    if (containsAny(text, [
      'volume mute करो',
      'वॉल्यूम म्यूट करो',
      'आवाज बंद करो',
      'आवाज़ बंद करो',
      'sound बंद करो',
      'म्यूट करो',
    ])) {
      await callNative('volumeMute');
      return;
    }

    // ----------------------------------------------------------
    // 17. VOLUME FULL
    // ----------------------------------------------------------

    if (containsAny(text, [
      'volume full करो',
      'वॉल्यूम फुल करो',
      'आवाज full करो',
      'आवाज़ फुल करो',
      'volume maximum करो',
      'वॉल्यूम maximum करो',
      'आवाज पूरी करो',
      'आवाज़ पूरी करो',
    ])) {
      await callNative('volumeMax');
      return;
    }

    // ----------------------------------------------------------
    // 18. MUSIC PLAY
    // ----------------------------------------------------------

    if (containsAny(text, [
      'गाना चलाओ',
      'गाना बजाओ',
      'music चलाओ',
      'music बजाओ',
      'play music',
      'play song',
      'म्यूजिक चलाओ',
      'म्यूजिक बजाओ',
    ])) {
      await callNative('musicPlay');
      return;
    }

    // ----------------------------------------------------------
    // 19. MUSIC PAUSE
    // ----------------------------------------------------------

    if (containsAny(text, [
      'गाना रोक दो',
      'गाना बंद करो',
      'गाना pause करो',
      'music बंद करो',
      'music रोक दो',
      'pause music',
      'stop music',
      'म्यूजिक रोक दो',
      'म्यूजिक बंद करो',
    ])) {
      await callNative('musicPause');
      return;
    }

    // ----------------------------------------------------------
    // 20. NEXT SONG
    // ----------------------------------------------------------

    if (containsAny(text, [
      'अगला गाना',
      'अगला song',
      'next song',
      'next music',
      'अगला म्यूजिक',
    ])) {
      await callNative('musicNext');
      return;
    }

    // ----------------------------------------------------------
    // 21. PREVIOUS SONG
    // ----------------------------------------------------------

    if (containsAny(text, [
      'पिछला गाना',
      'पिछला song',
      'previous song',
      'previous music',
      'पिछला म्यूजिक',
    ])) {
      await callNative('musicPrevious');
      return;
    }

    // ----------------------------------------------------------
    // 22. TORCH ON
    // ----------------------------------------------------------

    if (containsAny(text, [
      'टॉर्च चालू करो',
      'टॉर्च ऑन करो',
      'torch चालू करो',
      'torch on करो',
      'flashlight चालू करो',
      'फ्लैशलाइट चालू करो',
    ])) {
      await callNative('torchOn');
      return;
    }

    // ----------------------------------------------------------
    // 23. TORCH OFF
    // ----------------------------------------------------------

    if (containsAny(text, [
      'टॉर्च बंद करो',
      'टॉर्च ऑफ करो',
      'torch बंद करो',
      'torch off करो',
      'flashlight बंद करो',
      'फ्लैशलाइट बंद करो',
    ])) {
      await callNative('torchOff');
      return;
    }

    // ==========================================================
    // 24. DYNAMIC INSTALLED APP FINDER
    // ==========================================================
    //
    // उदाहरण:
    // "मेरे फोन में Spotify खोलो"
    // "फोन में Calculator चालू करो"
    // "मेरे मोबाइल में Files खोलो"
    // "WhatsApp application खोलो"
    //
    // अगर ऊपर वाला कोई specific command match नहीं हुआ,
    // तो जादू Android से installed launchable apps खोजेगा।
    // ==========================================================

    final appName = extractAppName(text);

    if (appName.isNotEmpty) {
      final opened = await openInstalledApp(appName);

      if (opened) {
        return;
      }
    }

    // ----------------------------------------------------------
    // UNKNOWN COMMAND
    // ----------------------------------------------------------

    if (!mounted) return;

    setState(() {
      heardText =
          'मैंने सुना:\n"$command"\n\n'
          'यह command अभी उपलब्ध नहीं है।';
    });
  }

  // ============================================================
  // EXTRACT POSSIBLE APP NAME
  // ============================================================

  String extractAppName(String text) {
    String result = text;

    final removeWords = [
      'मेरे फोन में',
      'मेरे मोबाइल में',
      'फोन में',
      'मोबाइल में',
      'मेरे फोन का',
      'मेरे मोबाइल का',
      'फोन का',
      'मोबाइल का',
      'application',
      'app',
      'ऐप',
      'एप',
      'खोलो',
      'खोल',
      'खोल दो',
      'चालू करो',
      'चालू कर दो',
      'चालू',
      'ओपन करो',
      'ओपन कर दो',
      'open करो',
      'open कर दो',
      'open',
    ];

    for (final word in removeWords) {
      result = result.replaceAll(word, ' ');
    }

    return result.replaceAll(RegExp(r'\s+'), ' ').trim();
  }

  // ============================================================
  // OPEN INSTALLED APP
  // ============================================================

  Future<bool> openInstalledApp(String appName) async {
    try {
      final result = await nativeChannel.invokeMethod<bool>(
        'openInstalledApp',
        {
          'appName': appName,
        },
      );

      return result == true;
    } on PlatformException catch (e) {
      if (!mounted) return false;

      setState(() {
        heardText =
            'ऐप खोलने में समस्या: ${e.message ?? 'App नहीं मिला'}';
      });

      return false;
    }
  }

  // ============================================================
  // NATIVE COMMAND
  // ============================================================

  Future<void> callNative(String method) async {
    try {
      await nativeChannel.invokeMethod(method);
    } on PlatformException catch (e) {
      if (!mounted) return;

      setState(() {
        heardText =
            'Command चलाने में समस्या: ${e.message}';
      });
    }
  }

  // ============================================================
  // CONTACT CALL
  // ============================================================

  Future<void> callContact(String name) async {
    try {
      await nativeChannel.invokeMethod(
        'callContact',
        {
          'name': name,
        },
      );
    } on PlatformException catch (e) {
      if (!mounted) return;

      setState(() {
        heardText =
            'Contact call करने में समस्या: ${e.message}';
      });
    }
  }

  // ============================================================
  // URL
  // ============================================================

  Future<void> openUrl(String url) async {
    try {
      await nativeChannel.invokeMethod(
        'openUrl',
        {
          'url': url,
        },
      );
    } catch (_) {
      if (!mounted) return;

      setState(() {
        heardText =
            'यह command अभी नहीं खुल पाई।';
      });
    }
  }

  // ============================================================
  // HELPER
  // ============================================================

  bool containsAny(String text, List<String> words) {
    for (final word in words) {
      if (text.contains(word)) {
        return true;
      }
    }

    return false;
  }

  @override
  void dispose() {
    speech.stop();
    super.dispose();
  }

  // ============================================================
  // UI
  // ============================================================

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
              onTap: isListening
                  ? stopListening
                  : startListening,
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
                  isListening
                      ? Icons.stop
                      : Icons.mic,
                  size: 48,
                  color: Colors.white,
                ),
              ),
            ),

            const SizedBox(height: 25),

            Text(
              isListening
                  ? 'बोलिए...'
                  : 'माइक दबाकर बोलें',
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
