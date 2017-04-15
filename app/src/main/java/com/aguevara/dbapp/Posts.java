package com.aguevara.dbapp;

/**
 * Created by aguevara on 14/04/2017.
 */

public class Posts
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