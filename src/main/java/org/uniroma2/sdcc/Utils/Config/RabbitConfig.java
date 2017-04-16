package org.uniroma2.sdcc.Utils.Config;

import java.io.Serializable;

/**
 * @author emanuele
 */
public class RabbitConfig implements Serializable{

    private static final long serialVersionUID = 42L;

    private String queue_name;
    private String hostname;
    private Integer port;

    public RabbitConfig() {
    }

    public String getQueue_name() {
        return queue_name;
    }

    public void setQueue_name(String queue_name) {
        this.queue_name = queue_name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}
