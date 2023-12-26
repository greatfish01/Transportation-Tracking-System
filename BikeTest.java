import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class BikeTest extends JFrame {

    private JList<String> stationList;
    private DefaultListModel<String> listModel;
    private JTextField searchField;
    private JComboBox<String> areaComboBox;

    private Connection connection;

    public BikeTest() {
        super("YouBike Information");

        listModel = new DefaultListModel<>();
        stationList = new JList<>(listModel);
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationList.setFont(new Font("標楷體", Font.PLAIN, 24)); // Set 標楷體 font with 24pt size
        stationList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    openDetailsFrame(stationList.getSelectedValue());
                }
            }
        });

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

        areaComboBox = new JComboBox<>();
        loadAreasFromJson();
        areaComboBox.setFont(new Font("標楷體", Font.PLAIN, 24)); // Set 標楷體 font with 24pt size
        areaComboBox.addActionListener(e -> performSearch());

        JScrollPane scrollPane = new JScrollPane(stationList);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel searchLabel = new JLabel("Search: ");
        searchLabel.setFont(new Font("Times New Roman", Font.BOLD, 24)); // Set font size to 24pt
        searchPanel.add(searchLabel);
        JLabel areaLabel = new JLabel("Area: ");
        areaLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(areaLabel);
        searchPanel.add(areaComboBox);

        panel.add(searchPanel, BorderLayout.WEST);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        setSize(1000, 600); // Set JFrame size to 1200x800
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        connectToDatabase();
        createTable();
        loadData();
        loadDataFromDatabase();
    }

    private void openDetailsFrame(String selectedStation) {
        if (selectedStation != null) {
            JFrame detailsFrame = new JFrame("Station Details");

            // Find the corresponding station JSON object based on the selected name
            JSONObject selectedStationObject = null;
            JSONArray allStations = loadDataFromDatabase();
            for (int i = 0; i < allStations.length(); i++) {
                JSONObject station = allStations.getJSONObject(i);
                if (station.getString("sna").equals(selectedStation)) {
                    selectedStationObject = station;
                    break;
                }
            }

            if (selectedStationObject != null) {
                detailsFrame.setLayout(new GridLayout(7, 1));

                detailsFrame.add(createLabel("Station Number: " + selectedStationObject.getString("sno")));
                detailsFrame.add(createLabel("Name: " + selectedStationObject.getString("sna")));
                detailsFrame.add(createLabel("Total Bikes: " + selectedStationObject.getInt("tot")));
                detailsFrame.add(createLabel("Available Bikes: " + selectedStationObject.getInt("sbi")));
                detailsFrame.add(createLabel("Area: " + selectedStationObject.getString("sarea")));
                detailsFrame.add(createLabel("Location: " + selectedStationObject.getString("ar")));

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

    private void loadAreasFromJson() {
        try {
            URL url = new URL("file:///D:\\JAVA\\Final\\B11009016_Final\\youBike.json");
            JSONArray jsonArray;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                jsonArray = new JSONArray(new JSONTokener(reader));
            }

            Set<String> uniqueAreas = new HashSet<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject station = jsonArray.getJSONObject(i);
                uniqueAreas.add(station.getString("sarea"));
            }

            areaComboBox.addItem("All");
            for (String area : uniqueAreas) {
                areaComboBox.addItem(area);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            String sql = "CREATE TABLE IF NOT EXISTS bike_stations (" +
                    "sno VARCHAR(10) PRIMARY KEY," +
                    "sna VARCHAR(255)," +
                    "tot INT," +
                    "sbi INT," +
                    "sarea VARCHAR(255)," +
                    "ar VARCHAR(255)" +
                    ")";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertDataIntoDatabase(JSONArray jsonArray) {
        try {
            String sql = "INSERT INTO bike_stations (sno, sna, tot, sbi, sarea, ar) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject station = jsonArray.getJSONObject(i);
                    statement.setString(1, station.getString("sno"));
                    statement.setString(2, station.getString("sna"));
                    statement.setInt(3, station.getInt("tot"));
                    statement.setInt(4, station.getInt("sbi"));
                    statement.setString(5, station.getString("sarea"));
                    statement.setString(6, station.getString("ar"));
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JSONArray loadDataFromDatabase() {
        try {
            String sql = "SELECT * FROM bike_stations";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {
                JSONArray jsonArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject station = new JSONObject();
                    station.put("sno", resultSet.getString("sno"));
                    station.put("sna", resultSet.getString("sna"));
                    station.put("tot", resultSet.getInt("tot"));
                    station.put("sbi", resultSet.getInt("sbi"));
                    station.put("sarea", resultSet.getString("sarea"));
                    station.put("ar", resultSet.getString("ar"));
                    jsonArray.put(station);
                }
                displayBikeStations(jsonArray);
                return jsonArray;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    private void loadData() {
        try {
            URL url = new URL("file:///D:\\JAVA\\Final\\B11009016_Final\\youBike.json");
            JSONArray jsonArray;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                jsonArray = new JSONArray(new JSONTokener(reader));
            }

            insertDataIntoDatabase(jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performSearch() {
        String area = areaComboBox.getSelectedItem().toString();
        String keyword = searchField.getText().toLowerCase();

        JSONArray filteredStations;

        if ("All".equals(area)) {
            filteredStations = loadDataFromDatabase();
        } else {
            filteredStations = filterByArea(area);
        }

        filteredStations = filterByKeyword(filteredStations, keyword);
        displayBikeStations(filteredStations);
    }

    private JSONArray filterByKeyword(JSONArray stations, String keyword) {
        JSONArray filteredStations = new JSONArray();

        for (int i = 0; i < stations.length(); i++) {
            JSONObject station = stations.getJSONObject(i);
            if (station.toString().toLowerCase().contains(keyword)) {
                filteredStations.put(station);
            }
        }

        return filteredStations;
    }

    private JSONArray filterByArea(String area) {
        JSONArray allStations = loadDataFromDatabase();
        JSONArray filteredStations = new JSONArray();

        for (int i = 0; i < allStations.length(); i++) {
            JSONObject station = allStations.getJSONObject(i);
            if (station.getString("sarea").equals(area)) {
                filteredStations.put(station);
            }
        }

        return filteredStations;
    }

    private JSONArray filterByLocationAndKeyword(JSONArray stations, String location, String keyword) {
        JSONArray filteredStations = new JSONArray();

        for (int i = 0; i < stations.length(); i++) {
            JSONObject station = stations.getJSONObject(i);
            if (station.getString("ar").toLowerCase().contains(location) &&
                    station.toString().toLowerCase().contains(keyword)) {
                filteredStations.put(station);
            }
        }

        return filteredStations;
    }

    private void displayBikeStations(JSONArray bikeStations) {
        listModel.clear();

        for (int i = 0; i < bikeStations.length(); i++) {
            JSONObject station = bikeStations.getJSONObject(i);
            listModel.addElement(station.getString("sna")); // Display only the name
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BikeTest().setVisible(true));
    }
}
