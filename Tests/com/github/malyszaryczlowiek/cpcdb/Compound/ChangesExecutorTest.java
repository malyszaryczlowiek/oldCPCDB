package com.github.malyszaryczlowiek.cpcdb.Compound;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;


class ChangesExecutorTest
{
    @Test
    @DisplayName("check if listOfChanges returns the same reference as stream")
    void checkIfListOfChangesReturnsTheSameReferenceAsStream()
    {
        Compound[] compounds = {new Compound(1), new Compound(2), new Compound(3)};
        ArrayList<Compound> listOfChanges = new ArrayList<>(Arrays.asList(compounds));

        int id = 2;

        boolean itemExist = listOfChanges
                .parallelStream()
                .anyMatch(compoundChange -> compoundChange.getId().equals(id));

        Compound compoundToChange;

        if (!itemExist) // jeśli nie istnieje// to dodaj nowy item do listy
        {
            compoundToChange = new Compound(id);
            listOfChanges.add(compoundToChange);
        }
        else
        {
            Compound[] table = listOfChanges
                    .stream()
                    .filter(compound -> compound.getId().equals(id))
                    .toArray(Compound[]::new); //
            compoundToChange = table[0];
        }

        org.junit.jupiter.api.Assertions.assertSame(compounds[1], compoundToChange, () -> "are the same");
    }
}