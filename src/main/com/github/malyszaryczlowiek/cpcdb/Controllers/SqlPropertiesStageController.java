package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Util.CloseProgramNotifier;
import com.github.malyszaryczlowiek.cpcdb.HelperClasses.LaunchTimer;
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

        setInput();

        LaunchTimer timer = new LaunchTimer();
        //timer.startTimer();

        String remotePortNumberString = remotePortNumber.getText();
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

        // załaduj wszystkie Secure Properties (w innych wątkach)
        // i wtedy przypisz wszystkie poniższe dane

        SecureProperties.loadProperties();

        SecureProperties.setProperty("settings.db.remote.RDBMS", "mysql");
        SecureProperties.setProperty("settings.db.local.RDBMS", "mysql");

        String remoteServerAddressIPString = remoteServerAddressIP.getText();
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

        SecureProperties.setProperty("column.show.Smiles", "true");
        SecureProperties.setProperty("column.show.CompoundName", "true");
        SecureProperties.setProperty("column.show.Amount", "true");
        SecureProperties.setProperty("column.show.Unit", "true");
        SecureProperties.setProperty("column.show.Form", "true");
        SecureProperties.setProperty("column.show.TemperatureStability", "true");
        SecureProperties.setProperty("column.show.Argon", "true");
        SecureProperties.setProperty("column.show.Container", "true");
        SecureProperties.setProperty("column.show.StoragePlace", "true");
        SecureProperties.setProperty("column.show.LastModification", "true");
        SecureProperties.setProperty("column.show.AdditionalInfo", "true");

        CloseProgramNotifier.setToNotCloseProgram();

        timer.stopTimer("Loading properties when Save Button Clicked during initialization ");

        thisStage.close();
    }

    @FXML
    protected void onCancelButtonClicked()
    {

        thisStage.close();
    }

    void setStage(Stage stage)
    {
        thisStage = stage;
    }

    private void setInput()
    {
        remoteServerAddressIP.setText("remotemysql.com");
        remotePortNumber.setText("3306");
        remoteUser.setText("Wa1s8JBvyU");
        remotePassphrase.setText("5YlJQGAuml");
        remoteServerConfiguration.setText("");

        localUser.setText("root");
        localPassphrase.setText("Janowianka1922?");
        localServerConfiguration.setText("useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw");
    }
}
