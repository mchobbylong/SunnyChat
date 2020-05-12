package client;
import java.awt.BorderLayout;
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

	private JLabel backgroud; // background image
	private JLabel searchFriend;// username label
	private JButton exitButton,minButton;// exit button and minimize button   
	private JButton addButton;// exit button and minimize button
	private JTextField groupNumber; // group number
	// online users list
	private JPanel onlineList; 
	private DefaultListModel<String> listModel;
    private JList<String> list;  	
	protected JPanel clientPanel, userPanel;
	
	boolean isDragged = false;// record the status of mouse
	private Point frameTemp;// relative location of mouse
	private Point frameLoc;// frame location
 
    
    
	public ClientSearchGroupGUI() {
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
		searchFriend.setBounds(20,20,500,22);
		c.add(searchFriend);
		
		// group number input field: only allows number
		groupNumber = new JFormattedTextField(new java.text.DecimalFormat("#0"));
		groupNumber.setMaximumSize(new java.awt.Dimension(50, 21));
		groupNumber.setMinimumSize(new java.awt.Dimension(50, 21));
		groupNumber.setPreferredSize(new java.awt.Dimension(50, 21));
		groupNumber.setBounds(175,150,150,25);
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
                    	   groupNumber.setText(null);
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
				frameTemp = new Point(e.getX(),e.getY());
				isDragged = true;
				if(e.getY() < 700)
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(e.getY() < 700){
					if(isDragged) {
						frameLoc = new Point(getLocation().x+e.getX()-frameTemp.x,
								getLocation().y+e.getY()-frameTemp.y);
						setLocation(frameLoc);
					}
				}
			}
		});
		
		// Backgroud image
		backgroud = new JLabel(new ImageIcon("image/search/searchPage.jpg"));
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
    	// click exit just close the current window
    	if(e.getSource() == exitButton) {
    		this.dispose();
    	}
    	if(e.getSource() == addButton) {
    		String groupNum = groupNumber.getText().trim(); // get group number
    		if (groupNum != "") {
    			JOptionPane.showMessageDialog(this, "You must input a group number :(", "Prompt", JOptionPane.WARNING_MESSAGE);
    		}
    		else {
    			int groupID = Integer.parseInt(groupNum); 
    			/*查找需补充*/
    		}		
    	}
    }

    public static void main(String[] args) {
        new ClientSearchGroupGUI();
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
        list.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        JScrollPane listScrollPane = new JScrollPane(list);

        clientPanel.add(listScrollPane, BorderLayout.CENTER);
        onlineList.add(clientPanel, BorderLayout.CENTER);
    }
}
