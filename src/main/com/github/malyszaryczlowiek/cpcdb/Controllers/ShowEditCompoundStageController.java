package com.github.malyszaryczlowiek.cpcdb.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;


import java.net.URL;
import java.util.ResourceBundle;

public class ShowEditCompoundStageController implements Initializable
{
    private Stage stage;

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

    }

    public void changeData()
    {

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
}















