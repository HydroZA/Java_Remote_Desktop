package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketConnectRequest extends Packet<PacketConnectRequest>
{
    private String partner;
    private String requester;
    private boolean accepted;
    private Status status;

    
    // Status is from server's perspective
    enum Status
    {
        UNASSIGNED,
        ASSIGNED
    }
    
    public PacketConnectRequest(String requester, String partner)
    {
        super(Packet.Type.CONNECT_REQUEST);
        this.partner = partner;
        this.requester = requester;
        this.status = Status.UNASSIGNED;
    }

    public PacketConnectRequest()
    {
        super(Packet.Type.CONNECT_REQUEST);
        this.status = Status.UNASSIGNED;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public boolean isAccepted()
    {
        return accepted;
    }

    public void setAccepted(boolean accepted)
    {
        this.accepted = accepted;
    }


    public String getPartner()
    {
        return partner;
    }

    public void setPartner(String partner)
    {
        this.partner = partner;
    }

    public String getRequester()
    {
        return requester;
    }

    public void setRequester(String requester)
    {
        this.requester = requester;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        dos.writeUTF(getPartner());
        dos.writeUTF(getRequester());
        dos.writeBoolean(isAccepted());
        dos.writeUTF(getStatus().toString());
    }

    @Override
    public PacketConnectRequest deserialize(DataInputStream dis) throws IOException
    {
        setPartner(dis.readUTF());
        setRequester(dis.readUTF());
        setAccepted(dis.readBoolean());
        String st = dis.readUTF();
        setStatus(Status.valueOf(st));
        return this;
    }

}
