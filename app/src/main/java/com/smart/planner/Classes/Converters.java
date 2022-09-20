package com.smart.planner.Classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;

public class Converters {
    public static Bitmap ByteArrayToBitmap(byte[] byteArray){
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(byteArray);
        return BitmapFactory.decodeStream(arrayInputStream);
    }
}
