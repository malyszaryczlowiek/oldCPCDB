package com.github.malyszaryczlowiek.cpcdb.Util;

public class CloseProgramNotifier
{
    private static boolean CLOSE_UNINITIALIZED_PROGRAM = true;

    public static void setToNotCloseProgram()
    {
        CLOSE_UNINITIALIZED_PROGRAM = false;
    }


    public static boolean getIfCloseUninitializedProgram()
    {
        return CLOSE_UNINITIALIZED_PROGRAM;
    }
}
