package org.ruby_china.android;

import android.webkit.WebView;

import com.basecamp.turbolinks.TurbolinksSession;
import com.facebook.drawee.backends.pipeline.Fresco;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        if (BuildConfig.DEBUG) {
            this.enableDebugMode();
        }
    }

    /**
     * enable turbolink debug log
     * enable webview debug mode
     *
     * @see {@link WebView#setWebContentsDebuggingEnabled(boolean)}
     * @see {@link TurbolinksSession#setDebugLoggingEnabled(boolean)}
     */
    private void enableDebugMode() {
        TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);
    }
}
