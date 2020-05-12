package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * @author
 * RMI Assignment
 *
 */
public class ClientChatRoom extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;	
	private JPanel textPanel, inputPanel, leftPanel;
	private JTextArea textField;
	private String name, message;
	private Font meiryoFont = new Font("Meiryo", Font.PLAIN, 14);
	private Border blankBorder = BorderFactory.createEmptyBorder(10,10,20,10);//top,r,b,l
	private ChatClient3 chatClient;
    private JList<String> list;
    private DefaultListModel<String> listModel;
	private JButton exit_btn,min_btn;// exit button and minimize button   
	private JLabel backgroud;
    protected JTextArea textArea, userArea;
    protected JFrame frame;
    protected JButton privateMsgButton, startButton, sendButton,fileButton, findFriendsButton, findGroupsButton, logoutButton;
    protected JPanel clientPanel, userPanel;

	boolean isDragged = false;// record the status of mouse
	private Point frame_temp;// relative location of mouse
	private Point frame_loc;// frame location
	
	/**
	 * Main method to start client GUI
	 * @param args
	 */
	public static void main(String args[]){
		new ClientChatRoom();
	}//end main
	
	
	/**
	 * GUI Constructor
	 */
	public ClientChatRoom(){
	
		Container c = getContentPane();
		// set the layout
		c.setLayout(null);
		
		// minimize button
		min_btn = new JButton(new ImageIcon("image/login/min_btn.jpg"));
		min_btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setExtendedState(JFrame.ICONIFIED);
			}
		});
		min_btn.setBounds(740, 0, 30, 30);
		c.add(min_btn);
		
		// exit button
		exit_btn = new JButton(new ImageIcon("image/login/exit_btn.jpg"));
		exit_btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		exit_btn.setBounds(770, 0, 30, 30);
		c.add(exit_btn);
		
		// message input text field
		textField = new JTextArea();
		textField.setFont(meiryoFont);
		/*textField.setBounds(220, 50, 580, 220);*/
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
		fileButton.setBounds(616, 417, 164, 33);
		c.add(fileButton);

		// receive message
		textArea = new JTextArea(14, 34);
		textArea.setMargin(new Insets(10, 10, 10, 10));
		textArea.setFont(meiryoFont);	
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textArea);
		textPanel = new JPanel();
		textPanel.add(scrollPane);
		textPanel.setFont(new Font("TimeNewRomes", Font.PLAIN, 15));
		
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
				frame_temp = new Point(e.getX(),e.getY());
				isDragged = true;
				if(e.getY() < 500)
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(e.getY() < 500){
					if(isDragged) {
						frame_loc = new Point(getLocation().x+e.getX()-frame_temp.x,
								getLocation().y+e.getY()-frame_temp.y);
						setLocation(frame_loc);
					}
				}
			}
		});
		
		// Backgroud image
		backgroud = new JLabel(new ImageIcon("image/chatroom/bg.jpg"));
		backgroud.setBounds(0,0,800,500);
		c.add(backgroud);		
		
		
		this.setIconImage(new ImageIcon("image/brown.jpg").getImage());// window icon
		this.setSize(800,500);// window size
		this.setUndecorated(true);// delete the original frame give by Java
		this.setLocationRelativeTo(null);// show in the middle of the screen
		this.setVisible(true);
	}
	
	public JPanel myFriendsPanel() {
		// my friends panel
		userPanel = new JPanel(new BorderLayout());
		String  userStr = " My friends list     ";	
		JLabel userLabel = new JLabel(userStr, JLabel.CENTER);
		userPanel.add(userLabel, BorderLayout.NORTH);
		
		userLabel.setFont(new Font("TimeNewRomes", Font.PLAIN, 16));

		String[] noClientsYet = {"No friends yet"};
		setClientPanel(noClientsYet);
		
		userPanel.setBorder(blankBorder);
		userPanel.setBounds(20, 50, 160, 220);
		return userPanel;
	}

	public JPanel myGroupsPanel() {
		// my groups panel
		userPanel = new JPanel(new BorderLayout());
		String  groupStr = " Your Groups List     ";
		
		JLabel groupLabel = new JLabel(groupStr, JLabel.CENTER);
		userPanel.add(groupLabel, BorderLayout.NORTH);	
		groupLabel.setFont(new Font("TimeNewRomes", Font.PLAIN, 16));

		String[] noClientsYet = {"No groups yet"};
		setClientPanel(noClientsYet);

		clientPanel.setFont(meiryoFont);		
		userPanel.setBorder(blankBorder);
		userPanel.setBounds(20, 300, 160, 110);
		return userPanel;	
	}

    public void setClientPanel(String[] currClients) {  	
    	clientPanel = new JPanel(new BorderLayout());
        listModel = new DefaultListModel<String>();
        
        for(String s : currClients){
        	listModel.addElement(s);
        }
        if(currClients.length > 1){
        	privateMsgButton.setEnabled(true);
        }
        
        //Create the list and put it in a scroll pane.
        list = new JList<String>(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(8);
        list.setFont(meiryoFont);
        JScrollPane listScrollPane = new JScrollPane(list);

        clientPanel.add(listScrollPane, BorderLayout.CENTER);
        userPanel.add(clientPanel, BorderLayout.CENTER);
    }
	
	/**
	 * Action handling on the buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e){
		// get text and clear textField
		if(e.getSource() == sendButton){
			message = textField.getText();
			textField.setText("");
			System.out.println("Sending message : " + message);
		}
		// find online user
		if(e.getSource() == findFriendsButton){
			this.dispose();
			new ClientSearchFriendGUI();
		}
		// find groups
		if(e.getSource() == findGroupsButton){

		}
		// logout
		// jump to the login page
		if(e.getSource() == logoutButton){
			this.dispose();
			new ClientLoginGUI();
		}
	}
	
	/*file transportation*/
	private void eventOnImport(JButton fileButton) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("war","xml", "txt", "doc", "docx","jpg","png");
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
					// p.s. 这个if可以删掉 看需求
					if (set.contains(f.getName())) {// indicate whether the file has been existed in the system
						JOptionPane.showMessageDialog(new JDialog(),
								f.getName() + ":The selected file is already exist！");
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
					chatClient.serverIF.updateChat(name, successfulHint);
				}	
				JOptionPane.showMessageDialog(null, "Upload successfully. :)", "Hint",
						JOptionPane.INFORMATION_MESSAGE);
				} catch (FileNotFoundException e1) {
					JOptionPane.showMessageDialog(null, "Failed to upload. :(", "Hint",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Failed to upload. :(", "Hint",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
	}

}