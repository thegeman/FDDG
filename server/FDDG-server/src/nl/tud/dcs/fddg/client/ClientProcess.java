package nl.tud.dcs.fddg.client;

import nl.tud.dcs.fddg.game.Field;
import nl.tud.dcs.fddg.game.actions.AttackAction;
import nl.tud.dcs.fddg.game.actions.HealAction;
import nl.tud.dcs.fddg.game.actions.MoveAction;
import nl.tud.dcs.fddg.game.entities.Dragon;
import nl.tud.dcs.fddg.game.entities.Player;
import nl.tud.dcs.fddg.server.ServerInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientProcess extends UnicastRemoteObject implements ClientInterface, Runnable {

    private final int ID;
    private Logger logger;
    private ServerInterface server;
    private Field field;

    /**
     * Constructor: initializes the instance variables, the logger and binds the client to its registry
     *
     * @param id The process identifier of this client
     * @throws RemoteException
     */
    public ClientProcess(int id) throws RemoteException {
        super();
        this.ID = id;
        this.logger = Logger.getLogger(ClientProcess.class.getName());

        logger.log(Level.INFO, "Starting client with id " + id);
    }

    @Override
    public synchronized void updateField(Field field) throws RemoteException {
        // logger.log(Level.INFO, "Client " + this.ID + " received field update");
        this.field = field;
    }

    @Override
    public void receiveError(int errorId, String message) throws RemoteException {
        logger.log(Level.SEVERE, "Client " + this.ID + " received error: " + message);
    }

    @Override
    public void ping() throws RemoteException {

    }

    /**
     * Checks whether the player is still alive (hp>0)
     *
     * @return true iff the player is still alive
     */
    public boolean isAlive() {
        return field.getPlayer(this.ID).getCurHitPoints() > 0;
    }

    @Override
    public void run() {

        // send a connect message to the server
        try {
            server = (ServerInterface) Naming.lookup("FDDGServer/0");
            server.connect(this.ID);

            while (isAlive() && !field.gameHasFinished()) {
                Thread.sleep(1000);

                // check if there is a nearby player with hp < 50% to heal
                Dragon dragonToAttack;
                Player playerToHeal = field.isInRangeToHeal(this.ID);
                if (playerToHeal != null) {
                    server.performAction(new HealAction(this.ID, playerToHeal.getUnitId()));
                } else if ((dragonToAttack = field.dragonIsInRangeToAttack(this.ID)) != null) {
                    server.performAction(new AttackAction(this.ID, dragonToAttack.getUnitId()));
                } else {
                    Player p = field.getPlayer(this.ID);
                    int move = field.getDirectionToNearestDragon(p.getxPos(), p.getyPos());
                    if (move == -1) {
                        logger.log(Level.INFO, "Player" + this.ID + " couldn't move towards a dragon (blocked?)");
                        continue;
                    }
                    final int MAX_WIDTH_HEIGHT = Math.max(field.BOARD_HEIGHT, field.BOARD_WIDTH) + 5;
                    int newX = move % MAX_WIDTH_HEIGHT;
                    int newY = move / MAX_WIDTH_HEIGHT;

                    MoveAction moveAction = new MoveAction(this.ID, newX, newY);
                    server.performAction(moveAction);
                }
            }

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
