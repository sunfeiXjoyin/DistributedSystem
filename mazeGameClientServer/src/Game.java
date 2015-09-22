import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Benze on 8/31/15.
 */
public class Game {

    private GameState gameState;

    public Game(int numOfTreasure, int dimension) {
        gameState = new GameState(dimension, numOfTreasure);
    }
    
    public Game(GameState passInGameStatus){
    	this.gameState = passInGameStatus;
    }

    public void addPlayer(int id, Player player) {
        gameState.addPlayer(id, player);
    }

    public void initializeGameState() {
        // randomly set the treasure location
        Random random = new Random();
        int limit = gameState.getDimension() * gameState.getDimension();
        for(int i = 0; i < gameState.getNumOfTreasure(); i++) {
            int r = random.nextInt(limit);
            int x = r % gameState.getDimension();
            int y = r / gameState.getDimension();
            gameState.setBlockToTreasure(x, y);
        }
        // randomly set the initial location of players
        Map<Integer, Player> players = gameState.getPlayers();
        for(Integer key : players.keySet()) {
            Player player = players.get(key);
            while(true) {
                int r = random.nextInt(limit);
                int x = r % gameState.getDimension();
                int y = r / gameState.getDimension();
                if(gameState.isEmptyBlock(x, y)) {
                    gameState.setBlockToPlayer(x, y);
                    player.setCoordinate(new Coordinate(x, y));
                    break;
                }
            }
        }
    }

    public ServerMsg createMsgForPlayer(int id) {
        ServerMsg serverMsg = new ServerMsg(gameState);
        serverMsg.setPlayerPos(gameState.getPlayer(id).getCoordinate());
        return serverMsg;
    }

    public ServerMsg createGameOverMsgForPlayer(int id) {
        ServerMsg serverMsg = new ServerMsg(gameState);
        serverMsg.setPlayerPos(gameState.getPlayer(id).getCoordinate());
        serverMsg.setGameOver(true);
        return serverMsg;
    }

//    public GameState getGameState() {
//        return gameState;
//    }

//    public GameState getGameStateForPlayer(Player player) {
//    	GameState  gameSnapshot;
//		try {
//			gameSnapshot = gameState.clone();
//		    int X = player.getCoordinate().getX();
//		    int Y = player.getCoordinate().getY();
////			gameSnapshot.setMapLocation(X, Y.CUR_PLAYER);
//            gameSnapshot.setBlockToCurPlayer(X, Y);
//	    	return gameSnapshot;
//		} catch (CloneNotSupportedException e) {
//			e.printStackTrace();
//			return null;
//		}
//    }

    public boolean playerMove(int playerId, char dir) {
        Player player = gameState.getPlayer(playerId);
        Coordinate playerPos = player.getCoordinate();
        Coordinate target = getTargetCoordinate(playerPos, dir);
        if(gameState.isTargetReachable(target)) {
            if (gameState.isTreasure(target)) {
                gameState.treasureCollected();
                player.collectTreasure();
            }
            // update GameState and player coordinate
            gameState.playerMove(playerPos, target);
            player.setCoordinate(target);
            return true;
        }
        return false;
    }

    public boolean isGameOver() {
        return (gameState.getNumOfTreasure() == 0);
    }
    
    public GameState getGameState() {
		return gameState;
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
    

    public static Coordinate getTargetCoordinate(Coordinate origin, char dir) {
        int newX = origin.getX();
        int newY = origin.getY();
        switch (dir) {
            case 'W':
                newY--;
                break;
            case 'S':
                newY++;
                break;
            case 'A':
                newX--;
                break;
            case 'D':
                newX++;
                break;
        }
        return new Coordinate(newX, newY);
    }
}
