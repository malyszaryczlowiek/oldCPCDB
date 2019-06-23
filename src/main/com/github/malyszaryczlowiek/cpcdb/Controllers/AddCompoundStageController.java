package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Compound.TempStability;
import com.github.malyszaryczlowiek.cpcdb.Compound.Unit;
import com.github.malyszaryczlowiek.cpcdb.db.MySQLJDBCUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AddCompoundStageController implements Initializable
{
    private Stage stage;

    @FXML private AnchorPane addCompoundAnchorPane;
    @FXML private TextField smilesTextField;
    @FXML private TextField compoundNumberTextField;
    @FXML private TextField amountTextField;
    @FXML private TextField containerTextField;
    @FXML private TextField formTextField;
    @FXML private TextArea storagePlaceTextArea;
    @FXML private TextArea additionalInfoTextArea;
    @FXML private Button addButton;
    @FXML private Button cancelButton;
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


        ObservableList<String> temp = FXCollections.observableArrayList(
                TempStability.NS.getAbbreviation(),
                TempStability.RT.getAbbreviation(),
                TempStability.fridge.getAbbreviation(),
                TempStability.freezer.getAbbreviation());
        tempStabilityChoiceBox.setItems(temp);
        tempStabilityChoiceBox.setValue(TempStability.NS.toString());
    }

    @FXML
    protected void addButtonClicked(ActionEvent event)
    {

        System.out.println("add button clicked");

        // TODO zrobić to współbierznie
        /*
        Runnable runnableAdd = () ->
            {

            };

        Thread threadAdd = new Thread(runnableAdd);
        threadAdd.start();
        try
        {
            threadAdd.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
         */


        // tutaj robimy sprawdzanie wszelkich danych wejściowych
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

            amountTextField.requestFocus();

            return; // kończymy funkcję
        }

        String compoundNumber = compoundNumberTextField.getText(); // TODO zrobić sprawdzania czy dane wejściowe są w formie xxx-xx-xx;
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
            alert.setContentText("Amount input must have number date format.");

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


        try (Connection connection = MySQLJDBCUtility.getConnection())
        {
            String insertQuery = "INSERT INTO compound(Smiles, CompoundNumber, Amount, Unit, " +
                    "Form, Stability, Argon, Container, " +
                    "StoragePlace, LastModification, AdditionalInfo) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement statement = null;

            try
            {
                statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, smiles);
                statement.setString(2, compoundNumber);
                statement.setFloat(3, amount);
                statement.setString(4, unit);

                statement.setString(5, form);
                statement.setString(6, stability);
                statement.setBoolean(7, argon);
                statement.setString(8, container);

                statement.setString(9, storagePlace);
                statement.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(11, additionalInformation);

                int rawAffected = statement.executeUpdate();
                if (rawAffected == 1)
                    System.out.println("added one item");
                else
                    System.out.println("added different than number of items");
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

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
    }

    private boolean matchesFloatPattern(String string)
    {
        boolean resultInt = Pattern.matches("[0-9]+", string);
        boolean resultFloat = Pattern.matches("[0-9]*[.][0-9]+", string);

        return resultInt || resultFloat;
    }
}


















