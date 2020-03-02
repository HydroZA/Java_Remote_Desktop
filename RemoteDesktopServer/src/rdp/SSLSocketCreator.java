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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;

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
    public SSLSocketCreator()
    {
        
    }
    public SSLServerSocket getSecureServerSocket(InetAddress ip, int port, String tlsVersion, String trustStoreName,
            char[] trustStorePassword, String keyStoreName, char[] keyStorePassword)
            throws Exception
    {

        Objects.requireNonNull(tlsVersion, "TLS version is mandatory");
        
        if (port <= 0)
        {
            throw new IllegalArgumentException(
                    "Port number cannot be less than or equal to 0");
        }

        File ts = new File("certs/" + trustStoreName + ".jks");
        if (!ts.exists())
        {
            throw new FileNotFoundException("File Not Found");
        }
        File ks = new File("certs/" + keyStoreName + ".jks");
        if (!ks.exists())
        {
            throw new FileNotFoundException("File Not Found");
        }

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream tstore =  new FileInputStream(ts);
        
        trustStore.load(tstore, trustStorePassword);
        tstore.close();
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
        
        
        //
        SSLServerSocket sslss = (SSLServerSocket) factory.createServerSocket(port, 50, ip);
        //
        
        sslss.setEnabledProtocols(new String[]
        {
            tlsVersion
        });
        sslss.setNeedClientAuth(true);
        
        return sslss;
    }
}
