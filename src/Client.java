/*
Computer Science, KEA, Denmark.
3. Semester, Fall 2019.
Mandatory Assignment in Tech2.
Read Network_Assignment2.pdf for more information

@Author: Rasmus Knoth Nielsen.
 */

import java.net.InetAddress;

public class Client
{

    //Fields
    private InetAddress ipAddress;
    private int port;
    private String username;

    public Client(InetAddress ipAddress, int port, String username)
    {
        this.ipAddress = ipAddress;
        this.port = port;
        this.username = username;
    }

    public Client(InetAddress ipAddress, int port)
    {
        this.ipAddress = ipAddress;
        this.port = port;
        this.username = null;
    }

    public Client(){}

    public InetAddress getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String toString()
    {
        return "[IP: " + ipAddress + ", Port: " + port + ", Username: " + username + "]";
    }
}
