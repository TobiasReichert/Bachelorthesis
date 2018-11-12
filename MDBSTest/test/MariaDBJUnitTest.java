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

import eu.t5r.MDBS.MDBSConnection;
import eu.t5r.MDBS.sql.DBInterfaceMDBS;
import eu.t5r.MDBS.sql.MariaDBConnector;
import eu.t5r.MDBS.sql.SQLiteConnector;
import eu.t5r.MDBS.structs.GenDiffContainer;
import eu.t5r.MDBS.structs.MDBSSyncException;
import eu.t5r.MDBS.structs.MetaData;
import eu.t5r.MDBS.structs.MetaDataDeep;
import eu.t5r.MDBS.structs.RowContainer;
import eu.t5r.MDBS.structs.TableCommits;
import eu.t5r.MDBS.structs.socket.FullSyncB;
import eu.t5r.MDBS.structs.types.DiffTypes;
import eu.t5r.MDBS.structs.types.DirectionTypes;
import eu.t5r.MDBS.sync.Algorithm;
import eu.t5r.MDBSServer.MDBSServer;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 *
 *
 * <p>
 * This file is part of the MDBS
 *
 * @author Tobias Reichert [mail@teamtobias.de]
 * @version 0.1
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MariaDBJUnitTest {

    static MDBSConnection client1;
    static MDBSConnection client2;
    static Connection c1;
    static Connection c2;
    static Connection s;
    static MDBSServer server;

    public MariaDBJUnitTest() {
    }

    public void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Logger.getLogger(SQLiteJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @BeforeClass
    public static void onlyOnce() throws SQLException, IOException {
        String clientDB1 = "client1.db";
        String clientDB2 = "client2.db";

        deleteFile(clientDB1);
        deleteFile(clientDB2);

        c1 = DriverManager.getConnection("jdbc:sqlite:" + clientDB1);
        client1 = new MDBSConnection(new SQLiteConnector(c1));
        client1.setHost("127.0.0.1");
        System.out.println(client1);

        c2 = DriverManager.getConnection("jdbc:sqlite:" + clientDB2);
        client2 = new MDBSConnection(new SQLiteConnector(c2));
        client2.setHost("127.0.0.1");
        System.out.println(client2);

        s = DriverManager.getConnection("jdbc:mysql://localhost/mdbs?user=root&password=Hallo123!&useSSL=false");
        Statement statement = s.createStatement();
        try {
            statement.execute("DROP TABLE `_.LogIn._`;");
            statement.execute("DROP TABLE `_.Sync._`;");
            statement.execute("DROP TABLE `_.Sync_Meta._`;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            statement.execute("DROP TABLE Gehoert;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            statement.execute("DROP TABLE Person;");
            statement.execute("DROP TABLE Device;");
        } catch (Exception e) {
            e.printStackTrace();
        }
        server = new MDBSServer(new MariaDBConnector(s));
        server.start();

    }

    static void deleteFile(String path) {
        File file = new File(path);

        if (file.delete()) {
            System.out.println(file.getName() + " is deleted!");
        } else {
            System.out.println("Delete operation is failed.");
        }
    }

    @AfterClass
    public static void onlyEnd() throws SQLException, IOException {
        client1.close();
        client2.close();
        server.stop();
    }

    @Before
    public void setUp() throws SQLException {

    }

    @After
    public void tearDown() throws SQLException {

    }

    @Test
    public void Aprepare() throws SQLException {
        Statement s = c1.createStatement();
        s.execute("CREATE TABLE IF NOT EXISTS Person(id TEXT NOT NULL PRIMARY KEY, synctime INTEGER, name TEXT)");
        s.close();
    }

    @Test
    public void BaddSync() throws SQLException {
        client1.addSyncTable("Person", 1);
        List<String> result = client1.db.getSyncTables();
        assertTrue(result.contains("Person"));
    }

    @Test
    public void CcheckTest() throws SQLException {
        client1.db.checkDB();
        // assert no Exception
    }

    @Test
    public void DequalsTest() {
        MetaDataDeep a = new MetaDataDeep("testTable", "abc", 0, 0, true);
        MetaDataDeep b = new MetaDataDeep("testTable", "abc", 0, 0, true);
        MetaDataDeep c = new MetaDataDeep("testTable", "abcd", 0, 0, true);
        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
    }

    @Test
    public void EA_syncTest() {
        List<MetaData> a = new ArrayList<>();
        List<MetaData> b = new ArrayList<>();

        a.add(new MetaData("testTable", "a", 1));
        a.add(new MetaData("testTable", "b", 1));
        a.add(new MetaData("testTable", "c", 2));

        b.add(new MetaData("testTable", "b", 1));
        b.add(new MetaData("testTable", "c", 1));
        b.add(new MetaData("testTable", "d", 1));

        List<GenDiffContainer<RowContainer>> result = Algorithm.checkDiffOneWay(a, b);

        System.out.println(result);
        assertTrue(result.contains(new GenDiffContainer<>(DiffTypes.INSERT, DirectionTypes.SERVER_TO_CLIENT, new RowContainer("testTable", "a", 1, 0, false))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "a", 1, 0, false, DiffTypes.INSERT, DirectionTypes.CLIENT_TO_SERVER)));
        ////assertTrue(result.contains(new RowDiffContainer("b", 1, 0, false, DiffTypes.INSERT, DirectionTypes.CLIENT_TO_SERVER)));
        assertTrue(result.contains(new GenDiffContainer<>(DiffTypes.UPDATE, DirectionTypes.SERVER_TO_CLIENT, new RowContainer("testTable", "c", 2, 1, false))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "c", 2, 1, false, DiffTypes.UPDATE, DirectionTypes.CLIENT_TO_SERVER)));
        assertTrue(result.contains(new GenDiffContainer<>(DiffTypes.DELETE, DirectionTypes.SERVER_TO_CLIENT, new RowContainer("testTable", "d", 0, 0, true))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "d", 0, 0, true, DiffTypes.DELETE, DirectionTypes.CLIENT_TO_SERVER)));
    }

    @Test
    public void EB_syncTestBoth() throws MDBSSyncException {
        List<MetaDataDeep> a = new ArrayList<>();
        List<MetaDataDeep> b = new ArrayList<>();

        a.add(new MetaDataDeep("testTable", "a", 0, 0, false));
        a.add(new MetaDataDeep("testTable", "b", 0, 0, false));
        a.add(new MetaDataDeep("testTable", "c", 1, 0, false));
        a.add(new MetaDataDeep("testTable", "d", 0, 0, false));
        a.add(new MetaDataDeep("testTable", "f", 0, 0, true));
        a.add(new MetaDataDeep("testTable", "g", 0, 0, false));

        b.add(new MetaDataDeep("testTable", "b", 0, 0, false));
        b.add(new MetaDataDeep("testTable", "c", 0, 0, false));
        b.add(new MetaDataDeep("testTable", "d", 1, 0, false));
        b.add(new MetaDataDeep("testTable", "e", 0, 0, false));
        b.add(new MetaDataDeep("testTable", "f", 0, 0, false));
        b.add(new MetaDataDeep("testTable", "g", 0, 0, true));

        FullSyncB result = Algorithm.checkDiffBothWay(a, b, false);

        System.out.println("EB_syncTestBoth: " + result);
        assertTrue(result.getDiffContainers().contains(new GenDiffContainer<>(DiffTypes.INSERT, DirectionTypes.SERVER_TO_CLIENT, new RowContainer("testTable", "a", 0, 0, false))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "a", 0, 0, false, DiffTypes.INSERT, DirectionTypes.CLIENT_TO_SERVER)));
        ////assertTrue(result.contains(new RowDiffContainer("b", 0, 0, false, DiffTypes.INSERT, DirectionTypes.CLIENT_TO_SERVER)));
        assertTrue(result.getDiffContainers().contains(new GenDiffContainer<>(DiffTypes.UPDATE, DirectionTypes.SERVER_TO_CLIENT, new RowContainer("testTable", "c", 1, 0, false))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "c", 1, 0, false, DiffTypes.UPDATE, DirectionTypes.CLIENT_TO_SERVER)));
        assertTrue(result.getRequest().contains(new GenDiffContainer<>(DiffTypes.UPDATE, DirectionTypes.SERVER_TO_CLIENT, new MetaDataDeep("testTable", "d", 1, 0, false))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "d", 1, 0, false, DiffTypes.UPDATE, DirectionTypes.SERVER_TO_CLIENT)));
        assertTrue(result.getRequest().contains(new GenDiffContainer<>(DiffTypes.INSERT, DirectionTypes.SERVER_TO_CLIENT, new MetaDataDeep("testTable", "e", 0, 0, false))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "e", 0, 0, false, DiffTypes.INSERT, DirectionTypes.SERVER_TO_CLIENT)));
        assertTrue(result.getDiffContainers().contains(new GenDiffContainer<>(DiffTypes.DELETE, DirectionTypes.SERVER_TO_CLIENT, new RowContainer("testTable", "f", 0, 0, true))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "f", 0, 0, true, DiffTypes.DELETE, DirectionTypes.CLIENT_TO_SERVER)));
        assertTrue(result.getRequest().contains(new GenDiffContainer<>(DiffTypes.DELETE, DirectionTypes.SERVER_TO_CLIENT, new MetaDataDeep("testTable", "g", 0, 0, true))));
        //assertTrue(result.contains(new RowDiffContainer("testTable", "g", 0, 0, true, DiffTypes.DELETE, DirectionTypes.SERVER_TO_CLIENT)));
    }

    @Test(expected = MDBSSyncException.class)
    public void EB_syncTestBothError() throws MDBSSyncException {
        List<MetaDataDeep> a = new ArrayList<>();
        List<MetaDataDeep> b = new ArrayList<>();

        a.add(new MetaDataDeep("testTable", "a", 5, 0, false)); // 0 -> 5
        b.add(new MetaDataDeep("testTable", "a", 10, 0, false)); // 0 -> 10

        FullSyncB result = Algorithm.checkDiffBothWay(a, b, false);
        System.out.println(result);
    }

    @Test
    public void ECsyncTest() throws SQLException {
        Statement s = c1.createStatement();
        s.execute("INSERT INTO Person VALUES ('abc-def', 123, 'Hans');");
        s.execute("INSERT INTO Person VALUES('abc-xyz', 143, 'Günter');");
        client1.sync();

        List<MetaDataDeep> clientMeta = client1.db.getMetaDeepFromMetaTable("Person");
        assertTrue(clientMeta.contains(new MetaDataDeep("Person", "abc-def", 123, 0, false)));
        assertTrue(clientMeta.contains(new MetaDataDeep("Person", "abc-xyz", 143, 0, false)));
        assertTrue(clientMeta.size() == 2);
    }

    @Test
    public void EDsyncTest() throws SQLException {
        Statement s = c1.createStatement();
        s.execute("INSERT INTO Person VALUES ('abc-ght', 123, 'Dieter');");
        s.execute("UPDATE Person SET synctime = 124, name = 'Hans-Jakob' WHERE id = 'abc-def';");
        s.execute("DELETE FROM Person WHERE id = 'abc-xyz';");
        client1.sync();

        List<MetaDataDeep> clientMeta = client1.db.getMetaDeepFromMetaTable("Person");
        assertTrue(clientMeta.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(clientMeta.contains(new MetaDataDeep("Person", "abc-def", 124, 123, false)));
        assertTrue(clientMeta.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(clientMeta.size() == 3);
    }

    @Test
    public void FAServer() throws Exception {
        System.out.println("-----");
        System.out.println("FAServer()");

        Statement st = s.createStatement();
        server.sql.checkDB();
        st.execute("INSERT INTO `_.LogIn._` VALUES ('test', 'test');");

        st.execute("CREATE TABLE IF NOT EXISTS `Person`(id VARCHAR(255) NOT NULL PRIMARY KEY, synctime LONG, name TEXT)");
        server.sql.addSyncTable("Person", 1);

        server.sql.checkDB();
    }

    @Test
    public void FBLogIn() throws Exception {
        System.out.println("-----");
        System.out.println("FBLogIn()");

        client1.connect();
        client1.logIn("test", "test");

        sleep();

        assertTrue(client1.getLogedIn());

    }

    @Test
    public void GSocketAInsert() throws Exception {
        System.out.println("-----");
        System.out.println("GSocketAInsert()");

        client1.useIncremental(false);
        client1.sync();
        sleep();

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Person");
        System.out.println("serverMeta: " + serverMeta);
        assertTrue(serverMeta.contains(new MetaData("Person", "abc-ght", 123)));
        assertTrue(serverMeta.contains(new MetaData("Person", "abc-def", 124)));
        assertTrue(serverMeta.size() == 2);

        List<MetaDataDeep> serverMetaDeep = server.sql.getMetaDeepFromMetaTable("Person");
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-def", 124, 123, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(serverMetaDeep.size() == 3);
    }

    @Test
    public void GSocketBUpdate() throws Exception {
        System.out.println("-----");
        System.out.println("GSocketBUpdate()");

        Statement s = c1.createStatement();
        s.execute("UPDATE Person SET synctime = 154, name = 'Hans-Jakob der Erste' WHERE id = 'abc-def';");
        client1.sync();
        sleep();

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Person");
        assertTrue(serverMeta.contains(new MetaData("Person", "abc-ght", 123)));
        assertTrue(serverMeta.contains(new MetaData("Person", "abc-def", 154)));
        assertTrue(serverMeta.size() == 2);

        List<MetaDataDeep> serverMetaDeep = server.sql.getMetaDeepFromMetaTable("Person");
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-def", 154, 124, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(serverMetaDeep.size() == 3);
    }

    @Test
    public void GSocketCDelete() throws Exception {
        System.out.println("-----");
        System.out.println("GSocketCDelete()");

        Statement s = c1.createStatement();
        s.execute("DELETE FROM Person WHERE id = 'abc-def';");
        client1.sync();
        sleep();

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Person");
        assertTrue(serverMeta.contains(new MetaData("Person", "abc-ght", 123)));
        assertTrue(serverMeta.size() == 1);

        List<MetaDataDeep> serverMetaDeep = server.sql.getMetaDeepFromMetaTable("Person");
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-def", 0, 0, true)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(serverMetaDeep.size() == 3);
    }

    @Test
    public void HMultipleTablesA() throws Exception {
        System.out.println("-----");
        System.out.println("HMultipleTables()");

        String sql = "CREATE TABLE IF NOT EXISTS Device(id TEXT NOT NULL PRIMARY KEY, synctime INTEGER, name TEXT, type TEXT)";
        Statement clients = c1.createStatement();
        clients.execute(sql);
        client1.addSyncTable("Device", 1);

        TableCommits comC = client1.db.getCommits();
        assertTrue(comC.getCommits().containsKey("Device"));

        sql = "CREATE TABLE IF NOT EXISTS `Device`(id VARCHAR(255) NOT NULL PRIMARY KEY, synctime LONG, name TEXT, type TEXT)";
        Statement servers = s.createStatement();
        servers.execute(sql);
        server.addSyncTable("Device", 1);

        TableCommits comS = server.sql.getCommits();
        assertTrue(comS.getCommits().containsKey("Device"));

    }

    @Test
    public void HMultipleTablesB() throws Exception {
        System.out.println("-----");
        System.out.println("HMultipleTables()");

        Statement s = c1.createStatement();
        s.execute("INSERT INTO Device VALUES ('123', 12579, 'lenovo thinkpad t420s', 'Laptop');");
        client1.sync();
        sleep();

        List<MetaData> serverMetaPerson = server.sql.getMetaFromDataTable("Person");
        assertTrue(serverMetaPerson.contains(new MetaData("Person", "abc-ght", 123)));
        assertTrue(serverMetaPerson.size() == 1);

        List<MetaDataDeep> serverMetaDeep = server.sql.getMetaDeepFromMetaTable("Person");
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-def", 0, 0, true)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(serverMetaDeep.size() == 3);

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Device");
        System.out.println("serverMeta: " + serverMeta);
        assertTrue(serverMeta.contains(new MetaData("Device", "123", 12579)));
        assertTrue(serverMeta.size() == 1);

    }

    @Test
    public void Incremental1() throws Exception {
        System.out.println("-----");
        System.out.println("Incremental1()");

        client1.useIncremental(true);

        Statement s = c1.createStatement();
        s.execute("INSERT INTO Device VALUES ('444', 56, 'lenovo thinkpad t410', 'Laptop');");
        client1.sync();
        sleep();

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Device");
        assertTrue(serverMeta.contains(new MetaData("Device", "123", 12579)));
        assertTrue(serverMeta.contains(new MetaData("Device", "444", 56)));
        assertTrue(serverMeta.size() == 2);

        List<MetaData> serverMetaPerson = server.sql.getMetaFromDataTable("Person");
        assertTrue(serverMetaPerson.contains(new MetaData("Person", "abc-ght", 123)));
        assertTrue(serverMetaPerson.size() == 1);

        List<MetaDataDeep> serverMetaDeep = server.sql.getMetaDeepFromMetaTable("Person");
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-def", 0, 0, true)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(serverMetaDeep.size() == 3);

    }

    @Test
    public void Incremental2() throws Exception {
        System.out.println("-----");
        System.out.println("Incremental2()");

        Statement s = c1.createStatement();
        s.execute("INSERT INTO Device VALUES ('668', 1, 'abc', 'Laptop');");
        s.execute("UPDATE Device SET synctime = 57, name = 'lenovo thinkpad t410i' WHERE id = '444';");
        client1.sync();
        sleep();

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Device");
        assertTrue(serverMeta.contains(new MetaData("Device", "123", 12579)));
        assertTrue(serverMeta.contains(new MetaData("Device", "444", 57)));
        assertTrue(serverMeta.contains(new MetaData("Device", "668", 1)));
        assertTrue(serverMeta.size() == 3);

        List<MetaData> serverMetaPerson = server.sql.getMetaFromDataTable("Person");
        assertTrue(serverMetaPerson.contains(new MetaData("Person", "abc-ght", 123)));
        assertTrue(serverMetaPerson.size() == 1);

        List<MetaDataDeep> serverMetaDeep = server.sql.getMetaDeepFromMetaTable("Person");
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-def", 0, 0, true)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(serverMetaDeep.size() == 3);

    }

    @Test
    public void Incremental3() throws Exception {
        System.out.println("-----");
        System.out.println("Incremental3()");

        Statement s = c1.createStatement();
        s.execute("DELETE FROM Device WHERE id = '668';");
        client1.sync();
        sleep();

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Device");
        assertTrue(serverMeta.contains(new MetaData("Device", "123", 12579)));
        assertTrue(serverMeta.contains(new MetaData("Device", "444", 57)));
        assertTrue(serverMeta.size() == 2);

        List<MetaData> serverMetaPerson = server.sql.getMetaFromDataTable("Person");
        assertTrue(serverMetaPerson.contains(new MetaData("Person", "abc-ght", 123)));
        assertTrue(serverMetaPerson.size() == 1);

        List<MetaDataDeep> serverMetaDeep = server.sql.getMetaDeepFromMetaTable("Person");
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-def", 0, 0, true)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(serverMetaDeep.size() == 3);

    }

    @Test
    public void JAMultipleDevices() throws Exception {
        System.out.println("-----");
        System.out.println("JAMultipleDevices()");

        Statement s = c2.createStatement();

        s.execute("CREATE TABLE IF NOT EXISTS Person(id TEXT NOT NULL PRIMARY KEY, synctime INTEGER, name TEXT)");
        client2.addSyncTable("Person", 1);

        s.execute("CREATE TABLE IF NOT EXISTS Device(id TEXT NOT NULL PRIMARY KEY, synctime INTEGER, name TEXT, type TEXT)");
        client2.addSyncTable("Device", 1);

        client2.connect();
        client2.logIn("test", "test");
        client2.sync();
        sleep();

        List<MetaData> serverMeta = client2.db.getMetaFromDataTable("Device");
        assertTrue(serverMeta.contains(new MetaData("Device", "123", 12579)));
        assertTrue(serverMeta.contains(new MetaData("Device", "444", 57)));
        assertTrue(serverMeta.size() == 2);

        List<MetaData> serverMetaPerson = client2.db.getMetaFromDataTable("Person");
        assertTrue(serverMetaPerson.contains(new MetaData("Person", "abc-ght", 123)));
        assertTrue(serverMetaPerson.size() == 1);

        List<MetaDataDeep> serverMetaDeep = client2.db.getMetaDeepFromMetaTable("Person");
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-ght", 123, 0, false)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-def", 0, 0, true)));
        assertTrue(serverMetaDeep.contains(new MetaDataDeep("Person", "abc-xyz", 0, 0, true)));
        assertTrue(serverMetaDeep.size() == 3);
    }

    @Test
    public void JBMultipleDevices() throws Exception {
        System.out.println("-----");
        System.out.println("JBMultipleDevices()");

        Statement s = c1.createStatement();
        s.execute("INSERT INTO Device VALUES ('468', 2, 'S3', 'Handy');");
        client1.sync();
        sleep();
        sleep();

        List<MetaData> serverMeta = client2.db.getMetaFromDataTable("Device");
        assertTrue(serverMeta.contains(new MetaData("Device", "123", 12579)));
        assertTrue(serverMeta.contains(new MetaData("Device", "444", 57)));
        assertTrue(serverMeta.contains(new MetaData("Device", "468", 2)));
        assertTrue(serverMeta.size() == 3);
    }

    Boolean synced = false;

    @Test
    public void KSyncListener() throws Exception {
        System.out.println("-----");
        System.out.println("KSyncListener()");

        client2.addSyncListener((int from, boolean pass) -> {
            synced = true;
        });

        Statement s = c1.createStatement();
        s.execute("INSERT INTO Device VALUES ('456789', 75, 'S3 mini', 'Handy');");
        client1.sync();
        sleep();

        assertTrue(synced);
    }

    @Test
    public void LAForeignKey() throws Exception {
        System.out.println("-----");
        System.out.println("LAForeignKey()");

        String sql = "CREATE TABLE IF NOT EXISTS Gehoert("
                + "id TEXT NOT NULL PRIMARY KEY, "
                + "synctime INTEGER, "
                + "device TEXT, "
                + "person TEXT, "
                + "FOREIGN KEY(device) REFERENCES Device(id), "
                + "FOREIGN KEY(person) REFERENCES Person(id)"
                + ");";

        Statement st;

        st = c1.createStatement();
        st.execute(sql);
        client1.addSyncTable("Gehoert", 2);

        st = c2.createStatement();
        st.execute(sql);
        client2.addSyncTable("Gehoert", 2);

        sql = "CREATE TABLE IF NOT EXISTS `Gehoert`("
                + "id VARCHAR(255) NOT NULL PRIMARY KEY, "
                + "synctime LONG, "
                + "device VARCHAR(255), "
                + "person VARCHAR(255), "
                + "FOREIGN KEY(device) REFERENCES Device(id), "
                + "FOREIGN KEY(person) REFERENCES Person(id)"
                + ");";

        st = s.createStatement();
        st.execute(sql);
        server.addSyncTable("Gehoert", 2);
    }

    @Test
    public void LBForeignKey() throws Exception {
        System.out.println("-----");
        System.out.println("LAForeignKey()");

        Statement s = c1.createStatement();
        s.execute("INSERT INTO Gehoert VALUES ('1', 1, '456789', 'abc-ght');");
        client1.sync();
        sleep();

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Gehoert");
        assertTrue(serverMeta.contains(new MetaData("Gehoert", "1", 1)));
        assertTrue(serverMeta.size() == 1);

    }

    @Test
    public void LCForeignKey() throws Exception {
        System.out.println("-----");
        System.out.println("LAForeignKey()");

        Statement s = c1.createStatement();
        s.execute("INSERT INTO Device VALUES ('f43t5', 2, 'S3Neo', 'Handy');");
        s.execute("INSERT INTO Person VALUES ('ghz-ujs', 8, 'Doris');");
        s.execute("INSERT INTO Gehoert VALUES ('2', 1, 'f43t5', 'ghz-ujs');");
        client1.sync();
        sleep();

        List<MetaData> serverMeta = server.sql.getMetaFromDataTable("Gehoert");
        assertTrue(serverMeta.contains(new MetaData("Gehoert", "1", 1)));
        assertTrue(serverMeta.contains(new MetaData("Gehoert", "2", 1)));
        assertTrue(serverMeta.size() == 2);

    }

}
