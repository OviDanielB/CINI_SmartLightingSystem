package org.uniroma2.sdcc.Utils.MOM;

import org.uniroma2.sdcc.Utils.HeliosLog;
import sun.reflect.generics.reflectiveObjects.LazyReflectiveObjectGenerator;

/**
 * Created by ovidiudanielbarba on 21/04/2017.
 */
public class Test {

    public static void main(String[] args){

        QueueManger manager = new RabbitQueueManager("localhost",5672,"storm",QueueClientType.CONSUMER);


        while (true){
            HeliosLog.logOK("MAIN",manager.nextMessage());
        }
    }
}
