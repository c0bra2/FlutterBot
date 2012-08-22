import java.io.*;
import java.net.*;

/**
	@author c0bra2 (Jacob Mason)
	this class represents an irc bot framework.
	this code is protected under GPL
*/
public class JHat
{
	private Socket sock;
	private BufferedWriter out;
	private BufferedReader in;
	private String channel;
	private String botNick;
	private String rawString;
	private String [] whichChan;
	private String [] talker;
	private String [] line;
	private boolean connected = false;

	public JHat(String aNick, String aServer, String aChan, int aPort)
	{
		try
		{
			// setup connection and i/o streams
			System.out.println("Initializing...");
			sock = new Socket(aServer, aPort);
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			// tell the server who the bot is
			if (sock.isConnected())
			{
				connected = true;
				System.out.println("[Connected Successfully]");
				send("NICK " + aNick + "\r\n");
				send("USER " + aNick + " " + aServer + " bla :" + aNick +
				"\r\n");
				joinChan(aChan);

				// store botNick
				botNick = aNick;
			}

		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
		Joins to a channel
		@param chan channel to join 
	*/
	public void joinChan(String chan)
	{
		channel = chan;
		send(" JOIN " + chan + "\r\n");
	}
	
	/**
		parse method, parses data from the server
		should be called before accessor methods like getLine()
		parse() also takes care of any PING request
	*/ 
	public void parse()
	{
		rawString = recv();
		line = rawString.split(":");
		if (line.length >= 3)
			line = line[2].split("\\s");
		
		// get channel of person talking
		whichChan = rawString.split(":");
		if (whichChan.length >= 2)
			whichChan = whichChan[1].split("\\s");

		// get nick of the person talking
		talker = rawString.split(":");
		if (talker.length >= 2)
			if (talker[1].contains("!"))
				talker = talker[1].split("!");

		// take care of PING request
		if (rawString.contains("PING"))
			pong();
	}

	/**
		write shit 2 irc chan
		@param data the shit to write
	*/
	public void write(String data)
	{
		send("PRIVMSG " + channel + " :" + data + "\r\n");
	}

	/**
		send data to the server.
		similar to send(), but for
		use when you need to use
		MODE n stuff
		@param data the stuff to send
	*/
	public void put(String data)
	{
		send(data + "\r\n");
	}

	/**
		accessor method for connected
	*/
	public boolean getIsConnected()
	{
		return connected;
	}
	
	/**
		accessor method for talker
	*/
	public String getTalker()
	{
		return talker[0];
	}

	/**
		accessor method for initialized channel
	*/
	public String getChan()
	{
		return channel;
	}
	
	/**
		accessor method for whichChan
	*/
	public String getWhichChan()
	{
		if (whichChan.length >= 3)
			return whichChan[2];
		return "";
	}

	/**
		accessor method for line
	*/
	public String[] getLine()
	{
		return line;
	}

	/**
		accessor method for channel and nick, returned as array
	*/
	public String[] getSelf()
	{
		String [] res = {botNick, channel};
		return res;
	}

	/**
		accessor method for rawString
	*/
	public String getRS()
	{
		return rawString;
	}

	public void pong()
	{
		send("PONG " + wordParse(rawString, 1) + "\r\n");
	}

	/**
		split a string along whitespaces
		and returns the word at a specific position
		@param text sting to parse
		@param pos return word at this position
	*/
	private String wordParse(String text, int pos)
	{
		String [] words = text.split("\\s");
		return words[pos];
	}

	private void send(String data)
	{
		try 
		{
			out.write(data);
			out.flush();
		}

		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private String recv()
	{
		String data = "";

		try
		{	
			data = in.readLine();
			return data;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return data;
	}
}