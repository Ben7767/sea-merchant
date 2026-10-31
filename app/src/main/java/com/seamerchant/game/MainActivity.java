package com.seamerchant.game;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView web;
    private TextToSpeech tts;
    private volatile String ttsStatus = "init";

    /** JavaScript bridge: lets the game use the phone's own Hebrew text-to-speech voice. */
    private class Bridge {
        @JavascriptInterface public String status() { return ttsStatus; }
        @JavascriptInterface public void stop() { if (tts != null) tts.stop(); }
        @JavascriptInterface public void speak(String text, float pitch, float rate, float volume, String id) {
            if (tts == null || !"ok".equals(ttsStatus)) { done(id); return; }
            tts.setPitch(pitch);
            tts.setSpeechRate(rate);
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume);
            if (tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, id) != TextToSpeech.SUCCESS) done(id);
        }
        @JavascriptInterface public void openSettings() {
            try {
                Intent i = new Intent("android.speech.tts.engine.INSTALL_TTS_DATA");
                i.setPackage(GOOGLE_TTS);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            } catch (Exception e) { }
            try { startActivity(new Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return; }
            catch (Exception e) { }
            try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); } catch (Exception e2) { }
        }
    }

    private void done(final String id) {
        runOnUiThread(() -> web.evaluateJavascript("window.__ttsDone&&window.__ttsDone['" + id + "']&&window.__ttsDone['" + id + "']()", null));
    }

    private static final String GOOGLE_TTS = "com.google.android.tts";

    /** Prefer the Google speech engine (it has Hebrew); fall back to the phone's default engine (Samsung's has no Hebrew). */
    private void initTts() {
        ttsStatus = "init";
        try {
            tts = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS) setupTts(); else runOnUiThread(this::initDefaultTts);
            }, GOOGLE_TTS);
        } catch (Exception e) { initDefaultTts(); }
    }

    private void initDefaultTts() {
        try { if (tts != null) tts.shutdown(); } catch (Exception e) { }
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) setupTts(); else ttsStatus = "nohe";
        });
    }

    private void setupTts() {
        Locale he = new Locale("he", "IL");
        int r = tts.setLanguage(he);
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) { ttsStatus = "nohe"; return; }
        try {
            Voice best = null;
            for (Voice v : tts.getVoices()) {
                String lang = v.getLocale().getLanguage();
                if (!"he".equals(lang) && !"iw".equals(lang)) continue;
                if (v.getFeatures() != null && v.getFeatures().contains("notInstalled")) continue;
                if (best == null || v.getQuality() > best.getQuality()) best = v;
            }
            if (best != null) tts.setVoice(best);
        } catch (Exception e) { }
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) { }
            @Override public void onDone(String id) { done(id); }
            @Override public void onError(String id) { done(id); }
        });
        ttsStatus = "ok";
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0b4f78"));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        web.addJavascriptInterface(new Bridge(), "AndroidTTS");
        initTts();
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                if (url.startsWith("file:")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { }
                return true;
            }
        });

        if (state == null) web.loadUrl("file:///android_asset/index.html");
        else web.restoreState(state);
        hideBars();
    }

    private void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override protected void onPause() { super.onPause(); if (tts != null) tts.stop(); web.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); if (tts != null) { tts.stop(); tts.shutdown(); } }
    @Override protected void onResume() { super.onResume(); web.onResume(); hideBars(); }

    /** Back button: close an open dialog, else open the exit dialog; leave the app if the game is not running. */
    @Override
    public void onBackPressed() {
        String js = "(function(){try{if(document.querySelector('.ov')){closeOv();return '1'}"
            + "if(typeof S!=='undefined'&&S&&S.started){exitGame();return '1'}}catch(e){}return '0'})()";
        web.evaluateJavascript(js, value -> {
            if (value == null || !value.contains("1")) finish();
        });
    }
}
