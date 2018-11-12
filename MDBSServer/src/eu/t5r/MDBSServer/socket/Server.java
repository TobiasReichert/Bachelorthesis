/*                    
 * Mobile DataBase Synchronisation
 * by Tobias Reichert <mail@teamtobias.de>
 *  ____    ____  ______   ______     ______   
 * |_   \  /   _||_   _ `.|_   _ \  .' ____ \  
 *   |   \/   |    | | `. \ | |_) | | (___ \_| 
 *   | |\  /| |    | |  | | |  __'.  _.____`.  
 *  _| |_\/_| |_  _| |_.' /_| |__) || \____) | 
 * |_____||_____||______.'|_______/  \______.'                              
 */
package eu.t5r.MDBSServer.socket;

import eu.t5r.MDBS.structs.SocketSendable;
import eu.t5r.MDBS.structs.socket.Receivable;
import eu.t5r.MDBS.structs.socket.SocketContainer;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class Server implements SocketSendable {

    private final int port;
    private final Receivable receivable;
    private Logger L;

    private ServerSocket serverSocket;
    private int id = 0;
    public final HashMap<Integer, ServerBody> clients;

    public Server(int port, Receivable receivable, Logger l) {
        this.port = port;
        this.receivable = receivable;
        L = l;

        this.clients = new HashMap<>();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        new ServerListener().start();
        L.log(Level.INFO, "Server listening.");
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void send(int id, SocketContainer socketContainer) throws IOException {
        clients.get(id).send(socketContainer);
    }

    private synchronized int getNextID() {
        return ++id;
    }

    /**
     * The ServerListener listens for new connections and creates a new
     * ServerBody Thread
     */
    private class ServerListener extends Thread {

        @Override
        public void run() {
            try {
                while (!serverSocket.isClosed()) {
                    Integer id = getNextID();
                    ServerBody body = new ServerBody(serverSocket.accept(), id);
                    clients.put(id, body);
                    body.start();
                }

            } catch (EOFException | SocketException ex) {
                ; //NOP
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public class ServerBody extends Thread {

        Socket socket;
        Integer id;
        //Receivable receivable;

        private ObjectInputStream in = null;
        private ObjectOutputStream out = null;
        private String user = "";

        public ServerBody(Socket socket, Integer id) {
            this.socket = socket;
            this.id = id;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getUser() {
            return user;
        }

        @Override
        public void run() {
            L.log(Level.FINE, "New client {0}", socket.getInetAddress());

            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                for (Object o; (o = in.readObject()) != null;) {
                    SocketContainer socketContainer = (SocketContainer) o;
                    receivable.receive(id, socketContainer);
                }
            } catch (EOFException | SocketException ex) {
                ; //NOP
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                //try {
                    //in.close();
                    L.log(Level.FINE, "Lost client {0}", socket.getInetAddress());
//                } catch (IOException ex) {
//                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }

        }

        private void send(SocketContainer socketContainer) throws IOException {
            out.writeObject(socketContainer);
            out.flush();
        }

    }
}
