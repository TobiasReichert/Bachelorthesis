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
package eu.t5r.MDBS.socket;

import eu.t5r.MDBS.structs.SocketSendable;
import eu.t5r.MDBS.structs.socket.Receivable;
import eu.t5r.MDBS.structs.socket.SocketContainer;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a socket client for the MDBS.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class Client implements SocketSendable{

    private String host;
    private int port;
    private Receivable receivable;

    Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ClientBody cb;
    private Logger L;

    public Client(String host, int port, Receivable receivable, Logger L) {
        this.host = host;
        this.port = port;
        this.receivable = receivable;
        this.L = L;
    }

    public Client() {

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Receivable getReceivable() {
        return receivable;
    }

    public void setReceivable(Receivable receivable) {
        this.receivable = receivable;
    }

    public void connect() throws Exception {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());

        cb = new ClientBody(socket.getInputStream(), this);
        cb.start();
    }

    public void disconnect() throws Exception {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (socket != null) {
            socket.close();
        }
    }

    public boolean isConnected() {
        if (socket == null) {
            return false;
        }
        return socket.isConnected();
    }

    public void send(SocketContainer s) throws IOException {
        L.log(Level.FINE, "send: {0}", s);
        
        out.writeObject(s);
        out.flush();

    }

    @Override
    public void send(int id, SocketContainer socketContainer) throws IOException {
        send(socketContainer);
    }

    public class ClientBody extends Thread {

        private final InputStream i;
        Client client;

        public ClientBody(InputStream i, Client client) {
            this.i = i;
            this.client = client;
        }

        @Override
        public void run() {
            ObjectInputStream in;
            try {
                in = new ObjectInputStream(i);

                for (Object so; (so = in.readObject()) != null;) {
                    SocketContainer tso = (SocketContainer) so;
                    receivable.receive(0, tso);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (!client.socket.isClosed()) {
                    // closed(); Closeable?
                }

            }

        }
    }

}
