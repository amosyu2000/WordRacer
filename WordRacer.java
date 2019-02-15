import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class WordRacer extends JFrame
{	
	/*	========================
	*	DECLARING GUI COMPONENTS
	    ======================== */
	
	JPanel header, body, footer;

	// JPanels that will be used in footer
	JPanel card1, card2, card2left, card2center, card2right;
	
	// footer uses CardLayout
	CardLayout card;
	
	JLabel title;	
	
	// component for displaying instructions
	JLabel beginText;
	
	// components for displaying player stats (used in footer)
	JLabel lblLives, lblInput, lblScore;
	
	JTextField tfInput;
	
	JButton btnStart;
	
	// components for displaying words
	// 100 word components declared (but not all are used)
	JLabel wordObj[] = new JLabel[100];
	
	// used to store the state of each word
	boolean wordFlag[] = new boolean[100];
	
	
	
	/*	==========================
	*	DECLARING GLOBAL VARIABLES
	    ========================== */

	// instance of RunGame()
	// task will run on loop when the game begins
	TimerTask task = new RunGame();
	Timer timer = new Timer();

	// delay between task loops (in milliseconds)
	long speed = 50L; 
	
	// loops per word
	int intervalMax = 40; int interval = intervalMax; 
	
	// pixels to scroll each word every loop
	int step = 2; 
	
	// player begins with 3 lives
	int livesCount = 3;
	String lives[] = {"GAME OVER", "Lives: " + '\u2764', "Lives: " + '\u2764'+'\u2764', "Lives: " + '\u2764'+'\u2764'+'\u2764'};
	
	// font sizes
	int fontSmall = 18; int fontLarge = 40;
	
	// global variable to store score
	int score = 0; 
	
	// global variable to store total words typed
	int wordsTyped = 0; 
	
	// global variable to store total time elapsed (in milliseconds)
	long totalTime = 0L;
	
	// list that will store a large selection of words
	List<String> wordBank = new ArrayList<String>();
	
	// indexes words in the wordBank list
	int wordIndex = 0;
	
	
	
	/*	=========================
	*			FUNCTIONS
	    ========================= */
	
	// generates random integer in a given range of integers, inclusive
	int randomInt(int min, int max)
	{
		int diff = max-min;
		return(min + (int) (Math.random() * diff));
	}
	
	// checks whether a word will fit on a given row without overlapping another word
	// int i indexes the word being checked
	boolean checkForClearance(int i, int row)
	{
		//compares word i to every other word in the wordObj array
		for (int j = 0; j < wordObj.length; j++)
		{
			if (j != i &&
				wordFlag[j] && 
				row == wordObj[j].getY() && 
				wordObj[j].getX() <= wordObj[i].getWidth() + 50)
			{
				return true;
			}
		}
		return false;
	}

	// creates and prints a new word to the window
	void createWord()
	{
		// find an empty label component to use
		int i = 0;
		for(int j = 0; j < wordFlag.length; j++)
		{
			if (wordFlag[j] == false)
			{
				i = j;
				wordFlag[j] = true;
				break;
			}
		}
		
		// picks a word for the label component
		// process is pseudo-random; picks a random word within 50 words after the previous word
		wordIndex += randomInt(1, 50);
		if (wordIndex >= wordBank.size())
		{
			wordIndex -= wordBank.size();
		}
		
		// sets properties of the label component
		body.add(wordObj[i]);	// adds the label to the frame
		wordObj[i].setText(wordBank.get(wordIndex));	// sets the word that was picked
		wordObj[i].setFont(new Font("Monospaced", Font.PLAIN, fontSmall));	// sets font
		wordObj[i].setForeground(Color.GREEN);	// sets text colour to green
		
		// defines the size of the label as default
		// since the body frame has no layout, this has to be defined manually
		wordObj[i].setSize(wordObj[i].getPreferredSize());
		
		// places the label at a random, available row
		int row = 50*randomInt(0,11);
		while (checkForClearance (i, row))
		{
			row = 50*randomInt(0,11);
		}
		wordObj[i].setLocation(5, row);
	}

	// moves a word to the right by one step 
	// int i indexes the word being moved
	void scrollWord(int i) 
	{
		wordObj[i].setLocation(wordObj[i].getX() + step, wordObj[i].getY());
	}

	// sets the word colour to yellow if the word is past the halfway point
	// sets colour to red if the word is almost at the end of the screen
	void setWordColour(int i)
	{
		if (wordObj[i].getX() > 550)
		{
			wordObj[i].setForeground(Color.YELLOW);
		}
		if (wordObj[i].getX() > 850)
		{
			wordObj[i].setForeground(Color.RED);
		}
	}
	
	// removes a word from the screen
	// int i indexes the word being removed
	void deleteWord(int i)
	{
		wordObj[i].setText(" ");
		body.remove(wordObj[i]);
		body.repaint();
		wordFlag[i] = false;
	}
	
	// if a word has scrolled to the end of the screen
	// delete the word and remove a life
	// int i indexes the word being checked
	void checkForLifeLost(int i)
	{
		if (wordObj[i].getX() > 1000)
		{
			deleteWord(i);
			livesCount--;
			lblLives.setText(lives[livesCount]);
		}
	}

	// fetches wordBank.txt file and compiles the words into a list
	// reads the file one line at a time, and there is one word per line
	List<String> createWordBank (String filename)
	{
		List<String> words = new ArrayList<String>();
		
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String nextWord;
			while ((nextWord = reader.readLine()) != null)
			{
				//appends following word to the list
				words.add(nextWord);
			}
			reader.close();
		} 
		
		// if the .txt file cannot be found, disable the game and print an error message to the window
		catch (IOException e) 
		{
			beginText.setText("<html><center><b><u>ERROR</u></b><br>"
					+ "<b>Missing:</b> <i>wordBank.txt</i> required to generate words.<br>"
					+ "Put <i>wordBank.txt</i> in the same folder as <i>Word Racer v1.2.jar</i><br>"
					+ "then re-open this program.</html>");
			btnStart.setEnabled(false);
		}
		
		return words;
	}
	
	
	
	/*	=========================
	*		   CONSTRUCTOR
	    ========================= */
	
	WordRacer()
	{	
		// top-level container
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(20,20));
		
		// constructs all label components
		for (int i = 0; i < wordObj.length; i++)
		{
			wordObj[i] = new JLabel();
		}
		
		// constructs header
		// header layout is FlowLayout
		header = new JPanel();
		header.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		// adds title to header
		title = new JLabel("Word Racer");
		header.add(title);
		
		
		// constructs body
		// body layout is BoxLayout centered vertically
		body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.X_AXIS));
		body.setBackground(Color.BLACK);
		
		// adds instructions to body
		// sets the instructions text to center alignment
		beginText = new JLabel("<html><center><b><u>Instructions</u></b><br>"
					+ "Type words before they disappear from the screen.<br>"
					+ "The game gets faster over time.<br>"
					+ "You do not need to press Enter or the space bar.</html>", SwingConstants.CENTER);
		beginText.setForeground(Color.WHITE);
		beginText.setAlignmentX(0.5f);
		
		// centers the text horizontally
		body.add(Box.createGlue());	
		body.add(beginText);
		body.add(Box.createGlue());	
		
		
		// constructs footer
		// footer layout is CardLayout
		footer = new JPanel();
		card = new CardLayout();
		footer.setLayout(card);
		
		// the first card contains the start button
		card1 = new JPanel();
		card1.setLayout(new FlowLayout());
		footer.add(card1);
		
		btnStart= new JButton("Click to Begin");	// start button
		btnStart.addActionListener(new ButtonListener());	// adds listener
		card1.add(btnStart);
		
		// the second card contains gameplay components
		// second card uses GridLayout, each cell in the grid contains a panel using FlowLayout
		card2 = new JPanel();
		card2.setLayout(new GridLayout(1, 3));
		footer.add(card2);
		
		// the first cell displays lives
		card2left = new JPanel();
		card2left.setLayout(new FlowLayout(FlowLayout.CENTER));
		card2.add(card2left);
		
		lblLives = new JLabel(lives[livesCount]);
		card2left.add(lblLives);
		
		// the second cell contains the text input for the player
		card2center = new JPanel();
		card2center.setLayout(new FlowLayout(FlowLayout.CENTER));
		card2.add(card2center);
		
		lblInput = new JLabel("Enter words: ");
		card2center.add(lblInput);
		
		tfInput = new JTextField(10);
		tfInput.addKeyListener(new TextFieldListener());	// adds listener
		card2center.add(tfInput);
		
		// the third cell displays the player's score
		card2right = new JPanel();
		card2right.setLayout(new FlowLayout(FlowLayout.CENTER));
		card2.add(card2right);
		
		lblScore = new JLabel("Score: " + score);
		card2right.add(lblScore);
		
		// adds all panels to the top-level container
		// two anonymous panels are created for padding the sides
		cp.add(new JPanel(), BorderLayout.EAST);
		cp.add(new JPanel(), BorderLayout.WEST);
		cp.add(header, BorderLayout.NORTH);
		cp.add(body, BorderLayout.CENTER);
		cp.add(footer, BorderLayout.SOUTH);
		
		// create the word bank by fetching the .txt file
		wordBank = createWordBank("wordBank.txt");
		// assigns a random number as the starting index for the word bank
		wordIndex = randomInt(0, wordBank.size());
		
		// setting all the fonts
		title.setFont(new Font("", Font.BOLD, fontLarge));
		beginText.setFont(new Font("Monospaced", Font.PLAIN, fontSmall));
		btnStart.setFont(new Font("", Font.PLAIN, fontSmall));
		lblLives.setFont(new Font("", Font.PLAIN, fontSmall));
		lblInput.setFont(new Font("", Font.PLAIN, fontSmall));
		tfInput.setFont(new Font("", Font.PLAIN, fontSmall));
		lblScore.setFont(new Font("", Font.PLAIN, fontSmall));
		
		// sets properties of the main frame
		setTitle("Word Racer v1.3");
		setSize(1080,720);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	
	
	/*	===========================
	*		     LISTENERS
	    =========================== */
	
	private class RunGame extends TimerTask
	{
		// loops once the game begins
		@Override
		public void run() 
		{
			// increment the total time by the time of one loop (in milliseconds)
			totalTime += speed;
			
			// create three words at the very start of the game
			if(totalTime <= speed*3)
			{
				createWord();
			}
			
			// create a new word every time the method loops an intervalMax amount of times
			if(interval >= intervalMax)
			{
				interval = 1;
				createWord();
			}
			else
			{
				interval++;
			}
			
			// for every already-existing word, scroll the word,
			// set the word's colour, and check for any life lost
			for (int i = 0; i < wordObj.length; i++)
			{
				if (wordFlag[i])
				{
					scrollWord(i);
					setWordColour(i);
					checkForLifeLost(i);
				}
			}
			
			// if there are zero lives remaining, stop the game
			if(livesCount == 0)
			{
				cancel();
				card2center.removeAll();
				card2center.repaint();
				
				// remove all components from body
				body.removeAll();
				
				// sets body's layout to BoxLayout, centered vertically
				// displays the game stats
				body.setLayout(new BoxLayout(body, BoxLayout.X_AXIS));
				Box endScreen = Box.createVerticalBox();
				JLabel endText = new JLabel("<html><center>Time Played: " + totalTime/1000.0 + " seconds<br>Words Typed: " + wordsTyped + " words<br>Score: " + score + "</html>", SwingConstants.CENTER);
				endScreen.add(endText);
				// centers the game stats horizontally	
				body.add(Box.createHorizontalGlue());
				body.add(endScreen);
				body.add(Box.createHorizontalGlue());
				body.repaint();
				
				endText.setFont(new Font("Monospaced", Font.PLAIN, fontSmall));
				endText.setForeground(Color.WHITE);
				endText.setAlignmentX(0.5f);
			}
		}	
	}

	// runs when the start button is pressed
	private class ButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			body.removeAll();
			body.setLayout(null);
			body.repaint();
			
			// display the next footer card
			card.next(footer);
			
			// place the cursor inside of the text input
			// so that the player doesn't have to click in
			tfInput.requestFocusInWindow();
			
			// begin running the game
			timer.schedule(task, 0L, speed);
		}
	}

	// runs every time something is typed into the text input
	// tests if a word matches what is written in the text input
	// if there is, the word is removed and the text input is cleared
	private class TextFieldListener implements KeyListener
	{
		// not used, but are still defined so that it will compile
		@Override
		public void keyTyped(KeyEvent e) { }
		public void keyPressed(KeyEvent e) { }
		
		@Override
		public void keyReleased(KeyEvent e) 
		{	
			for (int i = 0; i < wordObj.length; i++)
			{
				if (wordFlag[i] && tfInput.getText().equals(wordObj[i].getText()))
				{
					tfInput.setText("");
					
					//increase the score
					// the score is dependent on the length of the word typed and how far
					// the word was along the screen
					score += 200 * wordObj[i].getText().length() - wordObj[i].getX();
					lblScore.setText("Score: " + score);
					
					// the game gets faster when the score reaches
					// 5000, 10 000, 20 000, 40 000, 80 000, etc.
					if(score >= Math.pow(2,(step-1)) * 5000)
					{
						step++;	// words move faster across the screen
						intervalMax -= 5;	// words are created more often
					}
					
					// tallys how many words have been typed
					wordsTyped++;
					
					// removes the word
					deleteWord(i);	
					
					break;
				}
			} 
		}
	}
	
	public static void main(String[] args)
	{
		 new WordRacer();
	}
}