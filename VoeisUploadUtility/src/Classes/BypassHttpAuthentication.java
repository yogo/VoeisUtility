/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Classes;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author afannin1
 */
public class BypassHttpAuthentication {
        public SSLSocketFactory bypassCerts() throws NoSuchAlgorithmException, KeyManagementException {
        //Install trust manager for all certs
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        //Create ssl socket factory
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        return sslSocketFactory;
    }
    
    final TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {    
        }

        @Override
        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
             return null;
        }
      }
    };
}
