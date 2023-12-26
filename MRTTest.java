import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class MRTTest extends JFrame {

    private JList<String> stationList;
    private DefaultListModel<String> listModel;
    private JTextField searchField;

    private Connection connection;

    public MRTTest() {
        super("MRT Station Information");

        listModel = new DefaultListModel<>();
        stationList = new JList<>(listModel);
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationList.setFont(new Font("標楷體", Font.PLAIN, 24)); // Set 標楷體 font with 24pt size
        stationList.addListSelectionListener(e -> openDetailsFrame(stationList.getSelectedValue()));

        searchField = new JTextField(20);
        searchField.setFont(new Font("標楷體", Font.PLAIN, 36)); // Set 標楷體 font with 36pt size
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                performSearch();
            }
        });

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Arial", Font.BOLD, 24)); // Set font size to 24pt
        searchButton.addActionListener(e -> performSearch());
        searchButton.setBackground(new Color(0xF4EFED)); // Set button color
        searchButton.setBorder(BorderFactory.createLineBorder(new Color(0xF4EFED), 3)); // Add line border

        JScrollPane scrollPane = new JScrollPane(stationList);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Set font size to 24pt
        searchPanel.add(searchLabel);

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        panel.add(searchPanel, BorderLayout.WEST);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        connectToDatabase();
        createTable();
        loadDataFromDatabaseAndDisplay();
    }

    private void openDetailsFrame(String selectedStation) {
        if (selectedStation != null) {
            JFrame detailsFrame = new JFrame("Station Details");

            // Find the corresponding station JSON object based on the selected name
            JSONObject selectedStationObject = getStationObjectByName(selectedStation);

            if (selectedStationObject != null) {
                detailsFrame.setLayout(new GridLayout(5, 2));

                detailsFrame.add(createLabel("Name: " + selectedStationObject.getString("name")));
                detailsFrame.add(createLabel("Line: " + selectedStationObject.getString("line")));
                detailsFrame.add(createLabel("Address: " + selectedStationObject.getString("address")));
                detailsFrame.add(createLabel("Latitude: " + selectedStationObject.getDouble("latitude")));
                detailsFrame.add(createLabel("Longitude: " + selectedStationObject.getDouble("longitude")));

                detailsFrame.setSize(600, 400);
                detailsFrame.setLocationRelativeTo(null);
                detailsFrame.setVisible(true);
            }
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("標楷體", Font.PLAIN, 24));
        return label;
    }

    private void performSearch() {
        String keyword = searchField.getText().toLowerCase();

        JSONArray filteredStations;

        if (keyword.isEmpty()) {
            // If the search field is empty, display all stations
            filteredStations = loadDataFromDatabaseAndDisplay();
        } else {
            // Load your JSON data into jsonArray
            JSONArray jsonArray = loadDataFromDatabase();
            filteredStations = filterByKeyword(jsonArray, keyword);
            displayMRTStations(filteredStations);
        }
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3006/youbikedb";
            String user = "root";
            String password = "Vio010803@";
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS mrtstations ("
                    + "id VARCHAR(10) PRIMARY KEY,"
                    + "name VARCHAR(255),"
                    + "line VARCHAR(255),"
                    + "address VARCHAR(255),"
                    + "latitude DOUBLE,"
                    + "longitude DOUBLE"
                    + ")";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JSONArray loadDataFromDatabase() {
        try {
            String sql = "SELECT * FROM mrtstations";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {
                JSONArray jsonArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject station = new JSONObject();
                    station.put("id", resultSet.getString("id"));
                    station.put("name", resultSet.getString("name"));
                    station.put("line", resultSet.getString("line"));
                    station.put("address", resultSet.getString("address"));
                    station.put("latitude", resultSet.getDouble("latitude"));
                    station.put("longitude", resultSet.getDouble("longitude"));
                    jsonArray.put(station);
                }
                return jsonArray;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    private JSONArray filterByKeyword(JSONArray stations, String keyword) {
        JSONArray filteredStations = new JSONArray();

        for (int i = 0; i < stations.length(); i++) {
            JSONObject station = stations.getJSONObject(i);
            String stationName = station.getString("name").toLowerCase(); // Get the station name

            if (stationName.contains(keyword)) {
                filteredStations.put(station);
            }
        }

        return filteredStations;
    }

    private void displayMRTStations(JSONArray mrtStations) {
        listModel.clear();

        for (int i = 0; i < mrtStations.length(); i++) {
            JSONObject station = mrtStations.getJSONObject(i);
            listModel.addElement(station.getString("name")); // Display only the name
        }
    }

    private JSONObject getStationObjectByName(String stationName) {
        JSONArray jsonArray = loadDataFromDatabase();

        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject station = jsonArray.getJSONObject(i);
                if (station.getString("name").equals(stationName)) {
                    return station;
                }
            }
        }
        return null;
    }

    private JSONArray loadDataFromDatabaseAndDisplay() {
        SwingUtilities.invokeLater(() -> {
            JSONArray jsonArray = loadDataFromDatabase();
            displayMRTStations(jsonArray);
        });
        return loadDataFromDatabase();
    }

    private void insertDataIntoDatabase(JSONArray jsonArray) {
        try {
            String sql = "INSERT INTO mrtstations (id, name, line, address, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject station = jsonArray.getJSONObject(i);
                    statement.setString(1, station.getString("id"));
                    statement.setString(2, station.getString("name"));
                    statement.setString(3, station.getString("line"));
                    statement.setString(4, station.getString("address"));
                    statement.setDouble(5, station.getDouble("latitude"));
                    statement.setDouble(6, station.getDouble("longitude"));
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            // Load JSON data from the local file
            String jsonFilePath = "D:\\JAVA\\Final\\B11009016_Final\\mrt-taipei.json";
            StringBuilder jsonContent = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(jsonFilePath), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }

            JSONArray jsonArray = new JSONArray(jsonContent.toString());

            // Print the contents of the jsonArray for debugging
            System.out.println("Loaded JSON data:");
            System.out.println(jsonArray.toString(2)); // Use 2 for indentation

            insertDataIntoDatabase(jsonArray);
            loadDataFromDatabaseAndDisplay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MRTTest mrtTest = new MRTTest();
            mrtTest.setVisible(true);
            mrtTest.loadData(); // Load data when the application starts
        });
    }
}
