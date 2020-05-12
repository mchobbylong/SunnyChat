package client;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;

public class ClientLoginGUI extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;

	private JLabel backgroud; // background image
	private JButton exit_btn,min_btn;// exit button and minimize button
	private JButton register_btn;
	
	private JLabel username_label;// username label
	private JTextField username;// username input field
	private JPasswordField password;// password input field
	private JLabel password_label;// password label
	private JButton login_btn;// login button
	
	boolean isDragged = false;// record the status of mouse
	private Point frame_temp;// relative location of mouse
	private Point frame_loc;// frame location

	public ClientLoginGUI() {
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
		min_btn.setBounds(498, 0, 30, 30);
		c.add(min_btn);
		
		// exit button
		exit_btn = new JButton(new ImageIcon("image/login/exit_btn.jpg"));
		exit_btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		exit_btn.setBounds(528, 0, 30, 30);
		c.add(exit_btn);
		
		// username label
		username_label = new JLabel("Username:");
		username_label.setForeground(Color.orange);
		username_label.setBounds(85,245,100,25);
		c.add(username_label);
				
		// username input field
		username = new JTextField();
		username.setBounds(179,245,200,25);
		c.add(username);
		
		// password label
		password_label = new JLabel("Password:");
		password_label.setForeground(Color.orange);
		password_label.setBounds(85,280,100,25);
		c.add(password_label);
				
		// password input field
		password = new JPasswordField();
		password.setBounds(179,280,200,25);
		c.add(password);
		
		// login button
		login_btn = new JButton(new ImageIcon("image/login/login_btn.png"));
		login_btn.setBounds(197,400,164,36);
		login_btn.addActionListener(this);
		c.add(login_btn);
		
		register_btn = new JButton(new ImageIcon("image/login/register.jpg"));
		register_btn.setForeground(Color.orange);
		register_btn.setBounds(150,490,265,25);
		register_btn.addActionListener(this);
		c.add(register_btn);
		
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
				if(e.getY() < 578)
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(e.getY() < 578){
					if(isDragged) {
						frame_loc = new Point(getLocation().x+e.getX()-frame_temp.x,
								getLocation().y+e.getY()-frame_temp.y);
						setLocation(frame_loc);
					}
				}
			}
		});
		
		// Backgroud image
		backgroud = new JLabel(new ImageIcon("image/login/login.jpg"));
		backgroud.setBounds(0,0,558,578);
		c.add(backgroud);		
		
		
		this.setIconImage(new ImageIcon("image/brown.jpg").getImage());// window icon
		this.setSize(558,578);// window size
		this.setUndecorated(true);// delete the original frame give by Java
		this.setLocationRelativeTo(null);// show in the middle of the screen
		this.setVisible(true);
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == login_btn){ // 点击登录
        	String account = username.getText().trim(); // get account
        	String pw = new String(password.getPassword());// get password
        	
        	if(!"".equals(account) && !"".equals(pw)) { // the account and the password are not empty
        		/* 
            	 * 
            	 * 此处应当判断登陆条件是否正确 
            	 * 如正确执行下两行代码
            	 * 
            	 * 
            	 */
            	this.dispose(); // 登录页面消失
            	new ClientChatRoom(); // 创建新GUI页面: 聊天室
        	} else if ("".equals(account)) {
        		JOptionPane.showMessageDialog(this, "You have not input your username:(", "Prompt", JOptionPane.WARNING_MESSAGE);
        	} else if ("".equals(pw)) {
        		JOptionPane.showMessageDialog(this, "You have not input your password:(", "Prompt", JOptionPane.WARNING_MESSAGE);
        	} else {
        		JOptionPane.showMessageDialog(this, "You have not input your username and password:(", "Prompt", JOptionPane.WARNING_MESSAGE);
        	} 	
        }
        
        // jump to the register window
        if(e.getSource() == register_btn) {
        	this.dispose();
        	new ClientRegisterGUI();
        }
    }

    public static void main(String[] args) {
        new ClientLoginGUI();
    }

}
