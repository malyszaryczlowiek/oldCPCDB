package com.github.malyszaryczlowiek.cpcdb.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class MySQLJDBCUtility
{
    private static Connection connection = null;

    public static Connection getConnection()
    {
        try (FileInputStream propertiesStream = new FileInputStream("config.properties"))
        {
            Properties properties = new Properties();
            properties.load(propertiesStream);

            final String URL = properties.getProperty("url");
            final String NAME = properties.getProperty("user");
            final String PASS =properties.getProperty("password");

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

    public static Connection getShortConnection()
    {
        try (FileInputStream propertiesStream = new FileInputStream("config.properties"))
        {
            Properties properties = new Properties();
            properties.load(propertiesStream);

            final String URL = properties.getProperty("url");
            final String NAME = properties.getProperty("user");
            final String PASS =properties.getProperty("password");

            // utwórz połączenie
            connection = DriverManager.getConnection(URL, NAME, PASS);

            final String useCPCDB = "USE cpcdb";
            PreparedStatement useCPCDBSqlQuery = connection.prepareStatement(useCPCDB);
            useCPCDBSqlQuery.execute();
        }
        catch (SQLException | IOException e)
        {
            e.printStackTrace();
        }

        return connection;
    }
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