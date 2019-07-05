package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Compound.Compound;
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

    private MainStageController mainStageControllerObject;

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
        unitChoiceBox.setValue(Unit.NS.toString());


        ObservableList<String> temp = FXCollections.observableArrayList(
                TempStability.NS.getAbbreviation(),
                TempStability.RT.getAbbreviation(),
                TempStability.FRIDGE.getAbbreviation(),
                TempStability.FREEZER.getAbbreviation());
        tempStabilityChoiceBox.setItems(temp);
        tempStabilityChoiceBox.setValue(TempStability.NS.toString());
    }

    @FXML
    protected void addButtonClicked(ActionEvent event)
    {
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


        try (Connection connection = MySQLJDBCUtility.getConnection())
        {
            String insertQuery = "INSERT INTO compound(Smiles, CompoundNumber, Amount, Unit, " +
                    "Form, Stability, Argon, Container, " +
                    "StoragePlace, LastModification, AdditionalInfo) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement addingStatement = null;

            try
            {
                addingStatement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                addingStatement.setString(1, smiles);
                addingStatement.setString(2, compoundNumber);
                addingStatement.setFloat(3, amount);
                addingStatement.setString(4, unit);

                addingStatement.setString(5, form);
                addingStatement.setString(6, stability);
                addingStatement.setBoolean(7, argon);
                addingStatement.setString(8, container);

                addingStatement.setString(9, storagePlace);
                LocalDateTime now = LocalDateTime.now();
                addingStatement.setTimestamp(10, Timestamp.valueOf(now));
                addingStatement.setString(11, additionalInformation);

                int rawAffected = addingStatement.executeUpdate();
                if (rawAffected == 1)
                {
                    System.out.println("added one item");

                    // TODO dogenerować tutaj potrzebny kod
                    try
                    {
                        String loadLastAddedItemId = "SELECT LAST_INSERT_ID()";
                        PreparedStatement loadDBStatement = connection.prepareStatement(loadLastAddedItemId);
                        ResultSet resultSet = loadDBStatement.executeQuery();
                        // to mi zwraca raw gdzie w kolumnie CompoundId mam największą wartoś
                        // dlatego muszę tę wartość już tylko wyłuskać. Robię to używająć metody
                        // getInt(1) bo pobieram wartość z pierwszej kolumny.

                        resultSet.next();
                        int generatedId = resultSet.getInt(1);

                        Compound compound = new Compound(generatedId, smiles, compoundNumber,amount,
                                Unit.stringToEnum(unit), form, TempStability.stringToEnum(stability),
                                argon, container, storagePlace, now, additionalInformation);
                        OnCompoundAdded listener = (OnCompoundAdded) mainStageControllerObject;
                        listener.notifyAboutAddedCompound(compound);
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                    System.out.println("added different than one number of items");
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

    public void setMainStageControllerObject(MainStageController controller)
    {
        mainStageControllerObject = controller;
    }

    public interface OnCompoundAdded
    {
        void notifyAboutAddedCompound(Compound compound);
    }
}


















