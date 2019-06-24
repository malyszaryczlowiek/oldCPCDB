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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.plaf.synth.SynthScrollBarUI;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MainStageController implements Initializable,
        AddCompoundStageController.OnCompoundAdded // Added live updating of TableView using
{
    private static Stage primaryStage;

    @FXML private VBox mainSceneVBox;


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
    @FXML private MenuItem menuFileSave;
    @FXML private MenuItem menuFileAddCompound;
    @FXML private MenuItem menuFileQuit;

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

    // menu contextowe tabeli
    @FXML private MenuItem idColumnMenuHide;
    @FXML private MenuItem smilesColumnMenuHide;
    @FXML private MenuItem compoundNameColumnMenuHide;
    @FXML private MenuItem amountColumnMenuHide;
    @FXML private MenuItem unitColumnMenuHide;
    @FXML private MenuItem formColumnMenuHide;
    @FXML private MenuItem tempStabilityColumnMenuHide;
    @FXML private MenuItem argonColumnMenuHide;
    @FXML private MenuItem containerColumnMenuHide;
    @FXML private MenuItem storagePlaceColumnMenuHide;
    @FXML private MenuItem lastModificationColumnMenuHide;
    @FXML private MenuItem additionalColumnMenuHide;


    private ChangesExecutor changesExecutor;
    private Map<Field, Boolean> mapOfRecentlyNotVisibleTableColumns;
    private ObservableList<Compound> observableList;
    // mapa z ilością kolumn, które były widoczne zanim użytkownik
    // odkliknął. że chce widzieć wszystkie.


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        // TODO przed uruchomieniem trzeba sprawdzić czy w properties jest wpisane:
        // hasło oraz host, port bo to i tak zawsze użytkowniekiem będzie root
        // jeśli użytkownik kliknie anuluj to robimy Platform.exit()

        makeSceneResizeable();
        setUpMapOfRecentlyNotVisibleTableColumns();
        setMenuAccelerators();
        setUpTableColumns();
        setUpMenuViewShowColumn();
        setUpColumnContextMenu();
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

    private void makeSceneResizeable()
    {
        mainSceneTableView.prefWidthProperty().bind(mainSceneVBox.widthProperty());
        additionalInfoCol.prefWidthProperty().bind(mainSceneVBox.widthProperty());
        mainSceneTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public static void setStage(Stage stage)
    {
        primaryStage = stage;
    }


    @FXML
    protected void onMenuFileQuit(ActionEvent event)
    {
        Platform.exit();
    }

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

    @FXML
    protected void menuFileAddCompound(ActionEvent event) throws IOException
    {
        System.out.println("add button pressed");

        Stage addCompoundStage = new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/addCompoundStage.fxml"));
        Parent root = loader.load();
        AddCompoundStageController controller = (AddCompoundStageController) loader.getController();
        Scene scene = new Scene(root, 770, 310);
        addCompoundStage.setScene(scene);
        addCompoundStage.initModality(Modality.APPLICATION_MODAL);
        addCompoundStage.setTitle("Add Compound");
        addCompoundStage.setMinHeight(340);
        addCompoundStage.setMinWidth(770);
        addCompoundStage.setResizable(true);
        addCompoundStage.setAlwaysOnTop(true);
        // solution taken from:
        // https://stackoverflow.com/questions/13246211/javafx-how-to-get-stage-from-controller-during-initialization
        controller.setStage(addCompoundStage);
        controller.setMainStageControllerObject(this);

        addCompoundStage.show();
    }

    private void loadTable(Connection connection)
    {
        String loadDBSQLQuery = "SELECT * FROM compound";

        try
        {
            PreparedStatement loadDBStatement = connection.prepareStatement(loadDBSQLQuery);
            ResultSet resultSet = loadDBStatement.executeQuery();

            ArrayList<Compound> compoundsUsedInTable = new ArrayList<>();

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
                compoundsUsedInTable.add(compound);
            }

            observableList = FXCollections.observableArrayList(compoundsUsedInTable);
            // TODO ewentualnie zmienić aby dodawać bezpośrednio do observableList zamiast do compoundsUsedInTable
            mainSceneTableView.setItems(observableList);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * metoda uruchamiana po kliknięciu save w menu programu
     * @param event
     */
    @FXML
    protected void onMenuFileSaveClicked(ActionEvent event)
    {
        changesExecutor.applyChanges();
        changesExecutor.clearListOfChanges();

        event.consume();
    }

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

    private void setMenuAccelerators()
    {
        menuFileAddCompound.setAccelerator(KeyCombination.keyCombination("Ctrl+I")); // i od insert
        menuFileSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        menuFileQuit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        menuViewFullScreen.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        menuHelpAboutCPCDB.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
    }

    private void setUpTableColumns()
    {
        //TODO zrobić to w innym wątku tak abu nie blokować głównego


        // Id comumn is not editable
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Smiles Column set up
        smilesCol.setCellValueFactory(new PropertyValueFactory<>("smiles"));
        smilesCol.setCellFactory(TextFieldTableCell.forTableColumn());
        smilesCol.setOnEditCommit((TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> pos = event.getTablePosition();
            String newSmiles = event.getNewValue();
            int row = pos.getRow();
            Compound compound = event.getTableView().getItems().get(row);
            compound.setSmiles(newSmiles);


            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.SMILES, newSmiles);
            }
            catch (IOException e)
            {
                e.printStackTrace();
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
            compound.setCompoundNumber(newNumber);

            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.COMPOUNDNUMBER, newNumber);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        // Amount column set up
        //amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Compound, String>, ObservableValue<String>>()
        {
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

            if (f != null)
                compound.setAmount(f);

            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.AMOUNT, f);
            }
            catch (IOException e)
            {
                e.printStackTrace();
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

            compound.setUnit(Unit.stringToEnum(newUnit));

            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.UNIT, newUnit);
            }
            catch (IOException e)
            {
                e.printStackTrace();
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
            compound.setCompoundNumber(newForm);

            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.FORM, newForm);
            }
            catch (IOException e)
            {
                e.printStackTrace();
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

            compound.setTempStability(TempStability.stringToEnum(newStability));

            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.TEMPSTABILITY, newStability);
            }
            catch (IOException e)
            {
                e.printStackTrace();
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

                        try
                        {
                            int id = compound.getId();
                            changesExecutor.makeChange(id, Field.ARGON, newValue);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        // TODO w tym listenerze będzie trzeba dodać change executor który treckuje zmiany wprowadzone przez użytkownika
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
            compound.setCompoundNumber(newContainer);

            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.CONTAINER, newContainer);
            }
            catch (IOException e)
            {
                e.printStackTrace();
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
            compound.setCompoundNumber(newStoragePlace);

            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.STORAGEPLACE, newStoragePlace);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });


        // Last Modification column set Up
        lastModificationCol.setCellValueFactory(new PropertyValueFactory<>("dateTimeModification"));
        lastModificationColumnMenuHide.setOnAction(event ->
        {
            lastModificationCol.setVisible(false);
            menuViewShowColumnLastMod.setSelected(false);
            mapOfRecentlyNotVisibleTableColumns.replace(Field.DATETIMEMODIFICATION, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
        });


        // setUp Additional Info column
        additionalInfoCol.setCellValueFactory(new PropertyValueFactory<>("additionalInfo"));
        additionalInfoCol.setCellFactory(TextFieldTableCell.forTableColumn());
        additionalInfoCol.setOnEditCommit( (TableColumn.CellEditEvent<Compound, String> event) ->
        {
            TablePosition<Compound, String> position = event.getTablePosition();
            String newInfo = event.getNewValue();
            int row = position.getRow();
            Compound compound = event.getTableView().getItems().get(row);
            compound.setCompoundNumber(newInfo);

            try
            {
                int id = compound.getId();
                changesExecutor.makeChange(id, Field.ADDITIONALINFO, newInfo);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

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

                // todo trzeba zrobić dodatkowe sprawdzenie czy wszystkie pozycje w mapie są teraz false jeśli tak to
                //  można odznaczyć, w menu View że wszystkie kolumny są widoczne
                // jeśli jakakolwiek wartość jest true to znaczy, że cały czas mamy w pamięci, że
                if (areAllColumnsVisible())
                    menuViewShowColumnsShowAllColumns.setSelected(true);
            }

            event.consume();
        });
    }

    private void setUpColumnContextMenu()
    {
        idColumnMenuHide.setOnAction(event ->
        {
            idCol.setVisible(false);
            menuViewShowColumnId.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.ID, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
            event.consume();
        });

        smilesColumnMenuHide.setOnAction(event ->
        {
            smilesCol.setVisible(false);
            menuViewShowColumnSmiles.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.SMILES, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
            event.consume();
        });

        compoundNameColumnMenuHide.setOnAction(event ->
        {
            compoundNumCol.setVisible(false);
            menuViewShowColumnCompoundName.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.COMPOUNDNUMBER, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
            event.consume();
        });

        amountColumnMenuHide.setOnAction(event ->
        {
            amountCol.setVisible(false);
            menuViewShowColumnAmount.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.AMOUNT, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
            event.consume();
        });

        unitColumnMenuHide.setOnAction(event ->
        {
            unitCol.setVisible(false);
            menuViewShowColumnUnit.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.UNIT, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
            event.consume();
        });

        formColumnMenuHide.setOnAction(event ->
        {
            formCol.setVisible(false);
            menuViewShowColumnForm.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.FORM, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
            event.consume();
        });

        tempStabilityColumnMenuHide.setOnAction(event ->
        {
            tempStabilityCol.setVisible(false);
            menuViewShowColumnTempStab.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.TEMPSTABILITY, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
            event.consume();
        });

        argonColumnMenuHide.setOnAction(event ->
        {
            argonCol.setVisible(false);
            menuViewShowColumnArgon.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.ARGON, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
            event.consume();
        });

        containerColumnMenuHide.setOnAction(event ->
        {
            containerCol.setVisible(false);
            menuViewShowColumnContainer.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.CONTAINER, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
        });

        storagePlaceColumnMenuHide.setOnAction(event ->
        {
            storagePlaceCol.setVisible(false);
            menuViewShowColumnStoragePlace.setSelected(false);

            mapOfRecentlyNotVisibleTableColumns.replace(Field.STORAGEPLACE, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
        });

        additionalColumnMenuHide.setOnAction(event ->
        {
            additionalInfoCol.setVisible(false);
            menuViewShowColumnAdditional.setSelected(false);
            mapOfRecentlyNotVisibleTableColumns.replace(Field.ADDITIONALINFO, true);
            menuViewShowColumnsShowAllColumns.setSelected(false);
        });
    }

    private void setUpMapOfRecentlyNotVisibleTableColumns()
    {
        mapOfRecentlyNotVisibleTableColumns = new HashMap<>();
        Arrays.stream(Field.values()).forEach(field -> mapOfRecentlyNotVisibleTableColumns.put(field, false));
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
        System.out.println("Adding operation was successful");
    }
}


// todo napisać bufor, który będzie w stanie kontrolować jakie zmiany zostały wprowadzone
// tak aby móc je jeszcze odwrócić




















