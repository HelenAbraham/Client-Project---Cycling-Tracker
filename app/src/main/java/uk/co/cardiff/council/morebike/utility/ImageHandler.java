package uk.co.cardiff.council.morebike.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHandler {

    // Adapted from: https://stackoverflow.com/questions/21190573/image-saving-in-android-device [Accessed: 26th March 2019]
    public static boolean saveImageToInternalStorage(Context context, Bitmap image, String fileName, String extension) {
        if (fileName == null || fileName.isEmpty())
            throw new IllegalArgumentException("File name must be defined.");
        else if (extension == null || extension.isEmpty())
            throw new IllegalArgumentException("File extension must be defined.");
        else if(image == null )
            throw new IllegalArgumentException("Image must be defined.");
        else if(context == null)
            throw new IllegalArgumentException("Context is not defined.");

        fileName = fileName + "." + extension;

        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);

            fos.close();

        } catch (IOException e) {
            Log.e("SaveImageToInternal", e.getMessage());
            return false;
        }
        return true;
    }

    public static Drawable getImageFromInternalStorage(Context context, String fileName) {
        if(fileName == null || fileName.isEmpty())
            throw new IllegalArgumentException("File path must be defined.");

        return Drawable.createFromPath(context.getFilesDir() + "/" + fileName);
    }
}
