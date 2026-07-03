package com.dhanaaspices.spicepro;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Base64;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Hosts the Spice Pro calculator (a self-contained HTML/CSS/JS app bundled under
 * assets/www) inside a WebView, and bridges PDF sharing/downloading and the
 * native image picker (for logo/signature/seal uploads) back into the page's JS.
 */
public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private long backPressedAt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar spinner = findViewById(R.id.loadingSpinner);
        webView = findViewById(R.id.webView);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (filePathCallback == null) return;
                    filePathCallback.onReceiveValue(uri == null ? null : new Uri[]{uri});
                    filePathCallback = null;
                });

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                spinner.setVisibility(View.GONE);
            }

            // Defensive fallback: the page normally shares PDFs via AndroidBridge and never
            // reaches its wa.me/mailto fallback links, but if it ever does, hand those off to
            // a real app instead of trapping them inside this WebView.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                boolean isOwnAssetHost = "https".equals(scheme)
                        && "appassets.androidplatform.net".equals(uri.getHost());
                if (scheme == null || isOwnAssetHost) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, R.string.no_app_found, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            // Enables native alert()/confirm()/prompt() dialogs used throughout the app,
            // and wires <input type="file"> (logo/signature/seal upload) to the system picker.
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
                                              FileChooserParams fileChooserParams) {
                filePathCallback = callback;
                try {
                    imagePickerLauncher.launch("image/*");
                } catch (ActivityNotFoundException e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        OnBackPressedCallback backCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                    return;
                }
                long now = System.currentTimeMillis();
                if (now - backPressedAt < 2000) {
                    finish();
                } else {
                    backPressedAt = now;
                    Toast.makeText(MainActivity.this, R.string.exit_confirm, Toast.LENGTH_SHORT).show();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backCallback);

        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html");
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    /** JS-facing bridge: window.AndroidBridge.shareOrDownloadPdf(dataUri, filename, mode) */
    private class AndroidBridge {
        @JavascriptInterface
        public void shareOrDownloadPdf(String base64DataUri, String filename, String mode) {
            try {
                Uri contentUri = writePdfToCache(base64DataUri, filename);
                runOnUiThread(() -> launchShareIntent(contentUri, mode));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        R.string.pdf_prep_failed, Toast.LENGTH_LONG).show());
            }
        }
    }

    private Uri writePdfToCache(String base64DataUri, String filename) throws IOException {
        String base64 = base64DataUri;
        int commaIdx = base64DataUri.indexOf(',');
        if (commaIdx >= 0) base64 = base64DataUri.substring(commaIdx + 1);
        byte[] pdfBytes = Base64.decode(base64, Base64.DEFAULT);

        String safeName = filename == null ? "document.pdf" : filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (safeName.isEmpty()) safeName = "document.pdf";

        File pdfDir = new File(getCacheDir(), "pdfs");
        if (!pdfDir.exists() && !pdfDir.mkdirs()) {
            throw new IOException("Could not create PDF cache directory");
        }
        File pdfFile = new File(pdfDir, safeName);
        try (FileOutputStream out = new FileOutputStream(pdfFile)) {
            out.write(pdfBytes);
        }
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
    }

    private void launchShareIntent(Uri contentUri, String mode) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if ("whatsapp".equals(mode)) {
            intent.setPackage("com.whatsapp");
            if (intent.resolveActivity(getPackageManager()) == null) {
                intent.setPackage(null);
            }
        } else if ("email".equals(mode)) {
            intent.setType("message/rfc822");
        }

        try {
            if (intent.getPackage() != null) {
                startActivity(intent);
            } else {
                startActivity(Intent.createChooser(intent, getString(R.string.app_name)));
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_LONG).show();
        }
    }
}
