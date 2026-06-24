import 'package:flutter/material.dart';
import 'package:continuous_speech_to_text/continuous_speech_to_text.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _continuousSpeechToText = ContinuousSpeechToText();
  String _recognizedText = '';
  bool _isListening = false;
  String _errorMessage = '';

  @override
  void initState() {
    super.initState();
    _continuousSpeechToText.onSpeechEvent.listen((eventMap) {
      final event = eventMap['event'];
      final data = eventMap['data'];

      setState(() {
        if (event == 'partialResult' || event == 'result') {
          _recognizedText = data as String;
          _errorMessage = '';
        } else if (event == 'error') {
          _errorMessage = data as String;
          _isListening = false;
        } else if (event == 'readyForSpeech') {
          _isListening = true;
          _errorMessage = '';
        } else if (event == 'stopped') {
          _isListening = false;
        }
      });
    });
  }

  void _toggleListening() {
    if (_isListening) {
      _continuousSpeechToText.stopListening();
    } else {
      setState(() {
        _recognizedText = '';
        _errorMessage = '';
      });
      _continuousSpeechToText.startListening(localeId: 'en-IN');
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(title: const Text('Continuous Speech to Text')),
        body: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (_errorMessage.isNotEmpty)
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.redAccent,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  margin: const EdgeInsets.only(bottom: 16),
                  child: Text(_errorMessage, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
                ),
              Expanded(
                child: Container(
                  padding: const EdgeInsets.all(16),
                  decoration: BoxDecoration(
                    color: Colors.grey[200],
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: SingleChildScrollView(
                    child: Text(
                      _recognizedText.isEmpty ? 'Tap the mic to start speaking...' : _recognizedText,
                      style: const TextStyle(fontSize: 20, height: 1.5),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 30),
              Center(
                child: SizedBox(
                  width: 80,
                  height: 80,
                  child: FloatingActionButton(
                    onPressed: _toggleListening,
                    backgroundColor: _isListening ? Colors.red : Colors.blueAccent,
                    shape: const CircleBorder(),
                    child: Icon(_isListening ? Icons.stop_rounded : Icons.mic_rounded, size: 36, color: Colors.white),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
