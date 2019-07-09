package com.github.malyszaryczlowiek.cpcdb.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AskToSaveChangesBeforeQuitController implements Initializable
{
    private Stage thisStage;
    private SaveOrCancelListener listener;


    @FXML private Button saveButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        saveButton.requestFocus();
    }

    public void setStage(Stage stage)
    {
        thisStage = stage;
    }

    public void setMainStageControllerObject(MainStageController controller)
    {
        listener = (SaveOrCancelListener) controller;
    }

    @FXML
    protected void onSaveButtonClicked(ActionEvent event)
    {
        thisStage.close();
        listener.onSaveChangesAndCloseProgram();
    }

    @FXML
    protected void onDoNotSaveButtonClicked(ActionEvent event)
    {
        thisStage.close();
        listener.onCloseProgramWithoutChanges();
    }

    @FXML
    protected void onCancelButtonClicked(ActionEvent event)
    {
        thisStage.close();
    }

    public interface SaveOrCancelListener
    {
        void onSaveChangesAndCloseProgram();
        void onCloseProgramWithoutChanges();
    }
}
