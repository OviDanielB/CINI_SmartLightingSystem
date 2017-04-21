package org.uniroma2.sdcc.Utils.MOM;

/**
 * Interface for generic queue operations
 * like sending, receiving
 */
public interface QueueManger {

    boolean send(String message);
    String nextMessage();
    void close();
}
