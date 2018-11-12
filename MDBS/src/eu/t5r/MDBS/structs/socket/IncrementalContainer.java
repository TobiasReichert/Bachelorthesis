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

import eu.t5r.MDBS.structs.GenDiffContainer;
import eu.t5r.MDBS.structs.RowContainer;
import eu.t5r.MDBS.structs.TableCommits;
import java.io.Serializable;
import java.util.List;

/**
 * This class is a Container to send over a socket, it has the data 
 * for the IncrementalSync.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class IncrementalContainer implements Serializable, Sendable {

    List<GenDiffContainer<RowContainer>> diffContainers;
    TableCommits commits;
        Boolean pass; // Passing onto other devices on the server

    public Boolean getPass() {
        return pass;
    }

    public IncrementalContainer(List<GenDiffContainer<RowContainer>> diffContainers, TableCommits commits, Boolean pass) {
        this.diffContainers = diffContainers;
        this.commits = commits;
        this.pass = pass;
    }

    public List<GenDiffContainer<RowContainer>> getDiffContainers() {
        return diffContainers;
    }

    public TableCommits getCommits() {
        return commits;
    }

    @Override
    public String toString() {
        return "IncrementalContainer{" + "diffContainers=" + diffContainers + ", commits=" + commits + '}';
    }

}
