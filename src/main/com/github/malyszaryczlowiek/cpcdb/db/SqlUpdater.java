package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.Compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.Compound.Field;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class SqlUpdater
{
    private List<Field> listOfFieldsToChange;
    private Compound compound;
    private Connection connection;

    public SqlUpdater(Compound compound, Connection connection)
    {
        this.compound = compound;
        this.connection = connection;
    }

    public void executeUpdate() throws SQLException
    {
        String stringStatement = prepareStatement(compound);

        PreparedStatement statement = connection.prepareStatement(stringStatement);

        int index = 1;
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
                    statement.setString(index, compound.getSmiles());
                    break;
                case COMPOUNDNUMBER:
                    statement.setString(index, compound.getCompoundNumber());
                    break;
                case AMOUNT:
                    statement.setFloat(index, compound.getAmount());
                    break;
                case UNIT:
                    statement.setString(index, compound.getUnit().toString());
                    break;
                case FORM:
                    statement.setString(index, compound.getForm());
                    break;
                case TEMPSTABILITY:
                    statement.setString(index, compound.getTempStability().toString());
                    break;
                case ARGON:
                    statement.setBoolean(index, compound.isArgon());
                    break;
                case CONTAINER:
                    statement.setString(index, compound.getContainer());
                    break;
                case STORAGEPLACE:
                    statement.setString(index, compound.getStoragePlace());
                    break;
                case DATETIMEMODIFICATION:
                    statement.setTimestamp(index, Timestamp.valueOf(compound.getDateTimeModification()));//
                    break;
                case ADDITIONALINFO:
                    statement.setString(index, compound.getAdditionalInfo());
                    break;
                default:
                    break;
            }
            ++index;
        }

        statement.executeUpdate();
    }

    private String prepareStatement(Compound compound)
    {
        listOfFieldsToChange = compound.getListOfOrderedFieldsToChange();

        StringBuilder updateQueryBuilder = new StringBuilder("UPDATE compound SET ");

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
                    updateQueryBuilder.append("Smiles = ?, ");
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

        int characterNumber = updateQueryBuilder.length();
        updateQueryBuilder.delete(characterNumber -2, characterNumber -1);

        updateQueryBuilder.append(" WHERE CompoundID = ");
        updateQueryBuilder.append(compound.getId());

        return updateQueryBuilder.toString();
    }
}











