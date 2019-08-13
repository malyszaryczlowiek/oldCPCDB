package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Compound.Compound;
import com.github.malyszaryczlowiek.cpcdb.Compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.Compound.Unit;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AddCompoundStageController implements Initializable
{
    private Stage stage;

    private MainStageController mainStageControllerObject;

    //@FXML private Label smilesLabel;
    @FXML private Label amountLabel;

    @FXML private Label storagePlaceLabel;
    @FXML private Label addtionalInfoLabel;

    @FXML private Separator separator1;
    @FXML private Separator separator2;
    @FXML private Separator separator3;

    @FXML private Label tempStabilityLabel;
    @FXML private TextField smilesTextField;
    @FXML private TextField compoundNumberTextField;
    @FXML private TextField amountTextField;
    @FXML private TextField containerTextField;
    @FXML private TextField formTextField;
    @FXML private TextArea storagePlaceTextArea;
    @FXML private TextArea additionalInfoTextArea;
    @FXML private ChoiceBox<String> unitChoiceBox;
    @FXML private ChoiceBox<String> tempStabilityChoiceBox;
    @FXML private CheckBox argonCheckBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        // setting up unit choice box
        ObservableList<String> units = FXCollections.observableArrayList(
                Unit.mg.getAbbreviation(),
                Unit.g.getAbbreviation(),
                Unit.kg.getAbbreviation(),
                Unit.ml.getAbbreviation(),
                Unit.l.getAbbreviation());
        unitChoiceBox.setItems(units);
        unitChoiceBox.setValue(Unit.NS.toString());


        ObservableList<String> temp = FXCollections.observableArrayList(
                TempStability.NS.getAbbreviation(),
                TempStability.RT.getAbbreviation(),
                TempStability.FRIDGE.getAbbreviation(),
                TempStability.FREEZER.getAbbreviation());
        tempStabilityChoiceBox.setItems(temp);
        tempStabilityChoiceBox.setValue(TempStability.NS.toString());


        // seting up resizability

    }

    @FXML
    protected void addButtonClicked(ActionEvent event)
    {
        String smiles = smilesTextField.getText(); // smiles nie może być null
        if (smiles.equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setResizable(true);
            alert.setWidth(700);
            alert.setHeight(400);
            alert.setTitle("Error");
            alert.setHeaderText("Smiles Cannot be empty.");
            alert.setContentText("You have to add Smiles.");

            alert.showAndWait();

            smilesTextField.requestFocus();

            return; // kończymy funkcję
        }

        String compoundNumber = compoundNumberTextField.getText();
        String amountString = amountTextField.getText();
        float amount;

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

            amountTextField.requestFocus();

            return; // kończymy funkcję
        }
        else
            amount = Float.valueOf(amountString);

        String unit = unitChoiceBox.getValue();
        String form = formTextField.getText();
        String stability = tempStabilityChoiceBox.getValue();
        String container = containerTextField.getText();
        boolean argon = argonCheckBox.isSelected();
        String storagePlace = storagePlaceTextArea.getText();
        String additionalInformation = additionalInfoTextArea.getText();

        LocalDateTime now = LocalDateTime.now();
        Compound compound = new Compound(smiles, compoundNumber,amount,
                Unit.stringToEnum(unit), form, TempStability.stringToEnum(stability),
                argon, container, storagePlace, now, additionalInformation);
        CompoundAddedListener listener =  mainStageControllerObject; // deleted casting (CompoundAddedListener)
        listener.notifyAboutAddedCompound(compound);

        event.consume();
        stage.close();
    }

    @FXML
    protected void cancelButtonClicked(ActionEvent event)
    {
        stage.close();
        event.consume();
    }

    void setStage(Stage stage)
    {
        this.stage = stage;
        Scene scene = stage.getScene();

        scene.widthProperty()
                .addListener( (ObservableValue<? extends Number> observableValue, Number number, Number t1) ->
                {
                    double width = (double) t1;
                    double widthOfTextFields = width - 180 ;

                    smilesTextField.prefWidthProperty().setValue( widthOfTextFields );
                    formTextField.prefWidthProperty().setValue( widthOfTextFields );
                    containerTextField.prefWidthProperty().setValue( widthOfTextFields );
                    storagePlaceTextArea.prefWidthProperty().setValue( widthOfTextFields );
                    additionalInfoTextArea.prefWidthProperty().setValue( widthOfTextFields );

                    double difference = width - 180 - argonCheckBox.getWidth() - separator3.getWidth()
                            - tempStabilityChoiceBox.getWidth() - tempStabilityLabel.getWidth()
                            - separator2.getWidth() - unitChoiceBox.getWidth() - amountTextField.getWidth()
                             - separator1.getWidth() - amountLabel.getWidth();
                    compoundNumberTextField.prefWidthProperty().setValue( difference );
                } );


        scene.heightProperty().addListener( (ObservableValue<? extends Number> observableValue, Number number, Number t1) ->
            {
                double height = (double) t1;
                double differenceByThree = (height - 370) /3 ;
                storagePlaceTextArea.prefHeightProperty().setValue(60 + differenceByThree);
                double storageTemporaryHeight = storagePlaceTextArea.getHeight();
                addtionalInfoLabel.setLayoutY(150 + 60 + differenceByThree + 10);
                additionalInfoTextArea.setLayoutY(storagePlaceLabel.getLayoutY() + storageTemporaryHeight + 10);
                additionalInfoTextArea.prefHeightProperty().setValue(105 + 2* differenceByThree);
            } );
    }

    private boolean matchesFloatPattern(String string)
    {
        boolean resultInt = Pattern.matches("[0-9]+", string);
        boolean resultFloat = Pattern.matches("[0-9]*[.][0-9]+", string);

        return resultInt || resultFloat;
    }

    void setMainStageControllerObject(MainStageController controller)
    {
        mainStageControllerObject = controller;
    }

    public interface CompoundAddedListener
    {
        void notifyAboutAddedCompound(Compound compound);
    }
}


















