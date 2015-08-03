package com.PopCorp.Sales.Requests;

import android.content.Context;
import android.graphics.Bitmap;

import com.octo.android.robospice.request.SpiceRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileSpiceRequest extends SpiceRequest<File> {

    private Context context;
    private String fileName;
    private Bitmap bitmap;

    public FileSpiceRequest(Context context, String fileName, Bitmap bitmap) {
        super(File.class);
        this.context = context;
        this.fileName = fileName;
        this.bitmap = bitmap;
    }

    @Override
    public File loadDataFromNetwork() throws Exception {
        File file;
        if (makeDir(context.getExternalCacheDir())) {
            file = new File(context.getExternalCacheDir() + "/tmp/" + fileName);
        } else if (makeDir(context.getCacheDir())) {
            file = new File(context.getCacheDir() + "/tmp/" + fileName);
        } else {
            throw new FileNotFoundException();
        }
        if (file.exists()){
            return file;
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }
        return file;
    }

    private boolean makeDir(File cacheDir) {
        File cache = new File(cacheDir.getAbsolutePath() + "/tmp");
        if (!cache.exists()) {
            if (!cache.mkdirs()) {
                return false;
            }
        }
        return true;
    }
}