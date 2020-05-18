package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;

public class ClientSeeGroupMember extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JLabel backgroud; // background image
	private JLabel searchFriend;// username label
	private JButton exitButton, minButton;// exit button and minimize button
	// online users list
	private JPanel memberPanel;
	private DefaultListModel<String> listModel;
	private JList<String> list;

	boolean isDragged = false;// record the status of mouse
	private Point frameTemp;// relative location of mouse
	private Point frameLoc;// frame location

	public ClientSeeGroupMember(String[] members) {
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
		searchFriend = new JLabel("Group Member List:");
		searchFriend.setForeground(Color.gray);
		searchFriend.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
		searchFriend.setBounds(20, 20, 500, 50);
		c.add(searchFriend);

		// member panel
		memberPanel = new JPanel(new BorderLayout());
		memberPanel.setBounds(20, 65, 460, 220);
		c.add(memberPanel);

		// Create the list and put it in a scroll pane.
		listModel = new DefaultListModel<String>();
		for (String s : members) {
			listModel.addElement(s);
		}
		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
		memberPanel.add(new JScrollPane(list), BorderLayout.CENTER);
		c.add(memberPanel);

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
	}
}
