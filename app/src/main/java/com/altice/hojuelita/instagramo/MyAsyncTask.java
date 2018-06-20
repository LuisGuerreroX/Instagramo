package com.altice.hojuelita.instagramo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {

    @SuppressLint("StaticFieldLeak")
    private ImageView bmImage;

  MyAsyncTask(ImageView bmImage) {
      this.bmImage = bmImage;
  }

  protected Bitmap doInBackground(String... urls) {
      String urldisplay = urls[0];
      Bitmap icon = null;
      try {
        InputStream in = new java.net.URL(urldisplay).openStream();
        icon = BitmapFactory.decodeStream(in);
      } catch (Exception e) {
          Log.e("Error", e.getMessage());
          e.printStackTrace();
      }
      return icon;
  }

  protected void onPostExecute(Bitmap result) {
      bmImage.setImageBitmap(result);
  }
}
