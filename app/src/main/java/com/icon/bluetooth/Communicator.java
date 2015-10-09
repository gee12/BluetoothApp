package com.icon.bluetooth;

/**
 * Created by cyrusmith
 * All rights reserved
 * http://interosite.ru
 * info@interosite.ru
 */
public interface Communicator {
    void startCommunication();
    void write(String message);
    void write(byte[] bytes);
    void stopCommunication();
}
