package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.SpecialExceptions.ExitProgramException;
import com.github.malyszaryczlowiek.cpcdb.Util.SecureProperties;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SqlPropertiesStageController implements Initializable
{
    private Stage thisStage;

    @FXML private TextField remoteServerAddressIP;
    @FXML private TextField remotePortNumber;
    @FXML private TextField remoteUser;
    @FXML private PasswordField remotePassphrase;
    @FXML private TextField remoteServerConfiguration;

    @FXML private TextField localUser;
    @FXML private PasswordField localPassphrase;
    @FXML private TextField localServerConfiguration;

    @FXML Button saveButton;
    @FXML Button cancelButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

    }

    @FXML
    protected void onSaveButtonClicked()
    {
        String remoteServerAddressIPString = remoteServerAddressIP.getText();
        String remotePortNumberString = remotePortNumber.getText();

        // TODO poprawić działanie programu gdy kliknie sie cancel
        /*
        if ( !remoteServerAddressIPString
                .matches("[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}") )
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setResizable(true);
            alert.setWidth(700);
            alert.setHeight(400);
            alert.setTitle("Error");
            alert.setHeaderText("Incorrect IP Address Number Format.");
            alert.setContentText("IP address should has 000.000.000.000 number format.");
            alert.showAndWait();

            return;
        }
         */


        try
        {
            Integer.parseInt( remotePortNumberString );
        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setResizable(true);
            alert.setWidth(700);
            alert.setHeight(400);
            alert.setTitle("Error");
            alert.setHeaderText("Incorrect Port Number Format.");
            alert.setContentText("Port Number should be integer number.");
            alert.showAndWait();

            return;
        }

        SecureProperties.setProperty("settings.db.remote.RDBMS", "mysql");
        SecureProperties.setProperty("settings.db.local.RDBMS", "mysql");

        String remoteUserNameString = remoteUser.getText();
        String remotePassphraseString = remotePassphrase.getText();
        String remoteServerConfigurationString = remoteServerConfiguration.getText();

        SecureProperties.setProperty("settings.db.remote.serverAddressIP", remoteServerAddressIPString);
        SecureProperties.setProperty("settings.db.remote.portNumber", remotePortNumberString);
        SecureProperties.setProperty("settings.db.remote.user", remoteUserNameString);
        SecureProperties.setProperty("settings.db.remote.passphrase", remotePassphraseString);
        SecureProperties.setProperty("settings.db.remote.serverConfiguration", remoteServerConfigurationString);

        String localUserNameString = localUser.getText();
        String localPassphraseString = localPassphrase.getText();
        String localServerConfigurationString = localServerConfiguration.getText();

        SecureProperties.setProperty("settings.db.local.user", localUserNameString);
        SecureProperties.setProperty("settings.db.local.passphrase", localPassphraseString);
        SecureProperties.setProperty("settings.db.local.serverConfiguration", localServerConfigurationString);

        thisStage.close();
    }

    @FXML
    protected void onCancelButtonClicked()
    {
        SecureProperties.setProperty("closeProgramDuringInitialization", "true");
        thisStage.close();
    }

    void setStage(Stage stage)
    {
        thisStage = stage;
    }

}
