package client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;


public class ClientSearchFriendGUI extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;

	private JLabel backgroud; // background image
	private JLabel searchFriend;// username label
	private JButton exit_btn,min_btn;// exit button and minimize button   
	boolean isDragged = false;// record the status of mouse
	private Point frame_temp;// relative location of mouse
	private Point frame_loc;// frame location
	private JPanel onlineList;
	
    private DefaultListModel<String> listModel;
    private JList<String> list;
    protected JPanel clientPanel, userPanel;
	public ClientSearchFriendGUI() {
		// get container
		Container c = this.getContentPane();
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
		min_btn.setBounds(440, 0, 30, 30);
		c.add(min_btn);
		
		// exit button
		exit_btn = new JButton(new ImageIcon("image/login/exit_btn.jpg"));
		exit_btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		exit_btn.setBounds(470, 0, 30, 30);
		c.add(exit_btn);
		
		// search friend label
		searchFriend = new JLabel("Current Online User List:");
		searchFriend.setForeground(Color.gray);
		searchFriend.setFont(new Font("TimeNewRomes", Font.BOLD, 22));
		searchFriend.setBounds(20,20,500,22);
		c.add(searchFriend);
		
		// online lists
		onlineList = new JPanel(new BorderLayout());
		String[] noClientsYet = {"No online friends now."};
		setClientPanel(noClientsYet);
		onlineList.setBounds(20, 50, 460, 220);
		c.add(onlineList);
		
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
		backgroud = new JLabel(new ImageIcon("image/chatroom/searchPage.jpg"));
		backgroud.setBounds(0,0,500,700);
		c.add(backgroud);		
		
		
		this.setIconImage(new ImageIcon("image/brown.jpg").getImage());// window icon
		this.setSize(500,700);// window size
		this.setUndecorated(true);// delete the original frame give by Java
		this.setLocationRelativeTo(null);// show in the middle of the screen
		this.setVisible(true);
	}

    @Override
    public void actionPerformed(ActionEvent e) {
    	
    }

    public static void main(String[] args) {
        new ClientSearchFriendGUI();
    }
    
    // 用于添加scroll pane的内容 需要时可调用 是遗留财产
    public void setClientPanel(String[] currClients) {  	
    	clientPanel = new JPanel(new BorderLayout());
        listModel = new DefaultListModel<String>();
        
        for(String s : currClients){
        	listModel.addElement(s);
        }
/*        if(currClients.length > 1){
        	privateMsgButton.setEnabled(true);
        }*/
        
        //Create the list and put it in a scroll pane.
        list = new JList<String>(listModel);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(8);
        list.setFont(new Font("TimeNewRomes", Font.PLAIN, 20));
        JScrollPane listScrollPane = new JScrollPane(list);

        clientPanel.add(listScrollPane, BorderLayout.CENTER);
        onlineList.add(clientPanel, BorderLayout.CENTER);
    }
}
