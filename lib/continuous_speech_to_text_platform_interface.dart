import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'continuous_speech_to_text_method_channel.dart';

abstract class ContinuousSpeechToTextPlatform extends PlatformInterface {
  /// Constructs a ContinuousSpeechToTextPlatform.
  ContinuousSpeechToTextPlatform() : super(token: _token);

  static final Object _token = Object();

  static ContinuousSpeechToTextPlatform _instance = MethodChannelContinuousSpeechToText();

  /// The default instance of [ContinuousSpeechToTextPlatform] to use.
  ///
  /// Defaults to [MethodChannelContinuousSpeechToText].
  static ContinuousSpeechToTextPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ContinuousSpeechToTextPlatform] when
  /// they register themselves.
  static set instance(ContinuousSpeechToTextPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  /// Starts listening to speech.
  Future<void> startListening({String localeId = 'en-IN'}) {
    throw UnimplementedError('startListening() has not been implemented.');
  }

  /// Stops listening to speech.
  Future<void> stopListening() {
    throw UnimplementedError('stopListening() has not been implemented.');
  }

  /// A stream of events from the native speech recognizer.
  Stream<Map<String, dynamic>> get speechEvents {
    throw UnimplementedError('speechEvents has not been implemented.');
  }
}
