package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Buffer.ChangesDetector;
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
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class EditCompoundStageController implements Initializable
{
    private Stage stage;
    private Compound compound;
    private EditChangesStageListener listener;

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


     void setStage(Stage stage)
    {
        this.stage = stage;
    }

    @FXML
    protected void onCancelButtonClicked(ActionEvent event)
    {
        stage.close();
        event.consume();
    }

    @FXML
    protected void onDeleteCompoundClicked(ActionEvent event)
    {
        // TODO info o zmienie będzie zapisane w głównym oknie
        listener.reloadTableAfterCompoundDeleting(compound);
        stage.close();
        event.consume();
    }

    @FXML
    protected void onSaveButtonClicked(ActionEvent event) throws IOException
    {
        saveChanges();
        listener.reloadTableAfterCompoundEdition();
        event.consume();
    }

    @FXML
    protected void onSaveAndCloseButtonClicked(ActionEvent event) throws IOException
    {
        saveChanges();
        stage.close();
        listener.reloadTableAfterCompoundEdition();
        event.consume();
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

        ChangesDetector changesDetector = new ChangesDetector();

        if ( !Float.valueOf(amount).equals( compound.getAmount() ) )
            changesDetector.makeEdit(compound, Field.AMOUNT, amount);

        String newSmiles = smilesShowEdit.getText();
        if ( !compound.getSmiles().equals(newSmiles) )
            changesDetector.makeEdit(compound, Field.SMILES, newSmiles);

        String newCompoundNumber = compoundNumberShowEdit.getText();
        if ( !compound.getCompoundNumber().equals(newCompoundNumber) )
            changesDetector.makeEdit(compound, Field.COMPOUNDNUMBER, newCompoundNumber);

        String newUnitString = unitChoiceBox.getValue();
        Unit newUnit = Unit.stringToEnum( newUnitString );
        if ( !compound.getUnit().equals(newUnit) )
            changesDetector.makeEdit(compound, Field.UNIT, newUnitString);

        String newForm = formShowEdit.getText();
        if ( !compound.getForm().equals(newForm) )
            changesDetector.makeEdit(compound, Field.FORM, newForm);

        String newTempString = tempStabilityChoiceBox.getValue();
        TempStability newTemp = TempStability.stringToEnum( newTempString );
        if ( !compound.getTempStability().equals(newTemp) )
            changesDetector.makeEdit(compound, Field.TEMPSTABILITY, newTempString);

        boolean newArgon = argonCheckBox.isSelected();
        if (compound.isArgon() != newArgon)
            changesDetector.makeEdit(compound, Field.ARGON, newArgon);

        String newContainer = containerShowEdit.getText();
        if ( !compound.getContainer().equals(newContainer) )
            changesDetector.makeEdit(compound, Field.CONTAINER, newContainer);

        String newStorage = storagePlaceShowEdit.getText();
        if ( !compound.getStoragePlace().equals(newStorage) )
            changesDetector.makeEdit(compound, Field.STORAGEPLACE, newStorage);

        String newAdditionalInfo = additionalInfoShowEdit.getText();
        if ( !compound.getAdditionalInfo().equals(newAdditionalInfo) )
            changesDetector.makeEdit(compound, Field.ADDITIONALINFO, newAdditionalInfo);
    }

    private boolean matchesFloatPattern(String string)
    {
        boolean resultInt = Pattern.matches("[0-9]+", string);
        boolean resultFloat = Pattern.matches("[0-9]*[.][0-9]+", string);

        return resultInt || resultFloat;
    }


    void setSelectedItem(Compound selectedCompound)
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

    void setListener(MainStageController controller)
    {
        listener = controller;
    }

    public interface EditChangesStageListener
    {
        void reloadTableAfterCompoundEdition();
        void reloadTableAfterCompoundDeleting(Compound compound);
    }

}















