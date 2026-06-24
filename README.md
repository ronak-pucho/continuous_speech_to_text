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

### Multiple Languages Support

You can pass a specific `localeId` to `startListening` to recognize speech in different languages. This relies on the languages available on the device's Android SpeechRecognizer. Ensure the language package is downloaded on the device for offline use or accurate recognition.

Examples of some common locale tags:
- English (India): `en-IN`
- Hindi (India): `hi-IN`
- Bengali (India): `bn-IN`
- Gujarati (India): `gu-IN`
- Kannada (India): `kn-IN`
- Malayalam (India): `ml-IN`
- Marathi (India): `mr-IN`
- Tamil (India): `ta-IN`
- Telugu (India): `te-IN`
- Punjabi (India): `pa-IN`
- Urdu (India): `ur-IN`
- Odia (India): `or-IN`
- Assamese (India): `as-IN`
- English (US): `en-US`
- Spanish (Spain): `es-ES`
- French (France): `fr-FR`
- German (Germany): `de-DE`

```dart
// Start listening in Hindi
_speechToText.startListening(localeId: 'hi-IN');

// Start listening in Spanish
_speechToText.startListening(localeId: 'es-ES');
```

You can check the full example in the `example` folder where a dropdown is implemented to switch between different languages dynamically.

## Platform Support
* **Android**: Supported (Android 5.0+)
* **iOS**: Not currently supported. Pull requests are welcome!
