package com.github.malyszaryczlowiek.cpcdb;

import com.github.malyszaryczlowiek.cpcdb.Controllers.MainStageController;

import javafx.application.Application;
import javafx.application.Platform;
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
        Parent root = FXMLLoader.load(getClass().getResource("../../../../res/mainStage.fxml"));
        primaryStage.setTitle("CPCDB");
        primaryStage.setScene(new Scene(root, 700, 400));
        primaryStage.setFullScreenExitHint("Exit full screen mode: Esc");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("Esc"));
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();
        primaryStage.setIconified(true);
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        MainStageController.getStage(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}


// todo lista rzeczy do zrobienia

/*

id związku
smiles
numer związku
ilość
rodzaj butelki
umiejscowienie
data ostatniej modyfikacji
informacje dodatkowe (otwierane w nowym oknie)



// sortowanie w tabeli powinno móc się odbywać tylko po numerze związku i po dacie modyfikacji. reszta sortowania nie ma sensu

jwzór sumaryczny Masa molowa
informacje szczegółowe
projekty w których był używany
data dodania
postać w jakiej występuje (osad olej barwa, lotna ciecz etc.)
lista osób pobierających i ilość przez nich pobrana

stowrzyć klasę pierwiastek mającą dwa atrybuty masę symbol, ilość wiązań
stworzy klasę związek budowaną z pierwiastków
gdzie nody


*/
