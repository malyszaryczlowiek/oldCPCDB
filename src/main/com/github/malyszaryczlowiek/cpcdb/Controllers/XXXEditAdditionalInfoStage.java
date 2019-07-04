package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Compound.Compound;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;


public class XXXEditAdditionalInfoStage
{
    private ArrayList<VBox> vBoxList;

    public XXXEditAdditionalInfoStage(ObservableList<Compound> selectedList)
    {
        int size = selectedList.size();

        vBoxList = new ArrayList<>(size);

        // ewentualnie zrobić z metodą add()
        for (Compound compound: selectedList)
        {
            vBoxList.add(generateVBox(compound.getId(), compound.getAdditionalInfo()));
        }




        VBox root = new VBox();

        // dodaje vBoxy do głównego cBoxu
        root.getChildren().addAll(vBoxList);

        Button cancel = new Button();
        Button save = new Button();

        Stage stage = new Stage();

        cancel.setOnAction(event ->
        {
            // nie wykonuje żadnej edycji
            stage.close();
        });

        save.setOnAction(event ->
        {
            // TODO dodać jeszcze obsługę tego co zostało zmienione w TextArea
            //laksgja;lkfjgal;kjfgl;lasjkg;

            stage.close();
        });

        root.getChildren().addAll(cancel, save);


        Scene scene = new Scene(root, 550, 250);

        stage.setScene(scene);
        stage.setTitle("Additional Info");
        stage.setScene(scene);
        //stage.setAlwaysOnTop(true);
        stage.sizeToScene();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private VBox generateVBox(int id, String additionalInfo)
    {
        VBox box = new VBox();

        Label label = new Label("Additional Info of: " + id);

        box.getChildren().add(label);

        TextArea textArea = new TextArea();
        textArea.setEditable(true);
        textArea.setText(additionalInfo);
        textArea.setPrefSize(100, 200);


        box.getChildren().add(textArea);

        return box;
    }

}
