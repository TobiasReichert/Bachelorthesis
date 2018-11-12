package eu.t5r.orga.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.t5r.MDBS.structs.DataContainer;
import eu.t5r.MDBS.structs.GenDiffContainer;
import eu.t5r.MDBS.structs.MetaData;
import eu.t5r.MDBS.structs.MetaDataDeep;
import eu.t5r.MDBS.structs.RowContainer;
import eu.t5r.MDBS.structs.TableCommits;
import eu.t5r.MDBS.structs.types.DiffTypes;
import eu.t5r.MDBS.structs.types.MDBSTypes;

import static android.R.attr.id;

/**
 * Created by tobias on 27.09.17.
 */

public class MDBSAndroidSQLite extends SQLiteOpenHelper implements eu.t5r.MDBS.sql.DBInterfaceMDBS {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "AndroidNotes.db";

    private static final String SYNC_TABLE_NAME = "_Sync_";

    private static final String SYNC_TABLE_SQL = "CREATE TABLE IF NOT EXISTS '"
            + SYNC_TABLE_NAME
            + "' ("
            + "tableName TEXT NOT NULL PRIMARY KEY, "
            + "lastCommit INTEGER, "
            + "priority INTEGER"
            + ");";

    private static final String META_TABLE_NAME = "_Sync_Meta_";

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

    private static final String LOGIN_TABLE_NAME = "_LogIn_";

    private static final String LOGIN_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS '"
            + LOGIN_TABLE_NAME
            + "' ("
            + "user TEXT NOT NULL, "
            + "pwd TEXT NOT NULL, "
            + "PRIMARY KEY(user)"
            + ");";

    public MDBSAndroidSQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    // MDBS
    public void addSyncTable() throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SYNC_TABLE_SQL);
    }

    public void addSyncTable(String tableName, int priority) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("tableName", tableName);
        values.put("lastCommit", 0);
        values.put("priority", priority);

        db.insert(SYNC_TABLE_NAME, null, values);
        db.close();
    }

    public void addLogInTable() throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(LOGIN_TABLE_CREATE);
    }

    public void removeSyncTable(String tableName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void checkDB() throws SQLException {
        addSyncTable();
        addMetaTable();
        addLogInTable();
    }

    public List<String> getSyncTables() throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                SYNC_TABLE_NAME,
                new String[]{"tableName"},
                null,
                null,
                null,
                null,
                null,
                null);

        List<String> tables = new ArrayList<>();

        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0));
        }
        cursor.close();
        return tables;
    }

    public List<MetaData> getMetaFromDataTable(String tableName) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                tableName,
                new String[]{"id", "synctime"},
                null,
                null,
                null,
                null,
                null,
                null);

        List<MetaData> result = new ArrayList<>();

        while (cursor.moveToNext()) {
            MetaData md = new MetaData(tableName, cursor.getString(0), cursor.getLong(1));
            result.add(md);
        }
        cursor.close();
        return result;
    }

    public List<MetaDataDeep> getAllMetaDeep() throws SQLException {
        List<MetaDataDeep> result = new ArrayList<>();

        TableCommits commits = getCommits();
        for (Map.Entry<String, Long> commit : commits.getCommits().entrySet()) {
            result.addAll(getMetaDeepFromMetaTable(commit.getKey()));
        }

        return result;
    }

    public List<MetaDataDeep> getMetaDeepFromMetaTable(String tableName) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                META_TABLE_NAME,
                new String[]{"id", "synctime", "lastSynctime", "deleted"},
                "tableName" + "=?",
                new String[]{tableName},
                null,
                null,
                null,
                null);

        List<MetaDataDeep> result = new ArrayList<>();

        while (cursor.moveToNext()) {
            MetaDataDeep mdd = new MetaDataDeep(tableName, cursor.getString(0), cursor.getLong(1), cursor.getLong(2), cursor.getInt(3) > 0 ? true : false);
            result.add(mdd);
        }
        cursor.close();
        return result;
    }

    public List<MetaData> getMetaFromMetaTable(String tableName) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                META_TABLE_NAME,
                new String[]{"id", "synctime"},
                "tableName" + "=?",
                new String[]{tableName},
                null,
                null,
                null,
                null);

        List<MetaData> result = new ArrayList<>();

        while (cursor.moveToNext()) {
            MetaData md = new MetaData(tableName, cursor.getString(0), cursor.getLong(1));
            result.add(md);
        }
        cursor.close();
        return result;
    }

    public void addMetaTable() throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(META_TABLE);
    }

    public void executeDiffContainerMeta(GenDiffContainer<RowContainer> diff) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        RowContainer row = diff.getContent();
        if (null != diff.getType()) {
            switch (diff.getType()) {
                case INSERT: {
                    ContentValues values = new ContentValues();
                    values.put("tableName", row.getTableName());
                    values.put("id", row.getUuid());
                    values.put("synctime", row.getSynctime());
                    values.put("lastSynctime", row.getLastsynctime());
                    values.put("deleted", row.isDeleted());
                    db.insert(META_TABLE_NAME, null, values);
                    db.close();
                    break;
                }
                case UPDATE:
                case DELETE:{
                    ContentValues values = new ContentValues();
                    values.put("synctime", row.getSynctime());
                    values.put("lastSynctime", row.getLastsynctime());
                    values.put("deleted", row.isDeleted());

                    db.update(
                            META_TABLE_NAME,
                            values,
                            "id = ? AND tableName = ?",
                            new String[]{row.getUuid(), row.getTableName()}
                    );
                    break;
                }
                default:
                    break;
            }
        }
    }

    public void executeDiffContainerData(GenDiffContainer<RowContainer> diff) throws SQLException {
        executeDiffContainerMeta(diff);

        SQLiteDatabase db = this.getWritableDatabase();

        RowContainer row = diff.getContent();
        Map<String, DataContainer> cells = row.getCells();
        if (cells.isEmpty() && diff.getType() != DiffTypes.DELETE) {
            return;
        }
        if (null != diff.getType()) {
            switch (diff.getType()) {
                case INSERT: {
                    // Insert the data
                    ContentValues values = new ContentValues();
                    for (Map.Entry<String, DataContainer> cell : cells.entrySet()) {
                        DataContainer data = cell.getValue();
                        switch (data.getType()) {
                            case INTEGER:
                                if (data.getData() instanceof Integer) {
                                    values.put(cell.getKey(), (int)data.getData());
                                } else {
                                    values.put(cell.getKey(), (long)data.getData());
                                }
                                break;
                            case REAL:
                                values.put(cell.getKey(), (double)data.getData());
                                break;
                            case TEXT:
                                values.put(cell.getKey(), (String)data.getData());
                                break;
                            case BLOB:
                                values.put(cell.getKey(), (Byte)data.getData());
                                break;
                        }
                    }
                    db.insert(row.getTableName(),null, values);
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
                    ContentValues values = new ContentValues();
                    for (Map.Entry<String, DataContainer> cell : cells.entrySet()) {
                        DataContainer data = cell.getValue();
                        switch (data.getType()) {
                            case INTEGER:
                                if (data.getData() instanceof Integer) {
                                    values.put(cell.getKey(), (int)data.getData());
                                } else {
                                    values.put(cell.getKey(), (long)data.getData());
                                }
                                break;
                            case REAL:
                                values.put(cell.getKey(), (double)data.getData());
                                break;
                            case TEXT:
                                values.put(cell.getKey(), (String)data.getData());
                                break;
                            case BLOB:
                                values.put(cell.getKey(), (Byte)data.getData());
                                break;
                        }
                    }

                    db.update(
                            row.getTableName(),
                            values,
                            "id = ?",
                            new String[] { (String) cells.get("id").getData() });
                    break;
                }
                case DELETE: {
                    db.delete(row.getTableName(), "id = ?",
                            new String[] { row.getUuid() });
                    break;
                }
                default:
                    System.out.println("CASE DEFAULT");
                    break;
            }
        }
    }

    public void getData(RowContainer result) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "SELECT * FROM '" + result.getTableName() + "' WHERE id = ?;";
        Cursor cursor = db.rawQuery(sql, new String[]{result.getUuid()});

        while (cursor.moveToNext()) {
            int columns = cursor.getColumnCount();
            for (int i = 0; i < columns; ++i) {
                MDBSTypes type;
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_STRING:
                        type = MDBSTypes.TEXT;
                        result.addCell(cursor.getColumnName(i), new DataContainer(type, cursor.getString(i)));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        type = MDBSTypes.INTEGER;
                        result.addCell(cursor.getColumnName(i), new DataContainer(type, cursor.getInt(i)));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        type = MDBSTypes.REAL;
                        Long l = cursor.getLong(i);
                        if(l < Integer.MAX_VALUE) {
                            result.addCell(cursor.getColumnName(i), new DataContainer(type, cursor.getInt(i)));
                        } else {
                            result.addCell(cursor.getColumnName(i), new DataContainer(type, l));
                        }
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        type = MDBSTypes.BLOB;
                        result.addCell(cursor.getColumnName(i), new DataContainer(type, cursor.getBlob(i)));
                        break;
                    case Cursor.FIELD_TYPE_NULL:
                        type = null;
                        result.addCell(cursor.getColumnName(i), new DataContainer(type, null));
                    default:
                        throw new SQLException("Not Defined Type: " + cursor.getType(i));

                }

            }
        }
    }

    public TableCommits getCommits() throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                SYNC_TABLE_NAME,
                new String[]{"tableName", "lastCommit"},
                null,
                null,
                null,
                null,
                null,
                null);

        Map<String, Long> result = new HashMap<>();

        while (cursor.moveToNext()) {
            result.put(cursor.getString(0), cursor.getLong(1));
        }
        cursor.close();
        return new TableCommits(result);
    }

    public void setCommits(TableCommits commits) throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Map.Entry<String, Long> commit : commits.getCommits().entrySet()) {
            ContentValues values = new ContentValues();
            values.put("lastCommit", commit.getValue());

            db.update(
                    SYNC_TABLE_NAME,
                    values,
                    "tableName" + " = ?",
                    new String[]{commit.getKey()}
            );
        }
    }

    public boolean checkLogIn(String user, String pwd) throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT user, pwd FROM '" + LOGIN_TABLE_NAME + "' WHERE user = ? AND pwd = ?;",
                new String[]{user, pwd});
        return cursor.moveToNext();
    }

    public Map<String, Integer> getPriority() throws SQLException {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                SYNC_TABLE_NAME,
                new String[]{"tableName", "priority"},
                null,
                null,
                null,
                null,
                null,
                null);

        Map<String, Integer> result = new HashMap<>();

        while (cursor.moveToNext()) {
            result.put(cursor.getString(0), cursor.getInt(1));
        }
        cursor.close();
        return result;
    }
}
