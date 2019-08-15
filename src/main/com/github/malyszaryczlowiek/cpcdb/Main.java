package com.github.malyszaryczlowiek.cpcdb;

import com.github.malyszaryczlowiek.cpcdb.Controllers.MainStageController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Parent root = FXMLLoader.load(getClass().getResource("../../../../res/mainStage.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../res/mainStage.fxml"));
        Parent root = loader.load();
        MainStageController controller =  loader.getController(); // casting (MainStageController)

        primaryStage.setTitle("CPCDB");
        primaryStage.setScene(new Scene(root, 1900, 1000));
        primaryStage.setFullScreenExitHint("Exit full screen mode: Esc");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("Esc"));
        primaryStage.setMaxWidth(4100);
        //primaryStage.centerOnScreen();
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        //primaryStage.setIconified(true);
        primaryStage.setMaximized(true);
        //primaryStage.setOnCloseRequest(e -> Platform.exit());
        controller.setStage(primaryStage);
        // primaryStage.show(); // this method is called in controller.setStage()
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}

