package net.yolosec.upckeygen.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import java.util.Random;

/**
 * Standard edit text that can detect when backspace is pressed on software keyboard
 * Created by miroc on 1.2.16.
 */
public class CustomEditText extends EditText {

    private static final String TAG = "CustomEditText";

    interface OnDeletePressedListener {
        void onDeletePressed();
    }

    private OnDeletePressedListener backPressedListener;
    private Random r = new Random();

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context) {
        super(context);
    }

    public void setOnDeletePressedListener(OnDeletePressedListener backPressedListener) {
        this.backPressedListener = backPressedListener;
    }

    //    public void setRandomBackgroundColor() {
//        setBackgroundColor(Color.rgb(r.nextInt(256), r.nextInt(256), r
//                .nextInt(256)));
//    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new CustomInputConnection(super.onCreateInputConnection(outAttrs), true);
    }

    private class CustomInputConnection extends InputConnectionWrapper {

        public CustomInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            Log.i(TAG, String.format("sendKeyEvent; action=%d, keyCode=%d", event.getAction(), event.getKeyCode()));

            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                if (backPressedListener != null){
                    backPressedListener.onDeletePressed();
                }
//                ZanyEditText.this.setRandomBackgroundColor();
                // Un-comment if you wish to cancel the backspace:
                // return false;
            }
            return super.sendKeyEvent(event);
        }

    }

}
