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
package eu.t5r.MDBS.sync;

import eu.t5r.MDBS.sql.DBInterfaceMDBS;
import eu.t5r.MDBS.structs.GenDiffContainer;
import eu.t5r.MDBS.structs.MDBSSyncException;
import eu.t5r.MDBS.structs.MetaDataDeep;
import eu.t5r.MDBS.structs.RowContainer;
import eu.t5r.MDBS.structs.SocketSendable;
import eu.t5r.MDBS.structs.SyncListener;
import eu.t5r.MDBS.structs.TableCommits;
import eu.t5r.MDBS.structs.socket.FullSyncA;
import eu.t5r.MDBS.structs.socket.FullSyncB;
import eu.t5r.MDBS.structs.socket.FullSyncC;
import eu.t5r.MDBS.structs.socket.IncrementalContainer;
import eu.t5r.MDBS.structs.socket.IncrementalOKContainer;
import eu.t5r.MDBS.structs.socket.SocketContainer;
import eu.t5r.MDBS.structs.socket.SocketContainerTypes;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class hold methods around the sync for the server and the client
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class SocketMethods {

    public static TableCommits makeNewCommits(DBInterfaceMDBS sql) throws SQLException {
        TableCommits commits = sql.getCommits();
        for (Map.Entry<String, Long> commit : commits.getCommits().entrySet()) {
            commit.setValue(TableCommits.getNewCommit());
        }
        return commits;
    }

    public static void executeChanges(DBInterfaceMDBS sql, List<GenDiffContainer<RowContainer>> changes) throws SQLException {
        final Map<String, Integer> priority = sql.getPriority();
        // Sorting
        Collections.sort(changes, new Comparator<GenDiffContainer<RowContainer>>() {
            @Override
            public int compare(GenDiffContainer<RowContainer> diff1, GenDiffContainer<RowContainer> diff2) {
                Integer i1 = priority.get(diff1.getContent().getTableName());
                Integer i2 = priority.get(diff2.getContent().getTableName());

                return i1.compareTo(i2);
            }
        });
        
        SQLException eResult = null;
        // Execute changes
        for (GenDiffContainer<RowContainer> diff : changes) {
            System.out.println("executeChanges: " + diff);
            try {
                sql.executeDiffContainerData(diff);
            } catch (SQLException e) {
                eResult = e;
            }
        }
        // Throw one error
        if (eResult != null) {
            throw eResult;
        }
    }

    public static void sendFullA(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int to, boolean pass) {
        try {
            List<MetaDataDeep> meta = sql.getAllMetaDeep();
            FullSyncA fullSyncA = new FullSyncA(meta, pass);
            socket.send(to, new SocketContainer(SocketContainerTypes.FULL_A, fullSyncA));

        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (IOException ex) {
            Logger.getLogger(SocketMethods.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void receiveFullA(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int from, FullSyncA container) {

        try {
            FullSyncB result = Algorithm.checkDiffBothWay(sql.getAllMetaDeep(), container.getMeta(), container.getPass());
            for (GenDiffContainer<RowContainer> diff : result.getDiffContainers()) {
                sql.getData(diff.getContent());
            }

            result.setNewCommits(makeNewCommits(sql));

            sendFullB(sl, sql, socket, from, result);

        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MDBSSyncException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void sendFullB(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int from, FullSyncB container) {
        try {
            socket.send(from, new SocketContainer(SocketContainerTypes.FULL_B, container));
        } catch (IOException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void receiveFullB(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int from, FullSyncB container) {
        // Get requested data
        List<GenDiffContainer<RowContainer>> response = new ArrayList<>();
        for (GenDiffContainer<MetaDataDeep> s : container.getRequest()) {
            RowContainer temp = new RowContainer(
                    s.getContent().getTableName(),
                    s.getContent().getUuid(),
                    s.getContent().getSynctime(),
                    s.getContent().getLastsynctime(),
                    s.getContent().isDeleted());

            try {
                sql.getData(temp);
            } catch (SQLException ex) {
                Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
            }
            response.add(new GenDiffContainer<>(s.getType(), s.getDirection(), temp));
        }
        // Execute changes
        try {
            executeChanges(sql, container.getDiffContainers());
        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Write commits
        try {
            sql.setCommits(container.getNewCommits());
        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }

        FullSyncC result = new FullSyncC(response, container.getPass());
        result.setNewCommits(container.getNewCommits());

        sendFullC(sl, sql, socket, from, result);
    }

    public static void sendFullC(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int from, FullSyncC container) {
        try {
            socket.send(from, new SocketContainer(SocketContainerTypes.FULL_C, container));
        } catch (IOException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        sl.syncPerformed(from, container.getPass());
    }

    public static void receiveFullC(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int from, FullSyncC container) {
        // Execute changes
        System.out.println(container.getDiffContainers());
        try {
            executeChanges(sql, container.getDiffContainers());
        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Write commits
        try {
            sql.setCommits(container.getNewCommits());
        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        sl.syncPerformed(from, container.getPass());
    }

    public static void sendIncremental(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int to, IncrementalContainer ic) {

        SocketContainer sc = new SocketContainer(SocketContainerTypes.INCREMENTAL, ic);
        try {
            socket.send(to, sc);
        } catch (IOException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void receiveIncremental(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int from, IncrementalContainer container) {
        try {
            // get server commits
            TableCommits serverCommits = sql.getCommits();

            // Check if all commits are greater zero
            for (Map.Entry<String, Long> entry : serverCommits.getCommits().entrySet()) {
                if (entry.getValue() == 0L) {
                    sendFullA(sl, sql, socket, from, container.getPass());
                    return;
                }
            }
            // Check if all commits are greater zero
            for (Map.Entry<String, Long> entry : container.getCommits().getCommits().entrySet()) {
                if (entry.getValue() == 0L) {
                    sendFullA(sl, sql, socket, from, container.getPass());
                    return;
                }
            }
            // Check if all commits are equals
            if (!serverCommits.equals(container.getCommits())) {
                //Commits not equals -> fullA
                sendFullA(sl, sql, socket, from, container.getPass());
                return;
            }

            // Execute changes
            try {
                executeChanges(sql, container.getDiffContainers());
            } catch (SQLException ex) {
                Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
            }

            // send OK
            sendIncrementalOK(sl, sql, socket, from, container.getPass());

        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void sendIncrementalOK(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int to, boolean pass) {
        try {
            TableCommits commits = makeNewCommits(sql);
            sql.setCommits(commits);

            IncrementalOKContainer result = new IncrementalOKContainer(commits, pass);
            socket.send(to, new SocketContainer(SocketContainerTypes.INCREMENTAL_OK, result));
        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        sl.syncPerformed(to, pass);
    }

    public static void receiveIncrementalOK(SyncListener sl, DBInterfaceMDBS sql, SocketSendable socket, int from, IncrementalOKContainer container) {
        try {
            sql.setCommits(container.getNewCommits());
        } catch (SQLException ex) {
            Logger.getLogger(SocketMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        sl.syncPerformed(from, container.getPass());

    }

}
