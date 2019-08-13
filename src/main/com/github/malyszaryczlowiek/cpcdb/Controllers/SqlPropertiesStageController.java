package com.github.malyszaryczlowiek.cpcdb.Controllers;

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

    @FXML TextField serverIPTextField;
    @FXML TextField portNumberTextField;
    @FXML TextField userNameTextField;
    @FXML TextField serverConfigurationTextField;
    @FXML PasswordField passwordField;

    @FXML Button saveButton;
    @FXML Button cancelButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {

    }

    @FXML
    protected void onSaveButtonClicked()
    {
        String serverIP = serverIPTextField.getText();
        String portNumber = portNumberTextField.getText();
        String userName = userNameTextField.getText();
        String pass = passwordField.getText();
        String serverConfiguration = serverConfigurationTextField.getText();

        if ( !serverIP.matches("[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}") )
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

        try
        {
            Integer.parseInt(portNumber);
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

        /*
settings.db.remote.serverAddressIP
settings.db.remote.portNumber
settings.db.remote.user
settings.db.remote.passphrase
settings.db.remote.serverConfiguration
*/

        SecureProperties.setProperty("settings.db.remote.serverAddressIP", serverIP);
        SecureProperties.setProperty("settings.db.remote.portNumber", portNumber);
        SecureProperties.setProperty("settings.db.remote.user", userName);
        SecureProperties.setProperty("settings.db.remote.passphrase", pass);
        SecureProperties.setProperty("settings.db.remote.serverConfiguration", serverConfiguration);

        SecureProperties.saveProperties();

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
}
