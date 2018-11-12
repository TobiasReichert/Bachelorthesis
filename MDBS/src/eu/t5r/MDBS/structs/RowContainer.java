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
package eu.t5r.MDBS.structs;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class hold a complete row of a table.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class RowContainer extends MetaDataDeep implements Serializable {

    HashMap<String, DataContainer> cells = new HashMap<>();

    public RowContainer(String tableName, String uuid, long synctime, long lastSynctime, boolean delete) {
        super(tableName, uuid, synctime, lastSynctime, delete);
    }

    public void addCell(String name, DataContainer dc) {
        cells.put(name, dc);
    }

    public HashMap<String, DataContainer> getCells() {
        return cells;
    }

    @Override
    public String toString() {
        return "IncrementalContainer{" + getString() + '}';
    }

    @Override
    public String getString() {
        return super.getString() + ", cells=" + cells;
    }

}
