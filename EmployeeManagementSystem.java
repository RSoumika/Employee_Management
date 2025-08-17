import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class EmployeeManagementSystem extends JFrame {
    private JTextField nameField, deptField, designationField, salaryField, joinDateField, statusField;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField, salaryMinField, salaryMaxField;
    private JComboBox<String> sortCombo;

    public EmployeeManagementSystem() {
        setTitle("Employee Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        nameField = new JTextField();
        deptField = new JTextField();
        designationField = new JTextField();
        salaryField = new JTextField();
        joinDateField = new JTextField();
        statusField = new JTextField("Active");

        formPanel.add(new JLabel("Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Department:")); formPanel.add(deptField);
        formPanel.add(new JLabel("Designation:")); formPanel.add(designationField);
        formPanel.add(new JLabel("Salary:")); formPanel.add(salaryField);
        formPanel.add(new JLabel("Join Date (YYYY-MM-DD):")); formPanel.add(joinDateField);
        formPanel.add(new JLabel("Status:")); formPanel.add(statusField);

        // Buttons
        JButton addButton = new JButton("Add");
        JButton viewButton = new JButton("View All");
        JButton filterButton = new JButton("Filter Salary");
        JButton inactiveButton = new JButton("Show Inactive");
        JButton deleteButton = new JButton("Delete");
        JButton updateButton = new JButton("Update");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(filterButton);
        buttonPanel.add(inactiveButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        // Search & Sort
        JPanel searchSortPanel = new JPanel();
        searchField = new JTextField(15);
        salaryMinField = new JTextField(5);
        salaryMaxField = new JTextField(5);
        sortCombo = new JComboBox<>(new String[]{"Sort By", "Salary", "Join Date", "Department"});
        searchSortPanel.add(new JLabel("Search Name/Dept:"));
        searchSortPanel.add(searchField);
        searchSortPanel.add(new JLabel("Min Salary:"));
        searchSortPanel.add(salaryMinField);
        searchSortPanel.add(new JLabel("Max Salary:"));
        searchSortPanel.add(salaryMaxField);
        searchSortPanel.add(sortCombo);

        // Table
        model = new DefaultTableModel();
        table = new JTable(model);
        JScrollPane tableScroll = new JScrollPane(table);

        model.setColumnIdentifiers(new String[]{"ID", "Name", "Department", "Designation", "Salary", "Join Date", "Status"});

        // *** Corrected part: Adjust column widths and center salary ***
        table.getColumnModel().getColumn(3).setPreferredWidth(200); // Designation wider
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Salary
        table.setRowHeight(25);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        // *** End of corrected part ***

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(searchSortPanel, BorderLayout.NORTH);
        bottomPanel.add(tableScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event Listeners
        addButton.addActionListener(e -> addEmployee());
        viewButton.addActionListener(e -> fetchEmployees("SELECT * FROM employees"));
        filterButton.addActionListener(e -> filterBySalary());
        inactiveButton.addActionListener(e -> fetchEmployees("SELECT * FROM employees WHERE status = 'Inactive'"));
        deleteButton.addActionListener(e -> deleteEmployee());
        updateButton.addActionListener(e -> updateEmployee());

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                fetchEmployees("SELECT * FROM employees WHERE name LIKE '%" + text + "%' OR department LIKE '%" + text + "%'");
            }
        });

        sortCombo.addActionListener(e -> {
            String sortBy = sortCombo.getSelectedItem().toString();
            switch (sortBy) {
                case "Salary" -> fetchEmployees("SELECT * FROM employees ORDER BY salary DESC");
                case "Join Date" -> fetchEmployees("SELECT * FROM employees ORDER BY join_date DESC");
                case "Department" -> fetchEmployees("SELECT * FROM employees ORDER BY department ASC");
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    nameField.setText(model.getValueAt(row, 1).toString());
                    deptField.setText(model.getValueAt(row, 2).toString());
                    designationField.setText(model.getValueAt(row, 3).toString());
                    salaryField.setText(model.getValueAt(row, 4).toString());
                    joinDateField.setText(model.getValueAt(row, 5).toString());
                    statusField.setText(model.getValueAt(row, 6).toString());
                }
            }
        });

        setVisible(true);
    }

    private void addEmployee() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO employees (name, department, designation, salary, join_date, status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nameField.getText());
            ps.setString(2, deptField.getText());
            ps.setString(3, designationField.getText());
            ps.setDouble(4, Double.parseDouble(salaryField.getText()));
            ps.setString(5, joinDateField.getText().isEmpty() ? java.time.LocalDate.now().toString() : joinDateField.getText());
            ps.setString(6, statusField.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Employee added successfully");
            fetchEmployees("SELECT * FROM employees");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void updateEmployee() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE employees SET name=?, department=?, designation=?, salary=?, join_date=?, status=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nameField.getText());
            ps.setString(2, deptField.getText());
            ps.setString(3, designationField.getText());
            ps.setDouble(4, Double.parseDouble(salaryField.getText()));
            ps.setString(5, joinDateField.getText());
            ps.setString(6, statusField.getText());
            ps.setInt(7, Integer.parseInt(model.getValueAt(row, 0).toString()));
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Employee updated successfully");
            fetchEmployees("SELECT * FROM employees");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void deleteEmployee() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete this employee?");
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM employees WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(model.getValueAt(row, 0).toString()));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Employee deleted successfully");
                fetchEmployees("SELECT * FROM employees");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
            }
        }
    }

    private void fetchEmployees(String query) {
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            model.setRowCount(0); // Clear existing data
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                // Correctly mapping the values from the database columns to table columns
                row.add(rs.getString("id"));          // Column 1: ID
                row.add(rs.getString("name"));        // Column 2: Name
                row.add(rs.getString("department"));  // Column 3: Department
                row.add(rs.getString("designation")); // Column 4: Designation
                row.add(rs.getString("salary"));      // Column 5: Salary
                row.add(rs.getString("join_date"));   // Column 6: Join Date
                row.add(rs.getString("status"));      // Column 7: Status
                model.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error retrieving data: " + ex.getMessage());
        }
    }

    private void filterBySalary() {
        try {
            double min = Double.parseDouble(salaryMinField.getText());
            double max = Double.parseDouble(salaryMaxField.getText());
            fetchEmployees("SELECT * FROM employees WHERE salary BETWEEN " + min + " AND " + max);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid salary range");
        }
    }

    public static void main(String[] args) {
        new EmployeeManagementSystem();
    }
}
