package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author RMI Assignment
 *
 */
public class ClientChatRoom extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel textPanel;
	private JTextArea textField;
	private String message;

	private ChatClient3 chatClient;
	private JList<String> list;
	private DefaultListModel<String> listModel;
	private JButton exitButton, minButton;// exit button and minimize button
	private JLabel backgroud;
	private JButton sendButton, fileButton;
	private JButton findFriendsButton, findGroupsButton, logoutButton;
	private Font msgFont = new Font(message, Font.PLAIN, 16);

	boolean isDragged = false;// record the status of mouse
	private Point frameTemp;// relative location of mouse
	private Point frameLoc;// frame location

	protected JTextArea textArea;
	protected JPanel clientPanel, userPanel;

	/**
	 * GUI Constructor
	 */
	public ClientChatRoom(ChatClient3 client) {
		// set the client
		chatClient = client;

		Container c = getContentPane();
		// set the layout
		c.setLayout(null);

		// minimize button
		minButton = new JButton(new ImageIcon("image/login/min_btn.jpg"));
		minButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setExtendedState(JFrame.ICONIFIED);
			}
		});
		minButton.setBounds(740, 0, 30, 30);
		c.add(minButton);

		// exit button
		exitButton = new JButton(new ImageIcon("image/login/exit_btn.jpg"));
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		exitButton.setBounds(770, 0, 30, 30);
		c.add(exitButton);

		// message input text field
		textField = new JTextArea();
		textField.setFont(msgFont);
		textField.setLineWrap(true);
		textField.setBounds(200, 300, 396, 180);
		c.add(textField);

		// send button
		sendButton = new JButton(new ImageIcon("image/chatroom/sendMsg_btn.png"));
		sendButton.addActionListener(this);
		sendButton.setBounds(616, 300, 164, 33);
		c.add(sendButton);

		// send file button
		fileButton = new JButton(new ImageIcon("image/chatroom/sendFile_btn.png"));
		fileButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				eventOnImport(new JButton());
			}
		});
		fileButton.setBounds(616, 350, 164, 33);
		c.add(fileButton);

		// receive message
		textArea = new JTextArea(14, 34);
		textArea.setMargin(new Insets(10, 10, 10, 10));
		textArea.setFont(msgFont);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		textPanel = new JPanel();
		textPanel.add(scrollPane);
		textPanel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));

		textArea.setBounds(200, 50, 580, 220);
		c.add(textArea);

		// add my friends panel
		c.add(myFriendsPanel());

		// my groups panel
		c.add(myGroupsPanel());

		// find friends button
		findFriendsButton = new JButton(new ImageIcon("image/chatroom/friend_btn.png"));
		findFriendsButton.addActionListener(this);
		findFriendsButton.setBounds(20, 430, 50, 42);
		c.add(findFriendsButton);

		// find groups button
		findGroupsButton = new JButton(new ImageIcon("image/chatroom/group_btn.png"));
		findGroupsButton.addActionListener(this);
		findGroupsButton.setBounds(80, 430, 50, 42);
		c.add(findGroupsButton);

		// logout button
		logoutButton = new JButton(new ImageIcon("image/chatroom/logout_btn.png"));
		logoutButton.addActionListener(this);
		logoutButton.setBounds(140, 430, 40, 42);
		c.add(logoutButton);

		// add listener to the mouse for dragging
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				isDragged = false;
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mousePressed(MouseEvent e) {
				frameTemp = new Point(e.getX(), e.getY());
				isDragged = true;
				if (e.getY() < 500)
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getY() < 500) {
					if (isDragged) {
						frameLoc = new Point(getLocation().x + e.getX() - frameTemp.x,
								getLocation().y + e.getY() - frameTemp.y);
						setLocation(frameLoc);
					}
				}
			}
		});

		// Backgroud image
		backgroud = new JLabel(new ImageIcon("image/chatroom/bg.jpg"));
		backgroud.setBounds(0, 0, 800, 500);
		c.add(backgroud);

		this.setIconImage(new ImageIcon("image/brown.jpg").getImage());// window icon
		this.setSize(800, 500);// window size
		this.setUndecorated(true);// delete the original frame give by Java
		this.setLocationRelativeTo(null);// show in the middle of the screen
		this.setVisible(true);
	}

	public JPanel myFriendsPanel() {
		// my friends panel
		userPanel = new JPanel(new BorderLayout());
		String userStr = " My Friend List";
		JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
		userPanel.add(userLabel, BorderLayout.NORTH);

		userLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		String[] noClientsYet = { "Empty friend list" };
		setClientPanel(noClientsYet);

		userPanel.setBounds(20, 50, 160, 220);
		return userPanel;
	}

	public JPanel myGroupsPanel() {
		// my groups panel
		userPanel = new JPanel(new BorderLayout());
		String groupStr = "My Group List";

		JLabel groupLabel = new JLabel(groupStr, JLabel.CENTER);
		userPanel.add(groupLabel, BorderLayout.NORTH);

		groupLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		String[] noClientsYet = { "Empty group list" };
		setClientPanel(noClientsYet);

		userPanel.setBounds(20, 300, 160, 110);
		return userPanel;
	}

	public void setClientPanel(String[] currClients) {
		clientPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel<String>();

		for (String s : currClients) {
			listModel.addElement(s);
		}
		/*
		 * if(currClients.length > 1){ privateMsgButton.setEnabled(true); }
		 */

		// Create the list and put it in a scroll pane.
		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setVisibleRowCount(8);
		list.setFont(msgFont);
		JScrollPane listScrollPane = new JScrollPane(list);

		clientPanel.add(listScrollPane, BorderLayout.CENTER);
		userPanel.add(clientPanel, BorderLayout.CENTER);
	}

	/**
	 * Action handling on the buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// get text and clear textField
		if (e.getSource() == sendButton) {
			message = textField.getText();
			textField.setText("");
			System.out.println("Sending message : " + message);
		}
		// find online user
		if (e.getSource() == findFriendsButton) {
			new ClientSearchFriendGUI();
		}
		// find groups
		if (e.getSource() == findGroupsButton) {
			new ClientSearchGroupGUI();
		}
		// logout
		// jump to the login page
		if (e.getSource() == logoutButton) {
			/*
			 * 锟斤拷锟节碉拷锟借定锟角登筹拷锟截碉拷login锟斤拷锟斤拷 锟斤拷锟斤拷锟斤拷锟借定没锟斤拷锟斤拷 锟斤拷锟斤拷锟斤拷锟揭拷锟斤拷锟斤拷没锟阶刺拷锟斤拷锟斤拷锟�
			 *
			 */
			this.dispose();
			new ClientLoginGUI(chatClient);
		}
	}

	/* file transportation */
	private void eventOnImport(JButton fileButton) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("war", "xml", "txt", "doc", "docx", "jpg", "png");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(fileButton);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File[] arrfiles = chooser.getSelectedFiles();
			if (arrfiles == null || arrfiles.length == 0) {
				return;
			}
			FileInputStream input = null;
			FileOutputStream out = null;
			String save_path = "./src/client/sourceFile"; // store path
			try {
				for (File f : arrfiles) {
					File dir = new File(save_path);
					File[] fs = dir.listFiles();
					HashSet<String> set = new HashSet<String>();
					for (File file : fs) {
						set.add(file.getName());// get the file name
					}
					// p.s. 锟斤拷锟絠f锟斤拷锟斤拷删锟斤拷 锟斤拷锟斤拷锟斤拷
					if (set.contains(f.getName())) {// indicate whether the file has been existed in the system
						JOptionPane.showMessageDialog(new JDialog(),
								f.getName() + ":The selected file is already exist锟斤拷");
						return;
					}
					input = new FileInputStream(f);
					byte[] buffer = new byte[1024];
					File des = new File(save_path, f.getName());
					out = new FileOutputStream(des);
					int len = 0;
					while (-1 != (len = input.read(buffer))) {
						out.write(buffer, 0, len);
					}
					out.close();
					input.close();
					String successfulHint = "I have upload " + f.getName() + "\n";
					chatClient.updateChat(successfulHint);
				}
				JOptionPane.showMessageDialog(null, "Upload successfully. :)", "Hint", JOptionPane.INFORMATION_MESSAGE);
			} catch (FileNotFoundException e1) {
				JOptionPane.showMessageDialog(null, "Failed to upload. :(", "Hint", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Failed to upload. :(", "Hint", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}

}
