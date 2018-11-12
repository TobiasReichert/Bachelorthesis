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
 * This class is a Container to send data over a socket.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class SocketContainer implements Serializable {

    SocketContainerTypes type;
    Sendable content;

    public SocketContainer(SocketContainerTypes type, Sendable content) {
        this.type = type;
        this.content = content;
    }

    public SocketContainerTypes getType() {
        return type;
    }

    public Sendable getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "SocketContainer{" + "type=" + type + ", content=" + content + '}';
    }

}
