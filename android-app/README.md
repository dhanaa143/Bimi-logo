# Spice Pro — Android app

This wraps `spice_pro_updated8.html` (the calculator/PI/PO/Packing List web app) in a
native Android WebView shell, so it installs and runs like a normal app — including
generating and sharing PDFs (WhatsApp/Email/Share sheet) and picking logo/signature/
seal images from the gallery — with **no internet connection required**. All libraries
(html2pdf.js, Font Awesome) are bundled inside the app under `assets/www/libs/`; the
app never depends on any CDN at runtime.

## How to open it

1. Open **Android Studio** → `File > Open` → select this `android-app` folder.
2. Let Gradle sync run. If Android Studio shows a banner saying the Gradle wrapper
   jar is missing, click the offered "OK" / "Create Gradle wrapper" action — Studio
   will generate `gradle/wrapper/gradle-wrapper.jar` for you automatically (only the
   jar binary was omitted from this drop; `gradle-wrapper.properties` already pins
   Gradle 8.9, matching Android Gradle Plugin 8.5.2).
3. Once sync finishes, press **Run ▶** on an emulator or a real device
   (minimum Android 8.0 / API 26).

If your installed Android Studio ships an older Gradle/AGP than what's pinned here,
just accept its "upgrade/downgrade Gradle" prompt on sync — the code itself has no
version-specific tricks.

## What's in here and why

- `app/src/main/assets/www/index.html` — a copy of the calculator app, with the two
  CDN tags (`cdnjs.cloudflare.com` for Font Awesome and html2pdf.js) repointed to the
  bundled local copies under `libs/`. If you update `spice_pro_updated8.html` at the
  repo root, re-copy it here and re-apply that same two-line swap (see
  "Updating the app content" below) — don't edit the CDN links back in, or PDF export
  will silently fail on a phone with no signal.
- `app/src/main/assets/www/libs/` — html2pdf.js 0.10.1 and Font Awesome 6.4.0 (solid,
  regular, brands), fetched from npm and vendored in so the app works fully offline.
- `MainActivity.java` — the WebView host:
  - Loads the page through **WebViewAssetLoader** at the virtual origin
    `https://appassets.androidplatform.net/assets/www/index.html`, not a `file://`
    URL. This is Google's recommended approach for local content: it gives the page
    a stable https-like origin (so `localStorage` — used for History, CRM, Fixed
    Costs, Seller Profile — persists correctly across app restarts) without the CORS
    / mixed-content restrictions `file://` pages run into.
  - Registers a `window.AndroidBridge` JavaScript interface with one method,
    `shareOrDownloadPdf(base64DataUri, filename, mode)`. The web app already calls
    this itself whenever it's running inside a WebView (see `isAndroidBridgeAvailable()`
    / `shareViaAndroidBridge()` in the HTML) — it decodes the PDF, writes it to
    `cacheDir/pdfs/`, and hands it to WhatsApp / a mail app / the system share sheet
    via a `FileProvider` content:// Uri, depending on which button the user tapped.
  - Implements `onShowFileChooser` so the logo/signature/seal `<input type="file">`
    fields open the real Android photo picker (plain WebView ignores file inputs
    without this).
  - Leaves `WebChromeClient`'s default `onJsAlert`/`onJsConfirm` behavior in place —
    that's what makes the app's many `alert()`/`confirm()` calls (Delete confirmations,
    "Duplicated!", etc.) show up as native dialogs; you must not remove the
    `setWebChromeClient` call.
  - Adds a "press back again to exit" guard so a stray back-press doesn't silently
    discard an in-progress invoice.
- `res/xml/file_paths.xml` + the `<provider>` entry in `AndroidManifest.xml` — required
  for the `FileProvider` used to share generated PDFs.
- Launcher icon (`res/drawable/ic_launcher_*.xml`, `res/mipmap-anydpi-v26/`) is a
  simple placeholder (dark-green background, white/amber spice mark) built as vector
  drawables so no binary image assets were needed — swap it for real artwork whenever
  you like via Android Studio's Image Asset wizard (`res` → New → Image Asset).

## Updating the app content

Whenever `spice_pro_updated8.html` changes at the repo root:

```bash
cp ../spice_pro_updated8.html app/src/main/assets/www/index.html
```

then re-apply this one-time edit inside that copied file (the original two CDN tags
must become the local paths shown on the right):

```
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  →  <link rel="stylesheet" href="libs/fontawesome/css/all.min.css">

<script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js" defer></script>
  →  <script src="libs/html2pdf/html2pdf.bundle.min.js" defer></script>
```

## Known limitation of this delivery

This project was assembled and reviewed carefully, but it was **not compiled** in
this environment — there is no Android SDK available here to run an actual Gradle
build. Everything here (Gradle files, manifest, Java, resources) follows current,
well-established Android/WebView patterns, but if Android Studio's build surfaces
any error on your machine, send it over and it can be fixed directly.
