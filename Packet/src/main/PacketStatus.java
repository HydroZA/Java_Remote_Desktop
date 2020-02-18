package main;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketStatus extends Packet <PacketStatus>
{
    private boolean success;
    private String message;
    
    public PacketStatus(boolean success, String message)
    {
        super(Packet.Type.STATUS);
        this.success = success;
        this.message = message;
    }
    
    public PacketStatus()
    {
        super(Packet.Type.STATUS);
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }
    
    public String getMessage()
    {
        return this.message;
    }
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    
    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        dos.writeInt(success ? 1 : 0);
        dos.writeUTF(getMessage());
    }

    @Override
    public PacketStatus deserialize(DataInputStream dis) throws IOException
    {
        this.success = dis.readInt() == 1;
        setMessage(dis.readUTF());
        
        return this;
    }
    
}
