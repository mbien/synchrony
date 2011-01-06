package com.synchrony.config;

/*
 * Created on Wednesday, October 06 2010
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Synchrony's configuration binding.
 * @see #getDefault()
 * @see #read(java.net.URI)
 * @see #save(java.net.URI)
 * 
 * @author Michael Bien
 */
@XmlType(name = "")
@XmlRootElement(name = "config")
public class Config {

    @XmlElement(required=true)
    public String multicastaddress;

    @XmlElement(required=true)
    public int multicastSendPort;
    
    @XmlElement(required=true)
    public int multicastListenPort;

    @XmlElement(required=true)
    public final int tcpSendPort;

    @XmlElement(required=true)
    public final int tcpListenPort;

    @XmlElement(required=true)
    public int heartbeat;
    
    @XmlElement(name="watcher", required=false)
    public final List<Watcher> watchers;

    public static class Watcher {

        @XmlAttribute(required=true)
        public String name;

        @XmlElement(required=true)
        public boolean enabled;

        @XmlElement(required=true)
        public String path;

        public Watcher() {
            name = null;
            enabled = false;
            path = null;
        }

    }

    private URI uri;

    private Config() {
        multicastSendPort = 0;
        tcpSendPort = 0;
        tcpListenPort = 0;
        heartbeat = 0;
        watchers = new ArrayList<>();
    }

    private Config(String multicastAddress, int multicastSendPort, int multicastListenPort, int tcpSendPort, int tcpListenPort, int heartbeat) {
        this.multicastaddress = multicastAddress;
        this.multicastSendPort = multicastSendPort;
        this.multicastListenPort = multicastListenPort;
        this.tcpSendPort = tcpSendPort;
        this.tcpListenPort = tcpListenPort;
        this.heartbeat = heartbeat;
        this.watchers = new ArrayList<>();
    }


    private static JAXBContext getContext() throws JAXBException {
        return JAXBContext.newInstance(Config.class);
    }

    private void setURI(URI uri) {
        this.uri = uri;
    }

    public static Config getDefault() {
        return new Config("224.0.0.1", 5001, 5001, 6000, 6000, 10000);
    }


    public static Config read(URI uri) throws IOException {
        try {
            JAXBContext context = getContext();
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Config config = (Config) unmarshaller.unmarshal(new File(uri));
            config.setURI(uri);
            return config;
        } catch (JAXBException ex) {
            throw new IOException("unable to unmarshal", ex);
        }
    }

    public void save() throws IOException {
        save(uri);
    }

    public void save(URI url) throws IOException {
        if(url == null) {
            throw new IllegalArgumentException("url was null");
        }
        try {
            JAXBContext context = getContext();
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(this, new FileOutputStream(new File(url)));
        } catch (final JAXBException | FileNotFoundException ex) {
            throw new IOException(ex);
        }
    }

}

