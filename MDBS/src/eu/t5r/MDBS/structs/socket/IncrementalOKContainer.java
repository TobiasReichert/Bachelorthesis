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

import eu.t5r.MDBS.structs.TableCommits;
import java.io.Serializable;

/**
 * This class is a Container to send over a socket, it has the acknowledgement 
 * for the IncrementalSync.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class IncrementalOKContainer implements Serializable, Sendable {

    TableCommits newCommits;
    Boolean pass; // Passing onto other devices on the server

    public Boolean getPass() {
        return pass;
    }

    public IncrementalOKContainer(TableCommits newCommits, Boolean pass) {
        this.newCommits = newCommits;
        this.pass = pass;
    }

    public TableCommits getNewCommits() {
        return newCommits;
    }

    @Override
    public String toString() {
        return "IncrementalOKContainer{" + "newCommits=" + newCommits + '}';
    }

}
