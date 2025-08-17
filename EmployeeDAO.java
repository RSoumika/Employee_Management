import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO employees (name, department, designation, salary, join_date, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emp.getName());
            stmt.setString(2, emp.getDepartment());
            stmt.setString(3, emp.getDesignation());
            stmt.setDouble(4, emp.getSalary());
            
            // Convert String to java.sql.Date for join_date
            stmt.setDate(5, java.sql.Date.valueOf(emp.getJoinDate()));
            
            stmt.setString(6, emp.getStatus());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.out.println("Error adding employee: " + e.getMessage());
            return false;
        }
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Employee emp = new Employee(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getString("designation"),
                    rs.getDouble("salary"),
                    rs.getDate("join_date").toString(), // Correct fetching of Date
                    rs.getString("status")
                );
                list.add(emp);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving employees: " + e.getMessage());
        }

        return list;
    }
}