package com.aguevara.dbapp;

public class Posts
{
    static PostEntry[] postEntry;
    static int count = 0;

    public static void init (int n)
    {
        postEntry = null; // OLD RECORDS IS NOW GARBAGE
        postEntry = new PostEntry[n];
        count = n;
    }
}