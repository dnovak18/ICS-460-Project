

import java.io.*;
import java.nio.ByteBuffer;
import java.net.*;

public class RecieverPacketHandler {

	Reciever reciever;

	// store incoming packets in ArrayList
	private byte[] buffer;

	// store name of file to write
	private String fileName;

	// store length of file to write
	private int fileLength;

	// keep track of how many bytes are in buffer
	private int bytesStored;

	// Server needs to know seqno of last good packet
	private int lastPacketReceived;

	// packet size, should be the same for all classes
	private int packetSize;

	// probability that cksum of any Packet will be corrupted
	private double corruptionProb;

	// probability that a Packet will fail on sending
	private double failureProb;

	/**
	 * Constructor
	 * @param packetSize
	 * @param failureProb
	 * @param corruptionProb
	 * @param Reciever
	 */
	public RecieverPacketHandler(int packetSize, double failureProb, double corruptionProb, Reciever reciever) {

		this.packetSize = packetSize;

		this.corruptionProb = corruptionProb;

		this.failureProb = failureProb;

		this.lastPacketReceived = -1;

		this.reciever = reciever;

		this.bytesStored = 0;

	}

	/**
	 * @return packetSize
	 */
	public int getPacketSize() {
		return (packetSize);
	}

	/**
	 * @param seqno
	 */
	public void setLastPacketReceived(int seqno) {
		this.lastPacketReceived = seqno;
	}

	/**
	 * @return lastPacketReceived
	 */
	public synchronized int getLastPacketReceived() {
		return (lastPacketReceived);
	}


	/**
	 * Check for corruption of ack Packet
	 * @return boolean
	 */
	public boolean corruptionCheck() {
		if (Math.random() < corruptionProb) {
			return (true);
		} else {
			return (false);
		}

	}

	/**
	 * Check for failure to send ack Packet
	 * @return boolean
	 */
	public boolean failureCheck() {
		if (Math.random() < failureProb) {
			return (true);
		} else {
			return (false);
		}

	}

	/**
	 * @return buffer.length
	 */
	public int getBufferLength() {
		return (buffer.length);
	}

	/**
	 * @param packet
	 */
	public void setFileName(Packet packet) {

		setLastPacketReceived(packet.getSeqno());
		byte[] name_in_bytes = packet.getData();
		String name = new String(name_in_bytes);
		this.fileName = "COPY_OF_" + name;

	}

	/**
	 * @param length
	 */
	public void setFileLength(int length) {
		this.fileLength = length;
		buffer = new byte[fileLength];
	}
	
	public int getFileLength() {
		return (fileLength);
	}

	/**
	 * Put received Packet data into buffer
	 * @param packet
	 */
	public void addToBuffer(Packet packet) {

		if (packet.getSeqno() == lastPacketReceived + 1) {

			setLastPacketReceived(packet.getSeqno());
			byte[] data = packet.getData();
			System.arraycopy(data, 0, buffer, bytesStored, data.length);
			bytesStored += data.length;

		}

	}
	
	/**
	 * Write to new file 
	 */
	public void outputFile() {
		
		try {
			File file = new File(fileName);
			if (!file.exists()){
				file.createNewFile();
			}
			FileOutputStream output_f = new FileOutputStream(file);
			output_f.write(buffer);
			output_f.flush();
			output_f.close();
			

		} catch (Exception ex) {
			System.out.println("Error writing file");

		}
		
	
	}


	/**
	 * Convert a Packet to a DatagramPacket for sending over UDP
	 * @param newPacket
	 * @param server
	 * @param port
	 * @return output datagram
	 */
	public DatagramPacket packetToDGPacket(Packet newPacket, InetAddress server, int port) {

		Packet input_p = newPacket;

		byte[] temp = new byte[input_p.getLength()];

		ByteBuffer buf = ByteBuffer.wrap(temp);

		buf.putShort(input_p.getCksum());

		buf.putShort(input_p.getLength());

		buf.putInt(input_p.getAckno());

		if (input_p.getLength() > 8) {

			buf.putInt(input_p.getSeqno());

			buf.put(input_p.getData());

		}

		DatagramPacket output_dg = new DatagramPacket(temp, temp.length, server, port);

		return (output_dg);

	}

	/**
	 * @return bytes_stored
	 */
	public int getBytesStored() {
		return bytesStored;
	}


	/**
	 * Convert Packet to DatagramPacket, without IP address or port
	 * @param newPacket
	 * @return output datagram
	 */
	public DatagramPacket packetToDGPacket(Packet newPacket) {

		Packet input_p = newPacket;

		byte[] temp = new byte[input_p.getLength()];

		ByteBuffer buf = ByteBuffer.wrap(temp);

		buf.putShort(input_p.getCksum());

		buf.putShort(input_p.getLength());

		buf.putInt(input_p.getAckno());

		if (input_p.getLength() > 8) {

			buf.putInt(input_p.getSeqno());

			buf.put(input_p.getData());

		}

		DatagramPacket output_dg = new DatagramPacket(temp, temp.length);

		return (output_dg);

	}

	/**
	 * Convert DatagramPacket to Packet, to read fields
	 * @param dgPacket
	 * @return output packet
	 */
	public Packet dgpacketToPacket(DatagramPacket dgPacket) {

		DatagramPacket input_dg = dgPacket;

		int data_length = input_dg.getLength() - 12;

		byte[] temp = input_dg.getData();

		ByteBuffer buf = ByteBuffer.wrap(temp);

		short cksum = buf.getShort();

		short length = buf.getShort();

		int ackno = buf.getInt();
		
		// if length 8, this is an ack Packet, so don't worry about data
		// Server shouldn't be getting ack Packets
		if (length == 8) {

			Packet output_p = new Packet(cksum, ackno);

			return (output_p);

		}

		// otherwise, this is a data Packet, so get seqno and data
		// Server should only be getting data packets
		else {

			int seqno = buf.getInt();

			byte[] data = new byte[data_length];

			buf.get(data);

			Packet output_p = new Packet(seqno, ackno, data);

			// check for corrupted packet
			if (cksum > 0) {

				output_p.setCksum((short) 1);

			}

			return output_p;

		}

	}

	// toString method, for testing
	@Override
	public String toString() {
		return "RecieverPacketHandler [packet size=" + packetSize + ", corruption prob=" + corruptionProb
				+ ", failure prob=" + failureProb + "]";
	}

}