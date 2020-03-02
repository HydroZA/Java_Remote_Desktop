/*
 * AUTHOR: James Legge
 * STUDENT#: 17008250
 * INSTITUTION: London Metropolitan University
 * SUBJECT: CS6P05 Project
 * PROJECT TITLE: Using Asymmetrical Encryption and Digital Signatures to Create a Secure Remote Desktop Environment
 * Project Supervisor: Dr. Qicheng Yu
 */
package rdp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author James Legge
 */
public class PacketCertificate extends Packet <PacketCertificate>
{

    private byte[] cert;

    public PacketCertificate(byte[] cert)
    {
        super(Packet.Type.CERTIFICATE);
        this.cert = cert;
    }
    public PacketCertificate()
    {
        super(Packet.Type.CERTIFICATE);
        
    }

    public byte[] getCert()
    {
        return cert;
    }

    public void setCert(byte[] cert)
    {
        this.cert = cert;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException
    {
        dos.writeInt(cert.length);
        dos.write(cert);
    }

    @Override
    public PacketCertificate deserialize(DataInputStream dis) throws IOException
    {
        int size = dis.readInt();
        cert = new byte[size];
        dis.readFully(cert, 0, size);
        
        return this;
    }
    
}
