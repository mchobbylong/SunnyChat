package client;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;

import javax.swing.*;

public class ClientLoginGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private ChatClient3 client;

	private JLabel backgroud; // background image
	private JButton exitButton, minButton;// exit button and minimize button
	private JButton registerButton;

	private JLabel usernameLabel;// username label
	private JTextField username;// username input field
	private JPasswordField password;// password input field
	private JLabel passwordLabel;// password label
	private JButton loginButton;// login button

	boolean isDragged = false;// record the status of mouse
	private Point frameTemp;// relative location of mouse
	private Point frameLoc;// frame location

	public ClientLoginGUI(ChatClient3 client) {
		// set the client instance
		this.client = client;
		// get container
		Container c = this.getContentPane();
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
		minButton.setBounds(498, 0, 30, 30);
		c.add(minButton);

		// exit button
		exitButton = new JButton(new ImageIcon("image/login/exit_btn.jpg"));
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		exitButton.setBounds(528, 0, 30, 30);
		c.add(exitButton);

		// username label
		usernameLabel = new JLabel("Username:");
		usernameLabel.setForeground(Color.orange);
		usernameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		usernameLabel.setBounds(85, 245, 100, 25);
		c.add(usernameLabel);

		// username input field
		username = new JTextField();
		username.setBounds(179, 245, 200, 25);
		c.add(username);

		// password label
		passwordLabel = new JLabel("Password:");
		passwordLabel.setForeground(Color.orange);
		passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		passwordLabel.setBounds(85, 280, 100, 25);
		c.add(passwordLabel);

		// password input field
		password = new JPasswordField();
		password.setBounds(179, 280, 200, 25);
		c.add(password);

		// login button
		loginButton = new JButton(new ImageIcon("image/login/login_btn.png"));
		loginButton.setBounds(197, 400, 164, 36);
		loginButton.addActionListener(this);
		c.add(loginButton);

		// button for jumping to the register window
		registerButton = new JButton(new ImageIcon("image/login/register.jpg"));
		registerButton.setForeground(Color.orange);
		registerButton.setBounds(150, 490, 265, 25);
		registerButton.addActionListener(this);
		c.add(registerButton);

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
				if (e.getY() < 578)
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getY() < 578) {
					if (isDragged) {
						frameLoc = new Point(getLocation().x + e.getX() - frameTemp.x,
								getLocation().y + e.getY() - frameTemp.y);
						setLocation(frameLoc);
					}
				}
			}
		});

		// Background image
		backgroud = new JLabel(new ImageIcon("image/login/login.jpg"));
		backgroud.setBounds(0, 0, 558, 578);
		c.add(backgroud);

		this.setIconImage(new ImageIcon("image/brown.jpg").getImage());// window icon
		this.setSize(558, 578);// window size
		this.setUndecorated(true);// delete the original frame give by Java
		this.setLocationRelativeTo(null);// show in the middle of the screen
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loginButton) { // �����¼
			String account = username.getText().trim(); // get account
			String pw = new String(password.getPassword());// get password

			if (!"".equals(account) && !"".equals(pw)) { // the account and the password are not empty
				if (client.login(account, pw)) {
					this.dispose();
				} else {
					JOptionPane.showMessageDialog(this, "Your username or password is wrong:(", "Prompt",
							JOptionPane.WARNING_MESSAGE);
				}
			} else if ("".equals(account)) {
				JOptionPane.showMessageDialog(this, "You have not input your username:(", "Prompt",
						JOptionPane.WARNING_MESSAGE);
			} else if ("".equals(pw)) {
				JOptionPane.showMessageDialog(this, "You have not input your password:(", "Prompt",
						JOptionPane.WARNING_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "You have not input your username and password:(", "Prompt",
						JOptionPane.WARNING_MESSAGE);
			}
		}

		// jump to the register window
		if (e.getSource() == registerButton) {
			this.dispose();
			new ClientRegisterGUI(client);
		}
	}
}
