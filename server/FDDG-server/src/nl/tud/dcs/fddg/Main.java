package nl.tud.dcs.fddg;

import nl.tud.dcs.fddg.client.ClientProcess;
import nl.tud.dcs.fddg.server.ServerProcess;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

public class Main {

    public static final int NUM_SERVERS = 1;
    public static final int NUM_CLIENTS = 100;
    public static final int SERVER_PORT = 6447;

    /**
     * The main function of the simulation.
     * This function starts {@link Main#NUM_SERVERS} servers and {@link Main#NUM_CLIENTS} processes.
     *
     * @param args Required variable, not used in this simulation.
     */
    public static void main(String[] args) {
        ArrayList<ServerProcess> serverProcesses = new ArrayList<ServerProcess>(NUM_SERVERS);
        ArrayList<ClientProcess> clientProcesses = new ArrayList<ClientProcess>(NUM_CLIENTS);

        Random random = new Random();

        try {
            java.rmi.registry.LocateRegistry.createRegistry(SERVER_PORT);

            // create the server processes
            for(int i=0; i<NUM_SERVERS; i++) {
                    ServerProcess process = new ServerProcess(i, NUM_SERVERS);
                    serverProcesses.add(process);
                    new Thread(process).start();
            }

            // create a client after a small delay
            Thread.sleep(2000);
            for(int i = 0; i < NUM_CLIENTS; i++) {
                Thread.sleep(random.nextInt(290) + 10);
                ClientProcess client = new ClientProcess(random.nextInt(Integer.MAX_VALUE));
                clientProcesses.add(client);
                new Thread(client).start();
            }


        } catch (RemoteException e){
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
