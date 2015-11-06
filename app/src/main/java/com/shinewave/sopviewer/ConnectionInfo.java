package com.shinewave.sopviewer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2015/10/29.
 */
public class ConnectionInfo {
    public String connectionName;
    public int protocol;
    public String protocolType;
    public String url;
    public String id;
    public String password;

    public String toString()
    {
        return connectionName;
    }

    public enum ProtocolType {
        FTP(0), SMB(1);

        private final int code;

        private ProtocolType(int code) {
            this.code = code;
        }

        public int toInt() {
            return code;
        }

        public String toString() {
            //only override toString, if the returned value has a meaning for the
            //human viewing this value
            return this.name();
        }
    }
}
