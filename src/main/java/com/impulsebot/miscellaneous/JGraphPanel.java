package com.impulsebot.miscellaneous;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

/**
 * Made by Rodrigo and edited by PlanetTeamSpeak to support null values and axis names.
 * @author Rodrigo
 */
public class JGraphPanel extends JPanel {

	public static final Color defaultLineColor = new Color(244, 164, 66, 180);
	public static final Color defaultPointColor = new Color(100, 100, 100, 180);
	public static final Color defaultGridColor = new Color(200, 200, 200, 200);
	private static final long serialVersionUID = -644976435234557911L;
	private final boolean invertHorizontal;
	private String yName;
	private String xName;
	private int padding = 25;
	private int labelPadding = 25;
	private Color lineColor;
	private Color pointColor;
	private Color gridColor;
	private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
	private int pointWidth = 4;
	private int numberYDivisions = 10;
	private List<Double> scores;

	public JGraphPanel(List<Double> scores) {
		this(scores, null, null);
	}

	public JGraphPanel(List<Double> scores, String yName, String xName) {
		this(scores, yName, xName, false);
	}

	public JGraphPanel(List<Double> scores, String yName, String xName, boolean invertHorizontal) {
		this(scores, yName, xName, invertHorizontal, null, null, null);
	}

	public JGraphPanel(List<Double> scores, String yName, String xName, boolean invertHorizontal, Color lineColor, Color pointColor, Color gridColor) {
		this.scores = scores;
		this.yName = yName == null ? "" : yName;
		this.xName = xName == null ? "" : xName;
		this.invertHorizontal = invertHorizontal;
		this.lineColor = lineColor == null ? defaultLineColor : lineColor;
		this.pointColor = lineColor == null ? defaultPointColor : pointColor;
		this.gridColor = lineColor == null ? defaultGridColor : gridColor;
	}

	@Override
	protected void paintComponent(Graphics g) {
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double xScale = ((double) getWidth() - 2 * padding - labelPadding) / (scores.size() - 1);
		double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

		List<Point> graphPoints = new ArrayList<>();
		for (int i = 0; i < scores.size(); i++) {
			if (scores.get(i) == null) continue;
			int x1 = (int) (i * xScale + padding + labelPadding);
			int y1 = (int) ((getMaxScore() - scores.get(i)) * yScale + padding);
			graphPoints.add(new Point(x1, y1));
		}

		// draw white background
		g2.setColor(Color.WHITE);
		g2.fillRect(padding + labelPadding, padding, getWidth() - 2 * padding - labelPadding, getHeight() - 2 * padding - labelPadding);
		g2.setColor(Color.BLACK);

		// create hatch marks and grid lines for y axis.
		for (int i = 0; i < numberYDivisions + 1; i++) {
			int x0 = padding + labelPadding;
			int x1 = pointWidth + padding + labelPadding;
			int y0 = getHeight() - (i * (getHeight() - padding * 2 - labelPadding) / numberYDivisions + padding + labelPadding);
			int y1 = y0;
			if (scores.size() > 0) {
				g2.setColor(gridColor);
				g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
				g2.setColor(Color.BLACK);
				String yLabel = (int) ((getMinScore() + (getMaxScore() - getMinScore()) * (i * 1.0 / numberYDivisions)) * 100) / 100.0 + "";
				FontMetrics metrics = g2.getFontMetrics();
				int labelWidth = metrics.stringWidth(yLabel);
				g2.drawString(yLabel, x0 - labelWidth - 5, y0 + metrics.getHeight() / 2 - 3);
			}
			g2.drawLine(x0, y0, x1, y1);
		}

		// and for x axis
		if (scores.size() > 1)
			for (int i : Main.range(scores.size())) {
				int x0 = i * (getWidth() - padding * 2 - labelPadding) / (scores.size() - 1) + padding + labelPadding;
				int x1 = x0;
				int y0 = getHeight() - padding - labelPadding;
				int y1 = y0 - pointWidth;
				if (i % ((int) (scores.size() / 20.0) + 1) == 0) {
					g2.setColor(gridColor);
					g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
					g2.setColor(Color.BLACK);
					String xLabel = (invertHorizontal ? Main.getOpposite(scores.size(), i) : i) + "";
					FontMetrics metrics = g2.getFontMetrics();
					int labelWidth = metrics.stringWidth(xLabel);
					g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
				}
				g2.drawLine(x0, y0, x1, y1);
			}
		// create x and y axes
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
		g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

		// add names of axes
		g2.drawString(yName, 5, 12);
		g2.drawString(xName, getWidth() - g2.getFontMetrics().stringWidth(xName) - 18, getHeight() - 15);

		Stroke oldStroke = g2.getStroke();
		g2.setColor(lineColor);
		g2.setStroke(GRAPH_STROKE);
		for (int i = 0; i < graphPoints.size() - 1; i++) {
			int x1 = graphPoints.get(i).x;
			int y1 = graphPoints.get(i).y;
			int x2 = graphPoints.get(i + 1).x;
			int y2 = graphPoints.get(i + 1).y;
			g2.drawLine(x1, y1, x2, y2);
		}

		g2.setStroke(oldStroke);
		g2.setColor(pointColor);
		for (int i = 0; i < graphPoints.size(); i++) {
			int x = graphPoints.get(i).x - pointWidth / 2;
			int y = graphPoints.get(i).y - pointWidth / 2;
			int ovalW = pointWidth;
			int ovalH = pointWidth;
			g2.fillOval(x, y, ovalW, ovalH);
		}
	}

	private double getMinScore() {
		double minScore = Double.MAX_VALUE;
		for (Double score : Main.removeNulls(scores))
			minScore = Math.min(minScore, score);
		return minScore;
	}

	private double getMaxScore() {
		double maxScore = Double.MIN_VALUE;
		for (Double score : Main.removeNulls(scores))
			maxScore = Math.max(maxScore, score);
		return maxScore;
	}

	public void setScores(List<Double> scores) {
		this.scores = scores;
		invalidate();
		repaint();
	}

	public List<Double> getScores() {
		return scores;
	}

}