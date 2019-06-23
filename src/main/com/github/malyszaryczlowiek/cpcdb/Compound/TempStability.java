package com.github.malyszaryczlowiek.cpcdb.Compound;

public enum TempStability
{
    NS("Not Selected"), RT("RT"), fridge("0 C"), freezer("-20 C");

    private String abbreviation;

    TempStability(String abbreviation)
    {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation()
    {
        return this.abbreviation;
    }

    @Override
    public String toString()
    {
        return this.abbreviation;
    }

    public static TempStability stringToEnum(String s)
    {
        switch (s)
        {
            case "Not Selected":
                return NS;
            case "RT":
                return RT;
            case "0 C":
                return fridge;
            case "-20 C":
                return freezer;
            default:
                return NS;
        }
    }
}
