package com.posstation;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ReverseTextActivity extends Activity {

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_text);
        CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        this.<TextView>findViewById(R.id.reverse_text).setText(reverse(text));
    }

    @NonNull
    private CharSequence reverse(@NonNull CharSequence text) {
        StringBuilder sb = new StringBuilder();
        for (int i = text.length() - 1; i >= 0; --i) {
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }
}
