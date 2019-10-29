#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint flutter_toutiao_adsdk.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'flutter_toutiao_adsdk'
  s.version          = '1.0.0'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  # s.dependency 'Bytedance-UnionAD'
  s.vendored_frameworks ="Frameworks/*.framework"
  s.resource = "Frameworks/*.bundle"

  s.frameworks = "StoreKit","MobileCoreServices","WebKit","MediaPlayer","CoreMedia","CoreLocation","AVFoundation","CoreTelephony","SystemConfiguration","AdSupport","CoreMotion","ImageIO","Accelerate"
  s.libraries = "z","c++","resolv.9"

  s.platform = :ios,'8.0'

  # Flutter.framework does not contain a i386 slice. Only x86_64 simulators are supported.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES','VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
end
