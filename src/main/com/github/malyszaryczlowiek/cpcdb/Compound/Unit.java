package com.github.malyszaryczlowiek.cpcdb.Compound;

public enum Unit
{
    mg("mg"), g("g"), kg("kg"),
    ml("ml"), l("l");

    private String abbreviation;

    Unit(String abbreviation)
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

    public static Unit stringToEnum(String s)
    {
        switch (s)
        {
            case "mg":
                return mg;
            case "g":
                return g;
            case "kg":
                return kg;
            case "ml":
                return ml;
            case "l":
                return l;
            default:
                return mg;
        }
    }
}
