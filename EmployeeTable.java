import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeTable extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public EmployeeTable() {
        setTitle("All Employees");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Table columns
        String[] columns = {"ID", "Name", "Department", "Designation", "Salary", "Join Date", "Status"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);

        // Load data from database
        loadEmployees();

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void loadEmployees() {
        EmployeeDAO dao = new EmployeeDAO();
        List<Employee> employees = dao.getAllEmployees();

        for (Employee emp : employees) {
            Object[] row = {
                emp.getId(),
                emp.getName(),
                emp.getDepartment(),
                emp.getDesignation(),
                emp.getSalary(),
                emp.getJoinDate(),
                emp.getStatus()
            };
            model.addRow(row);
        }
    }

    public static void main(String[] args) {
        new EmployeeTable();
    }
}
