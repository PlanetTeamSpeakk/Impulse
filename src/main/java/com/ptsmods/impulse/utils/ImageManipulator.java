package com.ptsmods.impulse.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import net.dv8tion.jda.core.entities.Member;

public class ImageManipulator {

	private ImageManipulator() {}

	public static Image impactMeme(Image image, String topText, String bottomText) throws IOException {
		return impactMeme(image, topText, bottomText, 48);
	}

	public static Image impactMeme(Image image, String topText, String bottomText, float size) throws IOException {
		if (image == null) throw new NullPointerException("The argument image cannot be null.");
		Font impact = getImpactFont(size);
		topText = topText.toUpperCase();
		bottomText = bottomText.toUpperCase();
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setFont(impact);
		int topX = image.getWidth(null)/2-g.getFontMetrics().stringWidth(topText)/2;
		int topY = impact.getSize();
		int bottomX = image.getWidth(null)/2-g.getFontMetrics().stringWidth(bottomText)/2;
		int bottomY = image.getHeight(null)-impact.getSize()+(int)(impact.getSize()/1.25);
		g.setColor(Color.WHITE);
		drawWithOutline(g, Color.BLACK, topText, topX, topY, 3);
		drawWithOutline(g, Color.BLACK, bottomText, bottomX, bottomY, 3);
		return image;
	}

	public static Image generateWelcomeImage(Member member) throws IOException {
		String line1 = "Welcome to " + member.getGuild().getName() + ",";
		String line2 = Main.str(member) + "!";
		String line3 = "You are the " + Main.getAsOrdinal(member.getGuild().getMembers().size()) + " user!";
		Font arvo = getArvoFont(48);
		FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, false);
		URLConnection connection = new URL(member.getUser().getEffectiveAvatarUrl()).openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
		Image avatar = ImageIO.read(connection.getInputStream());
		BufferedImage image = new BufferedImage((int) MathHelper.max(arvo.getStringBounds(line1, frc).getWidth(), arvo.getStringBounds(line2, frc).getWidth(), arvo.getStringBounds(line3, frc).getWidth()) + 280, 240, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(avatar, 0, 0, image.getHeight(), image.getHeight(), 0, 0, avatar.getWidth(null), avatar.getHeight(null), null);
		g.setFont(arvo);
		g.setColor(Color.WHITE);
		drawWithOutline(g, Color.BLACK, line1, image.getHeight()+40, g.getFont().getSize()  +10, 3);
		drawWithOutline(g, Color.BLACK, line2, image.getHeight()+40, g.getFont().getSize()*2+15, 3);
		drawWithOutline(g, Color.BLACK, line3, image.getHeight()+40, g.getFont().getSize()*3+20, 3);
		return image;
	}

	public static void drawWithOutline(Graphics2D g, Color outlineColor, String text, int x, int y, int thickness) {
		Color original = g.getColor();
		g.setColor(outlineColor);
		g.drawString(text, x-thickness, y-thickness);
		g.drawString(text, x-thickness, y+thickness);
		g.drawString(text, x+thickness, y-thickness);
		g.drawString(text, x+thickness, y+thickness);
		g.setColor(original);
		g.drawString(text, x, y);
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) return (BufferedImage) img;
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		img.flush();
		return bimage;
	}

	public static BufferedImage toBufferedImage(javafx.scene.image.Image img) {
		return SwingFXUtils.fromFXImage(img, new BufferedImage((int) img.getWidth(), (int) img.getHeight(), BufferedImage.TYPE_INT_ARGB));
	}

	/**
	 * Creates a new instance of the Impact font and returns it.
	 * @param size The size of the font.
	 * @return A new instance of the Impact font.
	 * @throws IOException If there was an error reading the ttf file.
	 */
	public static final Font getImpactFont(float size) throws IOException {
		try {return Font.createFont(Font.TRUETYPE_FONT, Main.getResourceAsStream("impact.ttf")).deriveFont(size);} catch (FontFormatException ignored) {/*Not possible, the file is there and is a legit ttf file.*/return new Font(Font.SANS_SERIF, Font.PLAIN, 0).deriveFont(size);}
	}

	/**
	 * Creates a new instance of the Arvo font and returns it.
	 * @param size The size of the font.
	 * @return A new instance of the Arvo font.
	 * @throws IOException If there was an error reading the ttf file.
	 */
	public static final Font getArvoFont(float size) throws IOException {
		try {return Font.createFont(Font.TRUETYPE_FONT, Main.getResourceAsStream("arvo.ttf")).deriveFont(size);} catch (FontFormatException ignored) {/*Not possible, the file is there and is a legit ttf file.*/return new Font(Font.SANS_SERIF, Font.PLAIN, 0).deriveFont(size);}
	}

}
