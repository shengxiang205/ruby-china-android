package org.ruby_china.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;

public class BaseActivity extends AppCompatActivity implements TurbolinksAdapter {
    protected static final String INTENT_URL = "intentUrl";

    protected String location;
    protected TurbolinksView turbolinksView;

    private ValueCallback<Uri[]> mFilePathCallback;
    private final int REQUEST_SELECT_FILE = 1001;
    /**
     * request code for sign in
     */
    private final int REQUEST_SIGN_IN = 1002;
    /**
     * result code when sign in is canceled
     */
    protected static final int RESULT_SIGN_IN_CANCELED = 1003;
    private boolean onSelectFileCallback = false;

    class WebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mFilePathCallback = filePathCallback;
            startActivityForResult(fileChooserParams.createIntent(), REQUEST_SELECT_FILE);

            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    mFilePathCallback.onReceiveValue(new Uri[] { data.getData() });
                } else {
                    mFilePathCallback.onReceiveValue(null);
                }
                onSelectFileCallback = true;
                break;
            case REQUEST_SIGN_IN:
                /**
                 * when sign in action is canceled, just finish self
                 */
                if (resultCode == RESULT_SIGN_IN_CANCELED) {
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TurbolinksSession.getDefault(this).getWebView().setWebChromeClient(new WebChromeClient());

        location = getIntent().getStringExtra(INTENT_URL);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (!onSelectFileCallback) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(true)
                    .view(turbolinksView)
                    .visit(location);
        } else {
            onSelectFileCallback = false;
        }
    }

    @Override
    public void onPageFinished() {

    }

    @Override
    public void onReceivedError(int errorCode) {

    }

    @Override
    public void pageInvalidated() {

    }

    @Override
    public void requestFailedWithStatusCode(int statusCode) {
        switch (statusCode) {
            case 401:
                visitProposedToLocationWithAction(getString(R.string.root_url) + "/account/sign_in", "advance");
                break;
            default:
                break;
        }
    }

    @Override
    public void visitCompleted() {

    }

    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        Intent intent;

        Uri uri = Uri.parse(location);

        if (location.startsWith(getString(R.string.root_url))) {
            String path = uri.getPath();
            if (path.matches("/topics")) {
                intent = new Intent(this, MainActivity.class);
            } else if (path.matches("/topics/\\d+")) {
                intent = new Intent(this, TopicActivity.class);
                intent.putExtra(INTENT_URL, location);
            } else if (path.matches("/topics/new")) {
                intent = new Intent(this, TopicNewActivity.class);
                intent.putExtra(INTENT_URL, location);
            } else if (path.matches("/topics/\\d+/edit")) {
                intent = new Intent(this, TopicEditActivity.class);
                intent.putExtra(INTENT_URL, location);
            } else if (path.matches("/topics/\\d+/replies/\\d+/edit")) {
                intent = new Intent(this, ReplyEditActivity.class);
                intent.putExtra(INTENT_URL, location);
            } else if (path.matches("/account/edit")) {
                intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(INTENT_URL, location);
            } else if (path.matches("/account/sign_in")) {
                /**
                 * SignIn seems to be a very special action as it can be canceled
                 * we share same webview across several activity and
                 * there is no way for webview to be notified when sign in is canceled
                 *
                 * so when we use SignInActivity, we expected it to have a result
                 * with an result, we will get whether sign in is canceled
                 *
                 * @see BaseActivity#onActivityResult(int, int, Intent)
                 */
                intent = new Intent(this, SignInActivity.class);
                intent.putExtra(INTENT_URL, location);

                startActivityForResult(intent, REQUEST_SIGN_IN);
                return;
            } else {
                intent = new Intent(this, EmptyActivity.class);
                intent.putExtra(INTENT_URL, location);
            }
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
        }

        this.startActivity(intent);
    }

}
