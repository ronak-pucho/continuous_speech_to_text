import 'continuous_speech_to_text_platform_interface.dart';

class ContinuousSpeechToText {
  /// Starts the speech recognition engine continuously.
  /// 
  /// The [localeId] parameter specifies the language to use (e.g., 'en-IN', 'hi-IN', 'gu-IN').
  /// If omitted, it defaults to 'en-IN'.
  Future<void> startListening({String localeId = 'en-IN'}) {
    return ContinuousSpeechToTextPlatform.instance.startListening(localeId: localeId);
  }

  /// Stops the speech recognition engine.
  Future<void> stopListening() {
    return ContinuousSpeechToTextPlatform.instance.stopListening();
  }

  /// A stream of events from the native speech recognizer.
  /// 
  /// Emits maps containing 'event' (String) and 'data' (String).
  /// Possible events: 'result', 'partialResult', 'readyForSpeech', 'error'.
  Stream<Map<String, dynamic>> get onSpeechEvent {
    return ContinuousSpeechToTextPlatform.instance.speechEvents;
  }
}
