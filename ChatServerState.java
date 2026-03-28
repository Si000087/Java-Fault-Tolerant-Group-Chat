import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ChatServerState {
    static final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    static final ConcurrentMap<String, String> clientUsernames = new ConcurrentHashMap<>();
    static final ConcurrentMap<String, String> clientIPs = new ConcurrentHashMap<>();
    static final ConcurrentMap<String, Integer> clientPorts = new ConcurrentHashMap<>();

    static int idCounter = 1;
    static volatile String idCoordinator = null;

    private ChatServerState() {
    }
}