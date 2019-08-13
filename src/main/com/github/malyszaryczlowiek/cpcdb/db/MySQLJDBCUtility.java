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
    private static Connection connection = null;

    public static Connection getConnection()
    {
        try
        {
            // jdbc:mysql://localhost:3306/?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw
            //final String URL = properties.getProperty("url");
            final String NAME = SecureProperties.getProperty("userName");
            final String PASS = SecureProperties.getProperty("pass");

            String URL;

            if ( SecureProperties.hasProperty("cpcdbExists") )
            {
                URL = "jdbc:mysql://" + // jdbc:mysql://
                        SecureProperties.getProperty("serverIP") + ":" + //   localhost: albo 127.0.0.1
                        SecureProperties.getProperty("portNumber") + "/cpcdb" +  "?" +  // 3306 wcześniej było "/?" zamiast "?"
                        //"useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw";
                        SecureProperties.getProperty("serverConfigs"); // useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw

                connection = DriverManager.getConnection(URL, NAME, PASS);



            }
            else
            {
                URL = "jdbc:mysql://" + // jdbc:mysql://
                        SecureProperties.getProperty("serverIP") + ":" + //   localhost: albo 127.0.0.1
                        SecureProperties.getProperty("portNumber") + "/?" +  // 3306 wcześniej było "/?" zamiast "?"
                        //"useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw";
                        SecureProperties.getProperty("serverConfigs"); // useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw

                connection = DriverManager.getConnection(URL, NAME, PASS);

                final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS cpcdb";
                PreparedStatement createDBifNotExist = connection.prepareStatement(databaseExistSQLQuery);
                createDBifNotExist.execute();
                System.out.println("create database if not exists, done without error");

                connection.setCatalog("cpcdb");
                /*
                final String useCPCDB = "USE cpcdb";
                PreparedStatement useCPCDBSqlQuery = connection.prepareStatement(useCPCDB);
                useCPCDBSqlQuery.execute();
                System.out.println("use cpcdb");
                 */




                //  sprawdzam czy w bazie danych isnieje tabela compound
                final String tableExistsSqlQuery = "SELECT * " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema = 'cpcdb' " +
                        "AND table_name = 'compound' " +
                        "LIMIT 10";

                PreparedStatement checkTableExists = connection.prepareStatement(tableExistsSqlQuery);
                ResultSet rs = checkTableExists.executeQuery();
                if (!rs.last())
                {
                    final String sqlQueryCreateTable = "CREATE TABLE " + //"IF NOT EXISTS " +
                            "compound(" +
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

                    PreparedStatement createTable = connection.prepareStatement(sqlQueryCreateTable);
                    createTable.execute();
                    System.out.println("table created");
                }
                else
                    System.out.println("Table " + "compound " + " already exist.");
            }

            System.out.println("Connected to MySQL");

            SecureProperties.setProperty("cpcdbExists", "true");

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }


        return connection;
    }


    public static Connection getShortConnection()
    {
        try
        {
            final String NAME = SecureProperties.getProperty("userName");
            final String PASS = SecureProperties.getProperty("pass");

            final String URL= "jdbc:mysql://" + // jdbc:mysql://
                    SecureProperties.getProperty("serverIP") + ":" + //   localhost: albo 127.0.0.1
                    SecureProperties.getProperty("portNumber") + "/?" +  // 3306
                    //"useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw";
                    SecureProperties.getProperty("serverConfigs"); // useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw

            // utwórz połączenie
            connection = DriverManager.getConnection(URL, NAME, PASS);

            /*
            final String useCPCDB = "USE cpcdb";
            PreparedStatement useCPCDBSqlQuery = connection.prepareStatement(useCPCDB);
            useCPCDBSqlQuery.execute();
             */

            connection.setCatalog("cpcdb");

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return connection;
    }

    /*

    public static Connection getConnection2()
    {
        try (FileInputStream propertiesStream = new FileInputStream("DB.properties"))
        {
            Properties properties = new Properties();
            properties.load(propertiesStream);

            // jdbc:mysql://localhost:3306/?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw
            //final String URL = properties.getProperty("url");
            final String NAME = properties.getProperty("user");
            final String PASS = properties.getProperty("password");

            final String URL= properties.getProperty("connectorDBSystem") + "://" + // jdbc:mysql://
                    properties.getProperty("localServerIP") + ":" + //   localhost:
                    properties.getProperty("portNumber") + "/" +  // 3306
                    properties.getProperty("serverConfigs"); // ?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=Europe/Warsaw

            // utwórz połączenie
            connection = DriverManager.getConnection(URL, NAME, PASS);
            System.out.println("Connected to MySQL");

            final String databaseExistSQLQuery = "CREATE DATABASE IF NOT EXISTS cpcdb";
            PreparedStatement createDBifNotExist = connection.prepareStatement(databaseExistSQLQuery);
            createDBifNotExist.execute();
            System.out.println("create database if not exists, done without error");

            final String useCPCDB = "USE cpcdb";
            PreparedStatement useCPCDBSqlQuery = connection.prepareStatement(useCPCDB);
            useCPCDBSqlQuery.execute();
            System.out.println("use cpcdb");

            //  sprawdzam czy w bazie danych isnieje tabela compound
            final String tableExistsSqlQuery = "SELECT * " +
                    "FROM information_schema.tables " +
                    "WHERE table_schema = 'cpcdb' " +
                    "AND table_name = 'compound' " +
                    "LIMIT 10";

            PreparedStatement checkTableExists = connection.prepareStatement(tableExistsSqlQuery);
            ResultSet rs = checkTableExists.executeQuery();
            if (!rs.last())
            {
                final String sqlQueryCreateTable = "CREATE TABLE " + //"IF NOT EXISTS " +
                        "compound(" +
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

                PreparedStatement createTable = connection.prepareStatement(sqlQueryCreateTable);
                createTable.execute();
                System.out.println("table created");
            }
            else
                System.out.println("Table " + "compound " + " already exist.");
        }
        catch (SQLException | IOException e)
        {
            e.printStackTrace();
        }

        return connection;
    }
     */
}


/*
query do sprawdzenia, czy mamy w bazie danych myFirstSLQDatabase
tabelę o nazwie  compound

SELECT *
FROM information_schema.tables
WHERE table_schema = 'myFirstSLQDatabase'
AND table_name = 'compound'
LIMIT 1;
 */