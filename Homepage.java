import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

public class Homepage extends JFrame {

    public Homepage() {
        setTitle("Transportation Tracking App");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Creating a label for the title with a custom font
        Font titleFont = new Font("Times New Roman", Font.BOLD, 24);
        JLabel titleLabel = new JLabel("Transportation Tracking System");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0)); // Adjusted top margin

        // Creating buttons with images and customized appearance
        Color buttonColor = new Color(0xF4EFED); // Set the button color
        JButton bicycleButton = createTransportButton("Bicycle",
                "D:\\JAVA\\Final\\B11009016_Final\\Image\\Bike.png", buttonColor, e -> openBikeTestFrame());
        JButton busButton = createTransportButton("Bus",
                "D:\\JAVA\\Final\\B11009016_Final\\Image\\Bus.png", buttonColor, e -> openBusTestFrame());
        JButton mrtButton = createTransportButton("MRT",
                "D:\\JAVA\\Final\\B11009016_Final\\Image\\Mrt.png", buttonColor, e -> openMRTTestFrame());

        // Adding the title label and buttons to the main frame with spacing
        add(titleLabel, BorderLayout.NORTH);
        add(Box.createVerticalStrut(50), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 16, 0)); // Adjusted horizontal spacing
        buttonPanel.add(Box.createGlue()); // Create empty space to the left of the first button
        buttonPanel.add(bicycleButton);
        buttonPanel.add(busButton);
        buttonPanel.add(mrtButton);
        buttonPanel.add(Box.createGlue()); // Create empty space to the right of the last button
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 200, 16)); // Adjusted margins

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createTransportButton(String buttonText, String imageName, Color buttonColor,
            ActionListener actionListener) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout());
        button.setBackground(buttonColor); // Set the background color of the button

        // Load image icon and resize it to 70 x 70
        ImageIcon icon = new ImageIcon(imageName);
        Image image = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(image));

        // Add action listener to open a new frame on button click
        button.addActionListener(actionListener);

        // Set square shape with rounded rectangle border (no stroke)
        Color borderColor = new Color(0xC6A28D); // Set the border color to #C6A28D
        Border roundedBorder = new RoundedStrokeBorder(borderColor);
        button.setBorder(roundedBorder);
        button.setPreferredSize(new Dimension(100, 100)); // Set button size

        return button;
    }

    private void openBikeTestFrame() {
        // Create the BikeTest frame
        JFrame bikeTestFrame = new BikeTest();
        bikeTestFrame.setVisible(true);
        bikeTestFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Handle closing event to hide the frame
                bikeTestFrame.setVisible(false);
            }
        });
    }

    private void openBusTestFrame() {
        SwingUtilities.invokeLater(() -> {
            BusTest busTest = new BusTest();
            busTest.setVisible(true);
            busTest.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // Handle closing event to hide the frame
                    busTest.setVisible(false);
                }
            });
        });
    }

    private void openMRTTestFrame() {
        SwingUtilities.invokeLater(() -> {
            MRTTest mrtTest = new MRTTest();
            mrtTest.setVisible(true);
            mrtTest.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // Handle closing event to hide the frame
                    mrtTest.setVisible(false);
                }
            });
        });
    }

    private static void centerOnScreen(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Homepage homepage = new Homepage();
            centerOnScreen(homepage);
            homepage.setVisible(true);
        });
    }

    // Custom border class for rounded rectangle without stroke
    private static class RoundedStrokeBorder implements Border {
        private final Color color;

        public RoundedStrokeBorder(Color color) {
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);

            int arc = 20; // Set the arc size for rounded corners
            g2d.draw(new RoundRectangle2D.Double(x, y, width - 1, height - 1, arc, arc));

            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
