/*
Computer Science, KEA, Denmark.
3. Semester, Fall 2019.
Mandatory Assignment in Tech2.
Read Network_Assignment2.pdf for more information

@Author: Rasmus Knoth Nielsen.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ThreadHandler {
    static class SenderThread extends Thread
    {

        private InetAddress serverIPAddress;
        private DatagramSocket udpClientSocket;
        private int serverPort;

        public SenderThread(InetAddress address, int serverPort) throws SocketException
        {
            this.serverIPAddress = address;
            this.serverPort = serverPort;

            // Create client DatagramSocket
            udpClientSocket = new DatagramSocket();
            udpClientSocket.connect(serverIPAddress, serverPort);
        }

        public DatagramSocket getSocket()
        {
            return this.udpClientSocket;
        }

        public void run()
        {
            try
            {
                //send blank message
                byte[] data = new byte[1024];
                data = "".getBytes();
                DatagramPacket blankPacket = new DatagramPacket(data,data.length , serverIPAddress, serverPort);
                udpClientSocket.send(blankPacket);

                // Create input stream
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

                while (true)
                {
                    // Message to send
                    String clientMessage = inFromUser.readLine();

                    // Create byte buffer to hold the message to send
                    byte[] sendData = new byte[1024];

                    // Put this message into our empty buffer/array of bytes
                    sendData = clientMessage.getBytes();

                    // Create a DatagramPacket with the data, IP address and port number
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverPort);

                    // Send the UDP packet to server
                    udpClientSocket.send(sendPacket);
                    if (clientMessage.equals("QUIT"))
                        break;

                    Thread.yield();
                }
            }
            catch (IOException ex)
            {
                System.err.println(ex);
            }
        }
    }

    static class ReceiverThread extends Thread
    {

        private DatagramSocket udpClientSocket;

        public ReceiverThread(DatagramSocket ds) throws SocketException
        {
            this.udpClientSocket = ds;
        }

        public void run()
        {

            // Create a byte buffer/array for the receive Datagram packet
            byte[] receiveData = new byte[1024];

            while (true)
            {
                // Set up a DatagramPacket to receive the data into
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                try
                {
                    // Receive a packet from the server (blocks until the packets are received)
                    udpClientSocket.receive(receivePacket);

                    // Extract the reply from the DatagramPacket
                    String serverReply =  new String(receivePacket.getData(), 0, receivePacket.getLength());

                    // Format text to comply with protocol
                    if (serverReply.startsWith("FROM"))
                    {
                        // Get indices of USERNAME and MESSAGE
                        int messageIndex = serverReply.indexOf("MESSAGE");
                        String username = serverReply.substring(5, messageIndex);
                        String message = serverReply.substring(messageIndex + 8);
                        // Pad username with whitespaces up to 20
                        username = String.format("%-20s", username);
                        System.out.println("From user: " + username + "Message: " + message);
                    }
                    else
                    {
                        System.out.println(serverReply);
                    }

                    Thread.yield();
                }
                catch (IOException ex)
                {
                    System.err.println(ex);
                }
            }
        }
    }
}
