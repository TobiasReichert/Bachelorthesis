#!/bin/bash
rm server-notes.db
java -jar ../../MDBSServer/dist/MDBSServer.jar server-notes.db sqlite.sql #sql.sql
 
