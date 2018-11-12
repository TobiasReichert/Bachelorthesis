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
 * This class is a empty exception for the MDBS.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class MDBSSyncException extends Exception implements Serializable{

    /**
     * Creates a new instance of <code>MDBSSyncException</code> without detail
     * message.
     */
    public MDBSSyncException() {
    }

    /**
     * Constructs an instance of <code>MDBSSyncException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public MDBSSyncException(String msg) {
        super(msg);
    }
}
