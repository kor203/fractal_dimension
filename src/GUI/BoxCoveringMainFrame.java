package GUI;

import analysis.BoxCovering;
import analysis.GiantComponent;
import data.IO.NetworkIO;
import data.structure.Network;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

public class BoxCoveringMainFrame extends JFrame {
    private ChartPanel centerPanel;
    private JTextField fileNameText;
    private JTextField minBoxSizeText;
    private JTextField maxBoxSizeText;
    private JTextField minBoxSizeDimensionText;
    private JTextField maxBoxSizeDimensionText;
    private JTextField boxDimensionText;
    private JButton exportButton;
    private JButton pathButton;
    private JButton calculateButton;
    private JButton calculateDimensionButton;
    private JRadioButton cpuRadioButton;
    private JRadioButton gpuRadioButton;
    private JRadioButton linearBinButton;
    private JRadioButton logBinButton;
    private ProgressMonitor boxCoveringMonitor;
    private File inputFile;
    private int minBoxSize;
    private int maxBoxSize;
    private int minBoxSizeDimension;
    private int maxBoxSizeDimension;
    private int[][] boxAmounts;
    private double[][] boxAmountsLog;
    private double[] results;
    private boolean useGPU;
    private boolean processorSelected;
    private boolean useLog;
    private double base = 1.2;

    private ChartPanel getStartingPanel(){
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "Box size",
                "Amount of boxes",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        LogarithmicAxis xAxis = new LogarithmicAxis("Box size");
        xAxis.setRange(new Range(1, 100));
        LogarithmicAxis yAxis = new LogarithmicAxis("Amount of boxes");
        yAxis.setRange(new Range(1, 1e5));
        yAxis.setExpTickLabelsFlag(true);

        XYPlot plot = chart.getXYPlot();
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        renderer.setSeriesShapesVisible(0, true);

        ChartPanel panel = new ChartPanel(chart);
        return panel;
    }

    private void allLock(){
        pathButton.setEnabled(false);
        calculateButton.setEnabled(false);
        calculateDimensionButton.setEnabled(false);
        exportButton.setEnabled(false);
        minBoxSizeText.setEnabled(false);
        maxBoxSizeText.setEnabled(false);
        minBoxSizeDimensionText.setEnabled(false);
        maxBoxSizeDimensionText.setEnabled(false);
        cpuRadioButton.setEnabled(false);
        gpuRadioButton.setEnabled(false);
        linearBinButton.setEnabled(false);
        logBinButton.setEnabled(false);
    }

    private void startLocks(){
        pathButton.setEnabled(true);
        calculateButton.setEnabled(false);
        calculateDimensionButton.setEnabled(false);
        exportButton.setEnabled(false);
        minBoxSizeText.setEnabled(false);
        maxBoxSizeText.setEnabled(false);
        minBoxSizeDimensionText.setEnabled(false);
        maxBoxSizeDimensionText.setEnabled(false);
        cpuRadioButton.setEnabled(false);
        gpuRadioButton.setEnabled(false);
        linearBinButton.setEnabled(false);
        logBinButton.setEnabled(false);
    }

    private void pathChosenLock(){
        pathButton.setEnabled(true);
        calculateButton.setEnabled(false);
        calculateDimensionButton.setEnabled(false);
        exportButton.setEnabled(false);
        minBoxSizeText.setEnabled(true);
        maxBoxSizeText.setEnabled(true);
        minBoxSizeDimensionText.setEnabled(false);
        maxBoxSizeDimensionText.setEnabled(false);
        cpuRadioButton.setEnabled(true);
        gpuRadioButton.setEnabled(true);
        linearBinButton.setEnabled(false);
        logBinButton.setEnabled(false);
    }

    private void pathAndProcessorSelectedLock(){
        pathButton.setEnabled(true);
        calculateButton.setEnabled(true);
        calculateDimensionButton.setEnabled(false);
        exportButton.setEnabled(false);
        minBoxSizeText.setEnabled(true);
        maxBoxSizeText.setEnabled(true);
        minBoxSizeDimensionText.setEnabled(false);
        maxBoxSizeDimensionText.setEnabled(false);
        cpuRadioButton.setEnabled(true);
        gpuRadioButton.setEnabled(true);
        linearBinButton.setEnabled(false);
        logBinButton.setEnabled(false);
    }

    private void boxCoveringCalculatedLock(){
        pathButton.setEnabled(true);
        calculateButton.setEnabled(true);
        calculateDimensionButton.setEnabled(true);
        exportButton.setEnabled(false);
        minBoxSizeText.setEnabled(true);
        maxBoxSizeText.setEnabled(true);
        minBoxSizeDimensionText.setEnabled(true);
        maxBoxSizeDimensionText.setEnabled(true);
        cpuRadioButton.setEnabled(true);
        gpuRadioButton.setEnabled(true);
        linearBinButton.setEnabled(true);
        logBinButton.setEnabled(true);
    }

    private void resultsReadyLock(){
        pathButton.setEnabled(true);
        calculateButton.setEnabled(true);
        calculateDimensionButton.setEnabled(true);
        exportButton.setEnabled(true);
        minBoxSizeText.setEnabled(true);
        maxBoxSizeText.setEnabled(true);
        minBoxSizeDimensionText.setEnabled(true);
        maxBoxSizeDimensionText.setEnabled(true);
        cpuRadioButton.setEnabled(true);
        gpuRadioButton.setEnabled(true);
        linearBinButton.setEnabled(true);
        logBinButton.setEnabled(true);
    }

    private void updatePanel(){
        XYSeries series = new XYSeries("Box covering");
        if (useLog){
            boxAmountsLog = BoxCovering.toLogBin(boxAmounts, base);
            for (int i = 0; i < boxAmountsLog[0].length; i++) {
                series.add(boxAmountsLog[0][i], boxAmountsLog[1][i]);
            }
        }
        else {
            for (int i = 0; i < boxAmounts[0].length; i++) {
                series.add(boxAmounts[0][i], boxAmounts[1][i]);
            }
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        XYPlot plot = centerPanel.getChart().getXYPlot();
        plot.setDataset(dataset);
        LogarithmicAxis yAxis = (LogarithmicAxis) plot.getRangeAxis();
        yAxis.autoAdjustRange();
        LogarithmicAxis xAxis = (LogarithmicAxis) plot.getDomainAxis();
        xAxis.setRange(new Range(1, Math.max(100, maxBoxSize)));

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesShapesVisible(0, true);

        centerPanel.revalidate();
    }

    private void importNetworkAndUpdatePanel(JFrame parentFrame){
        try {
            boxCoveringMonitor = new ProgressMonitor(parentFrame, "Calculating box covering", "", 0, 100);
            boxCoveringMonitor.setNote("Importing file");
            boxCoveringMonitor.setProgress(0);

            FileInputStream inputStream = new FileInputStream(inputFile.getAbsoluteFile());
            Network network = NetworkIO.importNetworkByEdges(inputStream);
            boxCoveringMonitor.setProgress(1);


            minBoxSize = Integer.parseInt(minBoxSizeText.getText());
            maxBoxSize = Integer.parseInt(maxBoxSizeText.getText());

            if (minBoxSize < 1){
                minBoxSize = 1;
                minBoxSizeText.setText("1");
            }
            if (maxBoxSize < minBoxSize + 1){
                maxBoxSize = minBoxSize + 1;
                maxBoxSizeText.setText(Integer.toString(maxBoxSize));
            }

            network = GiantComponent.getGiantComponent(network);
            network.reindex();


            boxCoveringMonitor.setNote("Calculating box covering");
            boxCoveringMonitor.setProgress(0);


            if (useGPU) {
                BoxCovering.GPUGreedyBoxCoveringTask task = new BoxCovering.GPUGreedyBoxCoveringTask(network, minBoxSize, maxBoxSize);
                task.addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("progress" == evt.getPropertyName()) {
                            int progress = (Integer) evt.getNewValue();
                            boxCoveringMonitor.setProgress(progress);
                            boxCoveringMonitor.setNote("Done in " + progress + "%");
                        }
                        if (boxCoveringMonitor.isCanceled()) {
                            task.cancel(true);
                            pathAndProcessorSelectedLock();
                            return;
                        }
                        if (task.isDone() && !boxCoveringMonitor.isCanceled()) {
                            try {
                                boxAmounts = task.get();
                                updatePanel();
                                boxCoveringCalculatedLock();
                            } catch (InterruptedException e) {
                                pathAndProcessorSelectedLock();
                            } catch (ExecutionException e) {
                                pathAndProcessorSelectedLock();
                            }
                        }
                    }
                });
                task.execute();
            }
            else{
                BoxCovering.CPUGreedyBoxCoveringTask task = new BoxCovering.CPUGreedyBoxCoveringTask(network, minBoxSize, maxBoxSize);
                task.addPropertyChangeListener(new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("progress" == evt.getPropertyName()) {
                            int progress = (Integer) evt.getNewValue();
                            boxCoveringMonitor.setProgress(progress);
                            boxCoveringMonitor.setNote("Done in " + progress + "%");
                        }
                        if (boxCoveringMonitor.isCanceled()) {
                            task.cancel(true);
                            pathAndProcessorSelectedLock();
                            return;
                        }
                        if (task.isDone() && !boxCoveringMonitor.isCanceled()) {
                            try {
                                boxAmounts = task.get();
                                updatePanel();
                                boxCoveringCalculatedLock();
                            } catch (InterruptedException e) {
                                pathAndProcessorSelectedLock();
                            } catch (ExecutionException e) {
                                pathAndProcessorSelectedLock();
                            }
                        }
                    }
                });
                task.execute();
            }


        } catch (FileNotFoundException e) {
            startLocks();
        }
    }

    private void calculateBoxDimension(){
        minBoxSizeDimension = Integer.parseInt(minBoxSizeDimensionText.getText());
        maxBoxSizeDimension = Integer.parseInt(maxBoxSizeDimensionText.getText());

        if (minBoxSizeDimension < minBoxSize){
            minBoxSizeDimension = minBoxSize;
            minBoxSizeDimensionText.setText(Integer.toString(minBoxSizeDimension));
        }
        if (minBoxSizeDimension > maxBoxSize - 1){
            minBoxSizeDimension = maxBoxSize - 1;
            minBoxSizeDimensionText.setText(Integer.toString(minBoxSizeDimension));
        }
        if (maxBoxSizeDimension < minBoxSizeDimension + 1){
            maxBoxSizeDimension = minBoxSizeDimension + 1;
            maxBoxSizeDimensionText.setText(Integer.toString(maxBoxSizeDimension));
        }
        if (maxBoxSizeDimension > maxBoxSize){
            maxBoxSizeDimension = maxBoxSize;
            maxBoxSizeDimensionText.setText(Integer.toString(maxBoxSizeDimension));
        }

        if (useLog)
            results = BoxCovering.boxDimension(boxAmountsLog, minBoxSizeDimension, maxBoxSizeDimension);
        else
            results = BoxCovering.boxDimension(boxAmounts, minBoxSizeDimension, maxBoxSizeDimension);

        int firstSignificantDigit = (int) Math.ceil(-Math.log10(results[1]));
        boxDimensionText.setText(String.format("%." + (firstSignificantDigit + 1) + "f", results[0]) +
                String.format(" (%.0f)", results[1] * Math.pow(10, firstSignificantDigit + 1)));

        XYSeries seriesBoxCover = new XYSeries("Box covering");
        if (useLog){
            boxAmountsLog = BoxCovering.toLogBin(boxAmounts, base);
            for (int i = 0; i < boxAmountsLog[0].length; i++) {
                seriesBoxCover.add(boxAmountsLog[0][i], boxAmountsLog[1][i]);
            }
        }
        else {
            for (int i = 0; i < boxAmounts[0].length; i++) {
                seriesBoxCover.add(boxAmounts[0][i], boxAmounts[1][i]);
            }
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(seriesBoxCover);

        XYSeries seriesBoxDim = new XYSeries("Box dimension");
        seriesBoxDim.add(minBoxSizeDimension, Math.pow(10, results[2] * Math.log10(minBoxSizeDimension) + results[4]));
        seriesBoxDim.add(maxBoxSizeDimension, Math.pow(10, results[2] * Math.log10(maxBoxSizeDimension) + results[4]));
        dataset.addSeries(seriesBoxDim);

        XYPlot plot = centerPanel.getChart().getXYPlot();
        plot.setDataset(dataset);
        LogarithmicAxis yAxis = (LogarithmicAxis) plot.getRangeAxis();
        yAxis.autoAdjustRange();

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        renderer.setSeriesShapesVisible(0, true);

        centerPanel.revalidate();
        resultsReadyLock();
    }

    private void setInputFile(){
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            inputFile = fileChooser.getSelectedFile();
            fileNameText.setText(inputFile.getName());
        }
        else {
            startLocks();
            return;
        }
        if (processorSelected)
            pathAndProcessorSelectedLock();
        else
            pathChosenLock();
    }

    BoxCoveringMainFrame(){
        super("Box dimension calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setMinimumSize(new Dimension(700, 700));
        TitledBorder title;
        processorSelected = false;
        exportButton = new JButton("<html><center>"+"Export results"+"<br>"+"to files"+"</center></html>");


        //central panel
        centerPanel = getStartingPanel();
        getContentPane().add(BorderLayout.CENTER, centerPanel);

        //buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(new EmptyBorder(new Insets(10, 0, 0, 0)));

        //import
        JPanel pathPanel = new JPanel();
        pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
        title = BorderFactory.createTitledBorder("Import network");
        title.setTitleJustification(TitledBorder.CENTER);
        pathPanel.setBorder(title);
        fileNameText = new JTextField();
        fileNameText.setEnabled(false);
        fileNameText.setPreferredSize(new Dimension(100, 20));
        fileNameText.setMaximumSize(new Dimension(100, 20));
        pathPanel.add(fileNameText);
        pathButton = new JButton("Import file");
        pathButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allLock();
                setInputFile();
            }
        });
        pathButton.setMaximumSize(new Dimension(100, 30));
        pathPanel.add(pathButton);
        buttonPanel.add(pathPanel);

        buttonPanel.add(Box.createRigidArea(new Dimension(100, 10)));

        //box covering calculations
        JPanel boxCoverCalcu = new JPanel();
        boxCoverCalcu.setLayout(new BoxLayout(boxCoverCalcu, BoxLayout.Y_AXIS));
        boxCoverCalcu.setMaximumSize(new Dimension(100, 180));
        title = BorderFactory.createTitledBorder("Box covering");
        title.setTitleJustification(TitledBorder.CENTER);
        boxCoverCalcu.setBorder(title);
        JPanel boxSizeTextfields = new JPanel();
        boxSizeTextfields.setLayout(new BoxLayout(boxSizeTextfields, BoxLayout.X_AXIS));
        boxSizeTextfields.setMaximumSize(new Dimension(100, 40));
        minBoxSizeText = new JTextField("1");
        maxBoxSizeText = new JTextField("100");
        title = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Min");
        title.setTitleJustification(TitledBorder.CENTER);
        minBoxSizeText.setBorder(title);
        minBoxSizeText.setToolTipText("Minimum value of calculated box size.");
        title = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Max");
        title.setTitleJustification(TitledBorder.CENTER);
        maxBoxSizeText.setBorder(title);
        maxBoxSizeText.setToolTipText("Maximum value of calculated box size.");
        Dimension textfieldDim = new Dimension(40, 40);
        minBoxSizeText.setPreferredSize(textfieldDim);
        minBoxSizeText.setMaximumSize(textfieldDim);
        maxBoxSizeText.setPreferredSize(textfieldDim);
        maxBoxSizeText.setMaximumSize(textfieldDim);
        boxSizeTextfields.add(Box.createGlue());
        boxSizeTextfields.add(minBoxSizeText);
        boxSizeTextfields.add(Box.createRigidArea(new Dimension(5, 20)));
        boxSizeTextfields.add(maxBoxSizeText);
        boxSizeTextfields.add(Box.createGlue());
        cpuRadioButton = new JRadioButton("Use CPU");
        cpuRadioButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cpuRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useGPU = false;
                processorSelected = true;
                calculateButton.setEnabled(true);
            }
        });
        cpuRadioButton.setPreferredSize(new Dimension(100, 30));
        cpuRadioButton.setMaximumSize(new Dimension(100, 30));
        gpuRadioButton = new JRadioButton("Use GPU");
        gpuRadioButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        gpuRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useGPU = true;
                processorSelected = true;
                calculateButton.setEnabled(true);
            }
        });
        gpuRadioButton.setPreferredSize(new Dimension(100, 30));
        gpuRadioButton.setMaximumSize(new Dimension(100, 30));
        ButtonGroup processorButtonGroup = new ButtonGroup();
        processorButtonGroup.add(cpuRadioButton);
        processorButtonGroup.add(gpuRadioButton);
        calculateButton = new JButton("<html><center>"+"Calculate"+"<br>"+"box covering"+"</center></html>");
        calculateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JFrame thisFrame = this;
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allLock();
                importNetworkAndUpdatePanel(thisFrame);
            }
        });
        calculateButton.setPreferredSize(new Dimension(100, 50));
        calculateButton.setMaximumSize(new Dimension(100, 50));
        boxCoverCalcu.add(boxSizeTextfields);
        boxCoverCalcu.add(Box.createRigidArea(new Dimension(100, 5)));
        boxCoverCalcu.add(cpuRadioButton);
        boxCoverCalcu.add(gpuRadioButton);
        boxCoverCalcu.add(Box.createRigidArea(new Dimension(100, 5)));
        boxCoverCalcu.add(calculateButton);
        buttonPanel.add(boxCoverCalcu);

        buttonPanel.add(Box.createRigidArea(new Dimension(100, 10)));

        //slope and box dimension
        JPanel boxSizeCalcu = new JPanel();
        boxSizeCalcu.setLayout(new BoxLayout(boxSizeCalcu, BoxLayout.Y_AXIS));
        boxSizeCalcu.setMaximumSize(new Dimension(100, 180));
        title = BorderFactory.createTitledBorder("Box dimension");
        title.setTitleJustification(TitledBorder.CENTER);
        boxSizeCalcu.setBorder(title);
        JPanel boxSizeDimensionTextfields = new JPanel();
        boxSizeDimensionTextfields.setLayout(new BoxLayout(boxSizeDimensionTextfields, BoxLayout.X_AXIS));
        boxSizeDimensionTextfields.setMaximumSize(new Dimension(100, 40));
        minBoxSizeDimensionText = new JTextField("1");
        maxBoxSizeDimensionText = new JTextField("100");
        title = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Min");
        title.setTitleJustification(TitledBorder.CENTER);
        minBoxSizeDimensionText.setBorder(title);
        minBoxSizeDimensionText.setToolTipText("Minimum value of box size used for box dimension calculation.");
        title = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Max");
        title.setTitleJustification(TitledBorder.CENTER);
        maxBoxSizeDimensionText.setBorder(title);
        maxBoxSizeDimensionText.setToolTipText("Maximum value of box size used for box dimension calculation.");
        minBoxSizeDimensionText.setPreferredSize(textfieldDim);
        minBoxSizeDimensionText.setMaximumSize(textfieldDim);
        maxBoxSizeDimensionText.setPreferredSize(textfieldDim);
        maxBoxSizeDimensionText.setMaximumSize(textfieldDim);
        boxSizeDimensionTextfields.add(Box.createGlue());
        boxSizeDimensionTextfields.add(minBoxSizeDimensionText);
        boxSizeDimensionTextfields.add(Box.createRigidArea(new Dimension(5, 20)));
        boxSizeDimensionTextfields.add(maxBoxSizeDimensionText);
        boxSizeDimensionTextfields.add(Box.createGlue());
        linearBinButton = new JRadioButton("Lin");
        linearBinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        linearBinButton.setSelected(true);
        linearBinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useLog = false;
                calculateDimensionButton.setEnabled(true);
                updatePanel();
            }
        });
        linearBinButton.setPreferredSize(new Dimension(100, 30));
        linearBinButton.setMaximumSize(new Dimension(100, 30));
        logBinButton = new JRadioButton("Log");
        logBinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logBinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useLog = true;
                calculateDimensionButton.setEnabled(true);
                updatePanel();
            }
        });
        logBinButton.setPreferredSize(new Dimension(100, 30));
        logBinButton.setMaximumSize(new Dimension(100, 30));
        ButtonGroup linLogButtonGroup = new ButtonGroup();
        linLogButtonGroup.add(linearBinButton);
        linLogButtonGroup.add(logBinButton);
        calculateDimensionButton = new JButton("<html><center>"+"Calculate"+"<br>"+"box dimension"+"</center></html>");
        calculateDimensionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        calculateDimensionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                allLock();
                calculateBoxDimension();
            }
        });
        calculateDimensionButton.setMaximumSize(new Dimension(100, 50));
        boxSizeCalcu.add(boxSizeDimensionTextfields);
        boxSizeCalcu.add(Box.createRigidArea(new Dimension(100, 5)));
        boxSizeCalcu.add(linearBinButton);
        boxSizeCalcu.add(logBinButton);
        boxSizeCalcu.add(Box.createRigidArea(new Dimension(100, 5)));
        boxSizeCalcu.add(calculateDimensionButton);
        buttonPanel.add(boxSizeCalcu);

        buttonPanel.add(Box.createRigidArea(new Dimension(100, 10)));

        //box dimension result
        boxDimensionText = new JTextField();
        boxDimensionText.setPreferredSize(new Dimension(100, 40));
        boxDimensionText.setMaximumSize(new Dimension(100, 40));
        title = BorderFactory.createTitledBorder("Result");
        title.setTitleJustification(TitledBorder.CENTER);
        boxDimensionText.setBorder(title);
        boxDimensionText.setToolTipText("Box dimension of the network.");
        buttonPanel.add(boxDimensionText);

        buttonPanel.add(Box.createRigidArea(new Dimension(100, 10)));

        //export results
        exportButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data.IO.ResultExport.export(boxAmounts, results, inputFile.getName(), minBoxSizeDimension, maxBoxSizeDimension);
                exportButton.setEnabled(false);
            }
        });
        exportButton.setMaximumSize(new Dimension(100, 50));
        buttonPanel.add(exportButton);

        getContentPane().add(BorderLayout.WEST, buttonPanel);

        startLocks();

        setVisible(true);
    }
}
