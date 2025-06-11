import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class OrderManager extends JFrame {
    private JComboBox<String> customerCombo, restaurantCombo;
    private JTable menuTable, cartTable;
    private DefaultTableModel menuModel, cartModel;
    private JButton addToCartButton, removeFromCartButton, placeOrderButton, viewOrdersButton;
    private JLabel totalLabel;
    private double totalAmount = 0.0;
    private List<CartItem> cartItems = new ArrayList<>();
    private JTextArea ordersArea;
    
    public OrderManager() {
        setTitle("Order Management");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Top panel for customer and restaurant selection
        JPanel topPanel = createTopPanel();
        
        // Center panel with menu and cart
        JPanel centerPanel = createCenterPanel();
        
        // Bottom panel with orders display
        JPanel bottomPanel = createBottomPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        loadCustomers();
        loadRestaurants();
        setupEventListeners();
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Order Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Customer selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1;
        customerCombo = new JComboBox<>();
        customerCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(customerCombo, gbc);
        
        // Restaurant selection
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Restaurant:"), gbc);
        gbc.gridx = 1;
        restaurantCombo = new JComboBox<>();
        restaurantCombo.setPreferredSize(new Dimension(200, 25));
        panel.add(restaurantCombo, gbc);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Menu panel
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu"));
        
        String[] menuColumns = {"Item ID", "Name", "Price", "Description"};
        menuModel = new DefaultTableModel(menuColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        menuTable = new JTable(menuModel);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        addToCartButton = new JButton("Add to Cart");
        menuPanel.add(new JScrollPane(menuTable), BorderLayout.CENTER);
        menuPanel.add(addToCartButton, BorderLayout.SOUTH);
        
        // Cart panel
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        
        String[] cartColumns = {"Name", "Price", "Quantity", "Subtotal"};
        cartModel = new DefaultTableModel(cartColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only quantity is editable
            }
        };
        cartTable = new JTable(cartModel);
        
        JPanel cartButtonPanel = new JPanel(new FlowLayout());
        removeFromCartButton = new JButton("Remove Item");
        placeOrderButton = new JButton("Place Order");
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        cartButtonPanel.add(removeFromCartButton);
        cartButtonPanel.add(placeOrderButton);
        cartButtonPanel.add(totalLabel);
        
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        cartPanel.add(cartButtonPanel, BorderLayout.SOUTH);
        
        panel.add(menuPanel);
        panel.add(cartPanel);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Order History"));
        
        ordersArea = new JTextArea(8, 80);
        ordersArea.setEditable(false);
        ordersArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        viewOrdersButton = new JButton("Refresh Orders");
        
        panel.add(new JScrollPane(ordersArea), BorderLayout.CENTER);
        panel.add(viewOrdersButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setupEventListeners() {
        restaurantCombo.addActionListener(e -> loadMenuItems());
        
        addToCartButton.addActionListener(e -> addToCart());
        
        removeFromCartButton.addActionListener(e -> removeFromCart());
        
        placeOrderButton.addActionListener(e -> placeOrder());
        
        viewOrdersButton.addActionListener(e -> viewAllOrders());
        
        // Listen for quantity changes in cart
        cartModel.addTableModelListener(e -> {
            if (e.getColumn() == 2) { // Quantity column
                updateCartQuantity(e.getFirstRow());
            }
        });
    }
    
    private void loadCustomers() {
        customerCombo.removeAllItems();
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT customer_id, name FROM customers ORDER BY name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                String item = rs.getInt("customer_id") + " - " + rs.getString("name");
                customerCombo.addItem(item);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadRestaurants() {
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
    
    private void loadMenuItems() {
        menuModel.setRowCount(0);
        if (restaurantCombo.getSelectedItem() == null) return;
        
        try {
            String selectedRestaurant = (String) restaurantCombo.getSelectedItem();
            int restaurantId = Integer.parseInt(selectedRestaurant.split(" - ")[0]);
            
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM food_items WHERE restaurant_id = ? ORDER BY name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("item_id"),
                    rs.getString("name"),
                    String.format("$%.2f", rs.getDouble("price")),
                    rs.getString("description")
                };
                menuModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void addToCart() {
        int selectedRow = menuTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a menu item!");
            return;
        }
        
        int itemId = (Integer) menuModel.getValueAt(selectedRow, 0);
        String itemName = (String) menuModel.getValueAt(selectedRow, 1);
        String priceStr = (String) menuModel.getValueAt(selectedRow, 2);
        double price = Double.parseDouble(priceStr.replace("$", ""));
        
        // Check if item already in cart
        for (CartItem item : cartItems) {
            if (item.itemId == itemId) {
                item.quantity++;
                updateCartDisplay();
                return;
            }
        }
        
        // Add new item to cart
        cartItems.add(new CartItem(itemId, itemName, price, 1));
        updateCartDisplay();
    }
    
    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a cart item to remove!");
            return;
        }
        
        cartItems.remove(selectedRow);
        updateCartDisplay();
    }
    
    private void updateCartQuantity(int row) {
        try {
            int newQuantity = Integer.parseInt(cartModel.getValueAt(row, 2).toString());
            if (newQuantity <= 0) {
                cartItems.remove(row);
            } else {
                cartItems.get(row).quantity = newQuantity;
            }
            updateCartDisplay();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity!");
            updateCartDisplay(); // Reset display
        }
    }
    
    private void updateCartDisplay() {
        cartModel.setRowCount(0);
        totalAmount = 0.0;
        
        for (CartItem item : cartItems) {
            double subtotal = item.price * item.quantity;
            totalAmount += subtotal;
            
            Object[] row = {
                item.name,
                String.format("$%.2f", item.price),
                item.quantity,
                String.format("$%.2f", subtotal)
            };
            cartModel.addRow(row);
        }
        
        totalLabel.setText(String.format("Total: $%.2f", totalAmount));
    }
    
    private void placeOrder() {
        if (customerCombo.getSelectedItem() == null || restaurantCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select customer and restaurant!");
            return;
        }
        
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }
        
        try {
            String selectedCustomer = (String) customerCombo.getSelectedItem();
            String selectedRestaurant = (String) restaurantCombo.getSelectedItem();
            int customerId = Integer.parseInt(selectedCustomer.split(" - ")[0]);
            int restaurantId = Integer.parseInt(selectedRestaurant.split(" - ")[0]);
            
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert order
            String orderSql = "INSERT INTO orders (customer_id, restaurant_id, total_amount) VALUES (?, ?, ?) RETURNING order_id";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql);
            orderStmt.setInt(1, customerId);
            orderStmt.setInt(2, restaurantId);
            orderStmt.setDouble(3, totalAmount);
            
            ResultSet rs = orderStmt.executeQuery();
            rs.next();
            int orderId = rs.getInt("order_id");
            
            // Insert order items
            String itemSql = "INSERT INTO order_items (order_id, item_id, quantity, price) VALUES (?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            
            for (CartItem item : cartItems) {
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.itemId);
                itemStmt.setInt(3, item.quantity);
                itemStmt.setDouble(4, item.price);
                itemStmt.addBatch();
            }
            
            itemStmt.executeBatch();
            conn.commit(); // Commit transaction
            
            JOptionPane.showMessageDialog(this, "Order placed successfully! Order ID: " + orderId);
            
            // Clear cart
            cartItems.clear();
            updateCartDisplay();
            viewAllOrders();
            
            rs.close();
            orderStmt.close();
            itemStmt.close();
            conn.setAutoCommit(true);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error placing order: " + e.getMessage());
            e.printStackTrace();
            try {
                DatabaseConnection.getConnection().rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void viewAllOrders() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT o.order_id, c.name as customer_name, r.name as restaurant_name, " +
                        "o.order_date, o.total_amount, o.status, " +
                        "f.name as item_name, oi.quantity, oi.price " +
                        "FROM orders o " +
                        "JOIN customers c ON o.customer_id = c.customer_id " +
                        "JOIN restaurants r ON o.restaurant_id = r.restaurant_id " +
                        "JOIN order_items oi ON o.order_id = oi.order_id " +
                        "JOIN food_items f ON oi.item_id = f.item_id " +
                        "ORDER BY o.order_date DESC, o.order_id";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            StringBuilder sb = new StringBuilder();
            int currentOrderId = -1;
            
            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                
                if (orderId != currentOrderId) {
                    if (currentOrderId != -1) {
                        sb.append("\n");
                    }
                    sb.append(String.format("Order #%d - %s\n", orderId, rs.getTimestamp("order_date")));
                    sb.append(String.format("Customer: %s | Restaurant: %s\n", 
                            rs.getString("customer_name"), rs.getString("restaurant_name")));
                    sb.append(String.format("Status: %s | Total: $%.2f\n", 
                            rs.getString("status"), rs.getDouble("total_amount")));
                    sb.append("Items:\n");
                    currentOrderId = orderId;
                }
                
                sb.append(String.format("  - %s x%d @ $%.2f each\n",
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")));
            }
            
            ordersArea.setText(sb.toString());
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error viewing orders: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Inner class for cart items
    private class CartItem {
        int itemId;
        String name;
        double price;
        int quantity;
        
        CartItem(int itemId, String name, double price, int quantity) {
            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }
}