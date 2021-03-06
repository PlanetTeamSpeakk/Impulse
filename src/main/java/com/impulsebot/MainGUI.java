package com.impulsebot;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.collect.Lists;
import com.impulsebot.Main.LogType;
import com.impulsebot.commands.Main;
import com.impulsebot.miscellaneous.JGraphPanel;
import com.impulsebot.utils.Config;
import com.impulsebot.utils.MathHelper;
import com.impulsebot.utils.UsageMonitorer;

/**
 * @deprecated This class is no longer used, take a look at
 *             {@link com.impulsebot.MainJFXGUI MainJFXGUI} instead.
 * @author PlanetTeamSpeak
 */
@Deprecated
public class MainGUI {

	public static final Font				sansSerif		= new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	private static boolean					initialized		= false;
	private static JFrame					mainFrame		= new JFrame();
	private static JTextArea				LTA				= new JTextArea();															// Log TextArea
	private static Map<String, JTextField>	configKeys		= new HashMap();
	private static final DecimalFormat		df				= new DecimalFormat("#", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	private static final String				impulsePngMD5	= "f71c6df8e8f8b70a366c54d2f88e4b2d";
	private static boolean					hasPngChanged	= true;
	private static final JRadioButton		devModeRBtn		= new JRadioButton("Devmode");

	/**
	 * Initialize the contents of the frame.
	 *
	 * @throws IllegalAccessException
	 */
	public static void initialize() throws IllegalAccessException {
		if (!initialized) {
			// Just checking if the file has changed, if not, set the icon and start
			// normally, if it has, don't and passive-aggressively ask the user to change it
			// back.
			// Because maybe they've changed the icon and are claiming they've made it.
			String MD5 = null;
			try {
				MD5 = DigestUtils.md5Hex(Main.getResourceAsStream("impulse.png"));
			} catch (IOException e2) {
			}
			if (MD5 != null && impulsePngMD5.equals(MD5)) try {
				hasPngChanged = false;
				mainFrame.setIconImage(ImageIO.read(Main.getResourceAsStream("impulse.png")));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Main.print(LogType.DEBUG, "Original MD5:", impulsePngMD5, "Current MD5:", MD5);
			mainFrame.setTitle("Impulse v" + Main.version + " - written by PlanetTeamSpeak");
			mainFrame.setResizable(false);
			mainFrame.setBounds(0, 0, 1080, 720);
			mainFrame.setAutoRequestFocus(true);
			mainFrame.getContentPane().setLayout(null);
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setBounds(0, 0, (int) mainFrame.getBounds().getWidth() - 6, (int) mainFrame.getBounds().getHeight() - 29);
			FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
			if (!hasPngChanged) {
				LTA.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
				LTA.setEditable(false);
				LTA.setBounds(0, 0, (int) mainFrame.getBounds().getWidth() - 9, (int) mainFrame.getBounds().getHeight() - 56);
				((DefaultCaret) LTA.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

				JPanel logPane = new JPanel();
				tabbedPane.addTab("Log", null, logPane, "The log.");
				logPane.setLayout(null);

				JScrollPane scrollPane = new JScrollPane(LTA);
				scrollPane.setBounds(LTA.getBounds());
				scrollPane.setAutoscrolls(true);
				logPane.add(scrollPane);

				JPanel settings = new JPanel();
				settings.setLayout(null);
				int pos = 0;
				for (String key : Config.INSTANCE.getKeys()) {
					JTextField textField = new JTextField(key);
					int width = (int) sansSerif.getStringBounds(key, frc).getWidth() + 8;
					int height = (int) sansSerif.getStringBounds(key, frc).getHeight() + 2;
					textField.setFont(sansSerif);
					textField.setBounds(5, pos, width, height);
					textField.setBorder(BorderFactory.createEmptyBorder());
					textField.setBackground(SystemColor.menu);
					textField.setEditable(false);
					settings.add(textField);
					pos += 20;
					JTextField textField1 = new JTextField(Config.INSTANCE.get(key));
					int width1 = (int) sansSerif.getStringBounds(Config.INSTANCE.get(key), frc).getWidth() + 8;
					int height1 = (int) sansSerif.getStringBounds(Config.INSTANCE.get(key), frc).getHeight() + 4;
					textField1.setFont(sansSerif);
					textField1.setBounds(5, pos, width1 < 16 ? 16 : width1, height1);
					textField1.addKeyListener(new KeyListener() {
						@Override
						public void keyTyped(KeyEvent e) {
							update();
						}

						@Override
						public void keyReleased(KeyEvent e) {
							update();
						}

						@Override
						public void keyPressed(KeyEvent e) {
							update();
						}

						private void update() {
							int newWidth = (int) sansSerif.getStringBounds(textField1.getText(), frc).getWidth() + 8;
							textField1.setBounds(5, (int) textField1.getBounds().getY(), newWidth < 16 ? 16 : newWidth, height1);
						}
					});
					configKeys.put(key, textField1);
					settings.add(textField1);
					pos += 20;
				}
				JTextField gameTF = new JTextField("Game");
				int width = (int) sansSerif.getStringBounds("Game", frc).getWidth() + 8;
				int height = (int) sansSerif.getStringBounds("Game", frc).getHeight() + 2;
				gameTF.setFont(sansSerif);
				gameTF.setBounds(5, pos, width, height);
				gameTF.setBorder(BorderFactory.createEmptyBorder());
				gameTF.setBackground(SystemColor.menu);
				gameTF.setEditable(false);
				settings.add(gameTF);
				pos += 20;
				JTextField gameTF1 = new JTextField(Main.devMode() ? "DEVELOPER MODE" : "try " + Config.INSTANCE.get("prefix") + "help");
				int width1 = (int) sansSerif.getStringBounds(gameTF1.getText(), frc).getWidth() + 8;
				int height1 = (int) sansSerif.getStringBounds(gameTF1.getText(), frc).getHeight() + 4;
				gameTF1.setFont(sansSerif);
				gameTF1.setBounds(5, pos, width1 < 16 ? 16 : width1, height1);
				gameTF1.addKeyListener(new KeyListener() {
					@Override
					public void keyTyped(KeyEvent e) {
						update();
					}

					@Override
					public void keyReleased(KeyEvent e) {
						update();
					}

					@Override
					public void keyPressed(KeyEvent e) {
						update();
					}

					private void update() {
						int newWidth = (int) sansSerif.getStringBounds(gameTF1.getText(), frc).getWidth() + 8;
						gameTF1.setBounds(5, (int) gameTF1.getBounds().getY(), newWidth < 16 ? 16 : newWidth, height1);
					}
				});
				settings.add(gameTF1);
				pos += 28;
				JButton saveBtn = new JButton("Save");
				saveBtn.setFont(sansSerif);
				saveBtn.setBounds(5, pos, 48, 24);
				saveBtn.setMargin(new Insets(0, 0, 0, 0));
				saveBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for (String key : configKeys.keySet())
							Config.INSTANCE.put(key, configKeys.get(key).getText());
						if (Main.done()) Main.setGame(gameTF1.getText());
						Main.playSound(Main.getResourceAsStream("chimes.wav"));
					}
				});
				settings.add(saveBtn);
				devModeRBtn.setFont(sansSerif);
				devModeRBtn.setSelected(Main.devMode());
				devModeRBtn.setBounds(94, pos, 128, 24);
				devModeRBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Main.devMode(devModeRBtn.isSelected());
						Main.print(LogType.INFO, "Devmode has been", devModeRBtn.isSelected() ? "enabled." : "disabled.");
					}
				});
				settings.add(devModeRBtn);
				pos += 32;
				JButton refreshBtn = new JButton("Refresh");
				refreshBtn.setFont(sansSerif);
				refreshBtn.setBounds(5, pos, 66, 24);
				refreshBtn.setMargin(new Insets(0, 0, 0, 0));
				refreshBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for (String key : configKeys.keySet()) {
							configKeys.get(key).setText(Config.INSTANCE.get(key));
							int newWidth = (int) sansSerif.getStringBounds(configKeys.get(key).getText(), frc).getWidth() + 8;
							configKeys.get(key).setBounds(5, (int) configKeys.get(key).getBounds().getY(), newWidth < 16 ? 16 : newWidth, (int) configKeys.get(key).getBounds().getHeight());
						}
						Main.playSound(Main.getResourceAsStream("chimes.wav"));
					}
				});
				settings.add(refreshBtn);
				pos += 32;
				JButton clearLogBtn = new JButton("Clear log");
				clearLogBtn.setFont(sansSerif);
				clearLogBtn.setBounds(5, pos, 73, 24);
				clearLogBtn.setMargin(new Insets(0, 0, 0, 0));
				clearLogBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						LTA.setText("");
						Main.print(LogType.INFO, "The log has been cleared!");
						Main.playSound(Main.getResourceAsStream("chimes.wav"));
					}
				});
				settings.add(clearLogBtn);
				JScrollPane settingsSP = new JScrollPane(settings);
				settingsSP.setBounds(settings.getBounds());
				tabbedPane.addTab("Settings", null, settingsSP, "Manage your bot's settings.");
				JPanel calculator = new JPanel();
				calculator.setLayout(null);
				JTextField sum = new JTextField();
				sum.setBackground(Color.WHITE);
				sum.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
				sum.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
				sum.setBounds(0, 0, 1069, 64);
				sum.addKeyListener(new KeyListener() {
					@Override
					public void keyReleased(KeyEvent e) {
					}

					@Override
					public void keyTyped(KeyEvent e) {
					}

					@Override
					public void keyPressed(KeyEvent e) {
						sum.setForeground(Color.BLACK);
						if (e.getKeyCode() == KeyEvent.VK_ENTER) calculate(sum);
					}
				});
				calculator.add(sum);
				JButton calcBtn = new JButton("Calculate");
				calcBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
				calcBtn.setBounds(487, 72, 96, 48);
				calcBtn.setMargin(new Insets(0, 0, 0, 0));
				calcBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						calculate(sum);
					}
				});
				calculator.add(calcBtn);
				JLabel desc = new JLabel("It does addition, subtraction, multiplication, division, exponentiation (using the ^ symbol), factorialization (! ");
				desc.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
				desc.setBounds(2, 136, (int) desc.getFont().getStringBounds(desc.getText(), frc).getWidth(), 20);
				calculator.add(desc);
				JLabel desc1 = new JLabel("before");
				desc1.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
				desc1.setBounds((int) desc.getFont().getStringBounds(desc.getText(), frc).getWidth() - 22, 136, (int) desc1.getFont().getStringBounds(desc1.getText(), frc).getWidth() + 8, 20);
				calculator.add(desc1);
				JLabel desc2 = new JLabel(" a number),");
				desc2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
				desc2.setBounds((int) desc.getFont().getStringBounds(desc.getText(), frc).getWidth() + (int) desc1.getFont().getStringBounds(desc1.getText(), frc).getWidth() - 22, 136, (int) desc2.getFont().getStringBounds(desc2.getText(), frc).getWidth(), 20);
				calculator.add(desc2);
				JLabel desc3 = new JLabel("and a few basic functions like sqrt, cbrt, sin, cos, and tan. It supports grouping using (...), and it gets the operator precedence and associativity rules correct.");
				desc3.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
				desc3.setBounds(2, 156, (int) desc3.getFont().getStringBounds(desc3.getText(), frc).getWidth(), 20);
				calculator.add(desc3);
				tabbedPane.addTab("Calculator", null, calculator, "Just a helpful little calculator.");
				JTabbedPane usage = new JTabbedPane(JTabbedPane.TOP);
				usage.setBounds(0, -10, tabbedPane.getWidth(), tabbedPane.getHeight());
				JGraphPanel ram = new JGraphPanel(new ArrayList(), "RAM usage in MB", "Seconds ago", true);
				Main.runAsynchronously(() -> {
					List<Double> scores = Lists.newArrayList(new Double[60]);
					while (!Main.isShuttingDown()) {
						scores.add(Main.formatFileSizeDoubleMb(UsageMonitorer.getProcessRamUsage()));
						if (scores.size() > 60) scores.remove(0);
						ram.setScores(scores);
						Main.sleep(1, TimeUnit.SECONDS);
					}
				});
				usage.addTab("RAM", null, ram, "Shows you the RAM usage of the last 60 seconds.");
				JGraphPanel cpu = new JGraphPanel(new ArrayList(), "CPU usage in %", "Seconds ago", true);
				Main.runAsynchronously(() -> {
					List<Double> scores = Lists.newArrayList(new Double[60]);
					while (!Main.isShuttingDown()) {
						double cpuUsage = UsageMonitorer.getSystemCpuLoad().doubleValue();
						if (cpuUsage > 95 && System.currentTimeMillis() - Main.started.getTime() > 1000 * 60 * 3) {
							Main.print(LogType.WARN, "System CPU load was above 95%, assuming bot crashed, shutting down.");
							Main.shutdown(0);
						}
						scores.add(cpuUsage);
						if (scores.size() > 60) scores.remove(0);
						cpu.setScores(scores);
						Main.sleep(1, TimeUnit.SECONDS);
					}
				});
				usage.addTab("CPU", null, cpu, "Shows you the CPU usage of the last 60 seconds.");
				tabbedPane.addTab("Usage", null, usage, "Shows you the usage of various things of the last 60 seconds.");
				JPanel shutdown = new JPanel();
				shutdown.setLayout(null);
				JButton shutdownBtn = new JButton("Shutdown");
				shutdownBtn.setBounds(470, 293, 128, 64);
				shutdownBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
				shutdownBtn.setForeground(Color.RED);
				shutdownBtn.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Main.shutdown(0);
					}
				});
				shutdown.add(shutdownBtn);
				tabbedPane.addTab("Shutdown", null, shutdown, "Shut the bot down.");
			} else {
				JPanel itIsNotYours = new JPanel();
				itIsNotYours.setLayout(null);
				JLabel rekt = new JLabel("Please be so kind to change the impulse.png file back to the default one, thanks!");
				rekt.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 27));
				rekt.setBounds(0, 0, tabbedPane.getWidth(), (int) rekt.getFont().getStringBounds(rekt.getText(), frc).getHeight());
				rekt.setForeground(Color.RED);
				itIsNotYours.add(rekt);
				tabbedPane.addTab("It's not yours", null, itIsNotYours, "You didn't make it.");
			}
			int width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
			int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
			int x = width / 2 - 540;
			int y = height / 2 - 360;
			mainFrame.getContentPane().add(tabbedPane);
			mainFrame.setLocation(x, y);
			mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			mainFrame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					mainFrame.setExtendedState(JFrame.ICONIFIED);
				}
			});
			mainFrame.setVisible(true);
			initialized = true;
		} else throw new IllegalAccessException("Already initialized.");
	}

	public static boolean isThief() {
		return Main.headless() ? false : hasPngChanged; // the variable hasPngChanged is always true while headless as the GUI is never
														// initialized and the image's MD5 isn't checked.
	}

	public static void logLine(String line) {
		if (!Main.isShuttingDown()) LTA.setText(LTA.getText() + line + System.lineSeparator());
	}

	private static final void calculate(JTextField sum) {
		try {
			sum.setText(df.format(MathHelper.eval(sum.getText())));
		} catch (RuntimeException e) {
			sum.setForeground(Color.RED);
			sum.setText("Error occurred: " + e.getMessage());
		}
	}

}
