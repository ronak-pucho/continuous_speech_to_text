import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'continuous_speech_to_text_platform_interface.dart';

/// An implementation of [ContinuousSpeechToTextPlatform] that uses method channels.
class MethodChannelContinuousSpeechToText extends ContinuousSpeechToTextPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('continuous_speech_to_text');

  /// The event channel used to receive speech events from the native platform.
  @visibleForTesting
  final eventChannel = const EventChannel('continuous_speech_to_text/events');

  @override
  Future<void> startListening({String localeId = 'en-IN'}) async {
    await methodChannel.invokeMethod('startListening', {'localeId': localeId});
  }

  @override
  Future<void> stopListening() async {
    await methodChannel.invokeMethod('stopListening');
  }

  @override
  Stream<Map<String, dynamic>> get speechEvents {
    return eventChannel.receiveBroadcastStream().map((event) => Map<String, dynamic>.from(event));
  }
}
