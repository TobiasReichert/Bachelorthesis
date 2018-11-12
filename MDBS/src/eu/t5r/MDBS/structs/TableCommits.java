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
import java.util.Map;
import java.util.Objects;

/**
 * This class hold infomration about the last commits from the syncable tables.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class TableCommits implements Serializable {

    Map<String, Long> commits;

    public TableCommits(Map<String, Long> commits) {
        this.commits = commits;
    }

    public Map<String, Long> getCommits() {
        return commits;
    }

    public static Long getNewCommit(){
        return System.currentTimeMillis();
    }
    @Override
    public String toString() {
        return "TableCommits{" + "commits=" + commits + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TableCommits other = (TableCommits) obj;
        if (!Objects.equals(this.commits, other.commits)) {
            return false;
        }
        return true;
    }
    
    

}
