package com.github.malyszaryczlowiek.cpcdb.HelperClasses;

public class LaunchTimer
{
    // private static long start = 0;

    private long localStart = 0;

    public LaunchTimer()
    {
        localStart = System.currentTimeMillis();
    }

    public LaunchTimer(Object o)
    {

    }

    public void startTimer()
    {
        localStart = System.currentTimeMillis();
    }

    public void stopTimer(String note)
    {
        long stop = System.currentTimeMillis();
        long difference = stop - localStart;
        long seconds, milli;

        seconds = difference / 1000;
        milli = difference - seconds * 1000;

        System.out.println();
        System.out.println(note);
        System.out.println("Difference time is: " + seconds + " seconds; " + milli + " millis");
        System.out.println();
    }

/*
    public static void startGlobalTimer()
    {
        start = System.currentTimeMillis();
    }

    public static void stopGlobalTimer(String note)
    {
        long stop = System.currentTimeMillis();
        long difference = stop - start;
        long seconds, milli;

        seconds = difference / 1000;
        milli = difference - seconds * 1000;

        System.out.println();
        System.out.println(note);
        System.out.println("Difference time is: " + seconds + " seconds; " + milli + " millis");
        System.out.println();
    }

 */
}

