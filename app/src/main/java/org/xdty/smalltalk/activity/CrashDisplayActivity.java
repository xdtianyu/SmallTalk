package org.xdty.smalltalk.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xdty.smalltalk.R;
import org.xdty.smalltalk.wrapper.HttpWrapper;

public class CrashDisplayActivity extends BaseActivity implements
        HttpWrapper.ReportCrashCallback {
    
    public final static String TAG = "CrashDisplayActivity";
    
    private Throwable exception;
    
    private TextView text2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.crash_dialog);

        exception = (Throwable) getIntent().getSerializableExtra("e");
        Throwable cause = findCause(exception);
        
        setFinishOnTouchOutside(false);
        
        HttpWrapper.Instance().setReportCrashCallback(this);


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
        text2 = (TextView) findViewById(R.id.text2);
        text2.setText(R.string.text_report_message);

        Button ignoreButton = (Button)findViewById(R.id.button_ignore);
        Button reportButton = (Button)findViewById(R.id.button_report);

        ignoreButton.setEnabled(false);
        reportButton.setEnabled(false);
        
        String message = "--------------\nMessage:\n--------------\n\n"+
                exception.getClass().getSimpleName()+": "+
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
                    exception.getClass().getSimpleName()+": "+
                    exception.getMessage();
            stringBuilder = new StringBuilder();
            for (StackTraceElement element:elements) {
                stringBuilder.append(element.toString());
                stringBuilder.append("\n");
            }
            message = message + "\n--------------\nStackTrace:\n--------------\n\n"+
                    stringBuilder.toString()+"\n--------------\n";
        }
        
        // hardware info
        stringBuilder = new StringBuilder();
        stringBuilder.append("Manufacturer: ");
        stringBuilder.append(Build.MANUFACTURER);
        stringBuilder.append("\n");
        stringBuilder.append("Board: ");
        stringBuilder.append(Build.BOARD);
        stringBuilder.append("\n");
        stringBuilder.append("Brand: ");
        stringBuilder.append(Build.BRAND);
        stringBuilder.append("\n");
        stringBuilder.append("Device: ");
        stringBuilder.append(Build.DEVICE);
        stringBuilder.append("\n");
        stringBuilder.append("Display: ");
        stringBuilder.append(Build.DISPLAY);
        stringBuilder.append("\n");
        stringBuilder.append("Model: ");
        stringBuilder.append(Build.MODEL);
        stringBuilder.append("\n");
        stringBuilder.append("Android: ");
        stringBuilder.append(Build.VERSION.RELEASE);
        stringBuilder.append("\n");
        stringBuilder.append("Sdk: ");
        stringBuilder.append(Build.VERSION.SDK_INT);
        stringBuilder.append("\n");
        
        message = message + "\n--------------\nHardware info:\n--------------\n\n"+
                stringBuilder.toString()+"\n--------------\n";

        message = message + "UID: "+smallTalkService.getUID()+"\n--------------";
        
        smallTalkService.reportCrash(message);
    }

    private CharSequence createSpanned(String s, Object... spans) {
        SpannableStringBuilder sb = new SpannableStringBuilder(s);
        for (Object span : spans) {
            sb.setSpan(span, 0, sb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }

    @Override
    public void onReportSucceed() {
        Toast.makeText(this.getApplicationContext(), R.string.report_succeed, Toast.LENGTH_SHORT).show();
        finishApplication();
    }
}
