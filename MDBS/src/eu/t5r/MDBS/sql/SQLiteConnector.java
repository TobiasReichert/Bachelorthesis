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

import eu.t5r.MDBS.structs.DataContainer;
import eu.t5r.MDBS.structs.GenDiffContainer;
import eu.t5r.MDBS.structs.RowContainer;
import eu.t5r.MDBS.structs.types.MDBSTypes;
import eu.t5r.MDBS.structs.MetaData;
import eu.t5r.MDBS.structs.MetaDataDeep;
import eu.t5r.MDBS.structs.TableCommits;
import eu.t5r.MDBS.structs.types.DiffTypes;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implemetation of the DBInterfaceMDBS for MariaDB.
 *
 * <p>
 * This file is part of the MDBS
 *
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
public class SQLiteConnector implements DBInterfaceMDBS {

    private static final String SYNC_TABLE_NAME = "_.Sync._";

    private static final String SYNC_TABLE_SQL = "CREATE TABLE IF NOT EXISTS '"
            + SYNC_TABLE_NAME
            + "' ("
            + "tableName TEXT NOT NULL PRIMARY KEY, "
            + "lastCommit INTEGER, "
            + "priority INTEGER"
            + ");";

    private static final String META_TABLE_NAME = "_.Sync_Meta._";

    private static final String META_TABLE = "CREATE TABLE IF NOT EXISTS '"
            + META_TABLE_NAME
            + "' ("
            + "tableName TEXT NOT NULL, "
            + "id TEXT NOT NULL, "
            + "synctime INTEGER, "
            + "lastSynctime INTEGER, "
            + "deleted INTEGER, "
            + "PRIMARY KEY(tableName, id)"
            + ");";

    private static final String LOGIN_TABLE_NAME = "_.LogIn._";

    private static final String LOGIN_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS '"
            + LOGIN_TABLE_NAME
            + "' ("
            + "user TEXT NOT NULL, "
            + "pwd TEXT NOT NULL, "
            + "PRIMARY KEY(user)"
            + ");";

    Connection self;

    public SQLiteConnector(Connection self) throws SQLException {
        this.self = self;

        self.setAutoCommit(true);
        self.setReadOnly(false);
    }

    @Override
    public void addSyncTable() throws SQLException {
        try (Statement statement = self.createStatement()) {
            statement.execute(SYNC_TABLE_SQL);
        }
    }

    @Override
    public void addSyncTable(String tableName, int priority) throws SQLException {
        addSyncTable();

        String sql = "INSERT INTO '" + SYNC_TABLE_NAME + "'(tableName, lastCommit, priority) VALUES (?,?,?);";
        try (PreparedStatement statement = self.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setInt(2, 0);
            statement.setInt(3, priority);
            statement.execute();
        }
    }

    @Override
    public void addLogInTable() throws SQLException {
        try (Statement statement = self.createStatement()) {
            statement.execute(LOGIN_TABLE_CREATE);
        }

    }

    @Override
    public void removeSyncTable(String tableName) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void checkDB() throws SQLException {
        addSyncTable();
        addMetaTable();
        addLogInTable();
    }

    @Override
    public List<String> getSyncTables() throws SQLException {
        Statement statement = self.createStatement();
        // Get all mentioned tabels from
        String sql = "SELECT tableName FROM '" + SYNC_TABLE_NAME + "';";
        List<String> tables = new ArrayList<>();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            tables.add(rs.getString("tableName"));
        }
        return tables;
    }

    @Override
    public List<MetaData> getMetaFromDataTable(String tableName) throws SQLException {
        List<MetaData> result = new ArrayList<>();
        String sql = "SELECT id, synctime FROM '" + tableName + "';";
        Statement statement = self.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            MetaData md = new MetaData(tableName, rs.getString("id"), rs.getLong("synctime"));
            result.add(md);
        }
        return result;
    }

    @Override
    public List<MetaDataDeep> getAllMetaDeep() throws SQLException {
        List<MetaDataDeep> result = new ArrayList<>();

        TableCommits commits = getCommits();
        for (Map.Entry<String, Long> commit : commits.getCommits().entrySet()) {
            result.addAll(getMetaDeepFromMetaTable(commit.getKey()));
        }

        return result;
    }

    @Override
    public List<MetaDataDeep> getMetaDeepFromMetaTable(String tableName) throws SQLException {
        List<MetaDataDeep> result = new ArrayList<>();
        String sql = "SELECT id, synctime, lastSynctime, deleted FROM '"
                + META_TABLE_NAME + "' WHERE tableName = ?;";
        PreparedStatement statement = self.prepareStatement(sql);
        statement.setString(1, tableName);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            MetaDataDeep mdd = new MetaDataDeep(tableName, rs.getString("id"), rs.getLong("synctime"), rs.getLong("lastSynctime"), rs.getBoolean("deleted"));
            result.add(mdd);
        }
        return result;
    }

    @Override
    public List<MetaData> getMetaFromMetaTable(String tableName) throws SQLException {
        List<MetaData> result = new ArrayList<>();
        String sql = "SELECT id, synctime FROM '"
                + META_TABLE_NAME + "' WHERE tableName = ?;";
        PreparedStatement statement = self.prepareStatement(sql);
        statement.setString(1, tableName);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            result.add(new MetaData(tableName, rs.getString("id"), rs.getLong("synctime")));
        }
        return result;
    }

    @Override
    public void addMetaTable() throws SQLException {
        try (Statement statement = self.createStatement()) {
            // Create META_TABLE
            statement.execute(META_TABLE);
        }
    }

    @Override
    public void executeDiffContainerMeta(GenDiffContainer<RowContainer> diff) throws SQLException { //rowDiffContainer
        RowContainer row = diff.getContent();
        if (null != diff.getType()) {
            switch (diff.getType()) {
                case INSERT: {
                    String sql = "INSERT INTO '" + META_TABLE_NAME + "' (tableName,id,synctime,lastSynctime,deleted) VALUES (?,?,?,?,?);";
                    PreparedStatement ps = self.prepareStatement(sql);
                    ps.setString(1, row.getTableName());
                    ps.setString(2, row.getUuid());
                    ps.setLong(3, row.getSynctime());
                    ps.setLong(4, row.getLastsynctime());
                    ps.setBoolean(5, row.isDeleted());
                    ps.execute();
                    break;
                }
                case UPDATE: {
                    String sql = "UPDATE '" + META_TABLE_NAME + "' SET synctime = ?, lastSynctime = ?, deleted = ? WHERE id = ? AND tableName = ?;";
                    PreparedStatement ps = self.prepareStatement(sql);
                    ps.setString(4, row.getUuid());
                    ps.setString(5, row.getTableName());
                    ps.setLong(1, row.getSynctime());
                    ps.setLong(2, row.getLastsynctime());
                    ps.setBoolean(3, row.isDeleted());
                    ps.execute();
                    break;
                }
                case DELETE: {
                    String sql = "UPDATE '" + META_TABLE_NAME + "' SET synctime = ?, lastSynctime = ?, deleted = ? WHERE id = ? AND tableName = ?;";
                    PreparedStatement ps = self.prepareStatement(sql);
                    ps.setString(4, row.getUuid());
                    ps.setString(5, row.getTableName());
                    ps.setLong(1, row.getSynctime());
                    ps.setLong(2, row.getLastsynctime());
                    ps.setBoolean(3, row.isDeleted());
                    ps.execute();
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    public void executeDiffContainerData(GenDiffContainer<RowContainer> diff) throws SQLException {
        executeDiffContainerMeta(diff);

        RowContainer row = diff.getContent();
        Map<String, DataContainer> cells = row.getCells();
        if (cells.isEmpty() && diff.getType() != DiffTypes.DELETE) {
            return;
        }
        if (null != diff.getType()) {
            switch (diff.getType()) {
                case INSERT: {
                    // Build SQL
                    String sql = "INSERT INTO '" + row.getTableName() + "'";

                    sql += "(";
                    for (Map.Entry<String, DataContainer> cell : cells.entrySet()) {
                        sql += cell.getKey() + ",";
                    }
                    sql = sql.subSequence(0, sql.length() - 1).toString();
                    sql += ") VALUES (";
                    for (int i = 0; i < cells.size(); i++) {
                        sql += "?,";
                    }
                    sql = sql.subSequence(0, sql.length() - 1).toString();

                    sql += ");";

                    // Insert the data
                    int i = 1;
                    PreparedStatement ps = self.prepareStatement(sql);
                    for (Map.Entry<String, DataContainer> cell : cells.entrySet()) {
                        DataContainer data = cell.getValue();
                        switch (data.getType()) {
                            case INTEGER:
                                if (data.getData() instanceof Integer) {
                                    ps.setInt(i, (int) data.getData());
                                } else {
                                    ps.setLong(i, (long) data.getData());
                                }
                                break;
                            case REAL:
                                ps.setDouble(i, (double) data.getData());
                                break;
                            case TEXT:
                                ps.setString(i, (String) data.getData());
                                break;
                            case BLOB:
                                ps.setBlob(i, (Blob) data.getData());
                                break;
                        }
                        i++;
                    }
                    ps.execute();
                    break;
                }
                case UPDATE: {
                    // Build SQL
                    String sql = "UPDATE '" + row.getTableName() + "'"; // SET synctime = ?, lastSynctime = ?, deleted = ? WHERE id = ?;";

                    sql += " SET ";
                    for (Map.Entry<String, DataContainer> cell : cells.entrySet()) {
                        if (cell.getKey().equals("id")) {
                            continue;
                        }
                        sql += cell.getKey() + " = ?,";
                    }
                    sql = sql.subSequence(0, sql.length() - 1).toString();

                    sql += " WHERE id = ?;";

                    // Insert the data
                    int i = 1;
                    PreparedStatement ps = self.prepareStatement(sql);
                    for (Map.Entry<String, DataContainer> cell : cells.entrySet()) {
                        if (cell.getKey().equals("id")) {
                            continue;
                        }
                        DataContainer data = cell.getValue();
                        switch (data.getType()) {
                            case INTEGER:
                                ps.setInt(i, (int) data.getData());
                                break;
                            case REAL:
                                ps.setDouble(i, (double) data.getData());
                                break;
                            case TEXT:
                                ps.setString(i, (String) data.getData());
                                break;
                            case BLOB:
                                ps.setBlob(i, (Blob) data.getData());
                                break;
                        }
                        i++;
                    }

                    ps.setString(i, (String) cells.get("id").getData());
                    ps.execute();
                    break;
                }
                case DELETE: {
                    String sql = "DELETE FROM '" + row.getTableName() + "' WHERE id = ?;";
                    PreparedStatement ps = self.prepareStatement(sql);
                    ps.setString(1, row.getUuid());
                    ps.execute();
                    break;
                }
                default:
                    System.out.println("CASE DEFAULT");
                    break;
            }
        }
    }

    @Override
    public void getData(RowContainer result) throws SQLException {
        String sql = "SELECT * FROM '" + result.getTableName() + "' WHERE id = ?;";
        PreparedStatement ps = self.prepareStatement(sql);
        ps.setString(1, result.getUuid());

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {

                MDBSTypes type;
                switch (meta.getColumnTypeName(i)) {
                    case "TEXT":
                        type = MDBSTypes.TEXT;
                        break;
                    case "INTEGER":
                        type = MDBSTypes.INTEGER;
                        break;
                    case "REAL":
                        type = MDBSTypes.REAL;
                        break;
                    case "BLOB":
                        type = MDBSTypes.BLOB;
                        break;

                    default:
                        throw new SQLException("Not Defined Type: " + meta.getColumnTypeName(i));
                }

                result.addCell(meta.getColumnName(i), new DataContainer(type, rs.getObject(i)));
            }
        }
    }

    @Override
    public TableCommits getCommits() throws SQLException {
        Map<String, Long> result = new HashMap<>();

        String sql = "SELECT tableName, lastCommit FROM '" + SYNC_TABLE_NAME + "';";
        Statement statement = self.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            result.put(rs.getString("tableName"), rs.getLong("lastCommit"));
        }
        return new TableCommits(result);
    }

    @Override
    public void setCommits(TableCommits commits) throws SQLException {
        PreparedStatement ps = self.prepareStatement("UPDATE `_.Sync._` SET `lastCommit` = ? WHERE `tableName` = ?");
        for (Map.Entry<String, Long> commit : commits.getCommits().entrySet()) {
            ps.setLong(1, commit.getValue());
            ps.setString(2, commit.getKey());
            ps.addBatch();

        }
        ps.executeBatch();
    }

    @Override
    public boolean checkLogIn(String user, String pwd) throws SQLException {
        String sql = "SELECT user, pwd FROM '" + LOGIN_TABLE_NAME + "' WHERE user = ? AND pwd = ?;";
        PreparedStatement ps = self.prepareStatement(sql);
        ps.setString(1, user);
        ps.setString(2, pwd);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    @Override
    public Map<String, Integer> getPriority() throws SQLException {
        Map<String, Integer> result = new HashMap<>();

        String sql = "SELECT tableName, priority FROM '" + SYNC_TABLE_NAME + "';";
        Statement statement = self.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next()) {
            result.put(rs.getString("tableName"), rs.getInt("priority"));
        }
        return result;
    }

}
