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

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SSLSocketConnector
{
    public SSLSocketConnector()
    {
        
    }
    public SSLSocket connect(InetAddress serverHost, int serverPort,
            String tlsVersion, String trustStoreName, char[] trustStorePassword,
            String keyStoreName, char[] keyStorePassword) throws Exception
    {

        Objects.requireNonNull(tlsVersion, "TLS version is mandatory");
        Objects.requireNonNull(serverHost, "Server host cannot be null");

        trustStoreName = "certs/" + trustStoreName + ".jks";
        keyStoreName = "certs/" + keyStoreName + ".jks";
        
        if (serverPort <= 0)
        {
            throw new IllegalArgumentException(
                    "Server port cannot be lesss than or equal to 0");
        }

        File ts = new File(trustStoreName);
        if (!ts.exists())
        {
            throw new FileNotFoundException("File Not Found");
        }
        File ks = new File(keyStoreName);
        if (!ks.exists())
        {
            throw new FileNotFoundException("File Not Found");
        }

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
