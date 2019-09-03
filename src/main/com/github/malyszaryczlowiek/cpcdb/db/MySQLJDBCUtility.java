package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.Controllers.MainStageController;
import com.github.malyszaryczlowiek.cpcdb.Util.SecureProperties;

import java.sql.*;


public class MySQLJDBCUtility
{
    private static Connection CONNECTION = null;
    private static final String DBNAME = "Wa1s8JBvyU";

    public static Connection connectToRemoteDB()
    {
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(SecureProperties.getProperty("settings.db.remote.RDBMS")) // RDBMS - relational database management system
                .append("://")
                .append(SecureProperties.getProperty("settings.db.remote.serverAddressIP"))
                .append(":")
                .append(SecureProperties.getProperty("settings.db.remote.portNumber"))
                .append("/");

        /*

         */

        try
        {
            if (SecureProperties.hasProperty("remoteDBExists"))
            {
                urlBuilder.append(DBNAME);
                String dbConfiguration = SecureProperties.getProperty("settings.db.remote.serverConfiguration");
                if (!dbConfiguration.equals(""))
                    urlBuilder.append("?").append(dbConfiguration);

                CONNECTION = DriverManager.getConnection(
                        urlBuilder.toString(),
                        SecureProperties.getProperty("settings.db.remote.user"),
                        SecureProperties.getProperty("settings.db.remote.passphrase"));
            }
            else // if db does not exist, we must create it
            {
                CONNECTION = DriverManager.getConnection(
                        urlBuilder.toString(),
                        SecureProperties.getProperty("settings.db.remote.user"),
                        SecureProperties.getProperty("settings.db.remote.passphrase"));

                final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS " + DBNAME;
                PreparedStatement createDBifNotExist = CONNECTION.prepareStatement(databaseExistSQLQuery);
                createDBifNotExist.execute();

                CONNECTION.setCatalog(DBNAME);

                final String checkIfTableExistsInDBSqlQuery = "SELECT * " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = '" + DBNAME + "' " +
                        "AND table_name = 'compounds' " +
                        "LIMIT 10";

                PreparedStatement checkTableExists = CONNECTION.prepareStatement(checkIfTableExistsInDBSqlQuery);
                ResultSet rs = checkTableExists.executeQuery();
                if (!rs.last())
                {
                    final String sqlQueryCreateTable = "CREATE TABLE " + //"IF NOT EXISTS " +
                            "compounds(" +
                            "CompoundID INT NOT NULL AUTO_INCREMENT, " +
                            "Smiles VARCHAR(255) NOT NULL, " +
                            "CompoundNumber VARCHAR(255), " +
                            "Amount FLOAT, " +
                            "Unit VARCHAR(255) CHARACTER SET utf8, " +
                            "Form VARCHAR(255) CHARACTER SET utf8, " +
                            "Stability VARCHAR(255) CHARACTER SET utf8, " +
                            "Argon BOOLEAN, " +
                            "Container VARCHAR(255) CHARACTER SET utf8, " +
                            "StoragePlace VARCHAR(255) CHARACTER SET utf8, " +
                            "LastModification TIMESTAMP(0), " +
                            "AdditionalInfo TEXT CHARACTER SET utf8, " +
                            "PRIMARY KEY (CompoundID)" +
                            ")";

                    PreparedStatement createTable = CONNECTION.prepareStatement(sqlQueryCreateTable);
                    createTable.execute();
                    SecureProperties.setProperty("remoteDBExists", "true");
                }
            }
        }
        catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e)
        {
            //e.printStackTrace();
            MainStageController.setErrorConnectionToRemoteDBToTrue();
            int i;
            // TODO wysłać listenera, że nie ma connection do remote DB tylko jest do local
            // napisać też service<> ,który się uruchomi aby sprawdzać czy jest możliwe ponowne połączenie
            // z serverem jeśli jest to należy zmergować aktualne dane w tabeli z tymi w remote DB i
            //
            // pobrać główną bazę. na lokalną.
            return connectToLocalDB();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return CONNECTION;
    }

    private static void createChangesTableIfNotExists()
    {
        Connection connectionToLocalDB;
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(SecureProperties.getProperty("settings.db.local.RDBMS")) // RDBMS - relational database management system
                .append("://")
                .append("localhost")
                .append(":")
                .append("3306")
                .append("/");

        try
        {
            if (SecureProperties.hasProperty("localDBExists"))
                urlBuilder.append(DBNAME);
            String dbConfiguration = SecureProperties.getProperty("settings.db.local.serverConfiguration");
            if (!dbConfiguration.equals(""))
                urlBuilder.append("?").append(dbConfiguration);

            connectionToLocalDB = DriverManager.getConnection(
                    urlBuilder.toString(),
                    SecureProperties.getProperty("settings.db.local.user"),
                    SecureProperties.getProperty("settings.db.local.passphrase"));

            final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS " + DBNAME;
            PreparedStatement createDBifNotExist = connectionToLocalDB.prepareStatement(databaseExistSQLQuery);
            createDBifNotExist.execute();

            connectionToLocalDB.setCatalog(DBNAME);

            //  check if compound table exists in our DB
            final String checkIfTableExistsInDBSqlQuery = "SELECT * " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = '" + DBNAME + "' " +
                    "AND table_name = 'changes' " +
                    "LIMIT 10";

            PreparedStatement checkTableExists = connectionToLocalDB.prepareStatement(checkIfTableExistsInDBSqlQuery);
            ResultSet rs = checkTableExists.executeQuery();
            if (!rs.last())
            {
                final String sqlQueryCreateTable = "CREATE TABLE " + //"IF NOT EXISTS " +
                        "changes(" +
                        "CompoundID INT NOT NULL, " +
                        //"Smiles VARCHAR(255) NOT NULL, " +
                        //"CompoundNumber VARCHAR(255), " +
                        "Amount FLOAT, " +
                        //"Unit VARCHAR(255) CHARACTER SET utf8, " +
                        //"Form VARCHAR(255) CHARACTER SET utf8, " +
                        //"Stability VARCHAR(255) CHARACTER SET utf8, " +
                        //"Argon BOOLEAN, " +
                        //"Container VARCHAR(255) CHARACTER SET utf8, " +
                        //"StoragePlace VARCHAR(255) CHARACTER SET utf8, " +
                        //"LastModification TIMESTAMP(0), " +
                        //"AdditionalInfo TEXT CHARACTER SET utf8, " +
                        //"PRIMARY KEY (CompoundID)" +
                        ")";

                PreparedStatement createTable = connectionToLocalDB.prepareStatement(sqlQueryCreateTable);
                createTable.execute();
                SecureProperties.setProperty("changesTableExists", "true");
            }
        }
        catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e)
        {
            // TODO tu będize trzeba poinformować użytkownika, że nie można się połączyć
            System.out.println(" Local Mysql server is turn off. ");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        System.out.println("table 'changes' is created correctly.");
    }


    public static Connection getShortConnectionToRemoteDB()
    {
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(SecureProperties.getProperty("settings.db.remote.RDBMS")) // RDBMS - relational database management system
                .append("://")
                .append(SecureProperties.getProperty("settings.db.remote.serverAddressIP"))
                .append(":")
                .append(SecureProperties.getProperty("settings.db.remote.portNumber"))
                .append("/")
                .append(DBNAME);

        String dbConfiguration = SecureProperties.getProperty("settings.db.remote.serverConfiguration");
        if (!dbConfiguration.equals(""))
            urlBuilder.append("?")
                    .append(dbConfiguration);

        try
        {
            CONNECTION = DriverManager.getConnection(
                    urlBuilder.toString(),
                    SecureProperties.getProperty("settings.db.remote.user"),
                    SecureProperties.getProperty("settings.db.remote.passphrase"));
        }
        catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e)
        {
            e.printStackTrace();
            int i;
            // TODO wysłać listenera, że nie ma connection do remote DB tylko jest do local
            // napisać też service<> ,który się uruchomi aby sprawdzać czy jest możliwe ponowne połączenie
            // z serverem jeśli jest to należy zmergować aktualne dane w tabeli z tymi w remote DB i
            // pobrać główną bazę. na lokalną.
            return getShortConnectionToLocalDB();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return CONNECTION;
    }

    private static Connection connectToLocalDB()
    {
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(SecureProperties.getProperty("settings.db.local.RDBMS")) // RDBMS - relational database management system
                .append("://")
                .append("localhost")
                .append(":")
                .append("3306")
                .append("/");

        try
        {
            if (SecureProperties.hasProperty("localDBExists"))
            {
                urlBuilder.append(DBNAME);
                String dbConfiguration = SecureProperties.getProperty("settings.db.local.serverConfiguration");
                if (!dbConfiguration.equals(""))
                    urlBuilder.append("?").append(dbConfiguration);

                CONNECTION = DriverManager.getConnection(
                        urlBuilder.toString(),
                        SecureProperties.getProperty("settings.db.local.user"),
                        SecureProperties.getProperty("settings.db.local.passphrase"));
            }
            else // if db does not exist, we must create it
            {
                String dbConfiguration = SecureProperties.getProperty("settings.db.local.serverConfiguration");
                if (!dbConfiguration.equals(""))
                    urlBuilder.append("?").append(dbConfiguration);

                CONNECTION = DriverManager.getConnection(
                        urlBuilder.toString(),
                        SecureProperties.getProperty("settings.db.local.user"),
                        SecureProperties.getProperty("settings.db.local.passphrase"));

                final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS " + DBNAME;
                PreparedStatement createDBifNotExist = CONNECTION.prepareStatement(databaseExistSQLQuery);
                createDBifNotExist.execute();

                CONNECTION.setCatalog(DBNAME);

                //  check if compound table exists in our DB
                final String checkIfTableExistsInDBSqlQuery = "SELECT * " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = '" + DBNAME + "' " +
                        "AND table_name = 'compounds' " +
                        "LIMIT 10";

                PreparedStatement checkTableExists = CONNECTION.prepareStatement(checkIfTableExistsInDBSqlQuery);
                ResultSet rs = checkTableExists.executeQuery();
                if (!rs.last())
                {
                    final String sqlQueryCreateTable = "CREATE TABLE " + //"IF NOT EXISTS " +
                            "compounds(" +
                            "CompoundID INT NOT NULL AUTO_INCREMENT, " +
                            "Smiles VARCHAR(255) NOT NULL, " +
                            "CompoundNumber VARCHAR(255), " +
                            "Amount FLOAT, " +
                            "Unit VARCHAR(255) CHARACTER SET utf8, " +
                            "Form VARCHAR(255) CHARACTER SET utf8, " +
                            "Stability VARCHAR(255) CHARACTER SET utf8, " +
                            "Argon BOOLEAN, " +
                            "Container VARCHAR(255) CHARACTER SET utf8, " +
                            "StoragePlace VARCHAR(255) CHARACTER SET utf8, " +
                            "LastModification TIMESTAMP(0), " +
                            "AdditionalInfo TEXT CHARACTER SET utf8, " +
                            "PRIMARY KEY (CompoundID)" +
                            ")";

                    PreparedStatement createTable = CONNECTION.prepareStatement(sqlQueryCreateTable);
                    createTable.execute();
                    SecureProperties.setProperty("localDBExists", "true");
                }
            }
        }
        catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e)
        {
            return null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        System.out.println("poprawnie stworzono bazę danych i lokalną tablę compounds");
        return CONNECTION;
    }

    private static Connection getShortConnectionToLocalDB()
    {
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append(SecureProperties.getProperty("settings.db.local.RDBMS")) // RDBMS - relational database management system
                .append("://")
                .append("localhost")
                .append(":")
                .append("3306")
                .append("/")
                .append(DBNAME);

        String dbConfiguration = SecureProperties.getProperty("settings.db.local.serverConfiguration");
        if (!dbConfiguration.equals(""))
            urlBuilder.append("?").append(dbConfiguration);

        try
        {
            CONNECTION = DriverManager.getConnection(
                    urlBuilder.toString(),
                    SecureProperties.getProperty("settings.db.local.user"),
                    SecureProperties.getProperty("settings.db.local.passphrase"));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return CONNECTION;
    }
}


/*
Relevant notes;
full url:
jdbc:mysql://localhost:3306/?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw

solution above is preferred according mySQL documentation
final String useCPCDB = "USE " + DBNAME
PreparedStatement useCPCDBSqlQuery = CONNECTION.prepareStatement(useCPCDB);
useCPCDBSqlQuery.execute();

better use:
CONNECTION.setCatalog( DBNAME );



 */


































