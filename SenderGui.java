
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Observable;
import java.util.Observer;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import java.awt.ScrollPane;
import javax.swing.SwingConstants;

public class SenderGui extends JFrame implements Observer {

	Sender sender;
	StartUdp Udp;

	private JPanel contentPane;

	protected File selectedFile;
	private JTextField fileNameField;
	private JButton btnSendFile;

	private JTextArea feedBackArea;

	private ScrollPane scrollPane;

	private JTextField packetLossTextField;
	private double packetLossPercentage = 0.0;

	private JTextField corruptionTextField;
	private double corruptionPercentage = 0.0;

	private JTextField packetSizeTextField;
	private int packet_size = 0;

	private JTextField timeoutTextField;
	private int timeout_interval = 0;
	private JTextField windowSizeTextField;

	/**
	 * Create the frame.
	 */
	/**
	 * @param sender
	 * @param runUdp
	 */
	public SenderGui(Sender sender, StartUdp Udp) {
		setTitle("Sender");
		this.sender = sender;
		this.Udp = Udp;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 603, 606);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		feedBackArea = new JTextArea();
		feedBackArea.setBounds(10, 229, 568, 328);

		JButton btnChooseFile = new JButton("Select File:");
		btnChooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// File chooser, starts in working folder
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new java.io.File("."));
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					// when you selected a file, store it
					selectedFile = fileChooser.getSelectedFile();
					// set the GUI nameField to show which file was selected
					fileNameField.setText(selectedFile.getName());
				}
			}
		});
		btnChooseFile.setBounds(20, 26, 98, 45);
		contentPane.add(btnChooseFile);

		fileNameField = new JTextField();
		fileNameField.setEditable(false);
		fileNameField.setBounds(128, 38, 135, 20);
		contentPane.add(fileNameField);
		fileNameField.setColumns(10);

		JButton btnSendFile = new JButton("Send File:");
		btnSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sender.setSelectedFile(selectedFile);

				double corruption_prob = Double.parseDouble(corruptionTextField.getText());
				double failure_prob = 0;
				
				int window_size = 1;

				int packet_size = Integer.parseInt(packetSizeTextField.getText());
				int timeout_interval = Integer.parseInt(timeoutTextField.getText());

				Udp.setParameters(failure_prob, corruption_prob, packet_size, timeout_interval, window_size);

				sender.run();
			}
		});
		btnSendFile.setBounds(20, 82, 98, 43);
		contentPane.add(btnSendFile);

		scrollPane = new ScrollPane();
		scrollPane.add(feedBackArea);
		scrollPane.setBounds(20, 186, 540, 372);
		contentPane.add(scrollPane);

		JLabel lblCorruption = new JLabel("Corruption (%):");
		lblCorruption.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCorruption.setBounds(297, 88, 92, 14);
		contentPane.add(lblCorruption);

		corruptionTextField = new JTextField();
		corruptionTextField.setBounds(395, 84, 86, 20);
		corruptionTextField.setText("0");
		contentPane.add(corruptionTextField);
		corruptionTextField.setColumns(10);

		JLabel lblPacketSize = new JLabel("Packet Size:");
		lblPacketSize.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPacketSize.setBounds(274, 29, 120, 14);
		contentPane.add(lblPacketSize);

		packetSizeTextField = new JTextField();
		packetSizeTextField.setToolTipText("Less than 32755 bytes");
		packetSizeTextField.setBounds(395, 26, 86, 20);
		packetSizeTextField.setText("1024");
		contentPane.add(packetSizeTextField);
		packetSizeTextField.setColumns(10);

		JLabel lblClientTimeoutms = new JLabel("Client Timeout (ms):");
		lblClientTimeoutms.setHorizontalAlignment(SwingConstants.RIGHT);
		lblClientTimeoutms.setBounds(274, 58, 125, 14);
		contentPane.add(lblClientTimeoutms);

		timeoutTextField = new JTextField();
		timeoutTextField.setBounds(395, 55, 86, 20);
		timeoutTextField.setText("2000");
		contentPane.add(timeoutTextField);
		timeoutTextField.setColumns(10);
	}

	
	/**
	 * Get the selected file
	 * @return selectedFile
	 */
	public File getSelectedFile() {
		return selectedFile;
	}

	/**
	 * Set the selected file
	 * @param selectedFile
	 */
	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
	}

	
	@Override
	public void update(Observable arg0, Object message) {

		// print the received message to the textArea
		feedBackArea.append(message + "\n");
		// Scrolls with the incoming new data
		scrollPane.setScrollPosition(0, feedBackArea.getDocument().getLength());

	}
}