

public class Packet {

	short cksum;

	short len;

	int ackno;

	int seqno;

	byte[] data;


	/**
	 * Constructor
	 * @param seqno
	 * @param data
	 */
	public Packet(int seqno, byte[] data) {

		this.cksum = 0;

		this.seqno = seqno;

		this.ackno = seqno + 1;

		this.data = data;

		this.len = (short) (12 + data.length);

	}


	/**
	 * Constructor for packet incl ackno
	 * @param seqno
	 * @param ackno
	 * @param data
	 */
	public Packet(int seqno, int ackno, byte[] data) {

		this.cksum = 0;

		this.seqno = seqno;

		this.ackno = ackno;

		this.data = data;

		this.len = (short) (12 + data.length);

	}

	/**
	 * Constructor for Ack Packet
	 * @param ackno
	 */
	public Packet(short cksum, int ackno) {

		this.cksum = cksum;

		this.len = 8;

		this.ackno = ackno;

	}

	
	/**
	 * @return cksum
	 */
	public short getCksum() {

		return cksum;

	}


	/**
	 * @return length
	 */
	public short getLength() {

		return len;

	}


	/**
	 * Get sequence number
	 * @return seqno
	 */
	public int getSeqno() {

		return seqno;

	}


	/**
	 * Get Acknowledgment number
	 * @return ackno
	 */
	public int getAckno() {

		return ackno;

	}

	
	/**
	 * @return data
	 */
	public byte[] getData() {

		return data;

	}

	/**
	 * @param newCksum
	 */
	public void setCksum(short newCksum) {

		cksum = newCksum;

	}

	// toString method
	public String toString() {
		return "Packet number " + seqno + " [data=" + (data != null ? arrayToString(data, data.length) : null)
				+ "] ackno " + ackno + " cksum " + cksum + " len " + len;
	}

	private String arrayToString(Object array, int len) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for (int i = 0; i < len; i++) {
			if (i > 0)
				buffer.append(", ");
			if (array instanceof byte[])
				buffer.append(((byte[]) array)[i]);
		}
		buffer.append("]");
		return buffer.toString();
	}

}