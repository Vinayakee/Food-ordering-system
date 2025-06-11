import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class CustomerManager extends JFrame {
    private JTextField nameField, emailField, phoneField;
    private JTextArea addressArea;
    private JButton addButton, viewButton;
    private JTextArea displayArea;
    
    public CustomerManager() {
        setTitle("Customer Management");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Customer"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        inputPanel.add(nameField, gbc);
        
        // Email field
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        inputPanel.add(emailField, gbc);
        
        // Phone field
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        inputPanel.add(phoneField, gbc);
        
        // Address field
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        inputPanel.add(new JScrollPane(addressArea), gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Customer");
        viewButton = new JButton("View All Customers");
        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        
        // Display area
        displayArea = new JTextArea(10, 50);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Add components to frame
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(displayArea), BorderLayout.SOUTH);
        
        // Event listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCustomer();
            }
        });
        
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewAllCustomers();
            }
        });
    }
    
    private void addCustomer() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();
        
        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Email are required!");
            return;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, address);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Customer added successfully!");
                clearFields();
                viewAllCustomers();
            }
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding customer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void viewAllCustomers() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM customers ORDER BY customer_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-5s %-20s %-25s %-15s %-30s\n", 
                    "ID", "Name", "Email", "Phone", "Address"));
            sb.append("-".repeat(95)).append("\n");
            
            while (rs.next()) {
                sb.append(String.format("%-5d %-20s %-25s %-15s %-30s\n",
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone") != null ? rs.getString("phone") : "",
                        rs.getString("address") != null ? rs.getString("address") : ""));
            }
            
            displayArea.setText(sb.toString());
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error viewing customers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
    }
}
