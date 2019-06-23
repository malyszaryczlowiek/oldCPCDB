package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.Compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.Compound.Field;

import java.sql.PreparedStatement;
import java.util.List;

public class SqlUpdater
{
    private PreparedStatement statement = null;

    public void executeUpdate(Compound compound)
    {



    }

    private String prepareStatement(Compound compound)
    {
        List<Field> listOfFieldsToChange = compound.getListOfFieldsToChange();

        StringBuilder updateQueryBuilder = new StringBuilder("UPDATE cpcdb SET ");

        for (Field field: listOfFieldsToChange)
        {
            /*
            Lepiej jest pozostawić wprowadzanie danych na poziomie budowania
            PreparedStatement bo to ami lepiej sobie poradzi z wprowadzaniem
            takich danych jak np LocalDateTime niż jakbyśmy tempo przeklejali
            je tutaj w postaci stringów.
             */
            switch (field)
            {
                case SMILES:
                    updateQueryBuilder.append("Sliles = ?, ");
                    break;
                case COMPOUNDNUMBER:
                    updateQueryBuilder.append("CompoundNumber = ?, ");
                    break;
                case AMOUNT:
                    updateQueryBuilder.append("Amount = ?, ");
                    break;
                case UNIT:
                    updateQueryBuilder.append("Unit = ?, ");
                    break;
                case FORM:
                    updateQueryBuilder.append("Form = ?, ");
                    break;
                case TEMPSTABILITY:
                    updateQueryBuilder.append("Stability = ?, ");
                    break;
                case ARGON:
                    updateQueryBuilder.append("Argon = ?, ");
                    break;
                case CONTAINER:
                    updateQueryBuilder.append("Container = ?, ");
                    break;
                case STORAGEPLACE:
                    updateQueryBuilder.append("StoragePlace = ?, ");
                    break;
                case DATETIMEMODIFICATION:
                    updateQueryBuilder.append("LastModification = ?, ");
                    break;
                case ADDITIONALINFO:
                    updateQueryBuilder.append("AdditionalInfo = ?, ");
                    break;
                default:
                    break;
            }
        }
        // TODO tu trzeba usunąc jeszcze ostatni przecinek na końcu wyrażenia
        int characterNumber = updateQueryBuilder.length();
        updateQueryBuilder.delete(characterNumber -2, characterNumber -1);

        updateQueryBuilder.append(" WHERE CompoundID = ");
        updateQueryBuilder.append(compound.getId());

        return updateQueryBuilder.toString();
    }

    //
}

/*
"Smiles VARCHAR(255) NOT NULL, " +
                        "CompoundNumber VARCHAR(255), " +
                        "Amount FLOAT, " +
                        "Unit VARCHAR(255) CHARACTER SET utf8, " +
                        "Form VARCHAR(255) CHARACTER SET utf8, " +
                        "Stability VARCHAR(255) CHARACTER SET utf8, " +
                        "Argon BOOLEAN, " +
                        "Container VARCHAR(255) CHARACTER SET utf8, " +
                        "StoragePlace VARCHAR(255) CHARACTER SET utf8, " +
                        "LastModification TIMESTAMP(0), " +
                        "AdditionalInfo TEXT CHARACTER SET utf8, " +
 */












