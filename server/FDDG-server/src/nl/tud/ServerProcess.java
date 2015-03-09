package nl.tud;

import nl.tud.client.ClientInterface;
import nl.tud.gameobjects.Field;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Martijn on 09-03-15.
 */
public class ServerProcess extends UnicastRemoteObject implements ServerInterface, Runnable {

    private final int ID, NUM_SERVERS;
    private Field field;
    private Logger logger;
    private Map<Integer, ClientInterface> connectedPlayers;

    public ServerProcess(int id, int num_servers) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.ID = id;
        this.NUM_SERVERS = num_servers;
        this.field = new Field();
        this.logger = Logger.getLogger(ServerProcess.class.getName());
        this.connectedPlayers = new HashMap<Integer, ClientInterface>();

        logger.log(Level.INFO, "Starting server with id " + id);

        java.rmi.Naming.bind("rmi://localhost:" + Main.SERVER_PORT + "/FDDGServer/" + id, this);
    }

    @Override
    public void run() {
        while (true) {
            // TODO implementation here
        }
    }

    public boolean isValidPlayerId(int playerId) {
        return field.isValidPlayerId(playerId);
    }

    @Override
    public void move(int playerId, int direction) throws RemoteException {
        logger.log(Level.INFO, "Server " + this.ID + " received move with direction " + direction + " from player " + playerId);
        if(!isValidPlayerId(playerId)) {
            // TODO send error message
        } else if(direction < 0 || direction > 3) {
            // TODO send error message
        }

        boolean result = field.movePlayer(playerId, direction);
        if(!result) {
            // TODO send error message
        } else {
            connectedPlayers.get(playerId).updateField(field);
        }
    }

    @Override
    public void heal(int playerId, int targetPlayer) throws RemoteException {

    }

    @Override
    public void attack(int playerId, int dragonId) throws RemoteException {

    }

    @Override
    public void connect(int playerId) throws RemoteException {

        logger.log(Level.INFO, "Client with id " + playerId + " connected");

        try {
            ClientInterface ci = (ClientInterface) Naming.lookup("rmi://localhost:" + Main.SERVER_PORT + "/FDDGClient/" + playerId);
            connectedPlayers.put(playerId, ci);
            field.addPlayer(playerId);
            ci.updateField(field);

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void heartBeat(int remoteId) throws RemoteException {

    }

    @Override
    public void pong() throws RemoteException {

    }
}
