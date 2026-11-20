import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;


public class ImportLiChess {
    public static List<GameSummary> importGames(String username)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://lichess.org/api/games/user/" + username + "?max=2"))
                .header("Accept", "application/x-chess-pgn")
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Lichess import failed (HTTP " + response.statusCode() + ")");
        }

        return PgnParser.parse(response.body());
    }
}
