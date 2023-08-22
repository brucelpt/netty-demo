package com.example.nettydemo.ssl;

import javax.net.ssl.SSLContext;

public interface SSLService {

    SSLContext serverSSLConfigInit() throws Exception;
}
