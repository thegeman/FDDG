package nl.tud.client;

import nl.tud.Main;
import nl.tud.ServerInterface;
import nl.tud.gameobjects.Field;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Martijn on 09-03-15.
 */
public class ClientProcess extends UnicastRemoteObject implements ClientInterface, Runnable {

    private final int ID;
    private Logger logger;
    private ServerInterface server;
    private Field field;

    public ClientProcess(int id) throws RemoteException, AlreadyBoundException, MalformedURLException {
        this.ID = id;
        this.logger = Logger.getLogger(ClientProcess.class.getName());

        logger.log(Level.INFO, "Starting client with id " + id);

        java.rmi.Naming.bind("rmi://localhost:" + Main.SERVER_PORT + "/FDDGClient/" + id, this);
    }

    @Override
    public synchronized void updateField(Field field) throws RemoteException {
        logger.log(Level.INFO, "Client " + this.ID + " received field update");

        this.field = field;
    }

    @Override
    public void receiveError(int errorId, String message) throws RemoteException {
        logger.log(Level.SEVERE, "Client " + this.ID + " received error: " + message);
    }

    @Override
    public void ping() throws RemoteException {

    }

    @Override
    public void run() {

        Random random = new Random();

        // send a connect message to the server
        try {
            server = (ServerInterface) Naming.lookup("rmi://localhost:" + Main.SERVER_PORT + "/FDDGServer/0");
            server.connect(this.ID);

            while(true) {
                Thread.sleep(1000);
                server.move(this.ID, random.nextInt(4));
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
