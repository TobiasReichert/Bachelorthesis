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
package eu.t5r.MDBS;

import eu.t5r.Logger.LogFormatter;
import eu.t5r.Logger.LogHandler;
import eu.t5r.MDBS.socket.Client;
import eu.t5r.MDBS.sql.DBInterfaceMDBS;
import eu.t5r.MDBS.structs.GenDiffContainer;
import eu.t5r.MDBS.structs.MetaData;
import eu.t5r.MDBS.structs.socket.Receivable;
import eu.t5r.MDBS.structs.socket.SocketContainer;
import eu.t5r.MDBS.structs.RowContainer;
import eu.t5r.MDBS.structs.SyncListener;
import eu.t5r.MDBS.structs.TableCommits;
import eu.t5r.MDBS.structs.socket.FullSyncA;
import eu.t5r.MDBS.structs.socket.FullSyncB;
import eu.t5r.MDBS.structs.socket.FullSyncC;
import eu.t5r.MDBS.structs.socket.IncrementalContainer;
import eu.t5r.MDBS.structs.socket.IncrementalOKContainer;
import eu.t5r.MDBS.structs.socket.LogIn;
import eu.t5r.MDBS.structs.socket.LogInResponse;
import eu.t5r.MDBS.structs.socket.SocketContainerTypes;
import eu.t5r.MDBS.sync.Algorithm;
import eu.t5r.MDBS.sync.SocketMethods;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the main class for the MDBS client.
 *
 * <p>
 * This file is part of the MDBS
 *
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class MDBSConnection extends SocketMethods implements Syncable, Receivable {

    private final int random = (int) (Math.random() * 9);
    private final Logger L = Logger.getLogger("Logger" + random);

    boolean doSync = true;
    boolean connect = true;
    boolean useIncremental = true;
    boolean logedIn = false;
    String host;
    int port = 3425;

    Client socket = null;
    public DBInterfaceMDBS db; //public for test

    List<SyncListener> syncListener = new ArrayList<>();

    /**
     * This constructor will save the DBInteface and create the internal tables.
     *
     * @param db The DB interface
     * @throws SQLException can throw a SQLException when creating the tables
     */
    public MDBSConnection(DBInterfaceMDBS db) throws SQLException {
        this.db = db;

        // LOGGER
        Formatter formatter = new LogFormatter("C" + (int) (Math.random() * 9));
        Handler handler = new LogHandler();
        handler.setFormatter(formatter);
        L.addHandler(handler);
        L.setLevel(Level.ALL);
        L.setUseParentHandlers(false);

        db.checkDB();
    }

    SyncListener sl = new SyncListener() {
        @Override
        public void syncPerformed(int from, boolean pass) {
            L.log(Level.FINE, "<--> client syncPerformed");

            for (SyncListener listen : syncListener) {
                listen.syncPerformed(from, pass);
            }
        }
    };

    public void addSyncListener(SyncListener listen) {
        syncListener.add(listen);
    }

    public void removeSyncListener(SyncListener listen) {
        syncListener.remove(listen);
    }

    public boolean getLogedIn() {
        return logedIn;
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

    /**
     * This function will try to connect to the Server. Make sure you have
     * specified a host and port.
     *
     * @throws Exception
     */
    public void connect() throws Exception {
        if (socket == null) {
            socket = new Client(host, port, this, L);
        }
        socket.connect();
    }

    /**
     * This function will try to log in on the server with the user und password
     *
     * @param user
     * @param pwd
     */
    public void logIn(String user, String pwd) {
        try {
            socket.send(new SocketContainer(SocketContainerTypes.LOG_IN, new LogIn(user, pwd)));
        } catch (IOException ex) {
            L.log(Level.SEVERE, null, ex);
        }
    }

    public boolean isDoSync() {
        return doSync;
    }

    /**
     * With this function you can determine if the MDBS client should
     * synchronize with the Server.
     *
     * @param doSync
     */
    public void setDoSync(boolean doSync) {
        this.doSync = doSync;
    }

    /**
     * With this function you can determine if the MDBS client should use
     * incremental synchronization.
     *
     * @param use
     */
    public void useIncremental(boolean use) {
        this.useIncremental = use;
    }

    /**
     * Adds a Table to the syncable tabels
     *
     * @param tableName The name of the table that is going to be added
     * @param priority The priority sets the order in which the changes of the
     * tables will be executed. The lower values will be executed first.
     * @throws SQLException
     */
    public void addSyncTable(String tableName, int priority) throws SQLException {
        db.checkDB();
        db.addSyncTable(tableName, priority);
    }

    /**
     * With this this function you can trigger the synchronization.
     * You should call it every time you have made a complet change on the data.
     * @throws SQLException 
     */
    @Override
    public void sync() throws SQLException {
        L.log(Level.INFO, "sync");

        if (doSync) {
            // get the last commits before checkDiff
            TableCommits commits = db.getCommits();
            //check diff between data and meta table
            List<GenDiffContainer<RowContainer>> l = checkDiff();

            if (socket != null && socket.isConnected()) { //TODO new Client()
                if (useIncremental) {
                    IncrementalContainer ic = new IncrementalContainer(l, commits, true);
                    sendIncremental(sl, db, socket, 0, ic);
                } else {
                    sendFullA(sl, db, socket, 0, true);
                }

            }

        }

    }

    /**
     * This Method checks the difference between the data tables and the internal
     * tables.
     * @return List of DiffContainer or null if no tables to sync
     * @throws SQLException
     */
    public List<GenDiffContainer<RowContainer>> checkDiff() throws SQLException {
        List<GenDiffContainer<RowContainer>> result = new ArrayList<>();
        TableCommits commits = db.getCommits();

        // For all syncableTables
        for (Map.Entry<String, Long> commit : commits.getCommits().entrySet()) {
            String syncableTable = commit.getKey();

            // Get meta from datatable
            List<MetaData> metaA = db.getMetaFromDataTable(syncableTable);
            // Get metaDeep from meta table 
            List<MetaData> metaB = db.getMetaFromMetaTable(syncableTable);

            //checkDiffOneWay
            List<GenDiffContainer<RowContainer>> partResult = Algorithm.checkDiffOneWay(metaA, metaB);

            // only if changes found
            if (partResult.size() > 0) {

                // enrich meta with table date
                for (GenDiffContainer<RowContainer> diff : partResult) {
                    db.getData(diff.getContent());
                }

                // write diff to meta
                for (GenDiffContainer<RowContainer> diff : partResult) {
                    db.executeDiffContainerMeta(diff);
                }

                // write new commits
                commit.setValue(TableCommits.getNewCommit());

                result.addAll(partResult);
            }

        }

        db.setCommits(commits);

        // return diff
        L.log(Level.FINER, "checkDiff(): {0}", result);
        return result;
    }

    public void close() throws SQLException {
        if (socket != null) {
            try {
                socket.disconnect();
            } catch (Exception ex) {
                Logger.getLogger(MDBSConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void receive(int from, SocketContainer socketContainer) {
        L.log(Level.FINER, "{0}: {1}", new Object[]{from, socketContainer});

        switch (socketContainer.getType()) {
            case INCREMENTAL:
                receiveIncremental(sl, db, socket, from, (IncrementalContainer) socketContainer.getContent());
                break;
            case INCREMENTAL_OK:
                receiveIncrementalOK(sl, db, socket, from, (IncrementalOKContainer) socketContainer.getContent());
                break;
            case FULL_A:
                receiveFullA(sl, db, socket, from, (FullSyncA) socketContainer.getContent());
                break;
            case FULL_B:
                receiveFullB(sl, db, socket, from, (FullSyncB) socketContainer.getContent());
                break;
            case FULL_C:
                receiveFullC(sl, db, socket, from, (FullSyncC) socketContainer.getContent());
                break;
            case LOG_IN_RESPONSE:
                logedIn = ((LogInResponse) socketContainer.getContent()).getOk();
                break;

        }
    }

}
