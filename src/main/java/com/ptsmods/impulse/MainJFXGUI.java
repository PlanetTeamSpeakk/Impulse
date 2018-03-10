package com.ptsmods.impulse;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.swing.JComponent;

import com.google.common.collect.Lists;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.commands.Main;
import com.ptsmods.impulse.miscellaneous.CommandExecutionHook;
import com.ptsmods.impulse.miscellaneous.CommandPermissionException;
import com.ptsmods.impulse.miscellaneous.ConsoleCommandEvent;
import com.ptsmods.impulse.miscellaneous.JGraphPanel;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.utils.UsageMonitorer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class MainJFXGUI extends Application {

	private static volatile TabPane tPane = null;
	private static volatile TextFlow LTF = null;
	private static AtomicBoolean isInitialized = new AtomicBoolean(false);
	private static AtomicBoolean started = new AtomicBoolean(false);

	public static void start(String[] args) throws IllegalAccessException {
		if (started.get()) throw new IllegalAccessException("The GUI has already been started.");
		started.set(true);
		Main.runAsynchronously(() -> {
			try {
				launch(args);
			} catch (RuntimeException shutdownExceptionIgnored) {}
		});
	}

	public static void startBlocking(String[] args) throws IllegalAccessException {
		start(args);
		while (!isInitialized())
			Main.sleep(25);
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Impulse v" + Main.version + " - written by PlanetTeamSpeak#4157");
		LTF = new TextFlow();
		tPane = new TabPane();
		tPane.setPrefSize(1090, 730);
		ScrollPane spLTF = new ScrollPane(LTF);
		spLTF.setFitToHeight(true);
		spLTF.setFitToWidth(true);
		spLTF.vvalueProperty().bind(LTF.heightProperty());
		tPane.getTabs().add(createTab("Log", "Everything that has ever been printed to the console.", spLTF));
		JGraphPanel ram = new JGraphPanel(new ArrayList(), "RAM usage in MB", "Seconds ago", true);
		Main.runAsynchronously(() -> {
			List<Double> scores = Lists.newArrayList(new Double[60]);
			while (!Main.isShuttingDown()) {
				scores.add(Main.formatFileSizeDoubleMb(UsageMonitorer.getRamUsage()));
				if (scores.size() > 60) scores.remove(0);
				ram.setScores(scores);
				Main.sleep(1, TimeUnit.SECONDS);
			}
		});
		JGraphPanel cpu = new JGraphPanel(new ArrayList(), "CPU usage in %", "Seconds ago", true);
		Main.runAsynchronously(() -> {
			List<Double> scores = Lists.newArrayList(new Double[60]);
			while (!Main.isShuttingDown()) {
				double cpuUsage = UsageMonitorer.getSystemCpuLoad().doubleValue();
				if (cpuUsage > 95 && System.currentTimeMillis()-Main.started.getTime() > 1000*60*3 && !Main.isShuttingDown() && !Main.devMode()) {
					Main.print(LogType.WARN, "System CPU load was above 95%, assuming bot crashed, shutting down.");
					Main.shutdown(0);
				}
				scores.add(cpuUsage);
				if (scores.size() > 60) scores.remove(0);
				cpu.setScores(scores);
				Main.sleep(1, TimeUnit.SECONDS);
			}
		});
		TabPane usage = new TabPane();
		usage.getTabs().add(createTab("RAM", "Shows you the RAM usage of the past 60 seconds.", swingToJFX(ram)));
		usage.getTabs().add(createTab("CPU", "Shows you the CPU usage of the past 60 seconds.", swingToJFX(cpu)));
		tPane.getTabs().add(createTab("Usage", null, usage));
		Pane console = new Pane();
		TextFlow consoleOutput = new TextFlow();
		TextArea consoleInput = new TextArea();
		List<String> commands = new ArrayList();
		AtomicInteger current = new AtomicInteger(-1);
		consoleInput.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				consoleInput.setText(consoleInput.getText().trim());
				String rawContent = consoleInput.getText();
				String[] parts = Arrays.copyOf(rawContent.trim().split("\\s+", 2), 2);
				String name = parts[0];
				String args = parts[1] == null ? "" : parts[1];
				int i = Main.getCommandIndex().getOrDefault(name.toLowerCase(), -1);
				if (i != -1) {
					Method command = Main.getCommands().get(i);
					while (args.split(" ").length != 0 && !Main.getSubcommands(command).isEmpty()) {
						boolean found = false;
						for (Method subcommand : Main.getSubcommands(command))
							if (subcommand.getAnnotation(Subcommand.class).name().equals(args.split(" ")[0])) {
								command = subcommand;
								args = Main.join(Main.removeArg(args.split(" "), 0));
								found = true;
								break;
							}
						if (!found) break;
					}
					ConsoleCommandEvent ccevent = new ConsoleCommandEvent(consoleOutput, args, command);
					for (CommandExecutionHook hook : Main.getCommandHooks())
						try {
							hook.run(ccevent);
						} catch (CommandPermissionException e) {}
					Main.runAsynchronously(null, command, ccevent);
				} else {
					Text text = new Text("The command '"+consoleInput.getText()+"' could not be found."+System.lineSeparator());
					text.setFont(Font.font("Monospaced", 13));
					text.setStyle("-fx-fill: RED");
					consoleOutput.getChildren().add(text);
				}
				commands.add(consoleInput.getText());
				current.set(commands.size());
				consoleInput.setText("");
				event.consume();
			} else if (event.getCode() == KeyCode.UP) {
				int i = current.get();
				if (i > 0 && i <= commands.size()) {
					current.decrementAndGet();
					consoleInput.setText(commands.get(i-1));
				}
			} else if (event.getCode() == KeyCode.DOWN) {
				int i = current.get();
				if (i >= 0 && i < commands.size()-1) {
					current.incrementAndGet();
					consoleInput.setText(commands.get(i+1));
				}
			}
		});
		consoleInput.setOnKeyReleased(event -> {
			if (consoleInput.getText().equalsIgnoreCase("clear")) {
				for (Node child : new ArrayList<>(consoleOutput.getChildrenUnmodifiable()))
					if (child instanceof Text) consoleOutput.getChildren().remove(child);
				consoleInput.setText("");
			}
		});
		ScrollPane consoleOutputSP = new ScrollPane(consoleOutput);
		consoleOutputSP.setPrefSize(1080, 655);
		consoleOutputSP.setFitToWidth(true);
		consoleOutputSP.setFitToHeight(true);
		consoleOutputSP.vvalueProperty().bind(consoleOutput.heightProperty());
		consoleInput.setPrefSize(1080, consoleInput.getFont().getSize()+4);
		consoleInput.setLayoutY(655);
		consoleInput.setPromptText("Command goes here.");
		console.getChildren().add(consoleOutputSP);
		console.getChildren().add(consoleInput);
		tPane.getTabs().add(createTab("Console", "Execute commands from the owner account set in the config.", console));
		StackPane shutdownPane = new StackPane();
		Button shutdown = new Button("Shutdown");
		shutdown.setFont(Font.font("Sans serif", 32));
		shutdown.setStyle("-fx-text-fill: RED;");
		AtomicBoolean clicked = new AtomicBoolean();
		shutdown.setOnAction(event -> {
			if (!clicked.get()) {
				Platform.runLater(() -> {
					try {
						Text text = new Text(Main.done() ? "Shutting down, this can take up to " + Main.formatMillis(Main.calculateTotalShutdownTimeout()).replaceAll("\\*", "") + ", please wait..." : "The bot is not done starting up yet, please try again once it's started.");
						text.setFont(Font.font("Sans Serif", 20));
						text.setStyle("-fx-fill: RED;");
						shutdownPane.getChildren().add(text);
						StackPane.setAlignment(text, Pos.TOP_CENTER);
						if (Main.done()) clicked.set(true);
					} catch (Exception e) {e.printStackTrace();}
				});
				if (Main.done())
					Platform.runLater(() -> {
						Main.shutdown(0);
					});
				else
					Main.runAsynchronously(() -> {
						Main.sleep(5000);
						Platform.runLater(() -> {
							for (Node child : new ArrayList<>(shutdownPane.getChildren()))
								if (child instanceof Text) shutdownPane.getChildren().remove(child);
						});
					});
			}
		});
		shutdownPane.getChildren().add(shutdown);
		tPane.getTabs().add(createTab("Shutdown", "Shut the bot down.", shutdownPane));
		Scene scene = new Scene(tPane, 1080, 720);
		scene.getStylesheets().add(Main.getResource("jfxGui.css").toExternalForm());
		stage.setScene(scene);
		stage.sizeToScene();
		stage.setResizable(false);
		stage.getIcons().add(new Image(Main.getResourceAsStream("impulse.png")));
		stage.centerOnScreen();
		stage.setOnCloseRequest(event -> {
			event.consume();
			stage.setIconified(true);
		});
		stage.show();
		isInitialized.set(true);
	}

	private final Tab createTab(String title, @Nullable String tooltip, Node content) {
		Tab tab = new Tab(title, content);
		tab.setClosable(false);
		Tooltip tooltipObj = new Tooltip(tooltip);
		tooltipObj.setStyle("-fx-font-size: 12;");
		if (tooltip != null && !tooltip.isEmpty()) tab.setTooltip(tooltipObj);
		return tab;
	}

	public static final void logLine(String line) {
		logLine(line, Color.BLACK);
	}

	public static final void logLine(String line, Color color) {
		if (!Main.isShuttingDown() && LTF != null && isInitialized.get() && line != null && !line.isEmpty())
			Platform.runLater(() -> {
				Text text = new Text(line.trim()+System.lineSeparator());
				text.setFont(Font.font("Monospaced", 13));
				text.setStyle("-fx-fill: #" + Main.colorToHex(color));
				LTF.getChildren().add(text);
			});
	}

	public static boolean isInitialized() {
		return isInitialized.get();
	}

	public static SwingNode swingToJFX(JComponent component) {
		SwingNode node = new SwingNode();
		node.setContent(component);
		return node;
	}

}