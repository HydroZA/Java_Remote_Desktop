package main;

import java.io.*;

/**
 *
 * @author James Legge
 * @param <T>
 */
public abstract class Packet<T extends Packet>
{
    public Type type;

    public Packet(Type type)
    {
        this.type = type;
    }

    enum Type
    {
        LOGIN,
        REGISTER,
        LOGOUT,
        CONNECT_REQUEST,
        STATUS,
        FRIENDS,
        ADD_FRIEND,
        REMOVE_FRIEND,
        FRAME
    }

    public abstract void serialize(DataOutputStream dos) throws IOException;

    public abstract T deserialize(DataInputStream dis) throws IOException;

    public void send(DataOutputStream dos) throws IOException
    {
        dos.writeInt(type.ordinal());
        serialize(dos);
    }
    public void sendOnlyType(DataOutputStream dos) throws IOException
    {
        dos.writeInt(type.ordinal());
    }

    public T read(DataInputStream dis) throws IOException
    {
        Type tempType = Type.values()[dis.readInt()];
        T pt = deserialize(dis);
        pt.setPacketType(tempType);

        return pt;
    }

    public void setPacketType(Type pt)
    {
        this.type = pt;
    }

    public Type getPacketType()
    {
        return type;
    }

}
