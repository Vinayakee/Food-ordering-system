import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FoodOrderingSystem extends JFrame {
    private JButton customerButton, restaurantButton, orderButton, exitButton;
    
    public FoodOrderingSystem() {
        setTitle("Food Ordering System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        
        // Create title label
        JLabel titleLabel = new JLabel("Food Ordering System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 102, 153));
        
        // Create buttons
        customerButton = new JButton("Manage Customers");
        restaurantButton = new JButton("Manage Restaurants & Menu");
        orderButton = new JButton("Place Orders");
        exitButton = new JButton("Exit");
        
        // Style buttons
        Dimension buttonSize = new Dimension(250, 40);
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        
        customerButton.setPreferredSize(buttonSize);
        customerButton.setFont(buttonFont);
        restaurantButton.setPreferredSize(buttonSize);
        restaurantButton.setFont(buttonFont);
        orderButton.setPreferredSize(buttonSize);
        orderButton.setFont(buttonFont);
        exitButton.setPreferredSize(buttonSize);
        exitButton.setFont(buttonFont);
        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);
        
        // Layout components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        add(titleLabel, gbc);
        
        gbc.gridy = 1;
        add(customerButton, gbc);
        
        gbc.gridy = 2;
        add(restaurantButton, gbc);
        
        gbc.gridy = 3;
        add(orderButton, gbc);
        
        gbc.gridy = 4;
        add(exitButton, gbc);
        
        // Add event listeners
        customerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CustomerManager().setVisible(true);
            }
        });
        
        restaurantButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RestaurantManager().setVisible(true);
            }
        });
        
        orderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new OrderManager().setVisible(true);
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                    FoodOrderingSystem.this,
                    "Are you sure you want to exit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    DatabaseConnection.closeConnection();
                    System.exit(0);
                }
            }
        });
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Test database connection
        if (DatabaseConnection.getConnection() == null) {
            JOptionPane.showMessageDialog(null, 
                "Failed to connect to database! Please check your connection settings.", 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FoodOrderingSystem().setVisible(true);
            }
        });
    }
}