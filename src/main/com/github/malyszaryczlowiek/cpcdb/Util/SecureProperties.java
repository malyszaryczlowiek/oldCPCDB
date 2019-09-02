package com.github.malyszaryczlowiek.cpcdb.Util;


import javax.crypto.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SecureProperties
{
    // TODO zanleźć mapy bezpieczne wielowątkowo. tak aby dało się w nich umieszczać dane jednocześnie
    //private static Map<String, byte[]> mapOfProperties = new TreeMap<>();
    private static ConcurrentMap<String, byte[]> mapOfProperties = new ConcurrentHashMap<>(30);
    private static Map<String, String> mapOfPropertiesWhenChangingKey = new TreeMap<>();
    private static byte[] loadedByteProperties;

    private static final char[] pwd = "l;askg;lawgh;ajnv;ar".toCharArray(); //  keystore passphrase
    private static final File keyStoreFile = new File("keyStore.jks"); // keystore file
    private static final File propertiesFile = new File("propertiesFile"); // properties binary file
    private static KeyStore keyStore; // keystore object

    /**
     * This static constructor loads key and generate cipher to encode and decode properties
     * if true properties are loaded if not are not.
     */
    public static void loadProperties()
    {
        try
        {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            if ( keyStoreFile.exists() )
            {
                try ( InputStream keyStoreStream = new FileInputStream(keyStoreFile) )
                {
                    keyStore.load(keyStoreStream, pwd);
                    System.out.println("KeyStore loaded from keyStoreFile.");
                }
            }
            else
            {
                keyStore.load(null, pwd);

                KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                int keyBitSize = 256;
                SecureRandom secureRandom = new SecureRandom();
                keyGenerator.init( keyBitSize, secureRandom );

                SecretKey secretKey = keyGenerator.generateKey();
                KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry( secretKey );

                KeyStore.ProtectionParameter protectionParameter =
                        new KeyStore.PasswordProtection(
                                "some7Simple5Passphrase3To2Get1PropertiesKey".toCharArray()
                        );

                keyStore.setEntry("propertiesEncryptionDecryptionKey", secretKeyEntry, protectionParameter );

                try ( FileOutputStream fileOutputStream = new FileOutputStream(keyStoreFile) )
                {
                    keyStore.store( fileOutputStream, pwd );
                    System.out.println("KeyStore saved to keyStoreFile.");
                }
                setProperty("keyStoreFileExists", "true");

                LocalDate today = LocalDate.now();
                String todayString = today.toString();
                setProperty("settings.keyDateValidity", todayString);
                setProperty("settings.keyValidityDuration", "always");
            }
        }
        catch ( KeyStoreException
                | IOException
                | NoSuchAlgorithmException
                | CertificateException e)
        {
            e.printStackTrace();
        }

        if ( propertiesFile.exists() )
        {
            try (FileInputStream fileInputStream = new FileInputStream("propertiesFile"))
            {
                loadedByteProperties = fileInputStream.readAllBytes();
                refactorLoadedBytesToMap();
                System.out.println("binary properties file loaded");
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    public static void saveProperties()
    {
        if ( propertiesFile.delete() )
            System.out.println("Properties file has been deleted.");
        else
            System.out.println("Cannot delete properties file. Probably there is no access to file.");

        if ( checkInvalidityOfKey() )
        {
            refactorPropertiesToString();
            if ( generateNewKey() )
                refactorPropertiesToBinary();
        }

        try ( FileOutputStream fileOutputStream = new FileOutputStream("propertiesFile") )
        {
            mapOfProperties.forEach( (stringKey, binaryEncryptedValue) ->
            {
                byte[] encryptedProperty = encryptString(stringKey);
                try
                {
                    int keyLength = encryptedProperty.length;
                    int valueLength = binaryEncryptedValue.length;

                    byte[] binaryKeyLength = ByteBuffer.allocate( Integer.BYTES )
                            .putInt( keyLength ).array();
                    byte[] binaryValueLength = ByteBuffer.allocate( Integer.BYTES )
                            .putInt( valueLength ).array();

                    fileOutputStream.write( binaryKeyLength );
                    fileOutputStream.write( binaryValueLength );
                    fileOutputStream.write( encryptedProperty );
                    fileOutputStream.write( binaryEncryptedValue );
                }
                catch ( IOException e)
                {
                    e.printStackTrace();
                }

            });
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    /*
    public static void deleteKeyStoreFile()
    {
        if (keyStoreFile.delete())
            setProperty("keyStoreFileExists", "false");
    }
     */



    public static void setProperty(String propertyName, String propertyValue)
    {
        byte[] binaryPropertyValue = encryptString(propertyValue);
        if ( mapOfProperties.containsKey(propertyName) )
            mapOfProperties.replace(propertyName, binaryPropertyValue);
        else
            mapOfProperties.put(propertyName, binaryPropertyValue);
    }


    /**
     *
     * @param propertyName propery name to find in properties keyStoreFile
     * @return decoded string property
     */
    public static String getProperty(String propertyName)
    {
        if ( mapOfProperties.size() > 0)
            return new String(
                    decryptString(mapOfProperties.get(propertyName)),
                    StandardCharsets.UTF_8);
        else
            return "";
    }


    public static boolean hasProperty(String property)
    {
        return mapOfProperties.containsKey(property);
    }










    private static byte[] encryptString(String string)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES");
            Key key = keyStore.getKey("propertiesEncryptionDecryptionKey",
                    "some7Simple5Passphrase3To2Get1PropertiesKey".toCharArray() );
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = string.getBytes( StandardCharsets.UTF_8 );

            return cipher.doFinal(plainText);
        }
        catch ( NoSuchAlgorithmException
                | NoSuchPaddingException
                | KeyStoreException
                | UnrecoverableKeyException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e)
        {
            e.printStackTrace();
            return new byte[0];
        }
    }


    private static byte[] decryptString( byte[] encryptedString )
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES");
            Key key = keyStore.getKey("propertiesEncryptionDecryptionKey",
                    "some7Simple5Passphrase3To2Get1PropertiesKey".toCharArray() );
            cipher.init(Cipher.DECRYPT_MODE, key);

            return cipher.doFinal( encryptedString ); // returns decrypted text
        }
        catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | KeyStoreException
                | UnrecoverableKeyException
                | InvalidKeyException
                | IllegalBlockSizeException
                | BadPaddingException e)
        {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Function converts loaded bytes from propertiesFile and convert them
     * to decrypted strings of keys and encrypted bytes[] of values.
     * Saves them all subsequently in ConcurrentHashMap object.
     */
    private static void refactorLoadedBytesToMap()
    {
        int index = 0;
        int lengthOfKey, lengthOfValue;

        while (index < loadedByteProperties.length)
        {
            byte[] binaryLengthOfKey = ByteBuffer.allocate(Integer.BYTES)
                    .put(loadedByteProperties, index, Integer.BYTES).array();
            index += Integer.BYTES;
            lengthOfKey = convertByteArrayToInteger( binaryLengthOfKey );

            byte[] binaryLengthOfValue = ByteBuffer.allocate(Integer.BYTES)
                    .put(loadedByteProperties, index, Integer.BYTES).array();
            index += Integer.BYTES;
            lengthOfValue = convertByteArrayToInteger( binaryLengthOfValue );

            byte[] encryptedKey = ByteBuffer.allocate(lengthOfKey)
                    .put(loadedByteProperties, index, lengthOfKey).array();
            index += lengthOfKey;

            byte[] encryptedValue = ByteBuffer.allocate(lengthOfValue)
                    .put(loadedByteProperties, index , lengthOfValue).array();
            index += lengthOfValue;

            // now we decrypting key and set them to string
            byte[] decryptedKey = decryptString(encryptedKey);
            String decryptedStringKey = new String(decryptedKey, StandardCharsets.UTF_8);

            mapOfProperties.put(decryptedStringKey, encryptedValue);
            if ( index == loadedByteProperties.length )
                break;
        }
    }


    private static int convertByteArrayToInteger(byte[] integerByteArray)
    {
        return  (integerByteArray[0]<<24) & 0xff000000 |
                (integerByteArray[1]<<16) & 0x00ff0000 |
                (integerByteArray[2]<< 8) & 0x0000ff00 |
                (integerByteArray[3]) & 0x000000ff ; // integerByteArray[3]<< 0
    }


    // ***********************************
    // METHODS TO CHANGING KEY
    // ***********************************


    /**
     *
     * @return true if key is INVALID!!! and must be changed.
     */
    private static boolean checkInvalidityOfKey()
    {
        if ( "always".equals(getProperty("settings.keyValidityDuration") ))
        {
            return false;
        }
        else
        {
            LocalDate today = LocalDate.now();
            String validityDateString = getProperty("settings.keyDateValidity");
            LocalDate validityDate = LocalDate.parse(validityDateString);
            boolean invalid = today.isAfter(validityDate);
            System.out.println("key is INVALID: " + invalid);
            return invalid;
        }
    }


    /**
     * Function decrypts properties with old key and saved DECRYPTED values
     * to /mapOfPropertiesWhenChangingKey/ map.
     */
    private static void refactorPropertiesToString()
    {
        mapOfProperties.forEach( (String key, byte[] encryptedValue) ->
            mapOfPropertiesWhenChangingKey.put(key,
                    new String(decryptString(encryptedValue), StandardCharsets.UTF_8))
        );
    }


    /**
     * Function generates new key, replace old key with new one in keystore object,
     * delete old keystore file and create new one with new key.
     * @return true if key generated properly and saved to keystore file,
     * false if impossible to delete keystore file or some exception thrown.
     */
    private static boolean generateNewKey()
    {
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            int keyBitSize = 256;
            SecureRandom secureRandom = new SecureRandom();
            keyGenerator.init( keyBitSize, secureRandom );

            SecretKey secretKey = keyGenerator.generateKey();
            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry( secretKey );


            KeyStore.ProtectionParameter protectionParameter =
                    new KeyStore.PasswordProtection(
                            "some7Simple5Passphrase3To2Get1PropertiesKey".toCharArray()
                    );


            if ( keyStoreFile.delete() )
            {
                keyStore.setEntry("propertiesEncryptionDecryptionKey",
                        secretKeyEntry, protectionParameter );

                try ( FileOutputStream fileOutputStream = new FileOutputStream(keyStoreFile) )
                {
                    keyStore.store( fileOutputStream, pwd );
                    System.out.println("KeyStore saved to keyStoreFile.");
                }
            }
            else
                return false;

            return true;
        }
        catch (NoSuchAlgorithmException |
                KeyStoreException |
                IOException |
                CertificateException e)
        {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Function refactors properties from string (decrypted with old key)
     * to properties encrypted with new key.
     */
    private static void refactorPropertiesToBinary()
    {
        mapOfPropertiesWhenChangingKey.forEach( SecureProperties::setProperty );
        mapOfPropertiesWhenChangingKey = null;
        LocalDate newValidityDate = null;

        String validityDuration = getProperty("settings.keyValidityDuration");

        switch (validityDuration)
        {
            case "year":
                newValidityDate = LocalDate.now().plusYears(1);
                break;
            case "quarter":
                newValidityDate = LocalDate.now().plusMonths(3);
                break;
            case "month":
                newValidityDate = LocalDate.now().plusMonths(1);
                break;
            case "week":
                newValidityDate = LocalDate.now().plusWeeks(1);
                break;
            case "day":
                newValidityDate = LocalDate.now().plusDays(1);
                break;
            case "session":
                newValidityDate = LocalDate.now();
                break;
            default:
                break;
        }
        if ( newValidityDate != null)
            setProperty("settings.keyDateValidity", newValidityDate.toString() );
    }
}













/*
    public static byte[] getEncryptedProperty(String propertyName)
    {
        if ( mapOfProperties.size() > 0)
            return mapOfProperties.get(propertyName);
        else
            return null;
    }


    public static Set<String> getSetOfProperties()
    {
        return mapOfProperties.keySet();
    }
     */











