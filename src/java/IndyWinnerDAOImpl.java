import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IndyWinnerDAOImpl implements IndyWinnerDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/IndyWinners";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    @Override
    public List<IndyWinner> getWinners(int offset, int limit) {
        List<IndyWinner> winners = new ArrayList<>();
        String query = "SELECT * FROM IndyWinners LIMIT ? OFFSET ?";
        
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, limit);  // Limit 10 results per page
            stmt.setInt(2, offset); // Offset for pagination

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int year = rs.getInt("year");
                winners.add(new IndyWinner(id, name, year));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return winners;
    }
}
