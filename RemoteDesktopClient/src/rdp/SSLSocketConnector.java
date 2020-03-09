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
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SSLSocketConnector
{
    public static SSLSocket connect(CertificateHandler ch) throws Exception
    {
        final String tlsVersion = "TLSv1.2";
        final String serverHost = ch.getSERVER_IP();
        final int serverPort = 1234;
        final File ts =ch.getTrustStore();
        final File ks = ch.getKeystore();
        final char[] trustStorePassword = ch.getTRUST_STORE_PWD().toCharArray();
        final char[] keyStorePassword = ch.getKEY_STORE_PWD().toCharArray();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream tstore = new FileInputStream(ts);

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

        SocketFactory factory = ctx.getSocketFactory();
        
        SSLSocket s = (SSLSocket) factory.createSocket(serverHost, serverPort);
        s.setEnabledProtocols(new String[]
        {
            tlsVersion
        });
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm("HTTPS");
        s.setSSLParameters(sslParams);
        
        return s;
    } 
}
