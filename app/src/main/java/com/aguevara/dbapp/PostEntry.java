package com.aguevara.dbapp;

/**
 * Created by aguevara on 14/04/2017.
 */

public class PostEntry
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