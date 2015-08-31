import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Benze on 8/31/15.
 */
public class MazeGameServerImpl2 implements MazeGameServer{

    private static Registry rmiRegistry;

    private final int GAME_INIT = 0;

    private final int GAME_PENDING_START = 1;

    private final int GAME_START = 2;

    private final int GAME_END = 3;

    private volatile int gameStatus = GAME_INIT;

    private Map<Integer, Player> players = new HashMap<Integer, Player>();

    private int playerNum = 0;

    // TODO: may need to synchronize
    //private List<MazeGameClient> gameClients = new ArrayList<>();

    /** registry port for testisng */
    private static final int REGISTRY_PORT = 8888;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private GameState gameState;

    private class GameInitializeTask implements Runnable {
        @Override
        public void run() {
            gameStatus = GAME_START;
            initializeGame();
            for(Integer key : players.keySet()) {
                Player player = players.get(key);
                try {
                    player.notifyGameStart(gameState);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initializeGame() {
        int mapSize = 5, numTresures = 5;
        gameState = new GameState(new char[5][5]);
//        gameState = new GameState(mapSize, players.size(), numTresures);
    }

    @Override
    public synchronized boolean joinGame(MazeGameClient client) throws RemoteException {
        if(gameStatus == GAME_INIT) {
            // The first player join in, notify game start in 20 seconds
            players.put(playerNum, new Player(playerNum, client));
            playerNum++;
            executor.schedule(new GameInitializeTask(), 20, TimeUnit.SECONDS);
            gameStatus = GAME_PENDING_START;
            System.out.println("first client");
            return true;
        } else if (gameStatus == GAME_PENDING_START) {
            players.put(playerNum, new Player(playerNum, client));
            playerNum++;
            System.out.println("new client");
            return true;
        } else {
            // game started, can't join anymore
            System.out.println("join refuse");
            return false;
        }
    }

    @Override
    public GameState move(int playerID, String dir) throws RemoteException {
        Player player = players.get(playerID);

        //game.move(player, dir);

        return null;
    }

    public static void main(String[] args) {
        try {
            MazeGameServerImpl2 server = new MazeGameServerImpl2();
            MazeGameServer stub = (MazeGameServer) UnicastRemoteObject.exportObject(server, 0);
            // Bind the remote object's stub in the registry
            rmiRegistry = LocateRegistry.createRegistry(REGISTRY_PORT);
            rmiRegistry.bind("MazeGameServer", stub);
            System.out.println("Server ready");
        } catch (AccessException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}