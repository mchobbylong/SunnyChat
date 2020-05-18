package client;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import java.text.ParseException;

import javax.swing.*;

public class ClientSearchGroupGUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private Client client;

	private JLabel backgroud; // background image
	private JLabel searchFriend;// username label
	private JButton exitButton, minButton;// exit button and minimize button
	private JButton addButton;// exit button and minimize button
	private JTextField groupNumber; // group number
	protected JPanel clientPanel, userPanel;

	boolean isDragged = false;// record the status of mouse
	private Point frameTemp;// relative location of mouse
	private Point frameLoc;// frame location

	public ClientSearchGroupGUI(Client client) {
		// set the client
		this.client = client;
		// get container
		Container c = this.getContentPane();
		// set the layout
		c.setLayout(null);

		// minimize button
		minButton = new JButton(new ImageIcon("image/search/minimize_btn.png"));
		minButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setExtendedState(JFrame.ICONIFIED);
			}
		});
		minButton.setBounds(440, 0, 30, 30);
		c.add(minButton);

		// exit button
		exitButton = new JButton(new ImageIcon("image/search/exit_btn.png"));
		exitButton.addActionListener(this);
		exitButton.setBounds(470, 0, 30, 30);
		c.add(exitButton);

		// search friend label
		searchFriend = new JLabel("Input your group number:");
		searchFriend.setForeground(Color.gray);
		searchFriend.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
		searchFriend.setBounds(20, 20, 500, 50);
		c.add(searchFriend);

		// group number input field: only allows number
		groupNumber = new JFormattedTextField(new java.text.DecimalFormat("#0"));
		groupNumber.setMaximumSize(new java.awt.Dimension(50, 21));
		groupNumber.setMinimumSize(new java.awt.Dimension(50, 21));
		groupNumber.setPreferredSize(new java.awt.Dimension(50, 21));
		groupNumber.setBounds(175, 150, 150, 25);
		groupNumber.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent evt) {
				String old = groupNumber.getText();
				JFormattedTextField.AbstractFormatter formatter = ((JFormattedTextField) groupNumber).getFormatter();
				if (!old.equals("")) {
					if (formatter != null) {
						String str = groupNumber.getText();
						try {
							long page = (Long) formatter.stringToValue(str);
							groupNumber.setText(page + "");
						} catch (ParseException pe) {
							groupNumber.setText("");
						}
					}
				}
			}
		});
		c.add(groupNumber);

		// send button
		addButton = new JButton(new ImageIcon("image/search/join_btn.jpg"));
		addButton.addActionListener(this);
		addButton.setBounds(200, 200, 100, 24);
		c.add(addButton);

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
				if (e.getY() < 700)
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getY() < 700) {
					if (isDragged) {
						frameLoc = new Point(getLocation().x + e.getX() - frameTemp.x,
								getLocation().y + e.getY() - frameTemp.y);
						setLocation(frameLoc);
					}
				}
			}
		});

		// Background image
		backgroud = new JLabel(new ImageIcon("image/search/searchPage.jpg"));
		backgroud.setBounds(0, 0, 500, 700);
		c.add(backgroud);

		this.setIconImage(new ImageIcon("image/brown.jpg").getImage());// window icon
		this.setSize(500, 700);// window size
		this.setUndecorated(true);// delete the original frame give by Java
		this.setLocationRelativeTo(null);// show in the middle of the screen
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// click exit just close the current window
		if (e.getSource() == exitButton) {
			this.dispose();
		}
		if (e.getSource() == addButton) {
			String groupNum = groupNumber.getText(); // get group number
			if ("".equals(groupNum)) {
				JOptionPane.showMessageDialog(this, "You must input a group number :(", "Prompt",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			int cid = Integer.parseInt(groupNum);
			if (cid < 1 || cid > 9999) {
				JOptionPane.showMessageDialog(this, "Group number must be between 1 and 9999 :(", "Prompt",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (client.joinGroup(cid)) { // prompt for success
				JOptionPane.showMessageDialog(this, "Successfully joined :D", "Prompt", JOptionPane.PLAIN_MESSAGE);
				this.dispose();
			} else {
				JOptionPane.showMessageDialog(this, "You are already in this group :(", "Prompt",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
