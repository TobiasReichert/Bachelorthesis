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
package eu.t5r.MDBSServer;

import eu.t5r.Logger.LogFormatter;
import eu.t5r.Logger.LogHandler;
import eu.t5r.MDBS.sql.DBInterfaceMDBS;
import eu.t5r.MDBS.structs.SyncListener;
import eu.t5r.MDBS.structs.socket.FullSyncA;
import eu.t5r.MDBS.structs.socket.FullSyncB;
import eu.t5r.MDBS.structs.socket.FullSyncC;
import eu.t5r.MDBS.structs.socket.IncrementalContainer;
import eu.t5r.MDBS.structs.socket.IncrementalOKContainer;
import eu.t5r.MDBS.structs.socket.LogIn;
import eu.t5r.MDBS.structs.socket.LogInResponse;
import eu.t5r.MDBS.structs.socket.Receivable;
import eu.t5r.MDBS.structs.socket.SocketContainer;
import eu.t5r.MDBS.structs.socket.SocketContainerTypes;
import eu.t5r.MDBS.sync.SocketMethods;
import static eu.t5r.MDBS.sync.SocketMethods.sendFullA;
import eu.t5r.MDBSServer.socket.Server;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the main MDBSServer file.
 *
 * <p>
 * This file is part of the MDBS
 *
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class MDBSServer extends SocketMethods implements Receivable {

    private final int random = (int) (Math.random() * 9);
    private final Logger L = Logger.getLogger("Logger" + random);

    int port;

    Server socket;
    public DBInterfaceMDBS sql;

    SyncListener sl = new SyncListener() {
        @Override
        public void syncPerformed(int from, boolean pass) {
            if (pass) {
                L.log(Level.INFO, "<--> server syncPerformed");
                for (Map.Entry<Integer, Server.ServerBody> entry : socket.clients.entrySet()) {
                    int id = entry.getKey();
                    if (id != from) {
                        sendFullA(null, sql, socket, entry.getKey(), false);
                    }
                }
            }
        }
    };

//    SyncListener sl = (from, pass) -> {
//
//    };

    public MDBSServer(int port, DBInterfaceMDBS sql) throws SQLException {
        this.port = port;
        this.sql = sql;

        // LOGGER
        Formatter formatter = new LogFormatter("S" + (int) (Math.random() * 9));
        Handler handler = new LogHandler();
        handler.setFormatter(formatter);
        L.addHandler(handler);
        L.setLevel(Level.ALL);
        L.setUseParentHandlers(false);

        socket = new Server(port, this, L);

        sql.checkDB();

        L.log(Level.INFO, "Server started.");
    }

    public MDBSServer(DBInterfaceMDBS sql) throws SQLException {
        this(3425, sql);
    }

    public void addSyncTable(String tableName, int priority) throws SQLException {
        sql.addSyncTable(tableName, priority);
        sql.checkDB();
    }

    public void start() throws IOException {
        socket.start();
    }

    public void stop() throws IOException {
        socket.stop();
    }

    void receiveLogIn(int from, Server.ServerBody sb, LogIn logIn) {
        L.log(Level.INFO, "receiveLogIn");
        try {
            if (sql.checkLogIn(logIn.getUser(), logIn.getPwd())) {
                sb.setUser(logIn.getUser());
                socket.send(from, new SocketContainer(SocketContainerTypes.LOG_IN_RESPONSE, new LogInResponse(true)));
            } else {
                socket.send(from, new SocketContainer(SocketContainerTypes.LOG_IN_RESPONSE, new LogInResponse(false)));
            }
        } catch (SQLException | IOException ex) {
            L.log(Level.SEVERE, ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }

    @Override
    public void receive(int from, SocketContainer socketContainer) {
        L.log(Level.FINE, "{0}: {1}", new Object[]{from, socketContainer});

        Server.ServerBody sb = socket.clients.get(from);

        // if device is loged in
        if (sb.getUser().length() == 0) {
            if (socketContainer.getType() == SocketContainerTypes.LOG_IN) {
                receiveLogIn(from, sb, (LogIn) socketContainer.getContent());
            }
        } else {
            switch (socketContainer.getType()) {
                case INCREMENTAL:
                    receiveIncremental(sl, sql, socket, from, (IncrementalContainer) socketContainer.getContent());
                    break;
                case INCREMENTAL_OK:
                    receiveIncrementalOK(sl, sql, socket, from, (IncrementalOKContainer) socketContainer.getContent());
                    break;
                case FULL_A:
                    receiveFullA(sl, sql, socket, from, (FullSyncA) socketContainer.getContent());
                    break;
                case FULL_B:
                    receiveFullB(sl, sql, socket, from, (FullSyncB) socketContainer.getContent());
                    break;
                case FULL_C:
                    receiveFullC(sl, sql, socket, from, (FullSyncC) socketContainer.getContent());
                    break;

            }
        }
    }

}
