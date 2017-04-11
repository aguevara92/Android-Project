package com.aguevara.dbapp;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


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



public class MainActivity extends Activity
{
    Posts posts;
    PostEntry pe;

    String baseUrl = "http://andrs.ec/dev/mobile_computing/assignment2/";

    int n = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                addRecord ();
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

        TextView content = (TextView)findViewById(R.id.content);
        content.setText (Posts.postEntry[n].picture);
    }

    public void addRecord ()
    {
        /*
        EditText e1 = (EditText)findViewById(R.id.editText1);

        final String f = e1.getText ().toString ();

        EditText e2 = (EditText)findViewById(R.id.editText2);
        final String l = e2.getText ().toString ();

        EditText e3 = (EditText)findViewById(R.id.editText3);
        final String a = e3.getText ().toString ();

        EditText e4 = (EditText)findViewById(R.id.editText4);
        final String t  = e4.getText ().toString ();

        EditText e5 = (EditText)findViewById(R.id.editText5);
        final String em  = e5.getText ().toString ();

        new Thread ()
        {
            @Override
            public void run ()
            {
                final String s = getContent (baseUrl + "dbAddDB.php?firstname=" + f + "&lastname=" + l + "&address=" + a + "&telephone=" + t + "&email=" + em);

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
        */
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
