package com.github.malyszaryczlowiek.cpcdb.Bufor;

import com.github.malyszaryczlowiek.cpcdb.Compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.Compound.Field;
import com.github.malyszaryczlowiek.cpcdb.db.MySQLJDBCUtility;
import com.github.malyszaryczlowiek.cpcdb.db.SqlExecutor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChangesDetector
{
    private static int index = 0;
    private static ArrayList<CompoundChange> listOfChanges = new ArrayList<>();


    public void undo() throws IOException
    {
        // sprawdź jaki jest typ akcji w danym indexie
        // jeśli usuń to trzeba dodać compound
        // jeśli dodaj to trzeba usunąć -||-
        listOfChanges.get(index-1).swipeValues();
        --index;
    }

    public void redo() throws IOException
    {
        listOfChanges.get(index).swipeValues();
        ++index;
    }

    public <T> void makeEdit(Compound compound, Field field, T newValue) throws IOException
    {
        listOfChanges.add(new CompoundChange<>(compound, field, newValue));
        ++index;
    }


    public void makeInsert(Compound compound) throws IOException
    {
        listOfChanges.add(new CompoundChange(compound, ActionType.INSERT));
        ++index;
    }


    public void makeDelete(Compound compound)
    {
        try
        {
            listOfChanges.add(new CompoundChange(compound, ActionType.REMOVE));
            ++index;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void saveChangesToDatabase()
    {
        try (Connection connection = MySQLJDBCUtility.getShortConnection())
        {
            // generujemy sublistę od początku zmian do momentu w którym znajduje się index

            List<CompoundChange> finalListOfChanges = listOfChanges.subList(0, index);
            CollectorOfChanges.collect(finalListOfChanges);

            CollectorOfChanges.getListOfCompoundsToDeleteFromDB().parallelStream()
                    .forEach( compound -> new SqlExecutor( compound, connection ).executeDelete() );

            CollectorOfChanges.getListOfCompoundsToInsert().parallelStream()
                    .forEach( compound -> new SqlExecutor( compound, connection ).executeInsert() );

            CollectorOfChanges.getListOfCompoundsToEditInDB().parallelStream()
                    .forEach( compound -> new SqlExecutor( compound, connection ).executeUpdate() );

            CollectorOfChanges.clearAllLists();

            listOfChanges.clear();
            index = 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public int returnCurrentIndex()
    {
        return index; // jeśli index będize zero to znaczy, że nie ma żadnych zmian do zapisania w bazie
    }

    public boolean isEndBufferPosition()
    {
        return index >= listOfChanges.size();
    }
}















