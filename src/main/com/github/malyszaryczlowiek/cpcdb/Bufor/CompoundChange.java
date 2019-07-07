package com.github.malyszaryczlowiek.cpcdb.Bufor;

import com.github.malyszaryczlowiek.cpcdb.Compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.Compound.Field;
import com.github.malyszaryczlowiek.cpcdb.Compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.Compound.Unit;

import java.io.IOException;
import java.time.LocalDateTime;

class CompoundChange<T>
{
    private Compound compound;
    private ActionType actionType;
    private Field fieldToChange;

    private String temporaryString;
    private Float temporaryFloat;
    private LocalDateTime newModificationTime;
    private Boolean temporaryBoolean;


    CompoundChange(Compound compoundToChange, Field field, T changeValue) throws IOException
    {
        if (changeValue instanceof String
                &&
                (
                        field.equals(Field.SMILES) ||
                        field.equals(Field.COMPOUNDNUMBER) ||
                        field.equals(Field.UNIT) ||
                        field.equals(Field.FORM) ||
                        field.equals(Field.CONTAINER) ||
                        field.equals(Field.TEMPSTABILITY) ||
                        field.equals(Field.STORAGEPLACE) ||
                        field.equals(Field.ADDITIONALINFO)
                )
        )
            temporaryString = (String) changeValue;
        else if (changeValue instanceof Float
                && field.equals(Field.AMOUNT) )
            temporaryFloat = (Float) changeValue;
        else if (changeValue instanceof Boolean
                && field.equals(Field.ARGON) )
            temporaryBoolean = (Boolean) changeValue;
        else
            throw new IOException("Value type and Field are not consistent.");

        this.fieldToChange = field;
        this.compound = compoundToChange;

        newModificationTime = LocalDateTime.now();
        actionType = ActionType.EDIT;
        swipeValues();
    }


    CompoundChange(Compound compoundToChange, ActionType actionType) throws IOException
    {
        if ( actionType.equals(ActionType.INSERT) || actionType.equals(ActionType.REMOVE) )
        {
            this.compound = compoundToChange;
            this.actionType = actionType;
            if (actionType.equals(ActionType.REMOVE))
                swipeValues();
        }
        else
            throw new IOException("ActionType parameter must be ActionType.INSERT or ActionType.REMOVE.");
    }

    void swipeValues() throws IOException // todo to może być problem przy odwracaniu wartości bo tu są referencje
    {
        if ( actionType.equals(ActionType.EDIT) )
        {
            String temporaryString2;
            Float temporaryFloat2;
            LocalDateTime oldModificationTime;
            Boolean temporaryBoolean2;

            if ( fieldToChange == Field.SMILES )
            {
                temporaryString2 = compound.getSmiles();
                compound.setSmiles(temporaryString);
                temporaryString = temporaryString2;

            }
            else if ( fieldToChange == Field.COMPOUNDNUMBER )
            {
                temporaryString2 = compound.getCompoundNumber();
                compound.setCompoundNumber(temporaryString);
                temporaryString = temporaryString2;
            }
            else if ( fieldToChange == Field.AMOUNT )
            {
                temporaryFloat2 = compound.getAmount();
                compound.setAmount(temporaryFloat);
                temporaryFloat = temporaryFloat2;
            }
            else if ( fieldToChange == Field.UNIT  )
            {
                temporaryString2 = compound.getUnit().toString();
                compound.setUnit( Unit.stringToEnum(temporaryString) ); // TODO to może generowac błędy
                temporaryString = temporaryString2;
            }
            else if ( fieldToChange == Field.FORM  )
            {
                temporaryString2 = compound.getForm();
                compound.setForm(temporaryString);
                temporaryString = temporaryString2;
            }
            else if ( fieldToChange == Field.CONTAINER )
            {
                temporaryString2 = compound.getContainer();
                compound.setContainer(temporaryString);
                temporaryString = temporaryString2;
            }
            else if ( fieldToChange == Field.TEMPSTABILITY )
            {
                temporaryString2 = compound.getTempStability().toString();
                compound.setTempStability( TempStability.stringToEnum(temporaryString) );
                temporaryString = temporaryString2;
            }
            else if ( fieldToChange == Field.ARGON )
            {
                temporaryBoolean2 = compound.isArgon();
                compound.setArgon(temporaryBoolean);
                temporaryBoolean = temporaryBoolean2;
            }
            else if ( fieldToChange == Field.STORAGEPLACE )
            {
                temporaryString2 = compound.getStoragePlace();
                compound.setStoragePlace(temporaryString);
                temporaryString = temporaryString2;
            }
            else if ( fieldToChange == Field.ADDITIONALINFO )
            {
                temporaryString2 = compound.getAdditionalInfo();
                compound.setAdditionalInfo(temporaryString);
                temporaryString = temporaryString2;
            }
            else
                throw new IOException("Value type and Field are not consistent.");

            oldModificationTime = compound.getDateTimeModification();
            compound.setDateTimeModification(newModificationTime);
            //newModificationTime = oldModificationTime;
        }
        if (actionType.equals(ActionType.REMOVE))
        {
            if (compound.isToDelete())
                compound.setToDelete(false);
            else
                compound.setToDelete(true);
        }
    }


    Compound getCompound()
    {
        return compound;
    }


    Field getField()
    {
        return fieldToChange;
    }


    ActionType getActionType()
    {
        return actionType;
    }
}
