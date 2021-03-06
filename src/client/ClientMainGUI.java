package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
// import javax.swing.filechooser.FileNameExtensionFilter;

import common.*;

public class ClientMainGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JTextArea textField;
	private String message;
	private static Icon openEye = new ImageIcon("image/chatroom/see_group_members.png");
	private static Icon closeEye = new ImageIcon("image/chatroom/unable_see_group_members.png");

	// title label
	private JLabel titleLabel;

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
		fileButton.addActionListener(this);
		fileButton.setBounds(616, 350, 164, 33);
		c.add(fileButton);

		// title (with user's name)
		titleLabel = new JLabel(String.format("Welcome, %s!", client.me.userName), JLabel.RIGHT);
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 22));
		titleLabel.setBounds(200, 0, 520, 30);
		c.add(titleLabel);

		// receive message
		textArea = new JTextArea(8, 34);
		textArea.setMargin(new Insets(10, 10, 10, 10));
		textArea.setFont(msgFont);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(200, 50, 580, 220);
		c.add(scrollPane);

		// Add my friends panel
		friendPanel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("My Friend List", JLabel.CENTER);
		friendPanel.add(label, BorderLayout.NORTH);
		friendPanel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		friendPanel.setBounds(20, 50, 160, 220);

		// Initialize my friends list
		friendListModel = new ChatRoomListModel();
		friendList = new JList<>(friendListModel);
		friendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		friendList.setFont(msgFont);
		friendPanel.add(new JScrollPane(friendList));
		c.add(friendPanel);

		// Add my groups panel
		groupPanel = new JPanel(new BorderLayout());
		label = new JLabel("My Group List", JLabel.CENTER);
		groupPanel.add(label, BorderLayout.NORTH);
		groupPanel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		groupPanel.setBounds(20, 300, 160, 125);

		// Initialize my groups list
		groupListModel = new ChatRoomListModel();
		groupList = new JList<>(groupListModel);
		groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupList.setFont(msgFont);
		groupPanel.add(new JScrollPane(groupList));
		c.add(groupPanel);

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
					// Unselected the other list
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

		// Background image
		backgroud = new JLabel(new ImageIcon("image/chatroom/bg.jpg"));
		backgroud.setBounds(0, 0, 800, 500);
		c.add(backgroud);

		this.setIconImage(new ImageIcon("image/brown.jpg").getImage());// window icon
		this.setSize(800, 500);// window size
		this.setUndecorated(true);// delete the original frame give by Java
		this.setLocationRelativeTo(null);// show in the middle of the screen
		setTitle(String.format("Chat Client for [%s]", client.me.userName));
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
				JOptionPane.showMessageDialog(null, "You have not choose a chat room yet :(", "Hint",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			message = textField.getText();
			textField.setText("");
			if (!"".equals(message))
				chatClient.sendMessage(selectedChatRoomID, message);
		}
		// find online user
		else if (e.getSource() == findFriendsButton) {
			new ClientSearchFriendGUI(chatClient);
		}
		// find groups
		else if (e.getSource() == findGroupsButton) {
			new ClientSearchGroupGUI(chatClient);
		}
		// view group members
		else if (e.getSource() == seeGroupMemberButton) {
			new ClientSeeGroupMember(chatClient.getChatRoomMembers(selectedChatRoomID));
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
		} else if (e.getSource() == fileButton) {
			if (selectedChatRoomID == null) {
				JOptionPane.showMessageDialog(this, "You have not choose a chat room yet :(", "Hint",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			uploadFile();
		}
	}

	private void uploadFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// FileNameExtensionFilter filter = new FileNameExtensionFilter("war", "xml",
		// "txt", "doc", "docx", "jpg", "png");
		// chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(fileButton);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				chatClient.sendFile(selectedChatRoomID, file);
				JOptionPane.showMessageDialog(this, "Successfully upload the file :D", "Hint",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Failed to upload the file :(", "Hint", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void addChatRoom(ChatRoom room) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (room.type == 0)
					friendListModel.addChatRoom(room);
				else
					groupListModel.addChatRoom(room);
			}
		});
	}

	public void addMessage(ChatMessage message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// If the message belongs to the selected ChatRoom, then immediately display it
				if (selectedChatRoomID != null && selectedChatRoomID == message.cid) {
					textArea.append(message.displayMessage());
					textArea.setCaretPosition(textArea.getDocument().getLength());
				}
				friendListModel.addMessage(message, selectedChatRoomID);
				groupListModel.addMessage(message, selectedChatRoomID);
			}
		});
	}
}
