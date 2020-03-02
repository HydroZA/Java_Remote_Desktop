/*
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import static rdp.CertificateHandler.Type.SERVER;

/**
 * FUNCTIONS: 1. Takes in Strings representing the names of the certificate
 * files 2. If the certificate files exist, the File fields are simply assigned
 * the value of these files 3. If they don't exist then the files are created
 * and the File fields are populated with the generated files 4. Able to import
 * the certificate of the opposing party, if server -> client.cer, if client ->
 * server.cer
 */
public class CertificateHandler
{

    private final Type type;
    private File certificate, keystore, truststore;
    private final String SERVER_IP, TRUST_STORE_NAME, TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD, CERTIFICATE, ALPHA_NUMERIC_STRING;

    /**
     *
     * @param type Enum constant defining if the certificate is to be generated
     * for the CLIENT or SERVER
     * @param SERVER_IP String representing the Subject Alternative Name (SAN)
     * to define in the certificate
     * @param TRUST_STORE_NAME Name of the trust store, as defined in the config
     * file
     * @param TRUST_STORE_PWD Password of the trust store, as defined in the
     * config file
     * @param KEY_STORE_NAME Name of the keystore
     * @param KEY_STORE_PWD Password of the keystore
     * @param CERTIFICATE Local Certificate Name
     */
    public CertificateHandler(Type type, String SERVER_IP, String TRUST_STORE_NAME, char[] TRUST_STORE_PWD, String KEY_STORE_NAME, char[] KEY_STORE_PWD, String CERTIFICATE)
    {
        this.ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        this.type = type;
        this.SERVER_IP = SERVER_IP;
        this.TRUST_STORE_NAME = TRUST_STORE_NAME;
        this.TRUST_STORE_PWD = String.valueOf(TRUST_STORE_PWD);
        this.KEY_STORE_NAME = KEY_STORE_NAME;
        this.KEY_STORE_PWD = String.valueOf(KEY_STORE_PWD);
        this.CERTIFICATE = CERTIFICATE;
    }

    protected enum Type
    {
        CLIENT,
        SERVER
    }

    private String randomAlphaNumeric(int count)
    {
        StringBuilder builder = new StringBuilder();

        while (count-- != 0)
        {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }

        return builder.toString();
    }

    public void setCertificate(File certificate)
    {
        this.certificate = certificate;
    }

    public void setKeystore(File keystore)
    {
        this.keystore = keystore;
    }

    public void setTrustStore(File cacerts)
    {
        this.truststore = cacerts;
    }

    public File getCertificate()
    {
        return certificate;
    }

    public File getTrustStore()
    {
        return truststore;
    }

    public File getKeystore()
    {
        return keystore;
    }

    private void execute(String command) throws Exception
    {
        String[] options = command.trim().split("\\s+");
        sun.security.tools.keytool.Main.main(options);
    }

    /**
     *
     * @param certificate The certificate to import
     * @return File object referencing the certificate that has been written to
     * disk
     * @throws java.io.IOException
     */
    public File writeCertificateToDisk(byte[] certificate) throws IOException
    {
        String filename = randomAlphaNumeric(5);
        File certFile = new File("certs/" + filename + ".cer");
        FileUtils.writeByteArrayToFile(certFile, certificate);

        return certFile;
    }

    public void importCertificate(File certificate) throws Exception
    // Import certificate to our trusted certificates keystore
    {
        String command = " -import "
                + " -v "
                + " -noprompt "
                + " -trustcacerts "
                + " -alias " + certificate.getName() + " "
                + " -file certs/" + certificate.getName() + " "
                + " -keystore certs/" + TRUST_STORE_NAME + ".jks "
                + " -keypass " + TRUST_STORE_PWD + " "
                + " -storepass " + TRUST_STORE_PWD + " ";
        execute(command);
        
        // Restart SSLSocketCreator
        if (type == SERVER)
        {
            
        }
    }

    public CertificateHandler generate() throws Exception
    {
        switch (type)
        {
            case SERVER:
            {
                // Generate Keystore
                String command = " -genkey "
                        + " -alias server "
                        + " -keyalg RSA "
                        + " -keypass " + KEY_STORE_PWD + " "
                        + " -keystore certs/" + KEY_STORE_NAME + ".jks "
                        + " -storepass " + KEY_STORE_PWD + " "
                        + " -ext san=ip:" + SERVER_IP;
                execute(command);

                // Extract Certificate
                command = " -export "
                        + " -alias server "
                        + " -storepass " + KEY_STORE_PWD + " "
                        + " -file certs/" + CERTIFICATE + ".cer"
                        + " -keystore certs/" + KEY_STORE_NAME + ".jks ";
                execute(command);

                // Import our certificate into our trusted certificates keystore
                command = " -import "
                        + " -v "
                        + " -noprompt "
                        + " -trustcacerts "
                        + " -alias server "
                        + " -file certs/" + CERTIFICATE + ".cer"
                        + " -keystore certs/" + TRUST_STORE_NAME + ".jks "
                        + " -keypass " + TRUST_STORE_PWD + " "
                        + " -storepass " + TRUST_STORE_PWD + " ";
                execute(command);

                keystore = new File("certs/" + KEY_STORE_NAME + ".jks");
                certificate = new File("certs/" + CERTIFICATE + ".cer");
                truststore = new File("certs/" + TRUST_STORE_NAME + ".jks");
                break;
            }
            case CLIENT:
            {
                // Generate Keystore
                String command = " -genkey "
                        + " -alias client "
                        + " -keyalg RSA "
                        + " -keypass " + KEY_STORE_PWD + " "
                        + " -keystore certs/" + KEY_STORE_NAME + ".jks "
                        + " -storepass " + KEY_STORE_PWD + " "
                        + " -ext san=ip:" + SERVER_IP;
                execute(command);

                // Extract Certificate
                command = " -export "
                        + " -alias client "
                        + " -storepass " + KEY_STORE_PWD + " "
                        + " -file certs/" + CERTIFICATE + ".cer"
                        + " -keystore certs/" + KEY_STORE_NAME + ".jks ";
                execute(command);

                // Import our certificate into our trusted certificates keystore
                command = " -import "
                        + " -v "
                        + " -noprompt "
                        + " -trustcacerts "
                        + " -alias client "
                        + " -file certs/" + CERTIFICATE + ".cer"
                        + " -keystore certs/" + TRUST_STORE_NAME + ".jks "
                        + " -keypass " + TRUST_STORE_PWD + " "
                        + " -storepass " + TRUST_STORE_PWD + " ";
                execute(command);

                keystore = new File("certs/" + KEY_STORE_NAME + ".jks");
                certificate = new File("certs/" + CERTIFICATE + ".cer");
                truststore = new File("certs/" + TRUST_STORE_NAME + ".jks");
                break;
            }
        }
        return this;
    }
}
