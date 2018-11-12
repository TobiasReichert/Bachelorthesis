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
package eu.t5r.MDBS.structs.socket;

import eu.t5r.MDBS.structs.MetaDataDeep;
import java.io.Serializable;
import java.util.List;

/**
 * This class is a Container to send over a socket, it has the data 
 * for the first FullSync part.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class FullSyncA implements Serializable, Sendable {

    List<MetaDataDeep> meta;
    Boolean pass; // Passing onto other devices on the server

    public Boolean getPass() {
        return pass;
    }

    public FullSyncA(List<MetaDataDeep> meta, Boolean pass) {
        this.meta = meta;
        this.pass = pass;
    }

    public List<MetaDataDeep> getMeta() {
        return meta;
    }

    @Override
    public String toString() {
        return "FullSyncA{" + "meta=" + meta + '}';
    }

}
