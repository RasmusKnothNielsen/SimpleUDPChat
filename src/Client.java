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
    private InetAddress ipAdress;
    private int port;
    private String username;

    public Client(InetAddress ipAdress, int port, String username)
    {
        this.ipAdress = ipAdress;
        this.port = port;
        this.username = username;
    }

    public Client(InetAddress ipAdress, int port)
    {
        this.ipAdress = ipAdress;
        this.port = port;
        this.username = null;
    }

    public Client(){}

    public InetAddress getIpAdress()
    {
        return ipAdress;
    }

    public void setIpAdress(InetAddress ipAdress)
    {
        this.ipAdress = ipAdress;
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
        return "[IP: " + ipAdress + ", Port: " + port + ", Username: " + username + "]";
    }
}
