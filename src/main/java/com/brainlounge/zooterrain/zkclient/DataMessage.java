package com.brainlounge.zooterrain.zkclient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.zookeeper.data.Stat;

/**
 */
public class DataMessage extends ClientMessage {
    protected final Stat stat;
    protected String znode;
    protected byte[] rawData;

    public DataMessage(String znode, byte[] data, Stat stat) {
        this.znode = znode;
        rawData = data;
        this.stat = stat;
    }

    public Stat getStat() {
        return stat;
    }

    public String getZnode() {
        return znode;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public String getDataBase64Encoded() {
        return StringUtils.newStringUtf8(Base64.encodeBase64(rawData, false));
    }
    
    @Override
    public String toJson() {
        StringBuilder builder = new StringBuilder().
                append("{").
                append("\"type\":").append(quoted("B")).append(",").
                append("\"znode\":").append(quoted(znode)).append(",").
                append("\"data\":").append(quoted(getDataBase64Encoded()));
        if (stat != null) {
            builder.append(",").
            append("\"size\":").append(stat.getDataLength()).append(",").
            append("\"version\":").append(stat.getVersion());
        }
        builder.append("}");
        return builder.toString();
    }
}
