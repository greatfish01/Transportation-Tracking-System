import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class BusTest extends JFrame {

    private JList<String> stationList;
    private DefaultListModel<String> listModel;
    private JTextField searchField;

    private Connection connection;

    public BusTest() {
        super("Bus Station Information");

        listModel = new DefaultListModel<>();
        stationList = new JList<>(listModel);
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationList.setFont(new Font("標楷體", Font.PLAIN, 24)); // Set 標楷體 font with 24pt size
        stationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                openDetailsFrame(stationList.getSelectedValue());
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
        loadData();
        loadDataFromDatabase();
    }

    private void openDetailsFrame(String selectedStation) {
        if (selectedStation != null) {
            JFrame detailsFrame = new JFrame("Bus Station Details");

            // Find the corresponding station JSON object based on the selected name
            JSONObject selectedStationObject = null;
            JSONArray allStations = loadDataFromDatabase();
            for (int i = 0; i < allStations.length(); i++) {
                JSONObject station = allStations.getJSONObject(i);
                if (station.getString("站名").equals(selectedStation)) {
                    selectedStationObject = station;
                    break;
                }
            }

            if (selectedStationObject != null) {
                detailsFrame.setLayout(new GridLayout(6, 1));

                detailsFrame.add(createLabel("Station Name: " + selectedStationObject.getString("站名")));
                detailsFrame.add(createLabel("Address: " + selectedStationObject.getString("地址")));
                detailsFrame.add(createLabel("Phone: " + selectedStationObject.getString("電話")));

                // Get the last destination from the routes
                String[] routesArray = selectedStationObject.getString("路線").split(",");
                String lastDestination = routesArray[routesArray.length - 1].trim();
                detailsFrame.add(createLabel("routes: " + lastDestination));

                detailsFrame.add(createLabel("Parking Information: " + selectedStationObject.getString("停車資訊")));
                detailsFrame.add(createLabel("Location: Lat " + selectedStationObject.getDouble("緯度") + ", Lon "
                        + selectedStationObject.getDouble("經度")));

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
            String sql = "CREATE TABLE IF NOT EXISTS busstations (" +
                    "項次 INT PRIMARY KEY," +
                    "站名 VARCHAR(255)," +
                    "地址 VARCHAR(255)," +
                    "電話 VARCHAR(20)," +
                    "路線 VARCHAR(1000)," +
                    "停車資訊 VARCHAR(500)," +
                    "經度 DOUBLE," +
                    "緯度 DOUBLE" +
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
            String sql = "INSERT INTO busstations (項次, 站名, 地址, 電話, 路線, 停車資訊, 經度, 緯度) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject station = jsonArray.getJSONObject(i);
                    statement.setInt(1, station.getInt("項次"));
                    statement.setString(2, station.getString("站名"));
                    statement.setString(3, station.getString("地址"));
                    statement.setString(4, station.getString("電話"));
                    statement.setString(5, station.getString("路線"));
                    statement.setString(6, station.getString("停車資訊"));
                    statement.setDouble(7, station.getDouble("經度"));
                    statement.setDouble(8, station.getDouble("緯度"));
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JSONArray loadDataFromDatabase() {
        try {
            String sql = "SELECT * FROM busstations";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery()) {
                JSONArray jsonArray = new JSONArray();
                while (resultSet.next()) {
                    JSONObject station = new JSONObject();
                    station.put("項次", resultSet.getInt("項次"));
                    station.put("站名", resultSet.getString("站名"));
                    station.put("地址", resultSet.getString("地址"));
                    station.put("電話", resultSet.getString("電話"));
                    station.put("路線", resultSet.getString("路線"));
                    station.put("停車資訊", resultSet.getString("停車資訊"));
                    station.put("經度", resultSet.getDouble("經度"));
                    station.put("緯度", resultSet.getDouble("緯度"));
                    jsonArray.put(station);
                }
                displayBusStations(jsonArray);
                return jsonArray;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    private void loadData() {
        try {
            URL url = new URL("file:///D:\\JAVA\\Final\\B11009016_Final\\busstations.json");
            JSONArray jsonArray;

            try (InputStreamReader reader = new InputStreamReader(url.openStream())) {
                jsonArray = new JSONArray(new JSONTokener(reader));
            }

            insertDataIntoDatabase(jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performSearch() {
        String searchText = searchField.getText().toLowerCase();

        JSONArray allStations = loadDataFromDatabase();
        listModel.clear();

        for (int i = 0; i < allStations.length(); i++) {
            JSONObject station = allStations.getJSONObject(i);

            boolean nameMatch = station.getString("站名").toLowerCase().contains(searchText);

            if (nameMatch) {
                listModel.addElement(station.getString("站名"));
            }
        }
    }

    private void displayBusStations(JSONArray busStations) {
        listModel.clear();

        for (int i = 0; i < busStations.length(); i++) {
            JSONObject station = busStations.getJSONObject(i);
            listModel.addElement(station.getString("站名")); // Display only the name
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BusTest().setVisible(true));
    }
}
