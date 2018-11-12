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
 * This class is a Container to send over a socket, it has the data 
 * for the Login.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class LogIn implements Serializable, Sendable {

    String user;
    String pwd;

    public LogIn(String user, String pwd) {
        this.user = user;
        this.pwd = pwd;
    }

    public String getUser() {
        return user;
    }

    public String getPwd() {
        return pwd;
    }

    @Override
    public String toString() {
        return "LogIn{" + "user=" + user + ", pwd=" + pwd + '}';
    }

}
