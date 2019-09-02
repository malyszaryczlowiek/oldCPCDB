package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.HelperClasses.LaunchTimer;
import com.github.malyszaryczlowiek.cpcdb.Util.SecureProperties;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;



public class SettingsStageController implements Initializable
{
    private Stage stage;
    //private Scene scene;


    @FXML private TextField remoteServerAddressIP;
    @FXML private TextField remotePortNumber;
    @FXML private TextField remoteUser;
    @FXML private TextField remotePassphrase;
    @FXML private TextField remoteServerConfiguration;

    @FXML private TextField localUser;
    @FXML private TextField localPassphrase;
    @FXML private TextField localServerConfiguration;


    @FXML private Slider keyValidityDurationSlider;
    @FXML private Label keyValidityDurationLabel;

    private final String[] duration = {"Always", "Year", "Quarter", "Month", "Week", "Day", "Session"};


    //@FXML private AnchorPane mainAnchorPane;

    //@FXML private TabPane tabPane;

    //@FXML private ScrollPane databaseConnectionTabScrollPane;
    @FXML private AnchorPane innerAnchorPane;

    //@FXML private Button saveButton;
    //@FXML private Button cancelButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        LaunchTimer timer = new LaunchTimer();

        Task<Integer> task1 = new Task<>() {
            @Override protected Integer call()
            {
                remoteServerAddressIP.setText( SecureProperties.getProperty("settings.db.remote.serverAddressIP") );
                remotePortNumber.setText( SecureProperties.getProperty("settings.db.remote.portNumber") );
                remoteUser.setText( SecureProperties.getProperty("settings.db.remote.user") );
                remotePassphrase.setText( SecureProperties.getProperty("settings.db.remote.passphrase") );
                remoteServerConfiguration.setText( SecureProperties.getProperty("settings.db.remote.serverConfiguration") );

                return 0;
            }
        };

        Task<Integer> task2 = new Task<>()
        {
            @Override
            protected Integer call()
            {
                localUser.setText( SecureProperties.getProperty("settings.db.local.user") );
                localPassphrase.setText( SecureProperties.getProperty("settings.db.local.passphrase") );
                localServerConfiguration.setText( SecureProperties.getProperty("settings.db.local.serverConfiguration") );

                switch ( SecureProperties.getProperty("settings.keyValidityDuration") )
                {
                    case "always":
                        keyValidityDurationSlider.adjustValue(.0);
                        keyValidityDurationLabel.setText(duration[0]);
                        break;
                    case "year":
                        keyValidityDurationSlider.adjustValue(1.0);
                        keyValidityDurationLabel.setText(duration[1]);
                        break;
                    case "quarter":
                        keyValidityDurationSlider.adjustValue(2.0);
                        keyValidityDurationLabel.setText(duration[2]);
                        break;
                    case "month":
                        keyValidityDurationSlider.adjustValue(3.0);
                        keyValidityDurationLabel.setText(duration[3]);
                        break;
                    case "week":
                        keyValidityDurationSlider.adjustValue(4.0);
                        keyValidityDurationLabel.setText(duration[4]);
                        break;
                    case "day":
                        keyValidityDurationSlider.adjustValue(5.0);
                        keyValidityDurationLabel.setText(duration[5]);
                        break;
                    case "session":
                        keyValidityDurationSlider.adjustValue(6.0);
                        keyValidityDurationLabel.setText(duration[6]);
                        break;
                    default:
                        keyValidityDurationLabel.setText(duration[0]);
                        break;
                }

                keyValidityDurationSlider.valueProperty().addListener(
                        (ObservableValue<? extends Number> observableValue, Number number, Number t1) ->
                        {
                            int index = t1.intValue();
                            keyValidityDurationLabel.setText(duration[index]);
                            long l = Math.round( t1.doubleValue() );
                            keyValidityDurationSlider.setValue((double) l);
                        }
                );

                return 0;
            }
        };




        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        try
        {
            thread1.join();
            thread2.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        /*
        keyValidityDurationSlider.valueProperty().addListener(
                (ObservableValue<? extends Number> observableValue, Number number, Number t1) ->
                {
                    int index = t1.intValue();
                    keyValidityDurationLabel.setText(duration[index]);
                    long l = Math.round( t1.doubleValue() );
                    keyValidityDurationSlider.setValue((double) l);
                }
        );
         */

        timer.stopTimer("czas inicjalizacji otwierania okna ustawień z 2ma wątkami");

    }



    /*
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        LaunchTimer timer = new LaunchTimer();

        remoteServerAddressIP.setText( SecureProperties.getProperty("settings.db.remote.serverAddressIP") );
        remotePortNumber.setText( SecureProperties.getProperty("settings.db.remote.portNumber") );
        remoteUser.setText( SecureProperties.getProperty("settings.db.remote.user") );
        remotePassphrase.setText( SecureProperties.getProperty("settings.db.remote.passphrase") );
        remoteServerConfiguration.setText( SecureProperties.getProperty("settings.db.remote.serverConfiguration") );

        localUser.setText( SecureProperties.getProperty("settings.db.local.user") );
        localPassphrase.setText( SecureProperties.getProperty("settings.db.local.passphrase") );
        localServerConfiguration.setText( SecureProperties.getProperty("settings.db.local.serverConfiguration") );

        switch ( SecureProperties.getProperty("settings.keyValidityDuration") )
        {
            case "always":
                keyValidityDurationSlider.adjustValue(.0);
                keyValidityDurationLabel.setText(duration[0]);
                break;
            case "year":
                keyValidityDurationSlider.adjustValue(1.0);
                keyValidityDurationLabel.setText(duration[1]);
                break;
            case "quarter":
                keyValidityDurationSlider.adjustValue(2.0);
                keyValidityDurationLabel.setText(duration[2]);
                break;
            case "month":
                keyValidityDurationSlider.adjustValue(3.0);
                keyValidityDurationLabel.setText(duration[3]);
                break;
            case "week":
                keyValidityDurationSlider.adjustValue(4.0);
                keyValidityDurationLabel.setText(duration[4]);
                break;
            case "day":
                keyValidityDurationSlider.adjustValue(5.0);
                keyValidityDurationLabel.setText(duration[5]);
                break;
            case "session":
                keyValidityDurationSlider.adjustValue(6.0);
                keyValidityDurationLabel.setText(duration[6]);
                break;
            default:
                keyValidityDurationLabel.setText(duration[0]);
                break;
        }

        keyValidityDurationSlider.valueProperty().addListener(
                (ObservableValue<? extends Number> observableValue, Number number, Number t1) ->
                {
                    int index = t1.intValue();
                    keyValidityDurationLabel.setText(duration[index]);
                    long l = Math.round( t1.doubleValue() );
                    keyValidityDurationSlider.setValue((double) l);
                }
        );
        timer.stopTimer("czas inicjalizacji otwierania okna ustawień bez wątków");
    }
     */

    @FXML
    private void onSaveButtonClicked()
    {
        LaunchTimer timer = new LaunchTimer();
        // TODO do it in separate Thread

        Task<Integer> task0 = new Task<>()
        {
            @Override
            protected Integer call()
            {
                SecureProperties.setProperty("settings.db.remote.serverAddressIP",
                        remoteServerAddressIP.getText().trim() );
                SecureProperties.setProperty("settings.db.remote.portNumber",
                        remotePortNumber.getText().trim() );
                SecureProperties.setProperty("settings.db.remote.user",
                        remoteUser.getText().trim() );

                return 0;
            }
        };

        Task<Integer> task1 = new Task<>()
        {
            @Override
            protected Integer call()
            {

                SecureProperties.setProperty("settings.db.remote.passphrase",
                        remotePassphrase.getText().trim() );
                SecureProperties.setProperty("settings.db.remote.serverConfiguration",
                        remoteServerConfiguration.getText().trim() );
                SecureProperties.setProperty("settings.db.local.user",
                        localUser.getText().trim() );
                return 1;
            }
        };


        Task<Integer> task2 = new Task<>()
        {
            @Override
            protected Integer call()
            {
                SecureProperties.setProperty("settings.db.local.passphrase",
                        localPassphrase.getText().trim() );
                SecureProperties.setProperty("settings.db.local.serverConfiguration",
                        localServerConfiguration.getText().trim() );

                int selectedValidityDuration = (int) keyValidityDurationSlider.getValue();

                switch ( selectedValidityDuration )
                {
                    case 0:
                        SecureProperties.setProperty("settings.keyValidityDuration", "always");
                        break;
                    case 1:
                        SecureProperties.setProperty("settings.keyValidityDuration", "year");
                        break;
                    case 2:
                        SecureProperties.setProperty("settings.keyValidityDuration", "quarter");
                        break;
                    case 3:
                        SecureProperties.setProperty("settings.keyValidityDuration", "month");
                        break;
                    case 4:
                        SecureProperties.setProperty("settings.keyValidityDuration", "week");
                        break;
                    case 5:
                        SecureProperties.setProperty("settings.keyValidityDuration", "day");
                        break;
                    case 6:
                        SecureProperties.setProperty("settings.keyValidityDuration", "session");
                        break;
                    default:
                        break;
                }

                return 1;
            }
        };

        Thread thread0 = new Thread(task0);
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread0.start();
        thread1.start();
        thread2.start();

        try
        {
            thread0.join();
            thread1.join();
            thread2.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        stage.close();
        timer.stopTimer("zamykanie okna ustawień z jednym wątkiem");
    }

    /*
    @FXML
    private void onSaveButtonClicked()
    {
        // TODO do it in separate Thread
        SecureProperties.setProperty("settings.db.remote.serverAddressIP",
                remoteServerAddressIP.getText().trim() );
        SecureProperties.setProperty("settings.db.remote.portNumber",
                remotePortNumber.getText().trim() );
        SecureProperties.setProperty("settings.db.remote.user",
                remoteUser.getText().trim() );
        SecureProperties.setProperty("settings.db.remote.passphrase",
                remotePassphrase.getText().trim() );
        SecureProperties.setProperty("settings.db.remote.serverConfiguration",
                remoteServerConfiguration.getText().trim() );

        SecureProperties.setProperty("settings.db.local.user",
                localUser.getText().trim() );
        SecureProperties.setProperty("settings.db.local.passphrase",
                localPassphrase.getText().trim() );
        SecureProperties.setProperty("settings.db.local.serverConfiguration",
                localServerConfiguration.getText().trim() );

        int selectedValidityDuration = (int) keyValidityDurationSlider.getValue();

        switch ( selectedValidityDuration )
        {
            case 0:
                SecureProperties.setProperty("settings.keyValidityDuration", "always");
                break;
            case 1:
                SecureProperties.setProperty("settings.keyValidityDuration", "year");
                break;
            case 2:
                SecureProperties.setProperty("settings.keyValidityDuration", "quarter");
                break;
            case 3:
                SecureProperties.setProperty("settings.keyValidityDuration", "month");
                break;
            case 4:
                SecureProperties.setProperty("settings.keyValidityDuration", "week");
                break;
            case 5:
                SecureProperties.setProperty("settings.keyValidityDuration", "day");
                break;
            case 6:
                SecureProperties.setProperty("settings.keyValidityDuration", "session");
                break;
            default:
                break;
        }

        stage.close();
    }
     */

    @FXML
    private void onCancelButtonClicked()
    {
        stage.close();
    }

    void setStage(Stage stage)
    {
        this.stage = stage;

        Scene scene = stage.getScene();

        scene.widthProperty().addListener( (observableValue, number, t1) ->
        {
            double width = (double) t1;
            //double mainAnchorPaneWidth = mainAnchorPane.getWidth();
            //double scrollPaneWidth = databaseConnectionTabScrollPane.getWidth();

            //double innerAnchorPaneWidth = innerAnchorPane.getWidth();
            double innerAnchorPaneMinWidth = innerAnchorPane.getMinWidth();
            //System.out.println("scroll pane width: " + scrollPaneWidth);
            //System.out.println("scene width: " + width);
            //System.out.println("inner anchor pane width: " + innerAnchorPane.getWidth());
            innerAnchorPane.prefWidthProperty().setValue(width);

            if (width > innerAnchorPaneMinWidth)
                innerAnchorPane.prefWidthProperty().setValue(width);
        });
    }
}





































