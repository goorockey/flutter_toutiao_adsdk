import 'package:logger/logger.dart';

class Log {
  static final _logger = Logger(
    filter: null, // Use the default LogFilter (-> only log in debug mode)
    printer: PrettyPrinter(
      methodCount: 0, // number of method calls to be displayed
      errorMethodCount: 8, // number of method calls if stacktrace is provided
      lineLength: 120, // width of the output
      colors: false, // Colorful log messages
      printEmojis: true, // Print an emoji for each log message
      printTime: false, // Should each log print contain a timestamp
    ),
    output: null, // Use the default LogOutput (-> send everything to console)
  );

  Log._internal();
  static final _instance = Log._internal();
  factory Log() {
    return _instance;
  }

  static void v(dynamic message, [dynamic error, StackTrace stackTrace]) {
    _logger.v(message, error, stackTrace);
  }

  static void d(dynamic message, [dynamic error, StackTrace stackTrace]) {
    _logger.d(message, error, stackTrace);
  }

  static void i(dynamic message, [dynamic error, StackTrace stackTrace]) {
    _logger.i(message, error, stackTrace);
  }

  static void w(dynamic message, [dynamic error, StackTrace stackTrace]) {
    _logger.w(message, error, stackTrace);
  }

  static void e(dynamic message, [dynamic error, StackTrace stackTrace]) {
    _logger.e(message, error, stackTrace);
  }

  static void wtf(dynamic message, [dynamic error, StackTrace stackTrace]) {
    _logger.wtf(message, error, stackTrace);
  }
}
