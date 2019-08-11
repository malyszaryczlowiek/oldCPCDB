package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Buffer.ActionType;
import com.github.malyszaryczlowiek.cpcdb.Buffer.ChangesDetector;
import com.github.malyszaryczlowiek.cpcdb.Compound.*;
import com.github.malyszaryczlowiek.cpcdb.Util.SecureProperties;
import com.github.malyszaryczlowiek.cpcdb.db.MySQLJDBCUtility;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MainStageController implements Initializable,
        AddCompoundStageController.CompoundAddedListener, // Added live updating of TableView using
        SearchCompoundStageController.ChosenSearchingCriteriaListener,
        EditCompoundStageController.EditChangesStageListener,
        AskToSaveChangesBeforeQuitController.SaveOrCancelListener
{
    private Stage primaryStage;

    //@FXML private VBox mainSceneVBox;

    // tabela
    @FXML private TableView<Compound> mainSceneTableView;

    // kolumny tabeli
    //@FXML private TableColumn<Compound, Integer> idCol;
    @FXML private TableColumn<Compound, String> smilesCol;
    @FXML private TableColumn<Compound, String> compoundNumCol;
    @FXML private TableColumn<Compound, String> amountCol; // tu zmieniłem na String mimo, że normalenie powinno być float
    @FXML private TableColumn<Compound, String> unitCol;
    @FXML private TableColumn<Compound, String> formCol;
    @FXML private TableColumn<Compound, String> tempStabilityCol;
    @FXML private TableColumn<Compound, Boolean> argonCol;
    @FXML private TableColumn<Compound, String> containerCol;
    @FXML private TableColumn<Compound, String> storagePlaceCol;
    @FXML private TableColumn<Compound, LocalDateTime> lastModificationCol;
    @FXML private TableColumn<Compound, String> additionalInfoCol;

    // FILE ->
    @FXML private Menu menuFile;

    @FXML private MenuItem menuFileAddCompound;
    @FXML private MenuItem menuFileLoadFullTable;
    @FXML private MenuItem menuFileSave;
    @FXML private MenuItem menuFileSearch;
    @FXML private MenuItem menuFilePreferences;
    @FXML private MenuItem menuFileQuit;

    // Edit ->
    @FXML private Menu menuEdit;

    @FXML private MenuItem menuEditSelectedCompound;
    @FXML private MenuItem menuEditDeleteSelectedCompounds;

    // View -> Full Screen
    @FXML private CheckMenuItem menuViewFullScreen;

    // itemy z View -> Show Columns ->
    //@FXML private CheckMenuItem menuViewShowColumnId;
    @FXML private CheckMenuItem menuViewShowColumnSmiles;
    @FXML private CheckMenuItem menuViewShowColumnCompoundName;
    @FXML private CheckMenuItem menuViewShowColumnAmount;
    @FXML private CheckMenuItem menuViewShowColumnUnit;
    @FXML private CheckMenuItem menuViewShowColumnForm;
    @FXML private CheckMenuItem menuViewShowColumnTempStab;
    @FXML private CheckMenuItem menuViewShowColumnArgon;
    @FXML private CheckMenuItem menuViewShowColumnContainer;
    @FXML private CheckMenuItem menuViewShowColumnStoragePlace;
    @FXML private CheckMenuItem menuViewShowColumnLastMod;
    @FXML private CheckMenuItem menuViewShowColumnAdditional;

    @FXML private CheckMenuItem menuViewShowColumnsShowAllColumns;

    // Help -> About CPCDB
    @FXML private MenuItem menuHelpAboutCPCDB;

    @FXML private MenuItem menuEditUndo;
    @FXML private MenuItem menuEditRedo;
    @FXML private MenuItem editSelectedCompoundContext;
    @FXML private MenuItem deleteSelectedCompoundsContext;


    private ChangesDetector changesDetector;
    private Map<Field, Boolean> mapOfRecentlyNotVisibleTableColumns;

    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    // private int maximalLoadedIndexFromDB;
    // mapa z ilością kolumn, które były widoczne zanim użytkownik
    // odkliknął. że chce widzieć wszystkie.


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        checkProperties();
        setUpMapOfRecentlyNotVisibleTableColumns();
        setMenusAccelerators();
        setUpTableColumns();
        setUpMenuViewShowColumn();
        menuViewFullScreen.setSelected(false);


        try (Connection connection = MySQLJDBCUtility.getConnection())
        {
            changesDetector = new ChangesDetector();
            loadTable(connection);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        //mainSceneTableView.column
    }

    /*
     * ###############################################
     * FUNCTIONS FOR SETTING UP STAGE AND HIS COMPONENTS
     * ###############################################
     */

    private void checkProperties()
    {
        if ( SecureProperties.loadProperties() )
            setLoadedProperties();
        else
        {
            try
            {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/sqlLoadingPropertiesStage.fxml"));
                Parent root = loader.load();
                // SqlPropertiesStageController controller =
                // (SqlPropertiesStageController) loader.getController();
                SqlPropertiesStageController controller = loader.getController();

                Stage sqlPropertiesStage = new Stage();
                sqlPropertiesStage.setTitle("Set Database Connection Properties");
                sqlPropertiesStage.setScene(new Scene(root));
                sqlPropertiesStage.setResizable(true);
                sqlPropertiesStage.sizeToScene();
                controller.setStage(sqlPropertiesStage);
                sqlPropertiesStage.showAndWait();

                setSystemStartingProperties();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void setSystemStartingProperties()
    {
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
    }

    private void setLoadedProperties()
    {
        smilesCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.Smiles")) );
        compoundNumCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.CompoundName")) );
        amountCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.Amount")) );
        unitCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.Unit")) );
        formCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.Form")) );
        tempStabilityCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.TemperatureStability")) );
        argonCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.Argon")) );
        containerCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.Container")) );
        storagePlaceCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.StoragePlace")) );
        lastModificationCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.LastModification")) );
        additionalInfoCol.setVisible( "true".equals(SecureProperties.getProperty("column.show.AdditionalInfo")) );
        
        

        menuViewShowColumnSmiles.setSelected( "true".equals(SecureProperties.getProperty("column.show.Smiles")) );
        menuViewShowColumnCompoundName.setSelected( "true".equals(SecureProperties.getProperty("column.show.CompoundName")) );
        menuViewShowColumnAmount.setSelected( "true".equals(SecureProperties.getProperty("column.show.Amount")) );
        menuViewShowColumnUnit.setSelected( "true".equals(SecureProperties.getProperty("column.show.Unit")) );
        menuViewShowColumnForm.setSelected( "true".equals(SecureProperties.getProperty("column.show.Form")) );
        menuViewShowColumnTempStab.setSelected( "true".equals(SecureProperties.getProperty("column.show.TemperatureStability")) );
        menuViewShowColumnArgon.setSelected( "true".equals(SecureProperties.getProperty("column.show.Argon")) );
        menuViewShowColumnContainer.setSelected( "true".equals(SecureProperties.getProperty("column.show.Container")) );
        menuViewShowColumnStoragePlace.setSelected( "true".equals(SecureProperties.getProperty("column.show.StoragePlace")) );
        menuViewShowColumnLastMod.setSelected( "true".equals(SecureProperties.getProperty("column.show.LastModification")) );
        menuViewShowColumnAdditional.setSelected( "true".equals(SecureProperties.getProperty("column.show.AdditionalInfo")) );

        menuViewShowColumnsShowAllColumns.setSelected( areAllColumnsVisible() );


        if ( SecureProperties.hasProperty("column.width.Smiles") )
        {
            try
            {
                smilesCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Smiles") ));
                compoundNumCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.CompoundName") ));
                amountCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Amount") ));
                unitCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Unit") ));
                formCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Form") ));
                tempStabilityCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.TemperatureStability") ));
                argonCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Argon") ));
                containerCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.Container") ));
                storagePlaceCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.StoragePlace") ));
                lastModificationCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.LastModification") ));
                additionalInfoCol.setPrefWidth( Double.parseDouble( SecureProperties.getProperty("column.width.AdditionalInfo") ));


            }
            catch (NumberFormatException  e)
            {
                e.printStackTrace();
            }
        }

        System.out.println("properties loaded correctly");
    }


    public void setStage(Stage stage)
    {
        primaryStage = stage;

        primaryStage.setOnCloseRequest(windowEvent ->
        {
            windowEvent.consume();
            closeProgram();
        });

        mainSceneTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        mainSceneTableView.setOnContextMenuRequested(contextMenuEvent ->
        {
            int count = mainSceneTableView.getSelectionModel().getSelectedItems().size();

            // Edit Selected Compound
            if ( count == 1 )
                editSelectedCompoundContext.setDisable(false);
            else
                editSelectedCompoundContext.setDisable(true);

            //Delete Selected Compound(s)
            if ( count >= 1 )
                deleteSelectedCompoundsContext.setDisable(false);
            else
                deleteSelectedCompoundsContext.setDisable(true);

        });

        menuFile.setOnShowing(event ->
        {
            if ( changesDetector.returnCurrentIndex() > 0)
                menuFileSave.setDisable(false);
            else
                menuFileSave.setDisable(true);
        });

        menuEdit.setOnShowing(event ->
        {
            int count = mainSceneTableView.getSelectionModel().getSelectedItems().size();


            // Edit -> Undo
            if ( changesDetector.returnCurrentIndex() > 0 )
                menuEditUndo.setDisable(false);
            else
                menuEditUndo.setDisable(true);


            // Edit -> Redo
            if ( changesDetector.isNotBufferOnLastPosition() )
                menuEditRedo.setDisable(false);
            else
                menuEditRedo.setDisable(true);


            // Edit -> Edit Selected Compound
            if ( count == 1 )
                menuEditSelectedCompound.setDisable(false);
            else
                menuEditSelectedCompound.setDisable(true);


            // Edit -> Delete Selected Compound(s)
            if ( count >= 1 )
                menuEditDeleteSelectedCompounds.setDisable(false);
            else
                menuEditDeleteSelectedCompounds.setDisable(true);

        });


        //mainSceneTableView.prefWidthProperty().bind(primaryStage.widthProperty().subtract(20));
        //additionalInfoCol.prefWidthProperty().bind(mainSceneTableView.widthProperty());
    }

    private void setUpMapOfRecentlyNotVisibleTableColumns()
    {
        mapOfRecentlyNotVisibleTableColumns = new HashMap<>();
        Arrays.stream(Field.values())
                .forEach(field -> mapOfRecentlyNotVisibleTableColumns.put(field, false));
    }

    private boolean areAllColumnsVisible()
    {
        return //idCol.isVisible() &&
                 smilesCol.isVisible()
                && compoundNumCol.isVisible()
                && amountCol.isVisible()
                && unitCol.isVisible()
                && formCol.isVisible()
                && tempStabilityCol.isVisible()
                && argonCol.isVisible()
                && containerCol.isVisible()
                && storagePlaceCol.isVisible()
                && lastModificationCol.isVisible()
                && additionalInfoCol.isVisible();
    }

    private void setMenusAccelerators()
    {
        menuFileAddCompound.setAccelerator(KeyCombination.keyCombination("Ctrl+I")); // i from insert
        menuFileLoadFullTable.setAccelerator(KeyCombination.keyCombination("Ctrl+R")); // R from reload
        menuFileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S")); // S from save
        menuFileSearch.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S")); // S from search
        menuFilePreferences.setAccelerator(KeyCombination.keyCombination("Ctrl+P")); // P from preferences
        menuFileQuit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q")); // q from quit

        //menuEditUndo.setAccelerator(KeyCombination.keyCombination("Ctrl+U"));
        //menuEditRedo.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        menuEditSelectedCompound.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));

        menuViewFullScreen.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        menuHelpAboutCPCDB.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
    }


    private void setUpTableColumns()
    {
        // Smiles Column set up
        smilesCol.setCellValueFactory(new PropertyValueFactory<>("smiles"));
        smilesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        smilesCol.setOnEditCommit( (TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> pos = event.getTablePosition();
            String newSmiles = event.getNewValue();
            int row = pos.getRow();

            Compound compound = event.getTableView().getItems().get(row);

            if ( !newSmiles.equals(compound.getSmiles()) )
            {
                try
                {
                    changesDetector.makeEdit(compound, Field.SMILES, newSmiles);
                    mainSceneTableView.refresh();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        // Compound Number column set up
        compoundNumCol.setCellValueFactory(new PropertyValueFactory<>("compoundNumber"));
        compoundNumCol.setCellFactory(TextFieldTableCell.forTableColumn());
        compoundNumCol.setOnEditCommit( (TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> position = event.getTablePosition();
            String newNumber = event.getNewValue();
            int row = position.getRow();

            Compound compound = event.getTableView().getItems().get(row);

            if ( !newNumber.equals(compound.getCompoundNumber()) )
            {
                try
                {
                    changesDetector.makeEdit(compound, Field.COMPOUNDNUMBER, newNumber);
                    mainSceneTableView.refresh();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        // Amount column set up
        //amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Compound, String>, ObservableValue<String>>()
        {
            // przetwarzam float z tabeli na string do wyświetlenia
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Compound, String> compoundFloatCellDataFeatures)
            {
                Float f = compoundFloatCellDataFeatures.getValue().getAmount();
                String s = String.valueOf(f);
                return new SimpleStringProperty(s);
            }
        });
        amountCol.setCellFactory(TextFieldTableCell.forTableColumn());
        amountCol.setOnEditCommit((TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> position = event.getTablePosition();
            String newValue = event.getNewValue();
            int row = position.getRow();
            Compound compound = event.getTableView().getItems().get(row);
            Float f;
            try
            {
                f = Float.valueOf(newValue);
                if ( !f.equals(compound.getAmount()))
                {
                    try
                    {
                        changesDetector.makeEdit(compound, Field.AMOUNT, f);
                        mainSceneTableView.refresh();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setResizable(true);
                alert.setWidth(700);
                alert.setHeight(400);
                alert.setTitle("Error");
                alert.setHeaderText("Incorrect input type.");
                alert.setContentText("Input must be in number format.");
                alert.showAndWait();
            }
        });


        // Unit column set up
        unitCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Compound, String>, ObservableValue<String>>()
        {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Compound, String> compoundStringCellDataFeatures)
            {
                Compound compound = compoundStringCellDataFeatures.getValue();
                String unit = compound.getUnit().toString();

                return new SimpleStringProperty(unit);
            }
        });
        ObservableList<String> observableUnitList = FXCollections.observableArrayList(Unit.mg.toString(),
                Unit.g.toString(), Unit.kg.toString(), Unit.ml.toString(), Unit.l.toString());
        unitCol.setCellFactory(ComboBoxTableCell.forTableColumn(observableUnitList));
        unitCol.setOnEditCommit((TableColumn.CellEditEvent<Compound,String> event) ->
        {
            TablePosition<Compound,String> position = event.getTablePosition();

            String newUnit = event.getNewValue();
            int row = position.getRow();
            Compound compound = event.getTableView().getItems().get(row);

            if ( !Unit.stringToEnum(newUnit).equals(compound.getUnit()) )
            {
                try
                {
                    changesDetector.makeEdit(compound, Field.UNIT, newUnit);
                    mainSceneTableView.refresh();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });


        // Form column set Up
        formCol.setCellValueFactory(new PropertyValueFactory<>("form"));
        formCol.setCellFactory(TextFieldTableCell.forTableColumn());
        formCol.setOnEditCommit( (TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> position = event.getTablePosition();
            String newForm = event.getNewValue();
            int row = position.getRow();

            Compound compound = event.getTableView().getItems().get(row);

            if ( !newForm.equals(compound.getForm()) )
            {
                try
                {
                    changesDetector.makeEdit(compound, Field.FORM, newForm);
                    mainSceneTableView.refresh();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });


        // Temp Stability column set Up
        tempStabilityCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Compound, String>, ObservableValue<String>>()
        {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Compound, String> compoundStringCellDataFeatures)
            {
                Compound compound = compoundStringCellDataFeatures.getValue();
                String stability = compound.getTempStability().toString();

                return new SimpleStringProperty(stability);
            }
        });
        List<String> tempStabilityList = Arrays.stream(TempStability.values())
                .map(TempStability::toString)
                .collect(Collectors.toList());
        ObservableList<String> observableTempStabilityList = FXCollections.observableArrayList(tempStabilityList);
        tempStabilityCol.setCellFactory(ComboBoxTableCell.forTableColumn(observableTempStabilityList));
        tempStabilityCol.setOnEditCommit((TableColumn.CellEditEvent<Compound,String> event) ->
        {
            TablePosition<Compound,String> position = event.getTablePosition();

            String newStability = event.getNewValue();
            int row = position.getRow();
            Compound compound = event.getTableView().getItems().get(row);

            if ( !TempStability.stringToEnum(newStability).equals(compound.getTempStability()) )
            {
                try
                {
                    changesDetector.makeEdit(compound, Field.TEMPSTABILITY, newStability);
                    mainSceneTableView.refresh();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });


        // Argon column Set up
        argonCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Compound, Boolean>, ObservableValue<Boolean>>()
        {
            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Compound, Boolean> compoundBooleanCellDataFeatures)
            {
                Compound compound = compoundBooleanCellDataFeatures.getValue();
                SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(compound.isArgon());
                booleanProperty.addListener(new ChangeListener<Boolean>()
                {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue)
                    {
                        try
                        {
                            changesDetector.makeEdit(compound, Field.ARGON, newValue);
                            mainSceneTableView.refresh();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

                return booleanProperty;
            }
        });
        argonCol.setCellFactory(new Callback<TableColumn<Compound, Boolean>, TableCell<Compound, Boolean>>()
        {
            @Override
            public TableCell<Compound, Boolean> call(TableColumn<Compound, Boolean> compoundBooleanTableColumn)
            {
                CheckBoxTableCell<Compound, Boolean> cell = new CheckBoxTableCell<>();
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        });


        // Container column set Up
        containerCol.setCellValueFactory(new PropertyValueFactory<>("container"));
        containerCol.setCellFactory(TextFieldTableCell.forTableColumn());
        containerCol.setOnEditCommit( (TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> position = event.getTablePosition();
            String newContainer = event.getNewValue();
            int row = position.getRow();

            Compound compound = event.getTableView().getItems().get(row);

            if (!newContainer.equals(compound.getContainer()))
            {
                try
                {
                    changesDetector.makeEdit(compound, Field.CONTAINER, newContainer);
                    mainSceneTableView.refresh();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });


        // Storage Place column set up
        storagePlaceCol.setCellValueFactory(new PropertyValueFactory<>("storagePlace"));
        storagePlaceCol.setCellFactory(TextFieldTableCell.forTableColumn());
        storagePlaceCol.setOnEditCommit( (TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> position = event.getTablePosition();
            String newStoragePlace = event.getNewValue();
            int row = position.getRow();

            Compound compound = event.getTableView().getItems().get(row);

            if (!newStoragePlace.equals(compound.getStoragePlace()))
            {
                try
                {
                    changesDetector.makeEdit(compound, Field.STORAGEPLACE, newStoragePlace);
                    mainSceneTableView.refresh();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });


        // Last Modification column set Up
        lastModificationCol.setCellValueFactory(new PropertyValueFactory<>("dateTimeModification"));


        // setUp Additional Info column
        additionalInfoCol.setCellValueFactory(new PropertyValueFactory<>("additionalInfo"));
        additionalInfoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        additionalInfoCol.setOnEditCommit( (TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> position = event.getTablePosition();
            String newInfo = event.getNewValue();
            int row = position.getRow();

            Compound compound = event.getTableView().getItems().get(row);

            if (!newInfo.equals(compound.getAdditionalInfo()))
            {
                try
                {
                    changesDetector.makeEdit(compound, Field.ADDITIONALINFO, newInfo);
                    mainSceneTableView.refresh();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * metoda w której ustawiam listenery dla menuItem'ów używanych do pokazywania i
     * chowania kolumn
     */
    private void setUpMenuViewShowColumn()
    {
        /*
        menuViewShowColumnId.setOnAction(event ->
        {
            if (idCol.isVisible())
            {
                idCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.ID, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
            }
            else
            {
                idCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.ID, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
            }

            event.consume();
        });
         */

        menuViewShowColumnSmiles.setOnAction(event ->
        {
            if (smilesCol.isVisible())
            {
                smilesCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.SMILES, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.Smiles", "false");
            }
            else
            {
                smilesCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.SMILES, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.Smiles", "true");
            }

            event.consume();
        });

        menuViewShowColumnCompoundName.setOnAction(event ->
        {
            if (compoundNumCol.isVisible())
            {
                compoundNumCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.COMPOUNDNUMBER, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.CompoundName", "false");
            }
            else
            {
                compoundNumCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.COMPOUNDNUMBER, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.CompoundName", "true");
            }

            event.consume();
        });

        menuViewShowColumnAmount.setOnAction(event ->
        {
            if (amountCol.isVisible())
            {
                amountCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.AMOUNT, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.Amount", "false");
            }
            else
            {
                amountCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.AMOUNT, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.Amount", "true");
            }

            event.consume();
        });

        menuViewShowColumnUnit.setOnAction(event ->
        {
            if (unitCol.isVisible())
            {
                unitCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.UNIT, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.Unit", "false");
            }
            else
            {
                unitCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.UNIT, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.Unit", "true");
            }

            event.consume();
        });

        menuViewShowColumnForm.setOnAction(event ->
        {
            if (formCol.isVisible())
            {
                formCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.FORM, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.Form", "false");
            }
            else
            {
                formCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.FORM, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.Form", "true");
            }

            event.consume();
        });



        menuViewShowColumnTempStab.setOnAction(event ->
        {
            if (tempStabilityCol.isVisible())
            {
                tempStabilityCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.TEMPSTABILITY, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.TemperatureStability", "false");
            }
            else
            {
                tempStabilityCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.TEMPSTABILITY, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.TemperatureStability", "true");
            }

            event.consume();
        });

        menuViewShowColumnArgon.setOnAction(event ->
        {
            if (argonCol.isVisible())
            {
                argonCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.ARGON, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.Argon", "false");
            }
            else
            {
                argonCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.ARGON, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.Argon", "true");
            }

            event.consume();
        });

        menuViewShowColumnContainer.setOnAction(event ->
        {
            if (containerCol.isVisible())
            {
                containerCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.CONTAINER, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.Container", "false");
            }
            else
            {
                containerCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.CONTAINER, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.Container", "true");
            }

            event.consume();
        });

        menuViewShowColumnStoragePlace.setOnAction(event ->
        {
            if (storagePlaceCol.isVisible())
            {
                storagePlaceCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.STORAGEPLACE, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.StoragePlace", "false");
            }
            else
            {
                storagePlaceCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.STORAGEPLACE, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.StoragePlace", "true");
            }

            event.consume();
        });

        menuViewShowColumnLastMod.setOnAction(event ->
        {
            if (lastModificationCol.isVisible())
            {
                lastModificationCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.DATETIMEMODIFICATION, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.LastModification", "false");
            }
            else
            {
                lastModificationCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.DATETIMEMODIFICATION, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.LastModification", "true");
            }

            event.consume();
        });

        menuViewShowColumnAdditional.setOnAction(event ->
        {
            if (additionalInfoCol.isVisible())
            {
                additionalInfoCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.ADDITIONALINFO, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
                SecureProperties.setProperty("column.show.AdditionalInfo", "false");
            }
            else
            {
                additionalInfoCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.ADDITIONALINFO, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
                SecureProperties.setProperty("column.show.AdditionalInfo", "true");
            }

            event.consume();
        });
    }



    /*
    * ###############################################
    * FUNCTIONS FROM MENUS ITEMS
    * ###############################################
    */

    // FILE -> Add Compound


    @FXML
    protected void menuFileAddCompound(ActionEvent event) throws IOException
    {
        Stage addCompoundStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/addCompoundStage.fxml"));
        Parent root = loader.load();
        AddCompoundStageController controller = loader.getController(); // casting on (AddCompoundStageController)
        Scene scene = new Scene(root);
        addCompoundStage.setScene(scene);
        addCompoundStage.initModality(Modality.APPLICATION_MODAL);
        addCompoundStage.setTitle("Add Compound");
        addCompoundStage.setMinHeight( scene.getHeight() + 50);
        addCompoundStage.setMinWidth(770);
        addCompoundStage.setResizable(true);
        //addCompoundStage.setAlwaysOnTop(true);
        // solution taken from:
        // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        controller.setStage(addCompoundStage);
        controller.setMainStageControllerObject(this);

        addCompoundStage.show();
        event.consume();
    }

    // FILE -> Search

    @FXML
    protected void onFileSearchMenuItemClicked(ActionEvent actionEvent) throws IOException
    {
        Stage addCompoundStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/findDialogStage.fxml"));
        Parent root = loader.load();
        SearchCompoundStageController controller = loader.getController(); // casting on (SearchCompoundStageController)
        Scene scene = new Scene(root, 585, 350);
        addCompoundStage.setScene(scene);
        addCompoundStage.initModality(Modality.APPLICATION_MODAL);
        addCompoundStage.setTitle("Find Compounds");
        addCompoundStage.setMinHeight(355);
        addCompoundStage.setMinWidth(590);
        addCompoundStage.setResizable(true);
        addCompoundStage.setAlwaysOnTop(false);
        // solution taken from:
        // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        controller.setStage(addCompoundStage);
        controller.setMainStageControllerObject(this);

        addCompoundStage.show();
        actionEvent.consume();
    }

    // FILE -> Save

    /**
     * metoda uruchamiana po kliknięciu save w menu programu
     *
     */
    @FXML
    protected void onMenuFileSaveClicked(ActionEvent event)
    {
        if ( changesDetector.returnCurrentIndex() > 0 )
            changesDetector.saveChangesToDatabase();
        event.consume();
    }

    // FILE -> Quit

    @FXML
    protected void onMenuFileQuit()
    {
        closeProgram();
    }

    // EDIT -> Undo

    @FXML
    protected void onUndoClicked()
    {
        if ( changesDetector.returnCurrentIndex() > 0 )
        {
            try
            {
                Map<Integer, Compound> mapOfCompoundsToChangeInTableView = changesDetector.undo();
                ActionType actionType = changesDetector.getActionTypeOfCurrentOperation();
                if ( mapOfCompoundsToChangeInTableView != null )
                {
                    executeUndoRedo( mapOfCompoundsToChangeInTableView, actionType );
                }
                mainSceneTableView.refresh();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onRedoClicked()
    {
        if ( changesDetector.isNotBufferOnLastPosition() )
        {
            try
            {
                Map<Integer, Compound> mapOfCompoundsToChangeInTableView = changesDetector.redo();
                ActionType actionType = changesDetector.getActionTypeOfCurrentOperation();
                if ( mapOfCompoundsToChangeInTableView != null )
                {
                    executeUndoRedo( mapOfCompoundsToChangeInTableView, actionType );
                }
                mainSceneTableView.refresh();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void executeUndoRedo(Map<Integer, Compound> mapOfCompoundsToChangeInTableView, ActionType actionType)
    {
        if ( actionType.equals( ActionType.REMOVE ) )
        {
            if ( mapOfCompoundsToChangeInTableView
                    .values()
                    .stream()
                    .allMatch( Compound::isToDelete )
            )
                observableList.addAll( mapOfCompoundsToChangeInTableView.values() );
            else
                mapOfCompoundsToChangeInTableView.forEach(
                        (index, compound) -> observableList.add(index, compound)
                );

        }
        if ( actionType.equals( ActionType.INSERT ) )
        {
            if ( mapOfCompoundsToChangeInTableView
                    .values()
                    .stream()
                    .allMatch( Compound::isToDelete )
            )
                observableList.removeAll( mapOfCompoundsToChangeInTableView.values() );
            else
                mapOfCompoundsToChangeInTableView.forEach(
                        (index, compound) -> observableList.add(index, compound)
                );
        }
    }


    // VIEW -> Full Screen

    @FXML
    protected void changeFullScreenMode(ActionEvent event)
    {
        if (primaryStage.isFullScreen())
        {
            primaryStage.setFullScreen(false);
            menuViewFullScreen.setSelected(false);
        }
        else
        {
            primaryStage.setFullScreen(true);
            menuViewFullScreen.setSelected(true);
        }
        event.consume();
    }


    // VIEW -> SHOW -> Show All Columns

    @FXML
    protected void onMenuShowAllColumns(ActionEvent event)
    {
        // jeśli wszystkie są widoczne to czy są jakieś do schowanie
                // tak:
                       // schowaj je
                // nie: (warunek początkowy)
                        // nic nie rób

        // nie wszystkie są widoczne
               // zrób wszystkie widoczne

        boolean arrAllColumnsVisible = areAllColumnsVisible();
        boolean showHiddenColumns = mapOfRecentlyNotVisibleTableColumns.values().stream().anyMatch(Boolean::booleanValue);

        if (arrAllColumnsVisible)
        {
            if (showHiddenColumns)
            {
                for (Field field: mapOfRecentlyNotVisibleTableColumns.keySet())
                {
                /*
                jeśli dla danego pola było false to należy taką kolumne schować z powrotem i odznaczyć w liście w menu
                 że jest schowana
                 */
                    switch (field)
                    {
                        // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane
                        // a więc trzeba je ponownie zminimalizować
                        case SMILES:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                smilesCol.setVisible(false);
                                menuViewShowColumnSmiles.setSelected(false);
                                SecureProperties.setProperty("column.show.Smiles", "false");
                            }
                            break;
                        case COMPOUNDNUMBER:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                compoundNumCol.setVisible(false);
                                menuViewShowColumnCompoundName.setSelected(false);
                                SecureProperties.setProperty("column.show.CompoundName", "false");
                            }
                            break;
                        case AMOUNT:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                amountCol.setVisible(false);
                                menuViewShowColumnAmount.setSelected(false);
                                SecureProperties.setProperty("column.show.Amount", "false");
                            }
                            break;
                        case UNIT:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                unitCol.setVisible(false);
                                menuViewShowColumnUnit.setSelected(false);
                                SecureProperties.setProperty("column.show.Unit", "false");
                            }
                            break;
                        case FORM:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                formCol.setVisible(false);
                                menuViewShowColumnForm.setSelected(false);
                                SecureProperties.setProperty("column.show.Form", "false");
                            }
                            break;
                        case TEMPSTABILITY:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                tempStabilityCol.setVisible(false);
                                menuViewShowColumnTempStab.setSelected(false);
                                SecureProperties.setProperty("column.show.TemperatureStability", "false");
                            }
                            break;
                        case ARGON:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                argonCol.setVisible(false);
                                menuViewShowColumnArgon.setSelected(false);
                                SecureProperties.setProperty("column.show.Argon", "false");
                            }
                            break;
                        case CONTAINER:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                containerCol.setVisible(false);
                                menuViewShowColumnContainer.setSelected(false);
                                SecureProperties.setProperty("column.show.Container", "false");
                            }
                            break;
                        case STORAGEPLACE:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                storagePlaceCol.setVisible(false);
                                menuViewShowColumnStoragePlace.setSelected(false);
                                SecureProperties.setProperty("column.show.StoragePlace", "false");
                            }
                            break;
                        case DATETIMEMODIFICATION:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                lastModificationCol.setVisible(false);
                                menuViewShowColumnLastMod.setSelected(false);
                                SecureProperties.setProperty("column.show.LastModification", "false");
                            }
                            break;
                        case ADDITIONALINFO:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field))
                            {
                                additionalInfoCol.setVisible(false);
                                menuViewShowColumnAdditional.setSelected(false);
                                SecureProperties.setProperty("column.show.AdditionalInfo", "false");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            else
                menuViewShowColumnsShowAllColumns.setSelected(true);
        }
        else
        {
            //menuViewShowColumnId.setSelected(true);
            menuViewShowColumnSmiles.setSelected(true);
            menuViewShowColumnCompoundName.setSelected(true);
            menuViewShowColumnAmount.setSelected(true);
            menuViewShowColumnUnit.setSelected(true);
            menuViewShowColumnForm.setSelected(true);
            menuViewShowColumnTempStab.setSelected(true);
            menuViewShowColumnArgon.setSelected(true);
            menuViewShowColumnContainer.setSelected(true);
            menuViewShowColumnStoragePlace.setSelected(true);
            menuViewShowColumnLastMod.setSelected(true);
            menuViewShowColumnAdditional.setSelected(true);

           // lksdagj;lksfj

            // robie wszystkie kolumny widoczne
            //idCol.setVisible(true);

            smilesCol.setVisible(true);
            compoundNumCol.setVisible(true);
            amountCol.setVisible(true);
            unitCol.setVisible(true);
            formCol.setVisible(true);
            tempStabilityCol.setVisible(true);
            argonCol.setVisible(true);
            containerCol.setVisible(true);
            storagePlaceCol.setVisible(true);
            lastModificationCol.setVisible(true);
            additionalInfoCol.setVisible(true);

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

        }

        event.consume();
    }



    @FXML
    protected void showEditCompoundStage(ActionEvent event) throws IOException
    {
        ObservableList<Compound> selectedItems = mainSceneTableView.getSelectionModel()
                .getSelectedItems();

        Compound selectedCompound = selectedItems.get(0);

        Stage showEditStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/showEditCompoundStage.fxml"));
        Parent root = loader.load();
        EditCompoundStageController controller = loader.getController(); // casting on (EditCompoundStageController)

        showEditStage.setTitle("Edit Compound");
        showEditStage.setScene(new Scene(root,755,600));
        //showEditStage.setAlwaysOnTop(true);
        showEditStage.setResizable(true);
        showEditStage.sizeToScene();
        controller.setStage(showEditStage);
        controller.setSelectedItem(selectedCompound);
        controller.setListener(this);
        showEditStage.show();
        event.consume();
    }




    @Override
    public void notifyAboutAddedCompound(Compound compound)
    {
        observableList.add(compound);
        Integer index = observableList.indexOf(compound);
        Map<Integer, Compound> toInsert = new TreeMap<>();
        toInsert.put(index, compound);

        try
        {
            changesDetector.makeInsert(toInsert);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Metoda wywołana z interfejsu
     * SearchCompoundStageController.OnChosenSearchingCriteriaListener
     * która ma za zadanie odfiltrowanie compoundów spełniających żadane kryteria
     * a następnie umieszczenie znalezionych związków w tabeli
     */
    @Override
    public void searchingCriteriaChosen(String smiles, String smilesAccuracy,
                                        String compoundNumber, String form,
                                        String container, String storagePlace,
                                        String beforeAfter, LocalDate selectedLocalDate,
                                        String argon, String temperature)
    {
        // TODO poprawić na wypadek gdy dane wejściowe są puste, czyli gdy smiles jest np. "" itd. :)

        List<Compound> listOfMatchingCompounds = fullListOfCompounds
                .parallelStream()
                .filter(compound ->  // filtering via smiles
                {
                    String smilesSearchCriteria = smiles.replaceAll("[ ]+", "");
                    if (smilesSearchCriteria.equals(""))
                        return true;

                    String smilesOfCompound = compound.getSmiles();
                    if (smilesAccuracy.equals("Is Containing"))
                        return smilesOfCompound.contains(smilesSearchCriteria);
                    else // if must match exactly
                        return smilesOfCompound.equals(smilesSearchCriteria);
                })
                .filter(compound -> // filtering via compoundNumber
                {
                    String compoundNumberWithoutSpaces =
                            compoundNumber.trim().replaceAll("[ ]+", "");

                    if (compoundNumberWithoutSpaces.equals(""))
                        return true;
                    else
                        return compound.getCompoundNumber()
                                .toLowerCase().equalsIgnoreCase(compoundNumberWithoutSpaces);
                })
                .filter(compound -> // filtering via form
                {
                    /*
                    remove any ,:;. and additional spaces from form searching keywords
                     */
                    String formWithoutSpaces = form
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();


                    if (formWithoutSpaces.equals(""))
                        return true;

                    String formFromCompoundLowercase = compound.getForm()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();

                    if ( !formWithoutSpaces.equals("") && formFromCompoundLowercase.equals(""))
                        return false;

                    return  Arrays.stream(formFromCompoundLowercase.split(" "))
                            .anyMatch( formWithoutSpaces::contains );
                            // insted of lambda:
                            // wordFromCompoundForm ->  formWithoutSpaces.contains(wordFromCompoundForm)
                }) // searching in form
                .filter(compound -> // filtering via temperature stability
                {
                    switch (temperature)
                    { // "Any Temperature", "RT", "Fridge", "Freezer"
                        case "Any Temperature":
                            return true;
                        case "RT":
                            return compound.getTempStability().equals(TempStability.RT);
                        case "Fridge":
                            return compound.getTempStability().equals(TempStability.FRIDGE);
                        case "Freezer":
                            return compound.getTempStability().equals(TempStability.FREEZER);
                        default:
                            return true;
                    }
                })
                .filter(compound -> // filtering via argon stability
                {
                    switch (argon)
                    {
                        case "Any Atmosphere":
                            return true;
                        case "Without Argon":
                            return !compound.isArgon();
                        case "Under Argon":
                            return compound.isArgon();
                        default:
                            return true;
                    }
                })
                .filter(compound ->  // container filter
                {
                    String containerWithoutSpaces = container.trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();

                    if (containerWithoutSpaces.equals(""))
                        return true;

                    String containerFromCompoundLowercase = compound.getForm()
                            .trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .toLowerCase();

                    if ( !containerWithoutSpaces.equals("") && containerFromCompoundLowercase.equals(""))
                        return false;

                    return  Arrays.stream(containerFromCompoundLowercase.split(" "))
                            .anyMatch( containerWithoutSpaces::contains );
                            // instead of lambda expression
                    // .anyMatch(wordFromCompoundContainer ->  containerWithoutSpaces.contains(wordFromCompoundContainer)

                }) // container
                .filter(compound -> // filtering via storage place
                {
                    String storagePlaceWithoutSpaces = storagePlace.trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .trim()
                            .toLowerCase();

                    if (storagePlaceWithoutSpaces.equals(""))
                        return true;

                    String storagePlaceFromCompoundLowercase = compound.getForm()
                            .trim()
                            .replaceAll("[,;:.]+"," ")
                            .replaceAll("[ ]{2,}", " ")
                            .toLowerCase();

                    if ( !storagePlaceWithoutSpaces.equals("") && storagePlaceFromCompoundLowercase.equals(""))
                        return false;

                    return  Arrays.stream(storagePlaceFromCompoundLowercase.split(" "))
                            .anyMatch( storagePlaceWithoutSpaces::contains );
                             // instead of lambda expression:
                    // .anyMatch(wordFromCompoundStoragePlace ->  storagePlaceWithoutSpaces.contains(wordFromCompoundStoragePlace)
                })
                .filter(compound -> // filtering via last modification date
                { // "Before", "After"
                    if (beforeAfter.equals("Before"))
                    {
                        return compound.getDateTimeModification().toLocalDate().isBefore(selectedLocalDate)
                                || compound.getDateTimeModification().toLocalDate().isEqual(selectedLocalDate);
                    }
                    else
                    {
                        return compound.getDateTimeModification().toLocalDate().isAfter(selectedLocalDate);
                                //|| compound.getDateTimeModification().toLocalDate().isEqual(selectedLocalDate);
                    }
                })
                .collect(Collectors.toList());

        // wyświetlam znalezione związki
        boolean empty = listOfMatchingCompounds.isEmpty(); //
        if (!empty)
        {
            // todo ten obszar naprawić
            observableList.clear();
            observableList.setAll(listOfMatchingCompounds);
            mainSceneTableView.refresh();
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setResizable(true);
            alert.setWidth(700);
            alert.setHeight(500);
            alert.setTitle("Information");
            alert.setHeaderText("There was no matching compounds!");
            alert.setContentText("There is no matching compounds for selected criteria.");

            alert.showAndWait();
        }
    }


    /*
     * ###############################################
     * METHODS TO LOAD CONTENT OF TABLE
     * ###############################################
     */

    private void loadTable(Connection connection)
    {
        String loadDBSQLQuery = "SELECT * FROM compound";

        try
        {
            PreparedStatement loadDBStatement = connection.prepareStatement(loadDBSQLQuery);
            ResultSet resultSet = loadDBStatement.executeQuery();

            fullListOfCompounds = new ArrayList<>();

            while(resultSet.next())
            {
                int id = resultSet.getInt(1);
                String smiles = resultSet.getString(2);
                String compoundName = resultSet.getString(3);
                float amount = resultSet.getFloat(4);
                String unit = resultSet.getString(5);
                String form = resultSet.getString(6);
                String tempStability = resultSet.getString(7);
                boolean argon = resultSet.getBoolean(8);
                String container = resultSet.getString(9);
                String storagePlace = resultSet.getString(10);
                LocalDateTime dateTimeModification = resultSet.getTimestamp(11).toLocalDateTime();
                String additionalInformation = resultSet.getString(12);

                Compound compound = new Compound(smiles, compoundName, amount, Unit.stringToEnum(unit),
                        form, TempStability.stringToEnum(tempStability), argon, container,
                        storagePlace, dateTimeModification, additionalInformation);
                compound.setId(id);
                compound.setSavedInDatabase(true);

                fullListOfCompounds.add(compound);

                //if ( resultSet.isAfterLast() )
                 //   maximalLoadedIndexFromDB = id; // TODO to będzie wykorzystane do insertowania gdy nie będzie możliwości dodania do bazy danych.
            }

            observableList = FXCollections.observableArrayList(fullListOfCompounds);
            mainSceneTableView.setItems(observableList);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    protected void reloadTable()
    {
        // TODO sprawdzić czy inaczej nie da się reloadować listy
        observableList.clear();
        observableList.setAll(fullListOfCompounds);
        mainSceneTableView.refresh();
    }

    /*
     * ###############################################
     * METHODS OF TABLE VIEW CONTEXT MENU
     * ###############################################
     */

    @FXML
    protected void deleteSelectedCompounds(ActionEvent event)
    {
        ObservableList<Compound> selectedItems = mainSceneTableView.getSelectionModel()
                .getSelectedItems();

        // TODO trzeba jeszcze zaimplementować zapamiętywanie indexów które zostały usunięte i będzie git
        Map<Integer, Compound> mapOfCompounds = new TreeMap<>();
        selectedItems.forEach( compound ->
                mapOfCompounds.put( observableList.indexOf( compound ), compound )
        );

        changesDetector.makeDelete(mapOfCompounds);
        // lkjdsfglasjdg;
        // changesDetector.makeDelete( selectedItems.subList( 0,selectedItems.size() ) ); // to jest już ujebane

        observableList.removeAll(selectedItems.sorted());
        mainSceneTableView.refresh();

        fullListOfCompounds.clear();
        fullListOfCompounds.addAll(observableList.sorted());
        event.consume();
    }



    /*
     * ###############################################
     * METHODS TO CLOSE PROGRAM
     * ###############################################
     */

    private void closeProgram()
    {
        if ( changesDetector.returnCurrentIndex() > 0 )
        {

            /*
            Show window to ask if save changes
             */
            Stage askToSaveChangesBeforeQuit = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("../../../../../res/askToSaveChangesBeforeQuitStage.fxml"));

            try
            {
                Parent root = loader.load();
                AskToSaveChangesBeforeQuitController controller
                        = loader.getController();
                // casting on (AskToSaveChangesBeforeQuitController)
                Scene scene = new Scene(root, 605, 100);
                askToSaveChangesBeforeQuit.setScene(scene);
                askToSaveChangesBeforeQuit.initModality(Modality.APPLICATION_MODAL);
                askToSaveChangesBeforeQuit.setTitle("Save Changes?");
                askToSaveChangesBeforeQuit.sizeToScene();
                //askToSaveChangesBeforeQuit.setMinHeight(355);
                //askToSaveChangesBeforeQuit.setMinWidth(590);
                askToSaveChangesBeforeQuit.setResizable(true);
                askToSaveChangesBeforeQuit.setAlwaysOnTop(false);
                // solution taken from:
                // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
                controller.setStage(askToSaveChangesBeforeQuit);
                controller.setMainStageControllerObject(this);

                askToSaveChangesBeforeQuit.show();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            saveTableViewsColumnSizesAndOrder();
            SecureProperties.saveProperties();
            Platform.exit();
        }
    }

    @Override
    public void reloadTableAfterCompoundEdition()
    {
        mainSceneTableView.refresh();
    }

    @Override
    public void reloadTableAfterCompoundDeleting(Compound compound)
    {
        Map<Integer, Compound> compoundToDelete = new TreeMap<>();
        compoundToDelete.put( observableList.indexOf(compound), compound );
        changesDetector.makeDelete(compoundToDelete);
        observableList.remove(compound);
        mainSceneTableView.refresh();
    }

    @Override
    public void onSaveChangesAndCloseProgram() // TODO napisać tę funkcję jeszcze inaczej.
    {
        changesDetector.saveChangesToDatabase();
        saveTableViewsColumnSizesAndOrder();
        SecureProperties.saveProperties();
        Platform.exit();
    }

    @Override
    public void onCloseProgramWithoutChanges()
    {
        saveTableViewsColumnSizesAndOrder();
        SecureProperties.saveProperties();
        Platform.exit();
    }
    
    private void saveTableViewsColumnSizesAndOrder()
    {
        SecureProperties.setProperty("column.width.Smiles", String.valueOf( smilesCol.getWidth() ));
        SecureProperties.setProperty("column.width.CompoundName", String.valueOf( compoundNumCol.getWidth() ));
        SecureProperties.setProperty("column.width.Amount", String.valueOf( amountCol.getWidth() ));
        SecureProperties.setProperty("column.width.Unit", String.valueOf( unitCol.getWidth() ));
        SecureProperties.setProperty("column.width.Form", String.valueOf( formCol.getWidth() ));
        SecureProperties.setProperty("column.width.TemperatureStability", String.valueOf( tempStabilityCol.getWidth() ));
        SecureProperties.setProperty("column.width.Argon", String.valueOf( argonCol.getWidth() ));
        SecureProperties.setProperty("column.width.Container", String.valueOf( containerCol.getWidth() ));
        SecureProperties.setProperty("column.width.StoragePlace", String.valueOf( storagePlaceCol.getWidth() ));
        SecureProperties.setProperty("column.width.LastModification", String.valueOf( lastModificationCol.getWidth() ));
        SecureProperties.setProperty("column.width.AdditionalInfo", String.valueOf( additionalInfoCol.getWidth() ));
    }
}






        /*
        EventHandler<SortEvent> handler2 = (event) ->
        {

            event.consume();
            System.out.println("Sortowanka nie mamy");
        };

        EventHandler<MouseEvent> handler = (event) ->
        {
            if ( event.getButton().equals(MouseButton.SECONDARY) )
            {
                mainSceneTableView.addEventFilter(SortEvent.sortEvent(), handler2);
                System.out.println("blokujemy");
            }
            else
            {
                mainSceneTableView.kkdghklasgdhaslk;
                mainSceneTableView.removeEventFilter(SortEvent.sortEvent(), handler2);
                System.out.println("odblokowujemy");
            }

        };


        //idCol.addEventHandler(MouseEvent.MOUSE_CLICKED, handler);

        //idCol.getTableView().addEventFilter(MouseEvent.MOUSE_MOVED, handler);
        mainSceneTableView.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);

         */

//mainSceneVBox.ad

// EventHandler<SortEvent> sortEvent = SortEvent.consume();



//EventHandler<SortEvent> sortEvent = (sortEvents) -> sortEvents.consume();

//mainSceneTableView.addEventFilter(SortEvent.ANY, sortEvent);

// idCol.addEventHandler(SortEvent.ANY, sortEvent);

//mainSceneTableView.setFixedCellSize(30);

        /*
        EventHandler<MouseEvent> handler = (event) ->
        {
            if ( event.getButton().equals(MouseButton.SECONDARY) )
                event.consume();

        };

        idCol.addEventHandler(MouseEvent.MOUSE_CLICKED,handler);

         */

//mainSceneTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
// Id comumn is not editable
//idCol.setCellValueFactory(new PropertyValueFactory<>("id"));






















