/*
Computer Science, KEA, Denmark.
3. Semester, Fall 2019.
Mandatory Assignment in Tech2.
Read Network_Assignment2.pdf for more information

@Author: Rasmus Knoth Nielsen.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class UDPServer
{

    private static ArrayList<Client> existingClients = new ArrayList<>();
    private final static int SERVERPORT = 20202;

    public static void main(String args[]) throws Exception
    {

        // Open a new datagram socket on the specified port
        DatagramSocket udpServerSocket = new DatagramSocket(SERVERPORT);

        properStartUp();

        while (true)
        {

            // Create byte buffers to hold the messages to send and receive
            byte[] receiveData = new byte[1024];

            // Create an empty DatagramPacket packet
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            // Block until there is a packet to receive, then receive it  (into our empty packet)
            udpServerSocket.receive(receivePacket);

            // Extract the message from the packet and make it into a string, then trim off any end characters
            String clientMessage = (new String(receivePacket.getData())).trim();

            if (clientMessage.startsWith("JOIN: "))
            {
                // The user wants to join with a specific username
                // Get username
                String userName = clientMessage.substring(6);

                /* For aesthetic reasons, usernames are only allowed to be max 20 chars long
                    If longer usernames is wanted, remember to change this and the whitespace padding
                    of the username in our Threadhandler.ReceiverThread class
                 */
                if (userName.length() > 20)
                {
                    userName = userName.substring(0, 19);
                }

                // Check if client username already exists in our system
                boolean clientExists = false;
                for (Client client : existingClients)
                {
                    if (client.getUsername().equals(userName))
                    {
                        clientExists = true;
                    }
                }

                // Check if user has already joined the chat but tries to
                // join again with another username
                boolean clientAlreadyLoggedIn = false;
                for (Client client : existingClients)
                {
                    if (client.getIpAddress().equals(receivePacket.getAddress()) &&
                    client.getPort() == receivePacket.getPort())
                    {
                        clientAlreadyLoggedIn = true;
                    }
                }

                // If username isnt occupied and client is not logged in with other name
                if (!clientExists && !clientAlreadyLoggedIn)
                {
                    System.out.println("[+] Adding client to existingClients");
                    // Initialise the Client object
                    Client newClient = new Client(receivePacket.getAddress(), receivePacket.getPort(), userName);
                    existingClients.add(newClient);

                    sendToOneUser(newClient, "JOIN OK", udpServerSocket);
                    sendToAllUsers(newClient, "NEW CLIENT " + userName, udpServerSocket);

                    System.out.println("List of online clients: \n" + existingClients);
                }

                // If client is already logged in with other username
                else if (clientAlreadyLoggedIn)
                {
                    System.out.println("[-] " + getUsernameByPacket(receivePacket) +
                            " tries to JOIN with another username");
                    Client client = createClientObjectFromPacket(receivePacket);
                    sendToOneUser(client, "You are already logged in. " +
                            "to change username, please quit and join again.", udpServerSocket);
                }

                // If user tries to join with an already used username
                else
                {
                    Client newClient = new Client(receivePacket.getAddress(), receivePacket.getPort());
                    System.out.println("[-] User tried to JOIN on occupied username: " + userName + ", Client IP: " +
                            newClient.getIpAddress() + ", Client port: " + newClient.getPort());
                    sendToOneUser(newClient, "JOIN ERROR username occupied", udpServerSocket);
                    continue;
                }

            }

            else if (clientMessage.startsWith("MESSAGE: "))
            {
                //System.out.println("User tries to message");
                Client client = new Client();
                for (int i= 0; i < existingClients.size(); i++)
                {
                    if (existingClients.get(i).getIpAddress().equals(receivePacket.getAddress()) &&
                            existingClients.get(i).getPort() == receivePacket.getPort())
                    {
                        client = existingClients.get(i);
                    }
                }
                sendToAllUsers(client, "FROM " + client.getUsername() + " MESSAGE " + clientMessage.substring(9), udpServerSocket);
            }

            else if (clientMessage.startsWith("QUIT"))
            {

                // Remove the client object from the arraylist
                int indexOfClient = -1;
                for (int i = 0; i < existingClients.size(); i++)
                {
                    if (existingClients.get(i).getIpAddress().equals(receivePacket.getAddress()) &&
                            existingClients.get(i).getPort() == receivePacket.getPort())
                    {
                        indexOfClient = i;
                    }
                }

                if (indexOfClient > -1)
                {
                    Client newClient = existingClients.get(indexOfClient);
                    sendToAllUsers(newClient, newClient.getUsername() + " just quit the chat!",
                            udpServerSocket);
                    existingClients.remove(indexOfClient);
                    System.out.println("[*] " + newClient.getUsername() + " quit the server");
                    System.out.println(existingClients);
                }
            }

            else if (clientMessage.equals("WHOISONLINE"))
            {
                // Make a new Arraylist and fill it with all known usernames
                ArrayList<String> onlineUsers= new ArrayList<String>();
                for (Client client : existingClients)
                {
                    onlineUsers.add(client.getUsername());
                }
                Client client = createClientObjectFromPacket(receivePacket);
                // Send the usernames back to the requesting client
                sendToOneUser(client, "Users online : " + onlineUsers.toString(), udpServerSocket);
            }

            else if (clientMessage.equals("COMMANDS"))
            {
                Client client = createClientObjectFromPacket(receivePacket);
                String availableCommands = "On this chat server, you can use the following commands:\n" +
                        "'JOIN: (username)'\t\t\t Up to 20 characters are supported.\n" +
                        "'MESSAGE: (message)'\t\t Up to 200 characters are supported. \n" +
                        "'WHOISONLINE' \n" +
                        "'COMMANDS' \n" +
                        "'QUIT'";
                sendToOneUser(client, availableCommands, udpServerSocket);
            }
        }
    }

    public static void sendToAllUsers(Client newClient, String message, DatagramSocket udpServerSocket)
    {

        // Convert data to a byte array
        byte[] sendData = new byte[1024];
        sendData = message.getBytes();

        try
        {
            // Send the Message to all other clients connected.
            for (Client client : existingClients)
            {
                DatagramPacket sendPacket = new DatagramPacket(sendData, message.length(), client.getIpAddress(), client.getPort());
                System.out.println("Sending message to " + client);
                udpServerSocket.send(sendPacket);
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void sendToOneUser(Client client, String message, DatagramSocket udpServerSocket)
    {
        // Convert data to a byte array
        byte[] sendData = new byte[1024];
        sendData = message.getBytes();

        try
        {
            DatagramPacket sendPacket = new DatagramPacket(sendData, message.length(), client.getIpAddress(), client.getPort());
            System.out.println("Sending message to " + client);
            udpServerSocket.send(sendPacket);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static String getUsernameByPacket(DatagramPacket receivedPacket)
    {
        String username = null;
        for (Client client : existingClients)
        {
            if (receivedPacket.getAddress().equals(client.getIpAddress()) &&
                    receivedPacket.getPort() == client.getPort())
            {
                username = client.getUsername();
            }
        }
        return username;
    }

    public static Client createClientObjectFromPacket(DatagramPacket receivePacket)
    {
        Client client = new Client(receivePacket.getAddress(), receivePacket.getPort(),
                getUsernameByPacket(receivePacket));
        return client;
    }

    public static void properStartUp()
    {
        try
        {
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            System.out.println("X                                         X");
            System.out.println("X   WELCOME TO KNOTH INC SERVER SYSTEMS   X");
            System.out.println("X                V.0.1.                   X");
            System.out.println("X      Author: Rasmus Knoth Nielsen       X");
            System.out.println("X                                         X");
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            System.out.println();
            System.out.println("> Powering up server, please wait");
            TimeUnit.SECONDS.sleep(2);
            System.out.println("> Compiling server software");
            TimeUnit.SECONDS.sleep(1);
            System.out.println("> SERVER READY FOR ACTION");
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }
}