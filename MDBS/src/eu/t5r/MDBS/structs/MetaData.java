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
import java.util.Objects;

/**
 * This class holds the simple metadata.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class MetaData implements Serializable {

    String tableName;
    String uuid;    //not using UUID for freedom
    long synctime;  //not using Timestamp for freedom

    public MetaData(String tableName, String uuid, long synctime) {
        this.tableName = tableName;
        this.uuid = uuid;
        this.synctime = synctime;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getSynctime() {
        return synctime;
    }

    public void setSynctime(long synctime) {
        this.synctime = synctime;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.tableName);
        hash = 67 * hash + Objects.hashCode(this.uuid);
        hash = 67 * hash + (int) (this.synctime ^ (this.synctime >>> 32));
        return hash;
    }

    public boolean equalsUUID(MetaData other) {
        return Objects.equals(this.uuid, other.uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MetaData)) {
            return false;
        }
        final MetaData other = (MetaData) obj;
        if (this.synctime != other.synctime) {
            return false;
        }
        if (!Objects.equals(this.tableName, other.tableName)) {
            return false;
        }
        return Objects.equals(this.uuid, other.uuid);
    }

    @Override
    public String toString() {
        return "MetaData{" + getString() + '}';
    }

    public String getString() {
        return "tableName=" + tableName + ", uuid=" + uuid + ", synctime=" + synctime;
    }

}
