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
package eu.t5r.MDBSServer;

import eu.t5r.MDBS.sql.DBInterfaceMDBS;
import eu.t5r.MDBS.sql.MariaDBConnector;
import eu.t5r.MDBS.sql.SQLiteConnector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the Main file to start a MDBS server.
 * 
 * <p> This file is part of the MDBS
 * 
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class Main {

    /**
     * Main Method
     * @param args 1. dbfile 2. path to db setup
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {

        MDBSServer mdbsserver = null;
        Connection c = null;
        try {

            c = DriverManager.getConnection("jdbc:sqlite:" + args[0]);
            DBInterfaceMDBS db = new SQLiteConnector(c);
//            c = DriverManager.getConnection("jdbc:mysql://localhost/mdbs?user=root&password=Hallo123!");
//            DBInterfaceMDBS db = new MariaDBConnector(c);
            mdbsserver = new MDBSServer(db);
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        String content = new String(Files.readAllBytes(Paths.get(args[1])));
        String[] exes = content.split(";\n");
        for (String ex : exes) {
            System.out.println(ex);
            Statement s;
            try {
                s = c.createStatement();
                s.execute(ex);
                s.close();
            } catch (SQLException ex1) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex1);
            }

        }

        mdbsserver.start();

        // endless loop
        for (;;) {
            ;
        }
    }

}
