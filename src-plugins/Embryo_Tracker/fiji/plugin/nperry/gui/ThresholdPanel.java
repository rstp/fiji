package fiji.plugin.nperry.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.TextAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

import fiji.plugin.nperry.Feature;
import fiji.plugin.nperry.Utils;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class ThresholdPanel extends javax.swing.JPanel {
	
	private static final Font smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
	private static final Font boldFont = smallFont.deriveFont(Font.BOLD);
	private static final long serialVersionUID = 1L;
	private static final String DATA_SERIES_NAME = "Data";
	private JComboBox jComboBoxFeature;
	private ChartPanel chartPanel;
	private JButton jButtonAutoThreshold;
	private JRadioButton jRadioButtonBelow;
	private JRadioButton jRadioButtonAbove;
	private HistogramDataset dataset;
	private JFreeChart chart;
	private XYPlot plot;
	private IntervalMarker intervalMarker;
	private double threshold;
	private Feature feature;
	private Map<Feature, double[]> featureValues;
	/** Ordered feature array, as they are displayed in the combo-box. */
	private Feature[] features;
	
	
	
	/*
	 * CONSTRUCTOR
	 */
	
	public ThresholdPanel(Map<Feature, double[]> featureValues, Feature selectedFeature) {
		super();
		this.featureValues = featureValues;
		features = new TreeSet<Feature>(featureValues.keySet()).toArray(new Feature[0]); // Using a treeset will ensure the feature are sorted by the Feature enum order 
		initGUI();
		jComboBoxFeature.setSelectedItem(selectedFeature.toString());

	}
	
	public ThresholdPanel(Map<Feature, double[]> featureValues) {
		this(featureValues, featureValues.keySet().toArray(new Feature[0])[0]);
	}
	
	/*
	 * PUBLIC METHODS
	 */
	
	/**
	 * Return the threshold currently selected for the feature displayed in this panel.
	 * @see #isAboveThreshold()
	 */
	public double getThreshold() { return threshold; }
	
	/**
	 * Return true if the user selected the above threshold option for the feature displayed 
	 * in this panel.
	 * @see #getThreshold()
	 */
	public boolean isAboveThreshold() { return jRadioButtonAbove.isSelected(); }
	

	/** 
	 * Return the {@link Feature} selected in this panel.
	 */
	public Feature getFeature() { return feature; }
	
	
	/*
	 * PRIVATE METHODS
	 */
	
	private void comboBoxSelectionChanged() {
		Feature selectedFeature = features[jComboBoxFeature.getSelectedIndex()];
		double[] values = featureValues.get(selectedFeature);
		int nBins = Utils.getNBins(values);
		dataset = new HistogramDataset();
		dataset.addSeries(DATA_SERIES_NAME, values, nBins);
		plot.setDataset(dataset);
		resetAxes();
		autoThreshold();		
	}
	
	private void autoThreshold() {
		Feature selectedFeature = features[jComboBoxFeature.getSelectedIndex()];
		threshold = Utils.otsuThreshold(featureValues.get(selectedFeature));
		redrawThresholdMarker();
	}

	private void initGUI() {
		Dimension panelSize = new java.awt.Dimension(250, 140);
		Dimension panelMaxSize = new java.awt.Dimension(1000, 140);
		try {
			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.rowWeights = new double[] {0.0, 1.0, 0.0};
			thisLayout.rowHeights = new int[] {10, 7, 15};
			thisLayout.columnWeights = new double[] {0.0, 0.0, 1.0};
			thisLayout.columnWidths = new int[] {7, 20, 7};
			this.setLayout(thisLayout);
			this.setPreferredSize(panelSize);
			this.setMaximumSize(panelMaxSize);
			this.setBorder(new LineBorder(new java.awt.Color(252,117,0), 1, true));
			{
				String[] featureNames = new String[features.length];
				for (int i = 0; i < features.length; i++) 
					featureNames[i] = features[i].toString();
				ComboBoxModel jComboBoxFeatureModel = new DefaultComboBoxModel(featureNames);
				jComboBoxFeature = new JComboBox();
				this.add(jComboBoxFeature, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 5), 0, 0));
				jComboBoxFeature.setModel(jComboBoxFeatureModel);
				jComboBoxFeature.setFont(boldFont);
				jComboBoxFeature.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						comboBoxSelectionChanged();
					}
				});
			}
			{
				createHistogramPlot();
				chartPanel.setPreferredSize(new Dimension(0, 0));
				this.add(chartPanel, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				chartPanel.setOpaque(false);
			}
			{
				jButtonAutoThreshold = new JButton();
				this.add(jButtonAutoThreshold, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 0, 0));
				jButtonAutoThreshold.setText("Auto");
				jButtonAutoThreshold.setFont(smallFont);
				jButtonAutoThreshold.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						autoThreshold();
					}
				});
			}
			{
				jRadioButtonAbove = new JRadioButton();
				this.add(jRadioButtonAbove, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
				jRadioButtonAbove.setText("Above");
				jRadioButtonAbove.setFont(smallFont);
				jRadioButtonAbove.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						redrawThresholdMarker();
					}
				});
			}
			{
				jRadioButtonBelow = new JRadioButton();
				this.add(jRadioButtonBelow, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
				jRadioButtonBelow.setText("Below");
				jRadioButtonBelow.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						redrawThresholdMarker();
					}
				});
				jRadioButtonBelow.setFont(smallFont);
			}
			{
				ButtonGroup buttonGroup = new ButtonGroup();
				buttonGroup.add(jRadioButtonAbove);
				buttonGroup.add(jRadioButtonBelow);
				jRadioButtonAbove.setSelected(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Instantiate and configure the histogram chart.
	 */
	private void createHistogramPlot() {
		dataset = new HistogramDataset();
		chart = ChartFactory.createHistogram(null, null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
		
		plot = chart.getXYPlot();
		
		XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		renderer.setShadowVisible(false);
		renderer.setMargin(0);
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setDrawBarOutline(true);
		renderer.setSeriesOutlinePaint(0, Color.BLACK);
		renderer.setSeriesPaint(0, new Color(1, 1, 1, 0));
		
		plot.setBackgroundPaint(new Color(1, 1, 1, 0));
		plot.setOutlineVisible(false);
		plot.setDomainCrosshairVisible(false);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeCrosshairVisible(false);
		plot.setRangeGridlinesVisible(false);
		
		plot.getRangeAxis().setVisible(false);
		plot.getDomainAxis().setVisible(false);
		
		chart.setBorderVisible(false);
		chart.setBackgroundPaint(new Color(0.6f, 0.6f, 0.7f));
		
		intervalMarker = new IntervalMarker(0, 0, new Color(0.3f, 0.5f, 0.8f), new BasicStroke(), new Color(0, 0, 0.5f), new BasicStroke(1.5f), 0.5f);
		plot.addDomainMarker(intervalMarker);
		
		chartPanel = new ChartPanel(chart);
		MouseListener[] mls = chartPanel.getMouseListeners();
		for (MouseListener ml : mls)
			chartPanel.removeMouseListener(ml);
		
		chartPanel.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) { }
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				threshold = getXFromChartEvent(e);
				redrawThresholdMarker();
			}
		});
		chartPanel.addMouseMotionListener(new MouseMotionListener() {
			public void mouseMoved(MouseEvent e) {}
			public void mouseDragged(MouseEvent e) {
				threshold = getXFromChartEvent(e);
				redrawThresholdMarker();
			}
		});		
	}
	
	private double getXFromChartEvent(MouseEvent mouseEvent) {
		Point2D p = chartPanel.translateScreenToJava2D(mouseEvent.getPoint());
		Rectangle2D plotArea = chartPanel.getScreenDataArea();
		return plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
	}
	
	private void redrawThresholdMarker() {
		if (jRadioButtonAbove.isSelected()) {
			intervalMarker.setStartValue(threshold);
			intervalMarker.setEndValue(plot.getDomainAxis().getUpperBound());
		} else {
			intervalMarker.setStartValue(plot.getDomainAxis().getLowerBound());
			intervalMarker.setEndValue(threshold);
		}
		double x, y;
		if (threshold > 0.9 * plot.getDomainAxis().getUpperBound()) 
			x = threshold - 0.10 * plot.getDomainAxis().getRange().getLength();
		else 
			x = threshold + 0.05 * plot.getDomainAxis().getRange().getLength();
		y = 0.9 * plot.getRangeAxis().getUpperBound();
		double sx = plot.getDomainAxis().valueToJava2D(x, chartPanel.getScreenDataArea(), plot.getDomainAxisEdge());
		double sy = plot.getRangeAxis().valueToJava2D(y, chartPanel.getScreenDataArea(), plot.getRangeAxisEdge());

//		Point2D p = chartPanel.translateJava2DToScreen(new Point2D.Double(sx, sy));
		Graphics g = chartPanel.getGraphics();
		if (null == g) 
			return;
		System.out.println(sx +", "+sy);
		g.drawString(String.format("%.1f", threshold), (int)sx, (int)sy);
		((Graphics2D) g).drawString("PIF!", (int)sx, (int)sy);
		((Graphics2D) g).drawRect((int)sx, (int)sy, 100, 100);
	}
	
	private void resetAxes() {
		plot.getRangeAxis().setLowerMargin(0);
		plot.getRangeAxis().setUpperMargin(0);
		plot.getDomainAxis().setLowerMargin(0);
		plot.getDomainAxis().setUpperMargin(0);
	}
	
	
	
	/*
	 * MAIN METHOD
	 */
	
	
	/**
	* Display this JPanel inside a new JFrame.
	*/
	public static void main(String[] args) {
		// Prepare fake data
		final int N_ITEMS = 100;
		final Random ran = new Random();
		double mean;
		Feature[] features = new Feature[] { Feature.CONTRAST, Feature.ELLIPSOIDFIT_AXISPHI_A, Feature.MEAN_INTENSITY };
		Map<Feature, double[]> fv = new HashMap<Feature, double[]>(features.length);
		for (Feature feature : features) {
			double[] val = new double[N_ITEMS];
			mean = ran.nextDouble() * 10;
			for (int j = 0; j < val.length; j++) 
				val[j] = ran.nextGaussian() + 5 + mean;
			fv.put(feature, val);
		}
		
		// Create GUI
		ThresholdPanel tp = new ThresholdPanel(fv);
		tp.resetAxes();
		JFrame frame = new JFrame();
		frame.getContentPane().add(tp);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
