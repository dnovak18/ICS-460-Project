
import java.awt.EventQueue;

public class StartUdp{

	private final static int PORT = 7;

	private int packetSize;

	private int timeoutInterval;

	private double corruptionProb;

	private double failureProb;

	private int windowSize;

	private Sender sender;

	private Reciever reciever;


	/**
	 * Setup Client and Server
	 */
	public StartUdp() {

		this.sender = new Sender();
		SenderGui senderGui = new SenderGui(sender, this);
		senderGui.setVisible(true);

		sender.addObserver(senderGui);

		RecieverGui recieverGui = new RecieverGui();
		reciever = new Reciever(PORT, recieverGui);
		reciever.addObserver(recieverGui);

		recieverGui.setVisible(true);

	}

	/**
	 * Set the Checksum corruption, by user input
	 * @param corruptionProb
	 */
	public void setCorruptionProb(double corruptionProb) {

		this.corruptionProb = corruptionProb;
		this.sender.setCorruptionProb(corruptionProb);
		this.reciever.setCorruptionProb(corruptionProb);
	}

	/**
	 * Set Packet failure, by user input
	 * @param failureProb
	 */
	public void setFailureProb(double failureProb) {

		this.failureProb = failureProb;
		this.sender.setFailureProb(failureProb);
		this.reciever.setFailureProb(failureProb);
	}

	/**
	 * Set Packet size, by user input
	 * @param packetSize
	 */
	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
		this.sender.setPacketSize(packetSize);
		this.reciever.setPacketSize(packetSize);
	}

	/**
	 * Set Client timeout, by user input
	 * @param timeoutInterval
	 */
	public void setTimeoutInterval(int timeoutInterval) {
		this.timeoutInterval = timeoutInterval;
		this.sender.setTimeoutInterval(timeoutInterval);
		this.reciever.setTimeoutInterval(timeoutInterval);
	}

	private void setWindowSize(int windowSize) {

		this.windowSize = windowSize;
		this.sender.setWindowSize(windowSize);
		
		
	}
	
	/**
	 * Set initial Parameters, by user input
	 * @param failureProb
	 * @param corruptionProb
	 * @param packetSize
	 * @param timeoutInterval
	 */
	public void setParameters(double failureProb, double corruptionProb, int packetSize, int timeoutInterval, int windowSize) {
		

		this.setPacketSize(packetSize);
		this.setTimeoutInterval(timeoutInterval);
		this.setCorruptionProb(corruptionProb);
		this.setFailureProb(failureProb);
		this.setWindowSize(windowSize);

		Thread t = new Thread(reciever);
		t.start();

	}


	/**
	 * @return packetSize
	 */
	public int getPacketSize() {
		return packetSize;
	}

	/**
	 * @return timeoutInterval
	 */
	public int getTimeoutInterval() {
		return timeoutInterval;
	}

	/**
	 * @return corruptionProb
	 */
	public double getCorruptionProb() {
		return corruptionProb;
	}

	/**
	 * @return failureProb
	 */
	public double getFailureProb() {
		return failureProb;
	}

	
	//Main method
	public static void main(String[] args) {
		StartUdp mainRunUDP = new StartUdp();

	}
}