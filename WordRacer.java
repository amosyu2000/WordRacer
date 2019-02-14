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
	JPanel header, footer, body, card1, card2, card2left, card2center, card2right;
	JLabel title, lblInput, lblLives, lblScore;
	JTextField tfInput;
	JButton btnStart;
	CardLayout card;
	
	TimerTask task = new RunGame();
	Timer timer = new Timer();

	long speed = 50L; long totalTime = 0L;
	int intervalMax = 40; int interval = intervalMax; int step = 2; int livesCount = 3; int score = 0; int wordsTyped = 0; int wordIndex = 0;
	int fontSmall = 18; int fontLarge = 40;
	
	JLabel wordObj[] = new JLabel[100];
	boolean wordFlag[] = new boolean[100];
	String lives[] = {"GAME OVER", "Lives | " + '\u2764', "Lives | " + '\u2764'+'\u2764', "Lives | " + '\u2764'+'\u2764'+'\u2764'};
	
	List<String> wordBank = new ArrayList<String>();
	
	List<String> createWordBank (String filename)
	{
		List<String> words = new ArrayList<String>();
		
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String nextWord;
			while ((nextWord = reader.readLine()) != null)
			{
				words.add(nextWord);
			}
			reader.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return words;
	}
	
	int randomInt(int min, int max)
	{
		int diff = max-min;
		return(min + (int) (Math.random() * diff));
	}
	
	boolean checkForClearance(int i, int row)
	{
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
	
	void createWord()
	{
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
		
		wordIndex += randomInt(1, 50);
		if (wordIndex >= wordBank.size())
		{
			wordIndex -= wordBank.size();
		}
		String newWord = wordBank.get(wordIndex);
		
		body.add(wordObj[i]);
		wordObj[i].setText(newWord);
		wordObj[i].setFont(new Font("Monospaced", Font.PLAIN, fontSmall));
		wordObj[i].setForeground(Color.GREEN);
		wordObj[i].setSize(wordObj[i].getPreferredSize());
		
		int row = 50*randomInt(0,11);
		while (checkForClearance (i, row))
		{
			row = 50*randomInt(0,11);
		}
		wordObj[i].setLocation(5, row);
	}
	
	void scrollWord(int i) 
	{
		wordObj[i].setLocation(wordObj[i].getX() + step, wordObj[i].getY());
	}

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
	
	void checkForLifeLost(int i)
	{
		if (wordObj[i].getX() > 1000)
		{
			deleteWord(i);
			livesCount--;
			lblLives.setText(lives[livesCount]);
		}
	}
	
	void deleteWord(int i)
	{
		wordObj[i].setText(" ");
		body.remove(wordObj[i]);
		body.repaint();
		wordFlag[i] = false;
	}
	
	WordRacer()
	{	
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout(20,20));
		
		wordBank = createWordBank("wordBank.txt");
		wordIndex = randomInt(0, wordBank.size());
		
		for (int i = 0; i < wordObj.length; i++)
		{
			wordObj[i] = new JLabel();
		}
		
		header = new JPanel();
		header.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			title = new JLabel("Word Racer");
			header.add(title);
		
		footer = new JPanel();
		card = new CardLayout();
		footer.setLayout(card);
			card1 = new JPanel();
			card1.setLayout(new FlowLayout());
				btnStart= new JButton("Click to Begin");
				btnStart.addActionListener(new ButtonListener());
				card1.add(btnStart);
			footer.add(card1);
			card2 = new JPanel();
			card2.setLayout(new GridLayout(1, 3));
				card2left = new JPanel();
				card2left.setLayout(new FlowLayout(FlowLayout.CENTER));
					lblLives = new JLabel(lives[livesCount]);
					card2left.add(lblLives);
				card2center = new JPanel();
				card2center.setLayout(new FlowLayout(FlowLayout.CENTER));
					lblInput = new JLabel("Enter words: ");
					tfInput = new JTextField(10);
					tfInput.addKeyListener(new TextFieldListener());
					card2center.add(lblInput);
					card2center.add(tfInput);
				card2right = new JPanel();
				card2right.setLayout(new FlowLayout(FlowLayout.CENTER));
					lblScore = new JLabel("Score: " + score);
					card2right.add(lblScore);
				card2.add(card2left);
				card2.add(card2center);
				card2.add(card2right);
			footer.add(card2);
		
		body = new JPanel();
		body.setLayout(new BoxLayout(body, BoxLayout.X_AXIS));
		body.setBackground(Color.BLACK);
			JLabel beginText = new JLabel("<html><center><b><u>Instructions</u></b><br>"
					+ "Type words before they disappear from the screen.<br>"
					+ "The game gets faster over time.<br>"
					+ "You do not need to press Enter or the space bar.</html>", SwingConstants.CENTER);
			beginText.setFont(new Font("Monospaced", Font.PLAIN, fontSmall));
			beginText.setForeground(Color.WHITE);
			beginText.setAlignmentX(0.5f);
		body.add(Box.createGlue());	
		body.add(beginText);
		body.add(Box.createGlue());	
		
		cp.add(new JPanel(), BorderLayout.EAST);
		cp.add(new JPanel(), BorderLayout.WEST);
		cp.add(header, BorderLayout.NORTH);
		cp.add(body, BorderLayout.CENTER);
		cp.add(footer, BorderLayout.SOUTH);
		
		// setting all the fonts
		title.setFont(new Font("", Font.BOLD, fontLarge));
		btnStart.setFont(new Font("", Font.PLAIN, fontSmall));
		lblLives.setFont(new Font("", Font.PLAIN, fontSmall));
		lblInput.setFont(new Font("", Font.PLAIN, fontSmall));
		tfInput.setFont(new Font("", Font.PLAIN, fontSmall));
		lblScore.setFont(new Font("", Font.PLAIN, fontSmall));
		
		setSize(1080,720);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public static void main(String[] args)
	{
		 new WordRacer();
	}

	private class RunGame extends TimerTask
	{
		@Override
		public void run() 
		{
			totalTime += speed;
			if(totalTime <= speed*3)
			{
				createWord();
			}
			if(interval >= intervalMax)
			{
				interval = 1;
				createWord();
			}
			else
			{
				interval++;
			}
			
			for (int i = 0; i < wordObj.length; i++)
			{
				if (wordFlag[i])
				{
					scrollWord(i);
					setWordColour(i);
					checkForLifeLost(i);
				}
			}
			
			if(livesCount == 0)
			{
				cancel();
				tfInput.setEditable(false);
				
				body.removeAll();
				body.setLayout(new BoxLayout(body, BoxLayout.X_AXIS));
				Box endScreen = Box.createVerticalBox();
				JLabel endText = new JLabel("<html><center>Time Played: " + totalTime/1000.0 + " seconds<br>Words Typed: " + wordsTyped + " words<br>Score: " + score + "</html>", SwingConstants.CENTER);
				endScreen.add(endText);
					
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
	
	private class ButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			body.removeAll();
			body.setLayout(null);
			body.repaint();
			
			card.next(footer);
			tfInput.requestFocusInWindow();
			timer.schedule(task, 0L, speed);
		}
	}
	
	private class TextFieldListener implements KeyListener
	{
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
					score += 200 * wordObj[i].getText().length() - wordObj[i].getX();
					lblScore.setText("Score: " + score);
					
					if(score >= Math.pow(2,(step-1)) * 5000)
					{
						step++;
						intervalMax -= 5;
					}
					
					wordsTyped++;
					deleteWord(i);
					break;
				}
			} 
		}
	}
}