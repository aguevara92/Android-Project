package com.aguevara.dbapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap>
{
    private String url = null;
    private ImageView view = null;

    public ImageLoadTask(String url, ImageView view)
    {
        this.url = url;
        this.view = view;
    }

    @Override
    protected Bitmap doInBackground(Void... params)
    {
        try
        {
            URL urlConn = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection)urlConn.openConnection();

            connection.setDoInput(true);

            // Check if this bitmap is in the cache
            connection.setUseCaches(true);
            Object content = connection.getContent();
            if(content instanceof Bitmap)
            {
                return (Bitmap)content;
            }
            else if(content instanceof InputStream)
            {
                return BitmapFactory.decodeStream((InputStream)content);
            }

            connection.connect();

            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        super.onPostExecute(bitmap);

        if(this.view != null)
        {
            this.view.setImageBitmap(bitmap);
        }
    }
}