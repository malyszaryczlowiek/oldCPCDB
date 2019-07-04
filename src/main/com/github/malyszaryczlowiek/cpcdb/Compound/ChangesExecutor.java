package com.github.malyszaryczlowiek.cpcdb.Compound;

import com.github.malyszaryczlowiek.cpcdb.db.MySQLJDBCUtility;
import com.github.malyszaryczlowiek.cpcdb.db.SqlUpdater;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class ChangesExecutor
{
    private static ArrayList<Compound> listOfUpdates = new ArrayList<>();
    private static ArrayList<Compound> listOfCompoundsToDelete = new ArrayList<>();

    /**
     *
     * @param id id związku w bazie który będzie trzeba zmienić
     * @param field pole w związku, którę będzie trzeba zmienić
     * @param newValue nowa wartość pola
     * @param <T> klasa typu pola do zminy
     * @throws IOException błędny input w postaci nie takiego wprowadzonego typu
     * przypisanego do zmiennej
     */

    public <T> void makeUpdate(int id, Field field, T newValue) throws IOException
    {
        boolean itemExist = listOfUpdates
                .parallelStream()
                .anyMatch(compoundChange -> compoundChange.getId().equals(id));

        Compound compoundToChange;

        if (!itemExist) // jeśli nie istnieje// to dodaj nowy item do listy
        {
            compoundToChange = new Compound(id);
            listOfUpdates.add(compoundToChange);
        }
        else
        {
            Compound[] table = listOfUpdates
                    .stream()
                    .filter(compound -> compound.getId().equals(id))
                    .toArray(Compound[]::new); //
            compoundToChange = table[0];
        }

        compoundToChange.addChange(field, newValue);
    }

    public void makeDelete(int id)
    {
        boolean itemExist = listOfCompoundsToDelete
                .parallelStream()
                .anyMatch(compoundChange -> compoundChange.getId().equals(id));

        if (!itemExist) // jeśli nie istnieje// to dodaj nowy item do listy
            listOfCompoundsToDelete.add(new Compound(id));
    }

    /**
     * metoda która przy wywołaniu spowoduje zapisanie wszystkich wprowadzonych danych
     * do bazy danych a następnie wyczyści listę zmian
     */
    public void applyChanges()
    {
        // otwieramy połączenie i strumieniujemy wszystkie dane go bazy danych.
        // dla każdego ityemu z listy można wykonać wywołanie w innym wątku
        // każdy taki wątek musi wykonać sekwencję wywołań SQL aby  nie modyfikować
        // jednocześnie tego samego itemu przez kilka wątków na raz bo być może
        // powoduje to race conditions

        try (Connection connection = MySQLJDBCUtility.getConnection())
        {
            // TODO to może powodować problemy z ilością zapisów w bazie danych bo wiele wątków może
            // wywoływac statement na connection na raz.
            listOfUpdates.parallelStream()
                    .forEach(compound -> new SqlUpdater(compound, connection).executeUpdate());

            for (Compound compound: listOfCompoundsToDelete)
                new SqlUpdater(compound,connection).executeDelete();

            /*
            for (Compound compound: listOfUpdates)
            {
                SqlUpdater updater = new SqlUpdater(compound, connection);
                updater.executeUpdate();
            }
             */
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Metoda służąca do wyczyszczenie listy ze związkami gdzie mają zostać wprowadzone zmiany lub zostać usunięte
     */
    public void clearListOfUpdates()
    {
        listOfUpdates.clear();
    }

    public boolean isListOfUpdatesEmpty()
    {
        return listOfUpdates.isEmpty();
    }

    public void clearListOfCompoundsToDelete()
    {
        listOfCompoundsToDelete.clear();
    }

    public boolean isListOfCompoundsToDeleteEmpty()
    {
        return listOfCompoundsToDelete.isEmpty();
    }
}
