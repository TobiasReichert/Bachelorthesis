INSERT INTO '_.LogIn._' VALUES ('test','test');
CREATE TABLE IF NOT EXISTS Note(id TEXT NOT NULL PRIMARY KEY, synctime INTEGER, title TEXT, text TEXT);
INSERT INTO '_.Sync._'(tableName, lastCommit) VALUES ('Note', 0);
