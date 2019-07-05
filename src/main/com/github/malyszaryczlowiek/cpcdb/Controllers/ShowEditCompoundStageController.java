package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Compound.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;


import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ShowEditCompoundStageController implements Initializable//,
        //MainStageController.OnShowSelectedCompound
{
    private Stage stage;
    private Compound compound;
    private OnEditStageChangesSave listener;

    @FXML private TextField smilesShowEdit;
    @FXML private TextField compoundNumberShowEdit;
    @FXML private TextField amountShowEdit;

    @FXML private TextArea formShowEdit;
    @FXML private TextArea containerShowEdit;
    @FXML private TextArea storagePlaceShowEdit;
    @FXML private TextArea additionalInfoShowEdit;

    @FXML private ChoiceBox<String> unitChoiceBox;
    @FXML private ChoiceBox<String> tempStabilityChoiceBox;

    @FXML private CheckBox argonCheckBox;

    @FXML private Button cancelButtonShowEdit;
    @FXML private Button saveButtonShowEdit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        ObservableList<String> unitsObsList =
                FXCollections.observableList( Unit.returnValues() );
        unitChoiceBox.setItems(unitsObsList);

        ObservableList<String> tempObsList =
                FXCollections.observableList( TempStability.returnValues() );
        tempStabilityChoiceBox.setItems(tempObsList);

    }


    public void setStage(Stage stage)
    {
        this.stage = stage;
    }

    @FXML
    protected void onCancelButtonClicked(ActionEvent event)
    {
        stage.close();
    }

    @FXML
    protected void onDeleteCompoundClicked(ActionEvent event)
    {
        listener.reloadTableAfterCompoundDeleting(compound);
        // TODO implement deleteing
        // uzupełnić całe usówanie
    }

    @FXML
    protected void onSaveButtonClicked(ActionEvent event) throws IOException
    {
        saveChanges();
        listener.reloadTableAfterCompoundEdition();
    }

    @FXML
    protected void onSaveAndCloseButtonClicked(ActionEvent event) throws IOException
    {
        saveChanges();
        stage.close();
        listener.reloadTableAfterCompoundEdition();
    }

    private void saveChanges() throws IOException
    {
        float amount;
        String amountString = amountShowEdit.getText();

        if (!matchesFloatPattern(amountString))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setResizable(true);
            alert.setWidth(700);
            alert.setHeight(400);
            alert.setTitle("Error");
            alert.setHeaderText("Incorrect 'Amount' data type:");
            alert.setContentText("Amount input must have number data format.");

            alert.showAndWait();

            amountShowEdit.requestFocus();

            return; // kończymy funkcję
        }
        else
            amount = Float.valueOf(amountString);

        ChangesExecutor changesExecutor = new ChangesExecutor();
        boolean changeDate = false;
        int id = compound.getId();

        if ( !Float.valueOf(amount).equals( compound.getAmount() ) )
        {
            compound.setAmount(amount);
            changesExecutor.makeUpdate(id, Field.AMOUNT, amount);
            changeDate = true;
        }

        String newSmiles = smilesShowEdit.getText();
        if ( !compound.getSmiles().equals(newSmiles) )
        {
            compound.setSmiles(newSmiles);
            changesExecutor.makeUpdate(id, Field.SMILES, newSmiles);
            changeDate = true;
        }

        String newCompoundNumber = compoundNumberShowEdit.getText();
        if ( !compound.getCompoundNumber().equals(newCompoundNumber) )
        {
            compound.setCompoundNumber(newCompoundNumber);
            changesExecutor.makeUpdate(id, Field.COMPOUNDNUMBER, newCompoundNumber);
            changeDate = true;
        }

        String newUnitString = unitChoiceBox.getValue();
        Unit newUnit = Unit.stringToEnum( newUnitString );
        if ( !compound.getUnit().equals(newUnit) )
        {
            compound.setUnit( newUnit );
            changesExecutor.makeUpdate(id, Field.UNIT, newUnitString);
            changeDate = true;
        }

        String newForm = formShowEdit.getText();
        if ( !compound.getForm().equals(newForm) )
        {
            compound.setForm(newForm);
            changesExecutor.makeUpdate(id, Field.FORM, newForm);
            changeDate = true;
        }

        String newTempString = tempStabilityChoiceBox.getValue();
        TempStability newTemp = TempStability.stringToEnum( newTempString );
        if ( !compound.getTempStability().equals(newTemp) )
        {
            compound.setTempStability( newTemp );
            changesExecutor.makeUpdate(id, Field.TEMPSTABILITY, newTempString);
            changeDate = true;
        }

        boolean newArgon = argonCheckBox.isSelected();
        if (compound.isArgon() != newArgon)
        {
            compound.setArgon(newArgon);
            changesExecutor.makeUpdate(id, Field.ARGON, newArgon);
            changeDate = true;
        }

        String newContainer = containerShowEdit.getText();
        if ( !compound.getContainer().equals(newContainer) )
        {
            compound.setContainer(newContainer);
            changesExecutor.makeUpdate(id, Field.CONTAINER, newContainer);
            changeDate = true;
        }


        String newStorage = storagePlaceShowEdit.getText();
        if ( !compound.getStoragePlace().equals(newStorage) )
        {
            compound.setStoragePlace(newStorage);
            changesExecutor.makeUpdate(id, Field.STORAGEPLACE, newStorage);
            changeDate = true;
        }

        String newAdditionalInfo = additionalInfoShowEdit.getText();
        if ( !compound.getAdditionalInfo().equals(newAdditionalInfo) )
        {
            compound.setAdditionalInfo(newAdditionalInfo);
            changesExecutor.makeUpdate(id, Field.ADDITIONALINFO, newAdditionalInfo);
            changeDate = true;
        }

        if ( changeDate )
        {
            LocalDateTime now = LocalDateTime.now();
            compound.setDateTimeModification(now);
            changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
        }



        // tutaj trzeba zrobić jeszcze refresh table view aby było wiadomo, że zmiany zostały wprowadzone
    }

    private boolean matchesFloatPattern(String string)
    {
        boolean resultInt = Pattern.matches("[0-9]+", string);
        boolean resultFloat = Pattern.matches("[0-9]*[.][0-9]+", string);

        return resultInt || resultFloat;
    }


    public void setSelectedItem(Compound selectedCompound)
    {
        compound = selectedCompound;

        smilesShowEdit.setText(selectedCompound.getSmiles());
        compoundNumberShowEdit.setText(selectedCompound.getCompoundNumber());
        amountShowEdit.setText( String.valueOf( selectedCompound.getAmount() ) );

        formShowEdit.setText(selectedCompound.getForm());
        containerShowEdit.setText(selectedCompound.getContainer());
        storagePlaceShowEdit.setText(selectedCompound.getStoragePlace());
        additionalInfoShowEdit.setText(selectedCompound.getAdditionalInfo());

        argonCheckBox.setSelected(selectedCompound.isArgon());

        unitChoiceBox.setValue(selectedCompound.getUnit().toString());
        tempStabilityChoiceBox.setValue(selectedCompound.getTempStability().toString());
    }

    public void setListener(MainStageController controller)
    {
        listener = (OnEditStageChangesSave) controller;
    }

    // TODO w momencie gdy zostaną wprowadzone zmiany to trzeba wysłać sygnał, żeby refreshował
    // observable list w MainStageController

    public interface OnEditStageChangesSave
    {
        void reloadTableAfterCompoundEdition();
        void reloadTableAfterCompoundDeleting(Compound compound);
    }

}















