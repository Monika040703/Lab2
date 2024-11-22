import java.util.List;

public interface IndyWinnerDAO {
    List<IndyWinner> getWinners(int offset, int limit);  // Fetch winners with pagination
}
