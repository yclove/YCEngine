# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/yclove/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# 공통
#빌드 후 mapping seed usage cofing 파일을 만들어주는 옵션
-printmapping map.txt
-printseeds seed.txt
-printusage usage.txt
-printconfiguration config.txt

# 소스 파일의 라인을 섞지 않는 옵션 (이거 안해주면 나중에 stacktrace보고 어느 line에서 오류가 난 것인지 확인 불가)
-keepattributes SourceFile,LineNumberTable

# 소스 파일 변수 명 바꾸는 옵션
-renamesourcefileattribute SourceFile

# 보통 라이브러리는 딱히 난독화 할 필요없을 때 이렇게 적어준다.
# -keep class or interface 라이브러리패키지명.** { *; }
# -keep class com.test.** {
#    public *;
#} // com.test 하위 클래스 중 public 메소드만 난독화하지 않음
-keep class com.melodigm.post.protocol.data.** { *; }

# Inner 클래스를 사용하는 경우 몇몇 변수가 난독화가 되는 일이 일어나기도 한다고 합니다. 이런 경우에는 다음과 같이 Inner 클래스도 난독화에서 제외하는 코드를 추가해줍니다.
-keepclassmembers class com.melodigm.post.protocol.data** { *; }

# warning 뜨는거 무시할때
# -ignorewarnings

# 지정해서 warning 무시할 때
# -dontwarn 패키지명.**

# 아래의 3가지 것들은 default 요소들이지만 중요한 option이라 설명한다.
#-dontoptimize #없애면 난독화 X
#-dontobfuscate #없애면 최적화 X
#-keepresourcexmlattributenames manifest/** #없애면 manifest 난독화 X

# 페이스북
-dontwarn com.squareup.okhttp.**
-dontwarn com.google.appengine.api.urlfetch.**
-dontwarn rx.**
-dontwarn retrofit.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

# 카카오링크
-keep class com.kakao.** { *; }
-keepattributes Signature
# 특정 클래스의 맴버 원상태 유지
-keepclassmembers class * {
  public static <fields>;
  public *;
}
-dontwarn android.support.v4.**,org.slf4j.**,com.google.android.gms.**