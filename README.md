# Continuous Speech to Text

A highly robust, continuous speech-to-text plugin for Flutter (Android only).

Unlike standard speech-to-text plugins that time out after a short pause or play annoying "beep" sounds every time they restart, `continuous_speech_to_text` is designed for uninterrupted, truly continuous voice recognition.

## Features

* **Truly Continuous**: Pausing or thinking while speaking will not terminate the microphone session.
* **No Beep Sounds**: Seamlessly and silently reconnects to the Android SpeechRecognizer in the background without triggering system audio feedback.
* **Custom Locales**: Easily pass any locale (e.g., `en-IN`, `hi-IN`, `gu-IN`) directly to the recognizer.
* **Real-time Results**: Streams partial and final recognized text events continuously.

## Installation

Add it to your `pubspec.yaml`:
```yaml
dependencies:
  continuous_speech_to_text: ^0.0.1
```

## Usage

```dart
import 'package:continuous_speech_to_text/continuous_speech_to_text.dart';

final _speechToText = ContinuousSpeechToText();

// Listen to the speech events
_speechToText.onSpeechEvent.listen((eventMap) {
  final event = eventMap['event'];
  final data = eventMap['data'];

  if (event == 'partialResult' || event == 'result') {
    print('Recognized: $data');
  } else if (event == 'error') {
    print('Error: $data');
  }
});

// Start listening (defaults to en-IN)
_speechToText.startListening(localeId: 'en-IN');

// Stop listening
_speechToText.stopListening();
```

## Platform Support
* **Android**: Supported (Android 5.0+)
* **iOS**: Not currently supported. Pull requests are welcome!
