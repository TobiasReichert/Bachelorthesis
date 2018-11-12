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

import eu.t5r.MDBS.structs.types.DiffTypes;
import eu.t5r.MDBS.structs.types.DirectionTypes;
import java.io.Serializable;
import java.util.Objects;

/**
 * An Instance of this class can hold a change on a database.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class GenDiffContainer<A> implements Serializable {

    DirectionTypes direction;
    DiffTypes type;

    A content;

    public GenDiffContainer(DiffTypes type, DirectionTypes direction, A content) {
        this.direction = direction;
        this.type = type;
        this.content = content;
    }

    public DirectionTypes getDirection() {
        return direction;
    }

    public DiffTypes getType() {
        return type;
    }

    public A getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "DiffContainer{" + "direction=" + direction + ", type=" + type + ", content=" + content + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.direction);
        hash = 23 * hash + Objects.hashCode(this.type);
        hash = 23 * hash + Objects.hashCode(this.content);
        return hash;
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
        final GenDiffContainer<?> other = (GenDiffContainer<?>) obj;
        if (this.direction != other.direction) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        return true;
    }

}
