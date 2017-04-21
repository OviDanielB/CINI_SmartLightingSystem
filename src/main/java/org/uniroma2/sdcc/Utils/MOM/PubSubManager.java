package org.uniroma2.sdcc.Utils.MOM;

/**
 * Created by ovidiudanielbarba on 20/04/2017.
 */
public interface PubSubManager {
    boolean publish(String routingKey,String message);
}
