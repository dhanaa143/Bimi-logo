# Keep the JavaScript bridge methods reachable from the WebView's JS context.
-keepclassmembers class com.dhanaaspices.spicepro.MainActivity$AndroidBridge {
    public *;
}
