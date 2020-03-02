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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/*
  keytool -genkeypair -alias server -keyalg EC -sigalg SHA384withECDSA -keysize 256 -keystore servercert.p12 -storetype pkcs12 -v -storepass abc123 -validity 10000 -ext san=ip:127.0.0.1


 * -sigalg SHA384withECDSA -keysize 256 -keystore servercert.p12 \
 * -storetype pkcs12 -v -storepass abc123 -validity 10000 -ext san=ip:127.0.0.1
 */
public class SSLServerSocketCreator
{
    public SSLServerSocketCreator()
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

        File f = new File("certs/localhost-servercert.p12");
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
