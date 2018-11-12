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

/**
 * This class hold extendet metadata.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class MetaDataDeep extends MetaData implements Serializable {

    long lastsynctime;  // not using Timestamp for freedom
    boolean deleted;     // not used

    public MetaDataDeep(String tableName, String uuid, long synctime, long lastsynctime, boolean delete) {
        super(tableName, uuid, synctime);
        this.lastsynctime = lastsynctime;
        this.deleted = delete;
    }

    public long getLastsynctime() {
        return lastsynctime;
    }

    public void setLastsynctime(long lastsynctime) {
        this.lastsynctime = lastsynctime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MetaDataDeep)) {
            return false;
        }
        final MetaDataDeep other = (MetaDataDeep) obj;
        if (this.lastsynctime != other.lastsynctime) {
            return false;
        }
        if (this.deleted != other.deleted) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "MetaDataDeep{" + getString() + '}';
    }

    @Override
    public String getString() {
        return super.getString() + ", lastsynctime=" + lastsynctime + ", delete=" + deleted;
    }

}
