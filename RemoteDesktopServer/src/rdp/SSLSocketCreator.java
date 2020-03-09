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
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/*
  keytool -genkeypair -alias server -keyalg EC -sigalg SHA384withECDSA -keysize 256 -keystore servercert.p12 -storetype pkcs12 -v -storepass abc123 -validity 10000 -ext san=ip:127.0.0.1
 */
public class SSLSocketCreator
{

    /**
     * This class is used to return a SSLServerSocket based on the information
     * contained in a CertificateHandler object. It is in its own class for 
     * clarity sake.
     * @param ch CertificateHandler object containing all the information on
     * the certificate and keystore files
     * @return SSLServerSocket using the provided certificate and keystore
     * @throws Exception Many exceptions are thrown so generic Exception is used
     */
    public static SSLServerSocket getSecureServerSocket(CertificateHandler ch) throws Exception
    {
        final String tlsVersion = "TLSv1.2";
        final InetAddress ip = InetAddress.getByName(ch.getSERVER_IP());
        final int port = 1234;
        final File ts = ch.getTrustStore();
        final File ks = ch.getKeystore();
        final char[] trustStorePassword = ch.getTRUST_STORE_PWD().toCharArray();
        final char[] keyStorePassword = ch.getKEY_STORE_PWD().toCharArray();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream tstore = new FileInputStream(ts))
        {
            trustStore.load(tstore, trustStorePassword);
        }
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream kstore = new FileInputStream(ks);
        keyStore.load(kstore, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
                SecureRandom.getInstanceStrong());
        
        SSLServerSocketFactory factory = ctx.getServerSocketFactory();
        SSLServerSocket sslss = (SSLServerSocket) factory.createServerSocket(port, 50, ip);
        
        sslss.setEnabledProtocols(new String[]
        {
            tlsVersion
        });
        sslss.setNeedClientAuth(true);
        
        return sslss;
    }
}
