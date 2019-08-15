package com.github.malyszaryczlowiek.cpcdb.db;

import com.github.malyszaryczlowiek.cpcdb.Util.SecureProperties;

import java.sql.*;

/*
## List of used properties
settings.db.remote.serverAddressIP
settings.db.remote.portNumber
settings.db.remote.user
settings.db.remote.passphrase

settings.db.local.user
settings.db.local.passphrase

 */

public class MySQLJDBCUtility
{
    private static Connection CONNECTION = null;
    private static final String DBNAME = "Wa1s8JBvyU";

    public static Connection connectToRemoteDB()
    {
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append( SecureProperties.getProperty("settings.db.remote.RDBMS") ) // RDBMS - relational database management system
                .append( "://" )
                .append( SecureProperties.getProperty("settings.db.remote.serverAddressIP") )
                .append( ":" )
                .append( SecureProperties.getProperty("settings.db.remote.portNumber") )
                .append( "/" );

        try
        {
            if ( SecureProperties.hasProperty("remoteDBExists") )
            {
                urlBuilder.append( DBNAME );
                String dbConfiguration = SecureProperties.getProperty("settings.db.remote.serverConfiguration");
                if ( !dbConfiguration.equals("") )
                    urlBuilder.append("?").append( dbConfiguration );

                CONNECTION = DriverManager.getConnection(
                        urlBuilder.toString(),
                        SecureProperties.getProperty("settings.db.remote.user"),
                        SecureProperties.getProperty("settings.db.remote.passphrase") );
            }
            else // if db does not exist, we must create it
            {
                CONNECTION = DriverManager.getConnection(
                        urlBuilder.toString(),
                        SecureProperties.getProperty("settings.db.remote.user"),
                        SecureProperties.getProperty("settings.db.remote.passphrase") );

                final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS " + DBNAME;
                PreparedStatement createDBifNotExist = CONNECTION.prepareStatement(databaseExistSQLQuery);
                createDBifNotExist.execute();

                CONNECTION.setCatalog( DBNAME );

                final String checkIfTableExistsInDBSqlQuery = "SELECT * " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = '" + DBNAME + "' " +
                        "AND table_name = 'compounds' " +
                        "LIMIT 10";

                PreparedStatement checkTableExists = CONNECTION.prepareStatement( checkIfTableExistsInDBSqlQuery );
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

                    PreparedStatement createTable = CONNECTION.prepareStatement( sqlQueryCreateTable );
                    createTable.execute();
                    SecureProperties.setProperty("remoteDBExists", "true");
                }
            }
        }
        catch (com.mysql.cj.jdbc.exceptions.CommunicationsException e)
        {
            e.printStackTrace();
            return connectToLocalDB();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return CONNECTION;
    }


    public static Connection getShortConnectionToRemoteDB()
    {
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append( SecureProperties.getProperty("settings.db.remote.RDBMS") ) // RDBMS - relational database management system
                .append( "://" )
                .append( SecureProperties.getProperty("settings.db.remote.serverAddressIP") )
                .append( ":" )
                .append( SecureProperties.getProperty("settings.db.remote.portNumber") )
                .append( "/" )
                .append( DBNAME );

        String dbConfiguration = SecureProperties.getProperty("settings.db.remote.serverConfiguration");
        if ( !dbConfiguration.equals("") )
            urlBuilder.append("?").append( dbConfiguration );

        try
        {
            CONNECTION = DriverManager.getConnection(
                    urlBuilder.toString(),
                    SecureProperties.getProperty("settings.db.remote.user"),
                    SecureProperties.getProperty("settings.db.remote.passphrase") );
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
                .append( SecureProperties.getProperty("settings.db.local.RDBMS") ) // RDBMS - relational database management system
                .append( "://" )
                .append( "localhost" )
                .append( ":" )
                .append( "3306" )
                .append( "/" );

        try
        {
            if ( SecureProperties.hasProperty("localDBExists") )
            {
                urlBuilder.append( DBNAME );
                String dbConfiguration = SecureProperties.getProperty("settings.db.local.serverConfiguration");
                if ( !dbConfiguration.equals("") )
                    urlBuilder.append("?").append( dbConfiguration );

                CONNECTION = DriverManager.getConnection(
                        urlBuilder.toString(),
                        SecureProperties.getProperty("settings.db.local.user"),
                        SecureProperties.getProperty("settings.db.local.passphrase") );
            }
            else // if db does not exist, we must create it
            {
                CONNECTION = DriverManager.getConnection(
                        urlBuilder.toString(),
                        SecureProperties.getProperty("settings.db.local.user"),
                        SecureProperties.getProperty("settings.db.local.passphrase") );

                final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS " + DBNAME;
                PreparedStatement createDBifNotExist = CONNECTION.prepareStatement(databaseExistSQLQuery);
                createDBifNotExist.execute();

                CONNECTION.setCatalog( DBNAME );

                //  check if compound table exists in our DB
                final String checkIfTableExistsInDBSqlQuery = "SELECT * " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = '" + DBNAME + "' " +
                        "AND table_name = 'compounds' " +
                        "LIMIT 10";

                PreparedStatement checkTableExists = CONNECTION.prepareStatement( checkIfTableExistsInDBSqlQuery );
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

                    PreparedStatement createTable = CONNECTION.prepareStatement( sqlQueryCreateTable );
                    createTable.execute();
                    SecureProperties.setProperty("localDBExists", "true");
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return CONNECTION;
    }

    static Connection getShortConnectionToLocalDB()
    {
        StringBuilder urlBuilder = new StringBuilder("jdbc:")
                .append( SecureProperties.getProperty("settings.db.local.RDBMS") ) // RDBMS - relational database management system
                .append( "://" )
                .append( "localhost" )
                .append( ":" )
                .append( "3306" )
                .append( "/" )
                .append( DBNAME );

        String dbConfiguration = SecureProperties.getProperty("settings.db.local.serverConfiguration");
        if ( !dbConfiguration.equals("") )
            urlBuilder.append("?").append( dbConfiguration );

        try
        {
            CONNECTION = DriverManager.getConnection(
                    urlBuilder.toString(),
                    SecureProperties.getProperty("settings.db.local.user"),
                    SecureProperties.getProperty("settings.db.local.passphrase") );
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


































