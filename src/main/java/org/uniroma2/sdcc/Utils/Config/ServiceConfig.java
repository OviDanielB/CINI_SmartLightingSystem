package org.uniroma2.sdcc.Utils.Config;

/**
 * @author emanuele
 */
public class ServiceConfig {

    private static final long serialVersionUID = 42L;

    private String hostname;
    private Integer port;

    public static long getSerialVersionUID() {
        return serialVersionUID;
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
}
