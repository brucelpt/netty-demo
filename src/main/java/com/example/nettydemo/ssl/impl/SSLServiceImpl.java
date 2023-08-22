package com.example.nettydemo.ssl.impl;

import com.example.nettydemo.ssl.SSLService;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

@Slf4j
public class SSLServiceImpl implements SSLService {


    @Override
    public SSLContext serverSSLConfigInit() throws Exception {
        //
        String jksPath = "ssl/server.jks";  
        String pwd = "123";

        InputStream jksInputStream = SSLServiceImpl.class.getClassLoader().getResourceAsStream(jksPath);
        if (jksInputStream == null) {
            throw new RuntimeException("file not found!");
        }
        SSLContext sslContext = SSLContext.getInstance("TLS");

        KeyStore keystore = KeyStore.getInstance("JKS");

        keystore.load(jksInputStream, pwd.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keystore, pwd.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keystore);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),SecureRandom.getInstanceStrong());
        return sslContext;
    }
}
