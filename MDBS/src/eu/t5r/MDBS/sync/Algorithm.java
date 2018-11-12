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

import eu.t5r.MDBS.structs.GenDiffContainer;
import eu.t5r.MDBS.structs.types.DiffTypes;
import eu.t5r.MDBS.structs.types.DirectionTypes;
import eu.t5r.MDBS.structs.MDBSSyncException;
import eu.t5r.MDBS.structs.MetaData;
import eu.t5r.MDBS.structs.MetaDataDeep;
import eu.t5r.MDBS.structs.RowContainer;
import eu.t5r.MDBS.structs.socket.FullSyncB;
import java.util.ArrayList;
import java.util.List;

/**
 * This class hold the central synchronisation algorithm.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class Algorithm {

    /**
     * Checks the diff from a to b. Used to make a incremental sync
     *
     * @param a
     * @param b
     * @return
     */
    public static List<GenDiffContainer<RowContainer>> checkDiffOneWay(List<MetaData> a, List<MetaData> b) {
        List<GenDiffContainer<RowContainer>> result = new ArrayList<>();

        meta_for:
        for (MetaData aa : a) {
            for (MetaData bb : b) {
                if (aa.equalsUUID(bb)) {
                    if (aa.getSynctime() > bb.getSynctime()) {
                        // found update
                        /// uuid, new time from a, old synctime from b
                        result.add(new GenDiffContainer(DiffTypes.UPDATE, DirectionTypes.SERVER_TO_CLIENT, new RowContainer(aa.getTableName(), aa.getUuid(), aa.getSynctime(), bb.getSynctime(), false)));
                    }
                    continue meta_for;
                }
            }

            // found insert
            result.add(new GenDiffContainer(DiffTypes.INSERT, DirectionTypes.SERVER_TO_CLIENT, new RowContainer(aa.getTableName(), aa.getUuid(), aa.getSynctime(), 0, false)));

        }

        meta_del_for:
        for (MetaData bb : b) {
            for (MetaData aa : a) {
                if (bb.equalsUUID(aa)) {
                    continue meta_del_for;
                }
            }
            // found delete
            result.add(new GenDiffContainer(DiffTypes.DELETE, DirectionTypes.SERVER_TO_CLIENT, new RowContainer(bb.getTableName(), bb.getUuid(), 0, 0, true)));
        }

        return result;
    }

    /**
     * Checks the diff both way. Used to make incremental sync.
     *
     * @param me
     * @param you
     * @return
     * @throws eu.t5r.MDBS.structs.MDBSSyncException
     */
    public static FullSyncB checkDiffBothWay(List<MetaDataDeep> me, List<MetaDataDeep> you, boolean pass) throws MDBSSyncException {
        List<GenDiffContainer<RowContainer>> diffContainers = new ArrayList<>();
        List<GenDiffContainer<MetaDataDeep>> request = new ArrayList<>();
        FullSyncB result = new FullSyncB(diffContainers, request, pass);

        meta_for:
        for (MetaDataDeep my : me) {
            for (MetaDataDeep your : you) {
                if (my.equalsUUID(your)) {
                    if (my.isDeleted() && !(your.isDeleted())) {
                        // found delete from me to you
                        diffContainers.add(new GenDiffContainer(DiffTypes.DELETE, DirectionTypes.SERVER_TO_CLIENT, new RowContainer(my.getTableName(), my.getUuid(), 0, 0, my.isDeleted())));

                    } else if (!(my.isDeleted()) && your.isDeleted()) {
                        // found delete from you to me
                        request.add(new GenDiffContainer(DiffTypes.DELETE, DirectionTypes.SERVER_TO_CLIENT, new MetaDataDeep(your.getTableName(), your.getUuid(), 0, 0, your.isDeleted())));

                    } else if (my.getSynctime() > your.getSynctime()) {
                        // found update from me to you
                        if (my.getLastsynctime() == your.getSynctime()) {
                            diffContainers.add(new GenDiffContainer(DiffTypes.UPDATE, DirectionTypes.SERVER_TO_CLIENT, new RowContainer(my.getTableName(), my.getUuid(), my.getSynctime(), my.getLastsynctime(), my.isDeleted())));
                        } else {
                            throw new MDBSSyncException();
                        }

                    } else if (my.getSynctime() < your.getSynctime()) {
                        // found update from you to me
                        if (your.getLastsynctime() == my.getSynctime()) {
                            request.add(new GenDiffContainer(DiffTypes.UPDATE, DirectionTypes.SERVER_TO_CLIENT, new MetaDataDeep(your.getTableName(), your.getUuid(), your.getSynctime(), your.getLastsynctime(), your.isDeleted())));
                        } else {
                            throw new MDBSSyncException();
                        }
                    }
                    continue meta_for;
                }
            }
            // found insert from me to you
            diffContainers.add(new GenDiffContainer(DiffTypes.INSERT, DirectionTypes.SERVER_TO_CLIENT, new RowContainer(my.getTableName(), my.getUuid(), my.getSynctime(), my.getLastsynctime(), my.isDeleted())));
        }

        meta_del_for:
        for (MetaDataDeep your : you) {
            for (MetaDataDeep my : me) {
                if (your.equalsUUID(my)) {
                    continue meta_del_for;
                }
            }
            // found insert from you to me
            request.add(new GenDiffContainer(DiffTypes.INSERT, DirectionTypes.SERVER_TO_CLIENT, new MetaDataDeep(your.getTableName(), your.getUuid(), your.getSynctime(), your.getLastsynctime(), your.isDeleted())));
        }

        return result;

    }
}
