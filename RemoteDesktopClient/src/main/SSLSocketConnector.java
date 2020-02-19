// CREDIT TO Saptarshi Basu on StackOverflow for this
// https://stackoverflow.com/questions/53323855/sslserversocket-and-certificate-setup

package main;

import java.io.File;
import java.io.FileInputStream;
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

        if (serverPort <= 0)
        {
            throw new IllegalArgumentException(
                    "Server port cannot be lesss than or equal to 0");
        }
        
        File f = new File("certs/localhost-clientcert.p12");
        if (!f.exists())
        {
            throw new Exception("File Not Found");
        }

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream tstore =  new FileInputStream(f);
                            
        trustStore.load(tstore, trustStorePassword);
        tstore.close();
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream kstore = new FileInputStream(f);
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
