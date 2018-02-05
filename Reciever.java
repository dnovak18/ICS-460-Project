
import java.io.*;
import java.net.*;
import java.util.Observable;

public class Reciever extends Observable implements Runnable {

	RecieverGui recieverGui;

	private int packetSize;

	private int timeoutInterval;

	private final int port;

	private double failureProb;

	private double corruptionProb;

	private boolean firstPacketReceived = false;
	
	private boolean endOfFileReceived = false;

	private volatile boolean isShutDown = false;

	private String outputMessage = "";

	RecieverPacketHandler reciever_handler;

	/**
	 * @param port
	 * @param recieverGui
	 */
	public Reciever(int port, RecieverGui recieverGui) {

		this.port = port;

		this.recieverGui = recieverGui;

	}

	@Override
	public void run() {

		byte[] buffer = new byte[packetSize + 12];

		try (DatagramSocket socket = new DatagramSocket(port)) {

			reciever_handler = new RecieverPacketHandler(packetSize, failureProb, corruptionProb, this);

			while (true) {
				if (isShutDown) {
					return;
				}

				DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

				try {
					socket.receive(incoming);
					this.respond(socket, incoming);
				}

				catch (SocketTimeoutException ex) {
					if (isShutDown) {
						return;
					}
				}

				catch (IOException ex) {
					System.err.println(ex);
				}
			}
		}

		catch (SocketException ex) {
			System.err.println(ex);
		}
	}

	/**
	 * Stops the Server
	 */
	public void shutDown() {
		this.isShutDown = true;
	}

	/**
	 * Server Response
	 * 
	 * @param socket
	 * @param dgpacket
	 * @throws IOException
	 */
	public void respond(DatagramSocket socket, DatagramPacket dgpacket) throws IOException {

		// convert DatagramPacket to Packet
		Packet received = reciever_handler.dgpacketToPacket(dgpacket);

		// check for corrupted packet
		if (received.getCksum() == 1) {
			setOutputMessage("Server received and discarded corrupted packet " + received.getSeqno());
			return;
		}

		// check for first packet, received for the first time
		else if (received.getSeqno() == 0) {
			if (!firstPacketReceived) {
				byte[] name_in_bytes = received.getData();
				String name = new String(name_in_bytes);
				reciever_handler.setFileName(received);
				int length = received.getAckno();
				reciever_handler.setFileLength(length);
				setOutputMessage("Server received packet 0 of " + name);
				firstPacketReceived = true;
			} else {
				setOutputMessage("Server received duplicate packet 0");				
			}

		}

		// check for packet received out of sequence
		else if (received.getSeqno() > reciever_handler.getLastPacketReceived() + 1) {
			setOutputMessage(
					"Server received and discarded out-of-sequence packet no " + received.getSeqno());
			return;
		}

		// check for end of file packet
		else if (received.getSeqno() < 0) {
			if (!endOfFileReceived) {
				if (reciever_handler.getBytesStored() < reciever_handler.getFileLength()) {
					setOutputMessage("Server received out-of-sequence EOF");
					return;
				}
				reciever_handler.outputFile();
				setOutputMessage("Server received end of file");
			} else {
				setOutputMessage("Server received duplicate EOF");
			}
		}
		
		// check for duplicate packet
		else if (received.getSeqno() <= reciever_handler.getLastPacketReceived()) {
			setOutputMessage(
					"Server received duplicate packet no " + received.getSeqno());
		}

		// this is a good packet and not end of file
		else {
			setOutputMessage("Server received packet no " + received.getSeqno());
			reciever_handler.addToBuffer(received);
		}
		try {
			Thread.sleep(timeoutInterval / 3);
		}
		catch (InterruptedException ex) {
			System.out.println(ex);
		}

		// sending ack
		// if the received packet was corrupted, don't send ack
		if (received.getCksum() != 1) {
			int ackno;
			if (received.getSeqno() == 0) {
				ackno = 1;
			} else {
				ackno = received.getAckno();
			}
			Packet ackpacket = new Packet((short) 0, ackno);

			// check for corrupted ack
			if (reciever_handler.corruptionCheck()) {
				setOutputMessage("Corrupted acknowledgement for packet " + (ackno - 1));
				ackpacket.setCksum((short) 1);
			}

			// check for failure to send ack
			if (reciever_handler.failureCheck()) {
				setOutputMessage(
						"Server failed to send acknowledgement for packet " + (ackno - 1));
			}

			else {
				DatagramPacket outgoing = reciever_handler.packetToDGPacket(ackpacket, dgpacket.getAddress(),
						dgpacket.getPort());
				setOutputMessage(""
						+ "Server sending acknowledgement for packet " + (ackno - 1) + 
						"; ready for packet " + ackno);
				socket.send(outgoing);
			}
		}

	}

	/**
	 * @return outputMessage
	 */
	public String getOutputMessage() {
		return outputMessage;
	}

	/**
	 * Sets the message and notifies Observers
	 * 
	 * @param outputMessage
	 */
	public void setOutputMessage(String outputMessage) {
		this.outputMessage = outputMessage;
		setChanged();
		notifyObservers(outputMessage);
	}

	/**
	 * @return corruptionProb
	 */
	public double getCorruptionProb() {
		return corruptionProb;
	}

	/**
	 * @param corruptionProb
	 */
	public void setCorruptionProb(double corruptionProb) {
		this.corruptionProb = corruptionProb;
	}

	/**
	 * @return failureProb
	 */
	public double getFailureProb() {
		return failureProb;
	}

	/**
	 * @param failureProb
	 */
	public void setFailureProb(double failureProb) {
		this.failureProb = failureProb;
	}

	/**
	 * @return packetSize
	 */
	public int getPacketSize() {
		return packetSize;
	}

	/**
	 * @param packetSize
	 */
	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}

	/**
	 * @return timeoutInterval
	 */
	public int getTimeoutInterval() {
		return timeoutInterval;
	}

	/**
	 * @param timeoutInterval
	 */
	public void setTimeoutInterval(int timeoutInterval) {
		this.timeoutInterval = timeoutInterval;
	}

}