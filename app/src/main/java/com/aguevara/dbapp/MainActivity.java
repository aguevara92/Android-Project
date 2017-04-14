package com.aguevara.dbapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;



import static android.content.ContentValues.TAG;


class PostEntry
{
    String id;
    String title;
    String latitude;
    String longitude;
    String date;
    String likes;
    String picture;

    public PostEntry (String i, String ti, String lat, String lon, String da, String li, String pi)
    {
        id = i;
        title = ti;
        latitude = lat;
        longitude = lon;
        date = da;
        likes = li;
        picture = pi;
    }
}

class Posts
{
    static PostEntry[] postEntry;
    static int count = 0;
    static int newsInFocus = -1;

    public static void init (int n)
    {
        postEntry = null; // OLD RECORDS IS NOW GARBAGE
        postEntry = new PostEntry[n];
        count = n;
    }
}



public class MainActivity extends Activity implements LocationListener
{
    final Context context = this;

    Dialog dialog;

    Posts posts;
    PostEntry pe;

    String baseUrl = "http://andrs.ec/dev/mobile_computing/assignment2/";

    int n = -1;

    private Uri fileUri;
    String picturePath;
    String ba1;
    Uri selectedImage;
    Bitmap photo;
    ProgressDialog prgDialog;
    String encodedString;
    String imgPath, fileName;
    Bitmap bitmap;
    private static int RESULT_LOAD_IMG = 1;

    // CAMERA Permissions
    private static final int REQUEST_CAMERA = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private TextView latituteField;
    private TextView longitudeField;
    private LocationManager locationManager;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //final ImageView post_image = (ImageView) findViewById(R.id.post_image);
        //final String imgURL = "http://andrs.ec/dev/mobile_computing/assignment2/images/test_image.bmp";
        //new ImageLoadTask(imgURL, post_image).execute();

        verifyStoragePermissions(this);

        // At the start of the App - load everything
        new Thread ()
        {
            @Override
            public void run ()
            {
                final String s = getContent (baseUrl + "dbLoadDB.php");

                Log.i("XML",s);



                runOnUiThread (new Thread(new Runnable()
                {
                    public void run()
                    {
                        parseContent (s);
                        n = 0;
                        loadRecord ();
                    }
                }));
            }
        }.start ();



        final Button btn_load = (Button)findViewById(R.id.btn_load);
        btn_load.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View view)
            {
                new Thread ()
                {
                    @Override
                    public void run ()
                    {
                        final String s = getContent (baseUrl + "dbLoadDB.php");

                        Log.i("XML",s);

                        runOnUiThread (new Thread(new Runnable()
                        {
                            public void run()
                            {
                                parseContent (s);
                                n = 0;
                                loadRecord ();
                            }
                        }));
                    }
                }.start ();
            }
        });

        final Button btn_previous = (Button)findViewById(R.id.btn_previous);
        btn_previous.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View view)
            {
                if (n > 0)
                {
                    n --;
                    loadRecord ();
                }
            }
        });

        final Button btn_next = (Button)findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View view)
            {
                if (n < Posts.count - 1)
                {
                    n ++;
                    loadRecord ();
                }
            }
        });

        final Button btn_add = (Button)findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View view)
            {
                openDialog();
            }
        });


        final Button btn_delete = (Button)findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View view)
            {
                deleteRecord ();
            }
        });

        final Button btn_like = (Button)findViewById(R.id.btn_like);
        btn_like.setOnClickListener(new View.OnClickListener()
        {
            public void onClick (View view)
            {
                likePost ();
            }
        });


        latituteField = (TextView) findViewById(R.id.latitude_test);
        longitudeField = (TextView) findViewById(R.id.longitude_test);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            latituteField.setText("Location not available");
            longitudeField.setText("Location not available");
        }


    }

    public String getContent (String theURL)
    {
        String s = "";

        try
        {
            URL url  = new URL (theURL);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            int l = httpConnection.getContentLength();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                InputStream in = httpConnection.getInputStream();
                InputStreamReader inn= new InputStreamReader(in);
                BufferedReader bin= new BufferedReader(inn);

                do
                {
                    s = s + bin.readLine() + "\n";
                }
                while (s.length() < l);

                in.close();
            }
            else
            {
            }
        }
        catch (MalformedURLException e) {}
        catch (IOException e) {}
        finally {}

        return s;
    }

    public static void parseContent (String s)
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse (new ByteArrayInputStream(s.getBytes()));
            Element docEle = dom.getDocumentElement ();

            NodeList nl = docEle.getElementsByTagName("record");

            if (Posts.count == -1)
            {
                Posts.init (nl.getLength());
            }
            else // Unlikely!
            {
                Posts.count = -1;
                Posts.postEntry = null;
                Posts.init (nl.getLength());
            }

            for (int i = 0; i < nl.getLength(); i ++)
            {
                Element entry = (Element)nl.item(i);

                Posts.postEntry[i] = new PostEntry ("", "", "", "", "", "", "");

                Element id = (Element)entry.getElementsByTagName("id").item(0);
                Element title = (Element)entry.getElementsByTagName("title").item(0);
                Element latitude = (Element)entry.getElementsByTagName("latitude").item(0);
                Element longitude = (Element)entry.getElementsByTagName("longitude").item(0);
                Element date = (Element)entry.getElementsByTagName("date").item(0);
                Element likes = (Element)entry.getElementsByTagName("likes").item(0);
                Element picture = (Element)entry.getElementsByTagName("picture").item(0);

                Posts.postEntry[i].id = id.getFirstChild().getNodeValue();
                Posts.postEntry[i].title = title.getFirstChild().getNodeValue();
                Posts.postEntry[i].latitude = latitude.getFirstChild().getNodeValue();
                Posts.postEntry[i].longitude = longitude.getFirstChild().getNodeValue();
                Posts.postEntry[i].date = date.getFirstChild().getNodeValue();
                Posts.postEntry[i].likes = likes.getFirstChild().getNodeValue();
                Posts.postEntry[i].picture = picture.getFirstChild().getNodeValue();
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }
        finally
        {
        }
    }

    public void loadRecord ()
    {
        TextView text_title = (TextView)findViewById(R.id.text_title);
        text_title.setText (Posts.postEntry[n].title);

        // provisional location coords display
        TextView text_location = (TextView)findViewById(R.id.text_location);
        text_location.setText (Posts.postEntry[n].latitude + " - " + Posts.postEntry[n].longitude );

        // date is missing

        TextView text_likes = (TextView)findViewById(R.id.text_likes);
        text_likes.setText (Posts.postEntry[n].likes);

        //TextView content = (TextView)findViewById(R.id.content);
        ImageView post_image = (ImageView)findViewById(R.id.post_image);
        new ImageLoadTask((Posts.postEntry[n].picture), post_image).execute();
    }

    public void addRecord ()
    {
        EditText add_title = (EditText)dialog.findViewById(R.id.add_title);
        final String at  = add_title.getText ().toString ();

        ImageView post_preview = (ImageView)dialog.findViewById(R.id.post_preview);
        post_preview.buildDrawingCache();
        Bitmap bitmap = post_preview.getDrawingCache();
        final String b64 = encodeBitmap(bitmap);

        // Provisional - get latitude and longitude
        final String a = "0";



        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        String url = "http://andrs.ec/dev/mobile_computing/assignment2/dbAddDB.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new Thread ()
                {
                    @Override
                    public void run ()
                    {
                        final String s = getContent (baseUrl + "dbLoadDB.php");

                        runOnUiThread (new Thread(new Runnable()
                        {
                            public void run()
                            {
                                parseContent (s);
                                n ++;
                                loadRecord ();
                            }
                        }));
                    }
                }.start ();

            }
            }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {
                    //This code is executed if there is an error.
                }
            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("title", at); //Add the data you'd like to send to the server.
                    MyData.put("picture", b64); //Add the data you'd like to send to the server.
                    MyData.put("latitude", a); //Add the data you'd like to send to the server.
                    MyData.put("longitude", a); //Add the data you'd like to send to the server.
                    return MyData;
                }
            };
        MyRequestQueue.add(MyStringRequest);



    }


    public void deleteRecord ()
    {

        new Thread ()
        {
            @Override
            public void run ()
            {
                int i = Integer.parseInt(Posts.postEntry[n].id);
                final String s = getContent (baseUrl + "dbDeleteDB.php?id=" + i);

                runOnUiThread (new Thread(new Runnable()
                {
                    public void run()
                    {
                        parseContent (s);
                        n--;
                        loadRecord ();
                    }
                }));
            }
        }.start ();

    }

    public void likePost()
    {

        new Thread ()
        {
            @Override
            public void run ()
            {
                int i = Integer.parseInt(Posts.postEntry[n].id);
                final String s = getContent (baseUrl + "giveLike.php?id=" + i);

                runOnUiThread (new Thread(new Runnable()
                {
                    public void run()
                    {
                        parseContent (s);
                        loadRecord ();
                    }
                }));
            }
        }.start ();
    }

    public void openDialog (){
        // custom dialog
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.new_post);

        // if button is clicked, open the camera
        Button btn_add_photo = (Button) dialog.findViewById(R.id.btn_add_photo);
        btn_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });

        // if button is clicked, add the post and close the custom dialog
        Button btn_submit = (Button) dialog.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRecord ();
                dialog.dismiss();
            }
        });

        // if button is clicked, add the post and close the custom dialog
        TextView location_test = (TextView) dialog.findViewById(R.id.location_test);


        dialog.show();
    }


    private void openCamera() {

        verifyStoragePermissions(this);

        // Check Camera
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // Open default camera
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, 100);

        } else {
            Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            ImageView post_preview = (ImageView)dialog.findViewById(R.id.post_preview);
            post_preview.setImageBitmap(imageBitmap);


        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS,
                    REQUEST_CAMERA
            );
        }
    }

    public String encodeBitmap(Bitmap bm) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 10 , baos);
        byte[] img = baos.toByteArray();

        String s = Base64.encodeToString(img , Base64.DEFAULT);

        return s;
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        latituteField.setText(String.valueOf(lat));
        longitudeField.setText(String.valueOf(lng));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
