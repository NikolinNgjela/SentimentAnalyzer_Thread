/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentanalyzer;

import com.sun.javafx.geom.Shape;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.PiePlot;

/**
 *
 * @author Nikolin
 */
public class ChartController {
    
    public XYPlot plot_counter;
    public XYPlot plot_sentiment;
    public LinkedList<Integer> wordIndexesOnGraph = new LinkedList<>();
    public int interval = 0;
    public JFreeChart wordCountsChart;
    
    public void createAndPopulatePieChart(JPanel pnlPieChart, LinkedList<EntryElements> listOfData){
        
        DefaultPieDataset data = new DefaultPieDataset();
        
        listOfData.stream().forEach((listOfData1) -> {
            data.setValue(listOfData1.getKey(), listOfData1.getValue());
        });

        JFreeChart chart = ChartFactory.createPieChart(
        "Sent. Distr. for the time interval",
        data,
        true, // legend?
        true, // tooltips?
        false // URLs?
        );
        
        
        ChartPanel CP = new ChartPanel(chart);
        
        pnlPieChart.setLayout(new java.awt.BorderLayout());
        pnlPieChart.add(CP,BorderLayout.CENTER);
    }
    
    public void createAndPopulatePieChart__TESTER(JPanel pnlPieChart){
        
        DefaultPieDataset data = new DefaultPieDataset();
        
       //data.setValue("Positive", 43.2);
        data.setValue("Pie chart is not available", 100);
        //data.setValue("Negative", 22.9);
        
        JFreeChart chart = ChartFactory.createPieChart(
        "Sent. Distr. for Testing",
        data,
        false, // legend?
        false, // tooltips?
        false // URLs?
        );
        ChartPanel CP = new ChartPanel(chart);
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Pie chart is not available", Color.LIGHT_GRAY);
        
        //TextTitle legendText = new TextTitle("This is LEGEND: ");
        //legendText.setPosition(RectangleEdge.BOTTOM);
        // chart.addSubtitle(legendText);
        
        pnlPieChart.setLayout(new java.awt.BorderLayout());
        pnlPieChart.add(CP,BorderLayout.CENTER);
    }
    
    public void plotXYSeriesChart_withGivenWord(JPanel timeSeriesChart, EntryElementsManager listOfData, String word, int row,  int interval){
        this.interval = interval;
        int count = 0;
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        final XYSeries series = new XYSeries(word);

        int police = 0;
        for(int i=0; i<listOfData.lists.size(); i++){
            for(int j=0; j<listOfData.lists.get(i).wordsAndCreatedDates_ofHolder.size(); j++){
                //System.out.println(listOfData.lists.get(i).storeCountedWords_ofHolder.get(j).word + "           " + listOfData.lists.get(i).storeCountedWords_ofHolder.get(j).count);
                if(listOfData.lists.get(i).storeCountedWords_ofHolder.get(j).word.equals(word)){
                    series.add(listOfData.lists.get(i).cicleOfData, listOfData.lists.get(i).storeCountedWords_ofHolder.get(j).count);
                    count = listOfData.lists.get(i).storeCountedWords_ofHolder.get(j).count;
                    //JOptionPane.showMessageDialog(null, "DATE: " + listOfData.lists.get(i).cicleOfData + "   COUNT: " + listOfData.lists.get(i).storeCountedWords_ofHolder.get(j).count , "Got it", JOptionPane.INFORMATION_MESSAGE);
                    police = 1;
                    break;
                }
            }
            if(police == 0){
                series.add(listOfData.lists.get(i).cicleOfData, 0);
                //JOptionPane.showMessageDialog(null, "DATE: " + listOfData.lists.get(i).cicleOfData + "   COUNT: 0 - e jona", "The time you have selected...", JOptionPane.INFORMATION_MESSAGE);  
            }else{
                police = 0;
            }
        }
        
        series.setKey(word + " ");
        dataset.addSeries(series);
        
        //.postTheTimeSeriesChartOnTheGUI(timeSeriesChart, dataset, word, "counts", "words", row);
        this.addLineOfDataTo_XYSeriesLineChart(dataset, row, this.plot_counter);
        this.updateTheIntervalString_counts();
    }
    
    public void initializeCountWordsXYSeriesChart(JPanel timeSeriesChart, String title, String x, String y, int row, String whichOneToInitialize){
        if(whichOneToInitialize.equals("words")){
            this.postTheTimeSeriesChartOnTheGUI_words(timeSeriesChart, null, title, y, x, row);
        }else if(whichOneToInitialize.equals("sentiment")){
            this.postTheTimeSeriesChartOnTheGUI_sentiment(timeSeriesChart, null, title, y, x, row);
        }
    }
    
    public void updateTheIntervalString_counts(){
        NumberAxis domainAxis = (NumberAxis) this.plot_counter.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domainAxis.setAttributedLabel("Intervals of: " + this.interval + " hours");
    }
    
    public void postTheTimeSeriesChartOnTheGUI_words(JPanel timeSeriesChart, XYSeriesCollection dataset, String title, String  y, String  x, int row){
        this.wordCountsChart = ChartFactory.createXYLineChart(
            title,
            x,
            y,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false);
        
        this.wordIndexesOnGraph.add(row);
        this.plot_counter = this.wordCountsChart.getXYPlot();
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        //CHECK THIS
        renderer.setBaseShapesVisible(true);

        
        this.plot_counter.setRenderer(renderer);
        this.plot_counter.setOutlinePaint(Color.orange);
        
        NumberAxis rangeAxis = (NumberAxis) this.plot_counter.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setAttributedLabel("Counts");
        
        NumberAxis domainAxis = (NumberAxis) this.plot_counter.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domainAxis.setAttributedLabel("Intervals");
        

        this.wordCountsChart.setBackgroundPaint(Color.white);
        this.wordCountsChart.setBorderPaint(Color.orange);
        ChartPanel CP = new ChartPanel(this.wordCountsChart);
        CP.setPopupMenu(null);
        CP.setDomainZoomable(false);
        CP.setRangeZoomable(false);
        CP.setMouseZoomable(false);
        timeSeriesChart.setLayout(new java.awt.BorderLayout());
        timeSeriesChart.add(CP,BorderLayout.CENTER);
        timeSeriesChart.revalidate();
    }
    
    public void postTheTimeSeriesChartOnTheGUI_sentiment(JPanel timeSeriesChart, XYSeriesCollection dataset, String title, String  y, String  x, int row){
        JFreeChart chart = ChartFactory.createXYLineChart(
            title,
            y,
            x,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false);
        
        this.wordIndexesOnGraph.add(row);
        this.plot_sentiment = chart.getXYPlot();
        
        NumberAxis rangeAxis = (NumberAxis) this.plot_sentiment.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        NumberAxis domainAxis = (NumberAxis) this.plot_sentiment.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        chart.setBackgroundPaint(Color.white);
        chart.setBorderPaint(Color.ORANGE);
        ChartPanel CP = new ChartPanel(chart);
        timeSeriesChart.setLayout(new java.awt.BorderLayout());
        timeSeriesChart.add(CP,BorderLayout.CENTER);
        timeSeriesChart.revalidate();
    }
    
    public void addLineOfDataTo_XYSeriesLineChart(XYSeriesCollection dataset, int row, XYPlot plot){
        plot.setDataset(row, dataset);
        
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(true);
        renderer.setSeriesPaint(row, this.getRandomColor());
        plot.setRenderer(row, renderer);
        
        this.wordIndexesOnGraph.add(row);
    }
    
    public Color getRandomColor(){
        Color[] picker = new Color[10];
        picker[0] = Color.ORANGE;
        picker[1] = Color.BLUE;
        picker[2] = Color.GREEN;
        picker[3] = Color.YELLOW;
        picker[4] = Color.BLACK;
        picker[5] = Color.PINK;
        picker[6] = Color.CYAN;
        picker[7] = Color.MAGENTA;
        picker[8] = Color.RED;
        picker[9] = Color.GRAY;
        
        int index = new Random().nextInt(picker.length);
        return picker[index];
    }
    
    public void removeLIneOfDataTo_XYSeriesLineChart(int row, String were){
        if(were.equals("words")){
            this.plot_counter.setDataset(row, null);
        }else if(were.equals("sentiment")){
            this.plot_sentiment.setDataset(row, null);
        }
    }
    
    public void saveWordCountsChartPNG(String name){
        try{
            ChartUtilities.saveChartAsPNG(new File(name + ".png"), this.wordCountsChart, 800, 600);
            JOptionPane.showMessageDialog(null, "File saved successfully. Can be found on root directory of execution. File: " + name + ".png", "Saved", JOptionPane.INFORMATION_MESSAGE);
        }catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

}
