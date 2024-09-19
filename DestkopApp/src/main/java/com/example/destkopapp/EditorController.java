package com.example.destkopapp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class EditorController {

    String url = "jdbc:oracle:thin:@localhost:1521:xe";
    String username = "system";
    String password = "13072001";
    AppMode mode;
    Person selectedPerson;
    @FXML
    private Label ModeText;
    @FXML
    private TextField nameText;
    @FXML
    private TextField surNameText;
    @FXML
    private TextField ageText;
    @FXML
    private TextField adressText;
    @FXML
    private VBox rightPanel;
    @FXML
    private Button saveButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button createButton;

    private List<Person> people;
    public ListView<Person> listView;
    @FXML
    public void initialize() {

        disableMode();

        reload();
        listView.setCellFactory(new Callback<ListView<Person>, ListCell<Person>>() {
            @Override
            public ListCell<Person> call(ListView<Person> listView) {
                return new ListCell<Person>() {
                    @Override
                    protected void updateItem(Person person, boolean empty) {
                        super.updateItem(person, empty);
                        if (empty || person == null) {
                            setText(null);
                        } else {

                            setText(person.name + " " + person.surname + "\nAge: " + person.age + "\nAddress: " + person.address);
                        }
                    }
                };
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
               editButton.setDisable(false);
                nameText.setText(newValue.name);
                surNameText.setText(newValue.surname);
                ageText.setText(String.valueOf(newValue.age));
                adressText.setText(newValue.address);
                selectedPerson = newValue;
            }
        });
    }

    @FXML
    public void reload()
    {
        people = getAllPeople();
        ObservableList<Person> personList = FXCollections.observableArrayList(people);
        listView.setItems(personList);
    }
    @FXML
    public void createNewObject()
    {
        disableMode();
        mode = AppMode.Create;
        saveButton.setDisable(false);
        nameText.setDisable(false);
        surNameText.setDisable(false);
        ageText.setDisable(false);
        adressText.setDisable(false);


        ModeText.setText("Stwórz obiekt");
    }
    @FXML
    public void editObject()
    {
        mode = AppMode.Edit;
        saveButton.setDisable(false);
        nameText.setDisable(false);
        surNameText.setDisable(false);
        ageText.setDisable(false);
        adressText.setDisable(false);
        deleteButton.setDisable(false);
        createButton.setDisable(true);


        ModeText.setText("Edytuj obiekt");
    }
    @FXML void deleteObject()
    {
        int id = selectedPerson.id; // Zakładamy, że wybrana osoba ma swoje unikalne ID

// Tworzenie zapytania SQL do usunięcia rekordu
        String query = "DELETE FROM PEOPLE WHERE id = " + id;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            // Wykonanie zapytania usuwającego
            int rowsDeleted = statement.executeUpdate(query);

            // Sprawdzenie, ile wierszy zostało usuniętych
            if (rowsDeleted > 0) {
                System.out.println("Rekord został pomyślnie usunięty.");
            } else {
                System.out.println("Nie znaleziono rekordu do usunięcia.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        reload();
        disableMode();
    }
    @FXML
    public void save()
    {
        if(mode == AppMode.Create) {
            int id = getMaxId() + 1;
            String name = nameText.getText();
            String surname = surNameText.getText();
            String age = ageText.getText();
            String address = adressText.getText();

            // Tworzenie zapytania SQL
            String query = "INSERT INTO PEOPLE (id,name, surname, age, address) VALUES ('"
                    + id + "', '"
                    + name + "', '"
                    + surname + "', "
                    + age + ", '"
                    + address + "')";

            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                if (!resultSet.isBeforeFirst()) {
                    System.out.println("Brak wyników zapytania.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else if(mode == AppMode.Edit)
        {
            int id = selectedPerson.id; // Zakładamy, że masz wybraną osobę z unikalnym ID
            String name = nameText.getText();
            String surname = surNameText.getText();
            String age = ageText.getText();
            String address = adressText.getText();

// Tworzenie zapytania SQL do aktualizacji rekordu
            String query = "UPDATE PEOPLE SET name = '"
                    + name + "', surname = '"
                    + surname + "', age = "
                    + age + ", address = '"
                    + address + "' WHERE id = " + id;

            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()) {
                // Wykonanie zapytania aktualizującego
                int rowsUpdated = statement.executeUpdate(query);

                // Sprawdzanie, ile wierszy zostało zaktualizowanych
                if (rowsUpdated > 0) {
                    System.out.println("Rekord został zaktualizowany pomyślnie.");
                } else {
                    System.out.println("Nie znaleziono rekordu do zaktualizowania.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        reload();
        disableMode();
    }

    public void disableMode()
    {
        nameText.setText("");
        surNameText.setText("");
        ageText.setText("");
        adressText.setText("");
        nameText.setDisable(true);
        surNameText.setDisable(true);
        ageText.setDisable(true);
        adressText.setDisable(true);
        mode = AppMode.None;
        ModeText.setText("");
        saveButton.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        createButton.setDisable(false);
    }

    public List<Person> getAllPeople() {
        List<Person> people = new ArrayList<>();

        String query = "SELECT * from PEOPLE";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (!resultSet.isBeforeFirst()) {
                System.out.println("Brak wyników zapytania.");
            }
            while (resultSet.next()) {
                var id = resultSet.getInt("ID");
                var name = resultSet.getString("NAME");
                String surname = resultSet.getString("SURNAME");
                int age = resultSet.getInt("AGE");
                String address = resultSet.getString("ADDRESS");


                Person person = new Person(id,name, surname, age, address);
                people.add(person);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return people;
    }

    public int getMaxId()
    {
        String query = "SELECT MAX(ID) AS max_id FROM PEOPLE";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Sprawdzanie czy wynik istnieje
            if (resultSet.next()) {
                return resultSet.getInt("max_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
