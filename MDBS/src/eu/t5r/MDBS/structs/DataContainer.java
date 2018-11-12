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

import eu.t5r.MDBS.structs.types.MDBSTypes;
import java.io.Serializable;

/**
 * This class can hold a data cell.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class DataContainer implements Serializable{

    MDBSTypes type;
    Object data;

    public DataContainer(MDBSTypes type, Object data) {
        this.type = type;
        this.data = data;
    }

    public MDBSTypes getType() {
        return type;
    }

    public void setType(MDBSTypes type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataContainer{" + "type=" + type + ", data=" + data + '}';
    }

}
