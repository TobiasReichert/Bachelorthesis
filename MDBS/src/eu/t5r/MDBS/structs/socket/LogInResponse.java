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

import java.io.Serializable;

/**
 * This class is a Container to send over a socket, it has the acknowledgement 
 * for the Login.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class LogInResponse implements Serializable, Sendable {

    Boolean ok;

    public LogInResponse(Boolean ok) {
        this.ok = ok;
    }

    public Boolean getOk() {
        return ok;
    }

    @Override
    public String toString() {
        return "LogInResponse{" + "ok=" + ok + '}';
    }

}
