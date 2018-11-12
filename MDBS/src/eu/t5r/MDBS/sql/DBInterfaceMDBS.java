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
package eu.t5r.MDBS.sql;

import eu.t5r.MDBS.structs.GenDiffContainer;
import eu.t5r.MDBS.structs.MetaData;
import eu.t5r.MDBS.structs.MetaDataDeep;
import eu.t5r.MDBS.structs.RowContainer;
import eu.t5r.MDBS.structs.TableCommits;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * This interface specifes the connection between the MDBS and a Database
 *
 * <p>
 * This file is part of the MDBS
 *
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public interface DBInterfaceMDBS {

    /**
     * Creates the sync table if not exists. This table holds all tables that
     * will be synchronized
     *
     * @throws SQLException
     */
    public void addSyncTable() throws SQLException;

    /**
     * Adds a Table to the sync tables
     *
     * @param tableName The name of the table that is going to be added
     * @param priority The priority sets the order in which the changes of the
     * tables will be executed. The lower values will be executed first.
     * @throws SQLException
     */
    public void addSyncTable(String tableName, int priority) throws SQLException;
    
    /**
     * Removes a Table to the sync tables
     *
     * @param tableName The name of the table that is going to be added
     * @throws SQLException
     */
    public void removeSyncTable(String tableName) throws SQLException;

    /**
     * Creates the LOGIN_TABLE if not exists
     *
     * @throws SQLException
     */
    public void addLogInTable() throws SQLException;

    /**
     * Calls addSyncTable(), addMetaTable() and addLogInTable()
     * @throws SQLException 
     */
    public void checkDB() throws SQLException;

    /**
     * Returns a list of names from the syncable tables
     * @return List of names from the syncable tables
     * @throws SQLException 
     */
    public List<String> getSyncTables() throws SQLException;

    /**
     * Concerts the id and synctime from a table to a List of MetaData
     *
     * @param tableName the name of the table
     * @return list of MetaData objects
     * @throws SQLException
     */
    public List<MetaData> getMetaFromDataTable(String tableName) throws SQLException;

    /**
     * Collects all MetaDataDeep from the meta table
     * @return List of MetaDataDeep
     * @throws SQLException 
     */
    public List<MetaDataDeep> getAllMetaDeep() throws SQLException;

    /**
     * Collects the MetaDataDeep from the specified table in the meta table
     * @param tableName the specified table
     * @return List of MetaDataDeep
     * @throws SQLException 
     */
    public List<MetaDataDeep> getMetaDeepFromMetaTable(String tableName) throws SQLException;

    /**
     * Collects the MetaData from the specified table in the meta table
     * @param tableName the specified table
     * @return List of MetaData
     * @throws SQLException 
     */
    public List<MetaData> getMetaFromMetaTable(String tableName) throws SQLException;

    /**
     * Creates the META_TABLE_NAME if not exists
     *
     * @throws SQLException
     */
    public void addMetaTable() throws SQLException;

    /**
     * Writes the changes from the DiffContainer to the meta tabel
     * @param diff the changes
     * @throws SQLException 
     */
    public void executeDiffContainerMeta(GenDiffContainer<RowContainer> diff) throws SQLException;

    /**
     * Writes the changes from the DiffContainer to the syncable tabel
     * @param diff the changes
     * @throws SQLException 
     */
    public void executeDiffContainerData(GenDiffContainer<RowContainer> diff) throws SQLException;

    /**
     * Adds the missing data to a RowContainer that only has the meta data
     * @param result Empty RowContainer with only the meta data
     * @throws SQLException 
     */
    public void getData(RowContainer result) throws SQLException;

    /**
     * Return the commits of the syncable tables
     * @return Instance of TableCommits
     * @throws SQLException 
     */
    public TableCommits getCommits() throws SQLException;

    /**
     * Updates the commit table
     * @param commits Instance of TableCommits
     * @throws SQLException 
     */
    public void setCommits(TableCommits commits) throws SQLException;

    /**
     * This function checks if the given user is registred in the database
     * @param user
     * @param pwd
     * @return true if the given user is registred in the database
     * @throws SQLException 
     */
    public boolean checkLogIn(String user, String pwd) throws SQLException;

    /**
     * Return the syncable tables with the matching priority
     * @return
     * @throws SQLException 
     */
    public Map<String, Integer> getPriority() throws SQLException;
}
