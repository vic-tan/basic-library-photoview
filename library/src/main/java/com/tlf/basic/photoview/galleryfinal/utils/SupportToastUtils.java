package com.tlf.basic.photoview.galleryfinal.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tlf.basic.photoview.R;


/**
 * @author tanlifei
 * @ClassName: ToastUtils
 * @date 2015-01-26 下午3:30:25
 */
public class SupportToastUtils {

    public static final int DURATION = 1000;

    public static void show(Context context, int resId) {
        show(context,context.getResources().getText(resId), DURATION);
    }

    public static void show(Context context, int resId, int duration) {
        show(context,context.getResources().getText(resId), duration);
    }

    public static void show(Context context, CharSequence text) {
        show(context ,text, DURATION);
    }

    public static void show(Context context, CharSequence text, int duration) {
        View layout = LayoutInflater.from(context).inflate(R.layout.support_layout_toast, null);
        TextView textV = (TextView) layout.findViewById(R.id.toast_text);
        textV.setText(text);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    public static void show(Context context, int resId, Object... args) {
        show(context, String.format(context.getResources().getString(resId), args), DURATION);
    }

    public static void show(Context context , String format, Object... args) {
        show(context, String.format(format, args), DURATION);
    }

    public static void show(Context context, int resId, int duration, Object... args) {
        show(context, String.format(context.getResources().getString(resId), args), duration);
    }



}