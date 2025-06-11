import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class RestaurantManager extends JFrame {
    private JTextField restaurantNameField, restaurantAddressField, restaurantPhoneField;
    private JTextField itemNameField, itemPriceField, itemDescField;
    private JComboBox<String> restaurantCombo;
    private JTextArea displayArea;
    private JButton addRestaurantButton, addItemButton, viewMenuButton;
    
    public RestaurantManager() {
        setTitle("Restaurant & Menu Management");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Main panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Restaurant tab
        JPanel restaurantPanel = createRestaurantPanel();
        tabbedPane.addTab("Add Restaurant", restaurantPanel);
        
        // Menu item tab
        JPanel menuPanel = createMenuPanel();
        tabbedPane.addTab("Add Menu Items", menuPanel);
        
        // Display area
        displayArea = new JTextArea(15, 60);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // View button
        viewMenuButton = new JButton("View All Restaurants & Menus");
        viewMenuButton.addActionListener(e -> viewAllRestaurantsAndMenus());
        
        add(tabbedPane, BorderLayout.NORTH);
        add(viewMenuButton, BorderLayout.CENTER);
        add(new JScrollPane(displayArea), BorderLayout.SOUTH);
        
        loadRestaurantsToCombo();
        viewAllRestaurantsAndMenus();
    }
    
    private JPanel createRestaurantPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Restaurant"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Restaurant name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Restaurant Name:"), gbc);
        gbc.gridx = 1;
        restaurantNameField = new JTextField(20);
        panel.add(restaurantNameField, gbc);
        
        // Restaurant address
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        restaurantAddressField = new JTextField(20);
        panel.add(restaurantAddressField, gbc);
        
        // Restaurant phone
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        restaurantPhoneField = new JTextField(20);
        panel.add(restaurantPhoneField, gbc);
        
        // Add button
        gbc.gridx = 1; gbc.gridy = 3;
        addRestaurantButton = new JButton("Add Restaurant");
        addRestaurantButton.addActionListener(e -> addRestaurant());
        panel.add(addRestaurantButton, gbc);
        
        return panel;
    }
    
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add Menu Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Restaurant selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Restaurant:"), gbc);
        gbc.gridx = 1;
        restaurantCombo = new JComboBox<>();
        restaurantCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(restaurantCombo, gbc);
        
        // Item name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        itemNameField = new JTextField(20);
        panel.add(itemNameField, gbc);
        
        // Item price
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Price ($):"), gbc);
        gbc.gridx = 1;
        itemPriceField = new JTextField(20);
        panel.add(itemPriceField, gbc);
        
        // Item description
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        itemDescField = new JTextField(20);
        panel.add(itemDescField, gbc);
        
        // Add button
        gbc.gridx = 1; gbc.gridy = 4;
        addItemButton = new JButton("Add Menu Item");
        addItemButton.addActionListener(e -> addMenuItem());
        panel.add(addItemButton, gbc);
        
        return panel;
    }
    
    private void addRestaurant() {
        String name = restaurantNameField.getText().trim();
        String address = restaurantAddressField.getText().trim();
        String phone = restaurantPhoneField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Restaurant name is required!");
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO restaurants (name, address, phone) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setString(3, phone);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Restaurant added successfully!");
                restaurantNameField.setText("");
                restaurantAddressField.setText("");
                restaurantPhoneField.setText("");
                loadRestaurantsToCombo();
                viewAllRestaurantsAndMenus();
            }
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding restaurant: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addMenuItem() {
        if (restaurantCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a restaurant first!");
            return;
        }
        
        String itemName = itemNameField.getText().trim();
        String priceText = itemPriceField.getText().trim();
        String description = itemDescField.getText().trim();
        
        if (itemName.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Item name and price are required!");
            return;
        }
        
        try {
            double price = Double.parseDouble(priceText);
            String selectedRestaurant = (String) restaurantCombo.getSelectedItem();
            int restaurantId = Integer.parseInt(selectedRestaurant.split(" - ")[0]);
            
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO food_items (restaurant_id, name, price, description) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, restaurantId);
            stmt.setString(2, itemName);
            stmt.setDouble(3, price);
            stmt.setString(4, description);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Menu item added successfully!");
                itemNameField.setText("");
                itemPriceField.setText("");
                itemDescField.setText("");
                viewAllRestaurantsAndMenus();
            }
            stmt.close();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding menu item: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadRestaurantsToCombo() {
        restaurantCombo.removeAllItems();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT restaurant_id, name FROM restaurants ORDER BY name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                String item = rs.getInt("restaurant_id") + " - " + rs.getString("name");
                restaurantCombo.addItem(item);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void viewAllRestaurantsAndMenus() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT r.name as restaurant_name, r.address, r.phone, " +
                        "f.name as item_name, f.price, f.description " +
                        "FROM restaurants r " +
                        "LEFT JOIN food_items f ON r.restaurant_id = f.restaurant_id " +
                        "ORDER BY r.name, f.name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            StringBuilder sb = new StringBuilder();
            String currentRestaurant = "";
            
            while (rs.next()) {
                String restaurantName = rs.getString("restaurant_name");
                
                if (!restaurantName.equals(currentRestaurant)) {
                    if (!currentRestaurant.isEmpty()) {
                        sb.append("\n");
                    }
                    sb.append("=== ").append(restaurantName).append(" ===\n");
                    sb.append("Address: ").append(rs.getString("address") != null ? rs.getString("address") : "N/A").append("\n");
                    sb.append("Phone: ").append(rs.getString("phone") != null ? rs.getString("phone") : "N/A").append("\n");
                    sb.append("Menu Items:\n");
                    currentRestaurant = restaurantName;
                }
                
                String itemName = rs.getString("item_name");
                if (itemName != null) {
                    sb.append("  - ").append(itemName)
                      .append(" ($").append(rs.getDouble("price")).append(")")
                      .append(" - ").append(rs.getString("description") != null ? rs.getString("description") : "")
                      .append("\n");
                } else {
                    sb.append("  (No menu items yet)\n");
                }
            }
            
            displayArea.setText(sb.toString());
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error viewing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
