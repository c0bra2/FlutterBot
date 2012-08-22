import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
	@author applebloom
	this code is protected under GPL
*/
public class FlutterBot 
{
	public static void main(String [] args)
	{
		FlutterFrame frame = new FlutterFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
}

class FlutterFrame extends JFrame 
{
	final static int WIDTH = 300;
	final static int HEIGHT = 300;
	private int screenWidth;
	private int screenHeight;
	private JTextField botname;
	private JTextField botowner;
	private JTextField server;
	private JTextField channel;
	private JTextArea textArea;
	private JLabel discript;
	private JLabel out;
	private String text;
	private String [] botOwners = new String[10];
	private String [] shitList = new String[100];
	private String [] blackList = new String[100];
	private JButton sayIt;
	private JButton connect;
	private JButton exit;
	private boolean saidSomething = false;
	private int columnWidth = 25;

	public FlutterFrame()
	{
		//basic flutterbot frame window properties
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		screenHeight = screenSize.height;
		screenWidth = screenSize.width;
		setTitle("FlutterBot v1.0.0");
		setSize(WIDTH,HEIGHT);
		setLocation(screenWidth / 4, screenHeight / 4);
		Container contentPane = getContentPane();

		// add panels with text fields for
		// botname, server, channel, and botowner
		JPanel panel1 = new JPanel();
		botname = new JTextField("botname", columnWidth);
		botname.addMouseListener(new MouseInText1());
		panel1.add(botname);

		JPanel panel2 = new JPanel();
		server = new JTextField("irc.us.abjects.net", columnWidth);
		server.addMouseListener(new MouseInText2());
		panel2.add(server);

		JPanel panel3 = new JPanel();
		channel = new JTextField("#fightclub", columnWidth);
		channel.addMouseListener(new MouseInText3());
		panel3.add(channel);

		JPanel panel4 = new JPanel();
		botowner = new JTextField("botOwner", columnWidth);
		botowner.addMouseListener(new MouseInText4());
		panel4.add(botowner);

		// textArea and button panel
		JPanel textPanel = new JPanel();
		textArea = new JTextArea(3, columnWidth);
		textArea.setEnabled(false);
		textArea.setText("written by applebloom (fuzzy) \n2012");
		textArea.setLineWrap(true);
		textArea.addMouseListener(new MouseInText5());
		textPanel.add(textArea);
		JPanel buttonPanel = new JPanel();
		sayIt = new JButton("Say It!");
		sayIt.setEnabled(false);
		sayIt.addActionListener(new SayListener());
		connect = new JButton("Connect");
		connect.addActionListener(new ConnectIRC());
		exit = new JButton("Exit");			
		exit.addActionListener(new ExitProgram());
		buttonPanel.add(sayIt);
		buttonPanel.add(connect);
		buttonPanel.add(exit);

		// channel output labels
		discript = new JLabel("Input:", JLabel.LEFT);
		out = new JLabel("Not connected!", JLabel.LEFT);

		// top level panel
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(panel1, BorderLayout.NORTH);
		topPanel.add(panel2, BorderLayout.CENTER);
		topPanel.add(panel3, BorderLayout.SOUTH);

		// center panel
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(panel4, BorderLayout.NORTH);
		centerPanel.add(textPanel, BorderLayout.CENTER);
		centerPanel.add(buttonPanel, BorderLayout.SOUTH);

		// bottom panel
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(discript, BorderLayout.CENTER);
		bottomPanel.add(out, BorderLayout.SOUTH);

		// add topPanel to contentPane
		contentPane.add(topPanel, BorderLayout.NORTH);
		contentPane.add(centerPanel, BorderLayout.CENTER);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	private class SayListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			saidSomething = true;
			text = textArea.getText();
			textArea.setText("Waiting for server...");
			textArea.setEnabled(false);
		}
	}

	private class ConnectIRC implements ActionListener, Runnable
	{
		JHat bot;

		public void actionPerformed(ActionEvent event)
		{

			bot = new JHat(botname.getText(), server.getText(),
				channel.getText(), 6667);

			// disable connection button and enable sayIt and 
			// text area
			connect.setEnabled(false);
			sayIt.setEnabled(true);
			textArea.setEnabled(true);
			textArea.setText("Say something...");
			discript.setText("Input:");
			if (bot.getIsConnected())
				out.setText("Successfully connected!");
			else
				out.setText("Connection Error!");

			// add first bot owner
			botOwners[0] = botowner.getText();

			// start bot
			Thread t = new Thread(this);
			t.start();
		}

		public void run()
		{
			String [] line;
			boolean lockdown = false;

			while (true)
			{
				bot.parse();
				line = bot.getLine();
				System.out.println(bot.getRS());

				// irc output to GUI
				if (bot.getChan().contains("#"))
				{
					discript.setText(bot.getChan() + " " 
						+ bot.getTalker());
					out.setText(ArrayOperations.lineToString(line));
				}

				// bot user wants to say something
				if (saidSomething)
				{
					bot.write(text);
					textArea.setText("Say something...");
					textArea.setEnabled(true);
					saidSomething = false;
				}

				if (bot.getRS().contains("JOIN"))
				{
					if (ArrayOperations.arrayContains(bot.getTalker(), botOwners))
					{
						// what to do if an owner joins the channel
						bot.put("MODE " + bot.getChan() + " +o "
							+ bot.getTalker());
						bot.put("MODE " + bot.getChan() + " +a "
							+ bot.getTalker());
					}
					
					if (ArrayOperations.arrayContains(bot.getTalker(), shitList))
					{
						// shitListed joins channel
						bot.put("MODE " + bot.getChan() + " -a "
							+ bot.getTalker());
						bot.put("MODE " + bot.getChan() + " -o "
							+ bot.getTalker());
						bot.put("MODE " + bot.getChan() + " +v "
							+ bot.getTalker());
					}
					
					if (ArrayOperations.arrayContains(bot.getTalker(), blackList))
					{
						// blackListed joins channel
						bot.put("MODE " + bot.getChan() + " +b "
							+ bot.getTalker());
						bot.put("KICK " + bot.getChan() + " "
							+ bot.getTalker() + " no");
					}

					if (lockdown)
						if (!ArrayOperations.arrayContains(bot.getTalker(), botOwners))
						{
							bot.put("MODE " + bot.getChan() + " -a "
								+ bot.getTalker());
							bot.put("MODE " + bot.getChan() + " -o "
								+ bot.getTalker());
							bot.put("MODE " + bot.getChan() + " +v "
								+ bot.getTalker());
						}
				}
					// main bot logic
				if (ArrayOperations.arrayContains(bot.getTalker(), botOwners))
				{
					if (line[0].equals("!quit"))
					{
						bot.write("cya later " + 
							bot.getTalker() + " :D");
						System.exit(0);
					}

					if (line[0].equals("!shitlist"))
					{
						if (line.length >= 2)
						{
							if (line[1].equals("add"))
							{
								if (line.length >= 3)
								{
									ArrayOperations.arrayAdd(line[2], shitList);
									bot.write(line[2] + " added to shitlist");
								}
							}
							else if (line[1].equals("remove"))
							{
								if (line.length >= 3)
								{
									ArrayOperations.arrayPop(line[2], shitList);
									bot.write(line[2] + " removed from shitlist");
								}
							}
							else if (line[1].equals("show"))
								bot.write(ArrayOperations.lineToString(shitList));
						}
					}
			
					if (line[0].equals("!blacklist"))
					{
						if (line.length >= 2)
							if (line[1].equals("add"))
							{
								if (line.length >= 3)
								{
									ArrayOperations.arrayAdd(line[2], blackList);
									bot.write(line[2] + " added to blacklist");
								}
							}
							else if (line[1].equals("remove"))
							{
								if (line.length >= 3)
								{
									ArrayOperations.arrayPop(line[2], blackList);
									bot.write(line[2] + " removed from blacklist");
								}
							}
							else if (line[1].equals("show"))
								bot.write(ArrayOperations.lineToString(blackList));
					}

					if (line[0].equals("!ownerlist"))
					{
						if (line.length >= 2)
						{
							if (line[1].equals("add"))
							{
								if (line.length >= 3)
								{
									ArrayOperations.arrayAdd(line[2], botOwners);
									bot.write(line[2] + " added to owners");
								}
							}
							else if (line[1].equals("remove"))
							{
								if (line.length >= 3)
								{
									ArrayOperations.arrayPop(line[2], botOwners);
									bot.write(line[2] + " removed from owners");
								}
							}
							else if (line[1].equals("show"))
								bot.write(ArrayOperations.lineToString(botOwners));
						}
					}

					if (line[0].equals("!join"))
					{
						if (line.length >= 2)
							bot.joinChan(line[1]);
					}

					if (line[0].equals("!op"))
					{
						bot.put("MODE " + bot.getChan() + " +o "
							+ bot.getTalker());
						bot.put("MODE " + bot.getChan() + " +a "
							+ bot.getTalker());
					}

					if (line[0].equals("!lockdown"))
					{
						lockdown = true;
						bot.write("lockdown mode enabled");
					}

					if (line[0].equals("!unlock"))
					{
						lockdown = false;
						bot.write("lockdown mode disabled");
					}
				}
			}
		}
	}

	/**
		one of the five MouseInText adapters which are
		meant to listen if a user clicks into a textbox
		in which case i want the previous text it was holding
		to disappear so the user can enter their own.
	*/
	private class MouseInText1 extends MouseAdapter
	{
		public void mouseClicked(MouseEvent event)
		{
			botname.setText("");
		}
	}

	private class MouseInText2 extends MouseAdapter
	{
		public void mouseClicked(MouseEvent event)
		{
			server.setText("");
		}
	}

	private class MouseInText3 extends MouseAdapter
	{
		public void mouseClicked(MouseEvent event)
		{
			channel.setText("");
		}
	}

	private class MouseInText4 extends MouseAdapter
	{
		public void mouseClicked(MouseEvent event)
		{
			botowner.setText("");
		}
	}

	private class MouseInText5 extends MouseAdapter
	{
		public void mouseClicked(MouseEvent event)
		{
			textArea.setText("");
		}
	}

	private class ExitProgram implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			System.exit(0);
		}
	}
}

class ArrayOperations
{
	public static String lineToString(String [] lineArray)
	{
		// line [] to string

		String string = "";
		for (int i = 0; i < lineArray.length; i++)
		{	
			if (lineArray[i] != null)
				string = string + lineArray[i] + " ";
		}
		if (string.equals(""))
			return "Nothing in the list";
		return string;
	}

	public static void arrayAdd(String nick, String [] array)
	{
		int sub = 0;

		while (array[sub] != null)
		{
			sub++;
		}

		if (array[sub] == null)
			array[sub] = nick;
	}

	public static boolean arrayContains(String nick, String [] array)
	{
		int sub = 0;

		while (array[sub] != null)
		{
			if (array[sub].equals(nick))
				return true;
			sub++;
		}
		return false;
	}

	public static String[] arrayPop(String pop, String [] array)
	{
		String [] copyFrom = array;
		String [] copyTo = new String[array.length];
		int sub = 0;

		sub = 0;
		while (sub < array.length)
		{
			if (array[sub] != null)
			{
				if (array[sub].equals(pop))
				{
					array[sub] = null;
					sub = array.length;
				}
			}
			
			sub++;
		}

		sub = 0;
		for (int i = 0; i < copyTo.length; i++)
			if (copyFrom[i] != null)
			{
				copyTo[sub] = copyFrom[i];
				sub++;
			}
		array = copyTo;
		return array;
	}
}