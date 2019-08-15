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

public class SecureProperties
{
    private static Map<String, byte[]> mapOfProperties = new TreeMap<>();
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
    public static boolean loadProperties()
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

                LocalDate today = LocalDate.now();
                String todayString = today.toString();
                setProperty("keyDateValidity", todayString);
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
            return true;
        }
        else
            return false;
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

    public static boolean deleteKeyStoreFile()
    {
        return keyStoreFile.delete();
    }


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
                    .put(loadedByteProperties, index, lengthOfKey).array();// ByteBuffer.wrap(loadedByteProperties, index , lengthOfKey).array(); // Key must by encrypted and set to string and must be placed to map
            index += lengthOfKey;

            byte[] encryptedValue = ByteBuffer.allocate(lengthOfValue)
                    .put(loadedByteProperties, index , lengthOfValue).array(); // Value must stay encrypted and must be placed in map
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
        LocalDate today = LocalDate.now();
        String validityDateString = getProperty("keyDateValidity");
        LocalDate validityDate = LocalDate.parse(validityDateString);
        return today.isAfter(validityDate);
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
            {
                return false;
            }

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
    }
}












