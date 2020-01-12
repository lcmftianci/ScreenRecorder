package com.lcmf.xll.screenrecorder;

import android.content.Context;
import android.widget.Toast;

public class ScUtil {

    public static void show(Context ctx, String strinfo){
        Toast.makeText(ctx, strinfo, Toast.LENGTH_SHORT).show();
    }
}
