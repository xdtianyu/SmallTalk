package org.xdty.smalltalk.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import org.xdty.smalltalk.R;
import org.xdty.smalltalk.service.SmallTalkService;

public class CrashDisplayActivity extends BaseActivity {
    
    public final static String TAG = "CrashDisplayActivity";
    
    private SmallTalkService smallTalkService;
    
    private Throwable exception;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.crash_dialog);

        exception = (Throwable) getIntent().getSerializableExtra("e");
        Throwable cause = findCause(exception);
        
        setFinishOnTouchOutside(false);


// since API 14 one may also use android.app.ApplicationErrorReport.CrashInfo class 
//  to easily parse exception details into human readable form, like this:
//    
//    CrashInfo crashInfo = new CrashInfo(mException);
//    String exClassName = crashInfo.exceptionClassName;
//    String exStackTrace = crashInfo.stackTrace;

        TextView text = (TextView) findViewById(R.id.text);
        String exName = exception.getClass().getSimpleName();
        String causeName = cause.getClass().getSimpleName();
        CharSequence boldExName = createSpanned(exName, new StyleSpan(Typeface.BOLD));
        CharSequence boldCauseName = createSpanned(causeName, new StyleSpan(Typeface.BOLD));
        CharSequence crashTemplate;
        if (exception == cause) {
            crashTemplate = getText(R.string.message_crash);
        } else {
            crashTemplate = getText(R.string.message_crash_case);
        }
        CharSequence crashMessage = TextUtils.replace(crashTemplate,
                new String[] { "%1$s", "%2$s" },
                new CharSequence[] { boldExName, boldCauseName });
        text.setText(crashMessage);
    }

    private Throwable findCause(Throwable exception) {
        Throwable prev = null;
        Throwable cause = exception;
        while (cause.getCause() != null && cause != prev) {
            prev = cause;
            cause = cause.getCause();
        }
        return cause;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(CrashDisplayActivity.this, SmallTalkService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);

    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void onCloseClick(View v) {
        finishApplication();
    }

    // Kill the application in the way it won't be auto restarted by Android.
    //
    // Alternatively we may use some bootstrap activity, start it here with CLEAR_TOP flag
    // and then call finish() in its onCreate() method
    private void finishApplication() {

        sendBroadcast(new Intent(FINISH_FILTER));
    }

    public void onIgnoreClick(View view) {
        finish();
    }

    public void onReportClick(View v) {
        TextView text2 = (TextView) findViewById(R.id.text2);
        
        String message = "--------------\nMessage:\n--------------\n\n"+
                exception.getMessage()+"\n";

        StackTraceElement[] elements = exception.getStackTrace();

        StringBuilder stringBuilder = new StringBuilder();
        for (StackTraceElement element:elements) {
            stringBuilder.append(element.toString());
            stringBuilder.append("\n");
        }
        message = message + "\n--------------\nStackTrace:\n--------------\n\n"+
            stringBuilder.toString();

        while (exception.getCause()!=null) {
            exception = exception.getCause();
            message = message + "\n--------------\nCased by:\n--------------\n"+
                    exception.getMessage();
            stringBuilder = new StringBuilder();
            for (StackTraceElement element:elements) {
                stringBuilder.append(element.toString());
                stringBuilder.append("\n");
            }
            message = message + "\n--------------\nStackTrace:\n--------------\n\n"+
                    stringBuilder.toString()+"\n\n--------------\n";
        }
        
        smallTalkService.reportCrash(message);
    }

    private void append(SpannableStringBuilder sb, CharSequence text, Object... spans) {
        int start = sb.length();
        sb.append(text);
        int end = sb.length();
        for (Object span : spans) {
            sb.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    private CharSequence createSpanned(String s, Object... spans) {
        SpannableStringBuilder sb = new SpannableStringBuilder(s);
        for (Object span : spans) {
            sb.setSpan(span, 0, sb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SmallTalkService.SmallTalkServiceBinder binder = (SmallTalkService.SmallTalkServiceBinder)service;
            smallTalkService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

}
