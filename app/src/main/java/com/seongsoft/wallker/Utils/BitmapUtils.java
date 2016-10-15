package com.seongsoft.wallker.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by BeINone on 2016-10-06.
 */

public class BitmapUtils {

    public static Bitmap resizeBitmap(Context context, int id, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

}
