package com.github.malyszaryczlowiek.cpcdb.Controllers;

import com.github.malyszaryczlowiek.cpcdb.Compound.*;
import com.github.malyszaryczlowiek.cpcdb.db.MySQLJDBCUtility;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
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
        AddCompoundStageController.OnCompoundAdded, // Added live updating of TableView using
        SearchCompoundStageController.OnChosenSearchingCriteriaListener,
        AskToSaveChangesBeforeQuitController.ZmienMuNazwe
{
    private Stage primaryStage;

    //@FXML private VBox mainSceneVBox;

    // tabela
    @FXML private TableView<Compound> mainSceneTableView;

    // kolumny tabeli
    @FXML private TableColumn<Compound, Integer> idCol;
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

    // menu główne tego stage'a

    @FXML private MenuItem menuFileAddCompound;
    @FXML private MenuItem menuFileLoadFullTable;
    @FXML private MenuItem menuFileSave;
    @FXML private MenuItem menuFileSearch;
    @FXML private MenuItem menuFilePreferences;
    @FXML private MenuItem menuFileQuit;

    // Edit ->
    @FXML private Menu menuEdit;

    @FXML private MenuItem menuEditSelectedCompound;

    // View -> Full Screen
    @FXML private CheckMenuItem menuViewFullScreen;

    // itemy z View -> Show Columns ->
    @FXML private CheckMenuItem menuViewShowColumnId;
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

    @FXML private MenuItem editSelectedCompoundContext;


    private ChangesExecutor changesExecutor;
    private Map<Field, Boolean> mapOfRecentlyNotVisibleTableColumns;

    private List<Compound> fullListOfCompounds;
    private ObservableList<Compound> observableList;
    // mapa z ilością kolumn, które były widoczne zanim użytkownik
    // odkliknął. że chce widzieć wszystkie.


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        // TODO przed uruchomieniem trzeba sprawdzić czy w properties jest wpisane:
        // hasło oraz host, port bo to i tak zawsze użytkowniekiem będzie root
        // jeśli użytkownik kliknie anuluj to robimy Platform.exit()

        setUpMapOfRecentlyNotVisibleTableColumns();
        setMenusAccelerators();
        setUpTableColumns();
        setUpMenuViewShowColumn();
        menuViewFullScreen.setSelected(false);
        changesExecutor = new ChangesExecutor();


        try (Connection connection = MySQLJDBCUtility.getConnection())
        {
            loadTable(connection);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
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
            if (count == 1)
                editSelectedCompoundContext.setDisable(false);
            else
                editSelectedCompoundContext.setDisable(true);
        });

        menuEdit.setOnShowing(event ->
        {
            int count = mainSceneTableView.getSelectionModel().getSelectedItems().size();
            if (count == 1)
                menuEditSelectedCompound.setDisable(false);
            else
                menuEditSelectedCompound.setDisable(true);
        });


        //mainSceneTableView.prefWidthProperty().bind(primaryStage.widthProperty().subtract(20));
        //additionalInfoCol.prefWidthProperty().bind(mainSceneTableView.widthProperty());
    }

    /*
    * ###############################################
    * FUNCTIONS FOR MENUS ITEMS
    * ###############################################
    */

    // FILE ->


    @FXML
    protected void menuFileAddCompound(ActionEvent event) throws IOException
    {
        Stage addCompoundStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/addCompoundStage.fxml"));
        Parent root = loader.load();
        AddCompoundStageController controller = (AddCompoundStageController) loader.getController();
        Scene scene = new Scene(root, 770, 310);
        addCompoundStage.setScene(scene);
        addCompoundStage.initModality(Modality.APPLICATION_MODAL);
        addCompoundStage.setTitle("Add Compound");
        addCompoundStage.setMinHeight(440);
        addCompoundStage.setMinWidth(770);
        addCompoundStage.setResizable(true);
        addCompoundStage.setAlwaysOnTop(true);
        // solution taken from:
        // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        controller.setStage(addCompoundStage);
        controller.setMainStageControllerObject(this);

        addCompoundStage.show();
    }


    @FXML
    protected void onFileSearchMenuItemClicked(ActionEvent actionEvent) throws IOException
    {
        Stage addCompoundStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/findDialogStage.fxml"));
        Parent root = loader.load();
        SearchCompoundStageController controller = (SearchCompoundStageController) loader.getController();
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
    }

    /**
     * metoda uruchamiana po kliknięciu save w menu programu
     * @param event
     */
    @FXML
    protected void onMenuFileSaveClicked(ActionEvent event)
    {
        changesExecutor.applyChanges();
        changesExecutor.clearListOfUpdates();

        event.consume();
    }



    @FXML
    protected void onMenuFileQuit(ActionEvent event)
    {
        closeProgram();
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


    // VIEW -> SHOW ->

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
                        case ID:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                idCol.setVisible(false);
                                menuViewShowColumnId.setSelected(false);
                            }
                            break;
                        case SMILES:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                smilesCol.setVisible(false);
                                menuViewShowColumnSmiles.setSelected(false);
                            }
                            break;
                        case COMPOUNDNUMBER:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                compoundNumCol.setVisible(false);
                                menuViewShowColumnCompoundName.setSelected(false);
                            }
                            break;
                        case AMOUNT:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                amountCol.setVisible(false);
                                menuViewShowColumnAmount.setSelected(false);
                            }
                            break;
                        case UNIT:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                unitCol.setVisible(false);
                                menuViewShowColumnUnit.setSelected(false);
                            }
                            break;
                        case FORM:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                formCol.setVisible(false);
                                menuViewShowColumnForm.setSelected(false);
                            }
                            break;
                        case TEMPSTABILITY:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                tempStabilityCol.setVisible(false);
                                menuViewShowColumnTempStab.setSelected(false);
                            }
                            break;
                        case ARGON:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                argonCol.setVisible(false);
                                menuViewShowColumnArgon.setSelected(false);
                            }
                            break;
                        case CONTAINER:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                containerCol.setVisible(false);
                                menuViewShowColumnContainer.setSelected(false);
                            }
                            break;
                        case STORAGEPLACE:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                storagePlaceCol.setVisible(false);
                                menuViewShowColumnStoragePlace.setSelected(false);
                            }
                            break;
                        case DATETIMEMODIFICATION:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                lastModificationCol.setVisible(false);
                                menuViewShowColumnLastMod.setSelected(false);
                            }
                            break;
                        case ADDITIONALINFO:
                            if (mapOfRecentlyNotVisibleTableColumns.get(field)) // jeśli dane pole jest true to znaczy, że ostatnio było zminimalizowane a więc trzeba je ponownie zminimalizować
                            {
                                additionalInfoCol.setVisible(false);
                                menuViewShowColumnAdditional.setSelected(false);
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
            menuViewShowColumnId.setSelected(true);
            menuViewShowColumnSmiles.setSelected(true);
            menuViewShowColumnCompoundName.setSelected(true);
            menuViewShowColumnAmount.setSelected(true);
            menuViewShowColumnUnit.setSelected(true);
            menuViewShowColumnForm.setSelected(true);
            menuViewShowColumnTempStab.setSelected(true);
            menuViewShowColumnArgon.setSelected(true);
            menuViewShowColumnContainer.setSelected(true);
            menuViewShowColumnLastMod.setSelected(true);
            menuViewShowColumnAdditional.setSelected(true);

            // robie wszystkie kolumny widoczne
            idCol.setVisible(true);
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
        }

        event.consume();
    }

    private void setMenusAccelerators()
    {
        menuFileAddCompound.setAccelerator(KeyCombination.keyCombination("Ctrl+I")); // i from insert
        menuFileLoadFullTable.setAccelerator(KeyCombination.keyCombination("Ctrl+R")); // R from reload
        menuFileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S")); // S from save
        menuFileSearch.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S")); // S from search
        menuFilePreferences.setAccelerator(KeyCombination.keyCombination("Ctrl+P")); // P from preferences
        menuFileQuit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q")); // q from quit

        menuEditSelectedCompound.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));

        menuViewFullScreen.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        menuHelpAboutCPCDB.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
    }


    private void setUpTableColumns()
    {
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

        EventHandler<MouseEvent> handler = (event) ->
        {
            if ( event.getButton().equals(MouseButton.SECONDARY) )
                event.consume();

        };

        idCol.addEventHandler(MouseEvent.MOUSE_CLICKED,handler);













        //mainSceneTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Id comumn is not editable
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));


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
                compound.setSmiles(newSmiles);

                LocalDateTime now = LocalDateTime.now();
                compound.setDateTimeModification(now);

                try
                {
                    int id = compound.getId();
                    changesExecutor.makeUpdate(id, Field.SMILES, newSmiles);
                    changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
                compound.setCompoundNumber(newNumber);

                LocalDateTime now = LocalDateTime.now();
                compound.setDateTimeModification(now);

                try
                {
                    int id = compound.getId();
                    changesExecutor.makeUpdate(id, Field.COMPOUNDNUMBER, newNumber);
                    changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
            Float f = null;
            try
            {
                f = Float.valueOf(newValue);

                LocalDateTime now = LocalDateTime.now();
                if ( !f.equals(compound.getAmount()))
                {
                    compound.setAmount(f);
                    compound.setDateTimeModification(now);

                    try
                    {
                        int id = compound.getId();
                        changesExecutor.makeUpdate(id, Field.AMOUNT, f);
                        changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
                compound.setUnit(Unit.stringToEnum(newUnit));

                LocalDateTime now = LocalDateTime.now();
                compound.setDateTimeModification(now);

                try
                {
                    int id = compound.getId();
                    changesExecutor.makeUpdate(id, Field.UNIT, newUnit);
                    changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
                compound.setForm(newForm);

                LocalDateTime now = LocalDateTime.now();
                compound.setDateTimeModification(now);

                try
                {
                    int id = compound.getId();
                    changesExecutor.makeUpdate(id, Field.FORM, newForm);
                    changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
                compound.setTempStability(TempStability.stringToEnum(newStability));

                LocalDateTime now = LocalDateTime.now();
                compound.setDateTimeModification(now);

                try
                {
                    int id = compound.getId();
                    changesExecutor.makeUpdate(id, Field.TEMPSTABILITY, newStability);
                    changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
                        compound.setArgon(newValue);
                        LocalDateTime now = LocalDateTime.now();
                        compound.setDateTimeModification(now);

                        try
                        {
                            int id = compound.getId();
                            changesExecutor.makeUpdate(id, Field.ARGON, newValue);
                            changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
                compound.setContainer(newContainer);

                LocalDateTime now = LocalDateTime.now();
                compound.setDateTimeModification(now);

                try
                {
                    int id = compound.getId();
                    changesExecutor.makeUpdate(id, Field.CONTAINER, newContainer);
                    changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
                compound.setStoragePlace(newStoragePlace);

                LocalDateTime now = LocalDateTime.now();
                compound.setDateTimeModification(now);

                try
                {
                    int id = compound.getId();
                    changesExecutor.makeUpdate(id, Field.STORAGEPLACE, newStoragePlace);
                    changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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
                compound.setAdditionalInfo(newInfo);

                LocalDateTime now = LocalDateTime.now();
                compound.setDateTimeModification(now);

                try
                {
                    int id = compound.getId();
                    changesExecutor.makeUpdate(id, Field.ADDITIONALINFO, newInfo);
                    changesExecutor.makeUpdate(id, Field.DATETIMEMODIFICATION, now);
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

        menuViewShowColumnSmiles.setOnAction(event ->
        {
            if (smilesCol.isVisible())
            {
                smilesCol.setVisible(false);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.SMILES, true);
                menuViewShowColumnsShowAllColumns.setSelected(false);
            }
            else
            {
                smilesCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.SMILES, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                compoundNumCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.COMPOUNDNUMBER, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                amountCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.AMOUNT, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                unitCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.UNIT, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                formCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.FORM, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                tempStabilityCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.TEMPSTABILITY, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                argonCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.ARGON, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                containerCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.CONTAINER, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                storagePlaceCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.STORAGEPLACE, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                lastModificationCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.DATETIMEMODIFICATION, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
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
            }
            else
            {
                additionalInfoCol.setVisible(true);
                mapOfRecentlyNotVisibleTableColumns.replace(Field.ADDITIONALINFO, false);
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
            }

            event.consume();
        });
    }

    @FXML
    protected void showAdditionalInfo(ActionEvent event) throws IOException
    {
        ObservableList<Compound> selectedItems = mainSceneTableView.getSelectionModel()
                .getSelectedItems();
        //lkdasjgl;kajgflkj
        //new XXXEditAdditionalInfoStage(selectedItems);
        //event.consume();

        Stage showEditStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/showEditCompoundStage.fxml"));
        Parent root = loader.load();
        ShowEditCompoundStageController controller = (ShowEditCompoundStageController) loader.getController();

        showEditStage.setTitle("Edit Compound");
        showEditStage.setScene(new Scene(root,755,600));
        showEditStage.setAlwaysOnTop(true);
        showEditStage.setResizable(true);
        showEditStage.sizeToScene();
        controller.setStage(showEditStage);
        showEditStage.show();

    }


    private void setUpMapOfRecentlyNotVisibleTableColumns()
    {
        mapOfRecentlyNotVisibleTableColumns = new HashMap<>();
        Arrays.stream(Field.values())
                .forEach(field -> mapOfRecentlyNotVisibleTableColumns.put(field, false));
    }

    private boolean areAllColumnsVisible()
    {
        return idCol.isVisible()
                && smilesCol.isVisible()
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

    @Override
    public void notifyAboutAddedCompound(Compound compound)
    {
        observableList.add(compound);
    }

    /**
     * Metoda wywołana z interfejsu
     * SearchCompoundStageController.OnChosenSearchingCriteriaListener
     * która ma za zadanie odfiltrowanie compoundów spełniających żadane kryteria
     * a następnie umieszczenie znalezionych związków w tabeli
     * @param smiles
     * @param smilesAccuracy
     * @param compoundNumber
     * @param form
     * @param container
     * @param storagePlace
     * @param beforeAfter
     * @param selectedLocalDate
     * @param argon
     * @param temperature
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
                            .anyMatch(wordFromCompoundForm ->  formWithoutSpaces.contains(wordFromCompoundForm) // TODo to trzeba przemodelować na stream streamów
                            ); // ewentualnie formWithoutSpaces.contain(wordFromCompoundForm)
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
                            return compound.getTempStability().equals(TempStability.fridge);
                        case "Freezer":
                            return compound.getTempStability().equals(TempStability.freezer);
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
                            .anyMatch(wordFromCompoundContainer ->  containerWithoutSpaces.contains(wordFromCompoundContainer)
                            );
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
                            .anyMatch(wordFromCompoundStoragePlace ->  storagePlaceWithoutSpaces.contains(wordFromCompoundStoragePlace)
                            );
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
            //fullListOfCompounds = observableList.subList(0, observableList.size());
            observableList.clear();
            observableList.setAll(listOfMatchingCompounds);
            mainSceneTableView.refresh();
            //mainSceneTableView.setItems(observableList);
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

                Compound compound = new Compound(id, smiles, compoundName, amount, Unit.stringToEnum(unit),
                        form, TempStability.stringToEnum(tempStability), argon, container,
                        storagePlace, dateTimeModification, additionalInformation);
                fullListOfCompounds.add(compound);
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

        //mainSceneTableView.getSelectionModel().selectedIndexProperty().

        // Dla każdego itemu trzeba zdobyć id zapisać je w changeExecutor
        // an następnie usunąć każdy z tych compoundów z obserwowalnej listy zawierającej
        // obecnie

        observableList.removeAll(selectedItems.sorted());
        mainSceneTableView.refresh();

        fullListOfCompounds.clear();
        fullListOfCompounds.addAll(observableList.sorted());
        // TODO dodatć jeszcze change executor o tym, że te compoundy będą usuwane
        //l;skdag;lasfhg;lkadfjh;lkdfja;hlkj
    }



    /*
     * ###############################################
     * METHODS TO CLOSE PROGRAM
     * ###############################################
     */

    private void closeProgram()
    {
        if (!changesExecutor.isListOfUpdatesEmpty())
        {

            /*
            Show window to ask if save changes
             */
            Stage askToSaveChangesBeforeQuit = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/askToSaveChangesBeforeQuitStage.fxml"));

            try
            {
                Parent root = loader.load();
                AskToSaveChangesBeforeQuitController controller = (AskToSaveChangesBeforeQuitController) loader.getController();
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
            Platform.exit();
    }



    @Override
    public void onSaveChangesAndCloseProgram()
    {
        changesExecutor.applyChanges();
        changesExecutor.clearListOfUpdates();
        Platform.exit();
    }

    @Override
    public void onCloseProgramWithoutChanges()
    {
        Platform.exit();
    }

}


// todo napisać bufor, który będzie w stanie kontrolować jakie zmiany zostały wprowadzone
// tak aby móc je jeszcze odwrócić






















