package com.github.malyszaryczlowiek.cpcdb.Buffer;

import com.github.malyszaryczlowiek.cpcdb.Compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.Compound.Field;
import com.github.malyszaryczlowiek.cpcdb.db.MySQLJDBCUtility;
import com.github.malyszaryczlowiek.cpcdb.db.SqlExecutor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ChangesDetector
{
    private static int index = 0;
    private static List<CompoundChange> listOfChanges = new ArrayList<>(); // lista zmian zostaje nie zminiona
    private static ActionType actionType; // action type of current change

    public Map<Integer, Compound> undo() throws IOException
    {
        listOfChanges.get(--index).swipeValues();
        actionType = listOfChanges.get(index).getActionType();
        return listOfChanges.get(index).getMapOfCompounds();
    }

    public Map<Integer, Compound> redo() throws IOException
    {
        listOfChanges.get(index).swipeValues();
        actionType = listOfChanges.get(index).getActionType();
        return listOfChanges.get(index++).getMapOfCompounds();
    }

    public <T> void makeEdit(Compound compound, Field field, T newValue) throws IOException
    {
        listOfChanges.subList(index, listOfChanges.size()).clear();
        listOfChanges.add(new CompoundChange(compound, field, newValue));
        // tutaj wartość indexu wskazuje aktualną pozycję ostatno dodanej zmiany
        ++index;
        // TODO zrobić kasowanie tej części listy, od aktualnego miejsca do końca.
    }


    public void makeInsert(Map<Integer, Compound> mapOfCompounds) throws IOException
    {
        listOfChanges.subList(index, listOfChanges.size()).clear();
        listOfChanges.add(new CompoundChange(mapOfCompounds, ActionType.INSERT));
        ++index;
    }

    public void makeDelete(Map<Integer, Compound> mapOfDeletedCompounds)
    {
        try
        {
            listOfChanges.subList(index, listOfChanges.size()).clear();
            listOfChanges.add(new CompoundChange(mapOfDeletedCompounds, ActionType.REMOVE));
            ++index;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
    public void makeDelete(List<Compound> listOfDeletedCompounds)
    {
        try
        {
            listOfChanges.subList(index, listOfChanges.size()).clear();
            listOfChanges.add(new CompoundChange(listOfDeletedCompounds, ActionType.REMOVE));
            ++index;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
     */




















    public void saveChangesToDatabase()
    {
        try (Connection connection = MySQLJDBCUtility.getShortConnectionToRemoteDB())
        {
            // generujemy sublistę od początku zmian do momentu w którym znajduje się index

            List<CompoundChange> finalListOfChanges = listOfChanges.subList(0, index);
            CollectorOfChanges.collect(finalListOfChanges);

            CollectorOfChanges.getListOfCompoundsToDeleteFromDB().parallelStream()
                    .forEach(compound -> new SqlExecutor(compound, connection).executeDelete());

            CollectorOfChanges.getListOfCompoundsToInsert().parallelStream()
                    .forEach(compound -> new SqlExecutor(compound, connection).executeInsert());

            CollectorOfChanges.getListOfCompoundsToEditInDB().parallelStream()
                    .forEach(compound -> new SqlExecutor(compound, connection).executeUpdate());

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

    public boolean isBufferNotOnLastPosition()
    {
        return index < listOfChanges.size();
    }

    public ActionType getActionTypeOfCurrentOperation()
    {
        return actionType;
    }
}
















