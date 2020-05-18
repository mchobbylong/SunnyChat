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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import common.*;

public class ClientMainGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel textPanel;
	private JTextArea textField;
	private String message;
	private static Icon openEye = new ImageIcon("image/chatroom/see_group_members.png");
	private static Icon closeEye = new ImageIcon("image/chatroom/unable_see_group_members.png");

	private Client chatClient;
	private JButton exitButton, minButton;// exit button and minimize button
	private JLabel backgroud;
	private JButton sendButton, fileButton;
	private JButton findFriendsButton, findGroupsButton, logoutButton, seeGroupMemberButton;
	private Font msgFont = new Font(message, Font.PLAIN, 16);

	boolean isDragged = false;// record the status of mouse
	private Point frameTemp;// relative location of mouse
	private Point frameLoc;// frame location

	private Integer selectedChatRoomID = null;
	private JTextArea textArea;

	// My Friends Panel
	private JPanel friendPanel;
	private JList<ChatRoom> friendList;
	private ChatRoomListModel friendListModel;

	// My Groups Panel
	private JPanel groupPanel;
	private JList<ChatRoom> groupList;
	private ChatRoomListModel groupListModel;

	/**
	 * GUI Constructor
	 */
	public ClientMainGUI(Client client) {
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
		exitButton.addActionListener(this);
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

		// Add my friends panel
		friendPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("My Friend List", JLabel.CENTER);
		friendPanel.add(label, BorderLayout.NORTH);
		friendPanel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		friendPanel.setBounds(20, 50, 160, 220);
		c.add(friendPanel);

		// Initialize my friends list
		friendListModel = new ChatRoomListModel();
		friendList = new JList<>(friendListModel);
		friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		friendList.setFont(msgFont);
		friendPanel.add(new JScrollPane(friendList));

		// Add my groups panel
		groupPanel = new JPanel(new BorderLayout());
		label = new JLabel("My Group List", JLabel.CENTER);
		groupPanel.add(label, BorderLayout.NORTH);
		groupPanel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		groupPanel.setBounds(20, 300, 160, 125);
		c.add(groupPanel);

		// Initialize my groups list
		groupListModel = new ChatRoomListModel();
		groupList = new JList<>(groupListModel);
		groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupList.setFont(msgFont);
		groupPanel.add(new JScrollPane(groupList));

		// Add listener to switch between ChatRooms and corresponding messages
		ListSelectionListener switchChatRoomListener = new ListSelectionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChanged(ListSelectionEvent e) {
				JList<ChatRoom> list = (JList<ChatRoom>) e.getSource();
				if (list.getValueIsAdjusting()) {
					return;
				}
				if (!list.isSelectionEmpty()) {
					// Display messages in the ChatRoom
					ChatRoomListModel model = (ChatRoomListModel) list.getModel();
					ChatRoom selectedChatRoom = model.getElementAt(list.getSelectedIndex());
					selectedChatRoomID = selectedChatRoom.cid;
					textArea.setText(selectedChatRoom.messages);
					// Focus the textarea to the bottom (show the latest message)
					textArea.setCaretPosition(textArea.getDocument().getLength());
					// Clear unread count
					model.clearUnreadCount(selectedChatRoomID);
					// Unselect the other list
					if (list == friendList)
						groupList.clearSelection();
					else
						friendList.clearSelection();
				}
			}
		};
		friendList.addListSelectionListener(switchChatRoomListener);
		groupList.addListSelectionListener(switchChatRoomListener);

		// Add listener to set the state of seeGroupMemberButton
		// - No group is selected: seeGroupMemberButton is disabled
		// - One group is selected: seeGroupMemberButton is enabled
		groupList.addListSelectionListener(new ListSelectionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void valueChanged(ListSelectionEvent e) {
				JList<ChatRoom> list = (JList<ChatRoom>) e.getSource();
				if (list.isSelectionEmpty()) {
					seeGroupMemberButton.setEnabled(false);
					seeGroupMemberButton.setIcon(closeEye);
				} else {
					seeGroupMemberButton.setEnabled(true);
					seeGroupMemberButton.setIcon(openEye);
				}
			}
		});

		// find friends button
		findFriendsButton = new JButton(new ImageIcon("image/chatroom/friend_btn.png"));
		findFriendsButton.addActionListener(this);
		findFriendsButton.setBounds(20, 447, 33, 33);
		findFriendsButton.setToolTipText("Find your friend");
		c.add(findFriendsButton);

		// find groups button
		findGroupsButton = new JButton(new ImageIcon("image/chatroom/group_btn.png"));
		findGroupsButton.addActionListener(this);
		findGroupsButton.setToolTipText("Search a group");
		findGroupsButton.setBounds(62, 447, 33, 33);
		c.add(findGroupsButton);

		// see group member button
		seeGroupMemberButton = new JButton(closeEye);
		seeGroupMemberButton.addActionListener(this);
		seeGroupMemberButton.setToolTipText("See the group member");
		seeGroupMemberButton.setBounds(104, 447, 33, 33);
		seeGroupMemberButton.setEnabled(false);
		c.add(seeGroupMemberButton);

		// logout button
		logoutButton = new JButton(new ImageIcon("image/chatroom/logout_btn.png"));
		logoutButton.addActionListener(this);
		logoutButton.setToolTipText("Logout");
		logoutButton.setBounds(144, 447, 33, 33);
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

	/**
	 * Action handling on the buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// get text and clear textField
		if (e.getSource() == sendButton) {
			if (selectedChatRoomID == null) {
				JOptionPane.showMessageDialog(this, "You have not choose a chat room yet :(", "Hint",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			message = textField.getText();
			textField.setText("");
			if (message != "")
				chatClient.sendMessage(selectedChatRoomID, message);
		}
		// find online user
		else if (e.getSource() == findFriendsButton) {
			new ClientSearchFriendGUI();
		}
		// find groups
		else if (e.getSource() == findGroupsButton) {
			new ClientSearchGroupGUI(chatClient);
		} else if (e.getSource() == seeGroupMemberButton) {
			new ClientSeeGroupMember();
		}
		// logout
		// jump to the login page
		else if (e.getSource() == logoutButton) {
			chatClient.logout();
			this.dispose();
			new ClientLoginGUI(chatClient);
		} else if (e.getSource() == exitButton) {
			chatClient.logout();
			System.exit(0);
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

	public void addChatRoom(ChatRoom room) {
		if (room.type == 0)
			friendListModel.addChatRoom(room);
		else
			groupListModel.addChatRoom(room);
	}

	public void addMessage(ChatMessage message) {
		// If the message belongs to the selected ChatRoom, then immediately display it
		if (selectedChatRoomID == message.cid) {
			textArea.append(message.displayMessage());
			textArea.setCaretPosition(textArea.getDocument().getLength());
		}
		friendListModel.addMessage(message, selectedChatRoomID);
		groupListModel.addMessage(message, selectedChatRoomID);
	}
}
