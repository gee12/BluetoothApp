package com.icon.bluetooth;

/**
 * Created by cyrusmith
 * All rights reserved
 * http://interosite.ru
 * info@interosite.ru
 */
public interface Communicator {
    void listenMessage();
    void write(byte[] bytes);
    void writeAndListenResponce(byte[] bytes);
    void stopCommunication();
}
