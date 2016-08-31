package com.fafica.multimidia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by pedrodimoura on 30/08/16.
 */

public class ImageUtils {

    public static final int MEDIA_PICTURE = 0;
    public static final int MEDIA_VIDEO = 1;
    public static final int MEDIA_AUDIO = 2;

    public static final int REQUEST_CODE_PICTURE = 1;
    public static final int REQUEST_CODE_VIDEO = 2;
    public static final int REQUEST_CODE_AUDIO = 3;

    private static final String TAG_LAST_PICTURE = "LAST_PICTURE";
    private static final String TAG_LAST_VIDEO = "LAST_VIDEO";
    private static final String TAG_LAST_AUDIO = "LAST_AUDIO";

    private static String SHARED_PREFS_MEDIA = "SP_MEDIA";
    private static final String MEDIA_PATH = "mediaapp";

    private static final String[] EXTENSIONS =
            new String[]{".jpg", ".mp4", ".3gp"};

    private static final String[] PREF_KEYS =
            new String[]{TAG_LAST_PICTURE, TAG_LAST_VIDEO, TAG_LAST_AUDIO};

    public static File newMedia(int type) {
        Log.d("TAGM", "Criando nova mídia do tipo -> " + type);
        String mediaName = DateFormat.format(
                "yyy-MM-dd_hhmmss", new Date()).toString();

        Log.d("TAGM", "Criando File com o nome -> " + mediaName + EXTENSIONS[type]);

        File mediaFile = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                MEDIA_PATH
        );

        if (!mediaFile.exists()) mediaFile.mkdirs();

        return new File(mediaFile, mediaName + EXTENSIONS[type]);
    }

    public static void saveLastMedia(Context context, int type, String media) {
        Log.d("TAGM", "Salvando última mídia do tipo -> " + EXTENSIONS[type] + ", Path da Mídia -> " + media);
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(SHARED_PREFS_MEDIA, Context.MODE_PRIVATE);

        sharedPreferences.edit()
                .putString(PREF_KEYS[type], media)
                .apply();

        Log.d("TAGM", "Salvando no SharedPreferences com a key -> " + PREF_KEYS[type]);

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.parse(media);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static String getLastMedia(Context context, int type) {
        Log.d("TAGM", "Pegando última mídia do tipo -> " + EXTENSIONS[type]);

        Log.d("TAGM", "Path do arquivo -> " + context.getSharedPreferences(SHARED_PREFS_MEDIA, Context.MODE_PRIVATE)
                .getString(PREF_KEYS[type], null));

        return context.getSharedPreferences(SHARED_PREFS_MEDIA, Context.MODE_PRIVATE)
                .getString(PREF_KEYS[type], null);
    }

    public static Bitmap loadImage(File image, int width, int height) {
        if (width == 0 || height == 0) {
            Log.d("TAGM", "Largura e Altura da imagem é 0");
            return null;
        } else {
            Log.d("TAGM", "File Name -> " + image.getName());
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

            int widthPicture = bmOptions.outWidth;
            int heightPicture = bmOptions.outHeight;
            int scale = Math.min(
                    widthPicture / width,
                    heightPicture / height
            );

            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scale;
            bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            bitmap = rotate(bitmap, image.getAbsolutePath());

            return bitmap;
        }
    }

    private static Bitmap rotate(Bitmap bitmap, String path) {
        try {

            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotate(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotate(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotate(bitmap, 270);
                    break;
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return bitmap;

    }

    private static Bitmap rotate(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}
