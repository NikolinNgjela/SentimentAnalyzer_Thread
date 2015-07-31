/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentanalyzer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 *
 * @author Nikolin
 */
public class WorkingThread extends SwingWorker<Integer, String> {
    
    public String url;
    public String keyword;
    public String decode;
    public String fromTime;
    public String toTime;
    public int interval;
    
    public String responce;
    
    public JPanel pnlWordsCount;
    
    private SentimetAnalyzer mainWindow;
    
    public WorkingThread(String fromTime, String toTime, String url, String keyword, String decode, int interval, SentimetAnalyzer mainWindow, JPanel pnlWordsCount){
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.url = url;
        this.keyword = keyword;
        this.decode = decode;
        this.interval = interval;
        
        this.pnlWordsCount = pnlWordsCount;
        
        this.mainWindow = mainWindow;
    }

    @Override
    protected Integer doInBackground() throws Exception{
        int progress = 10;
        
        publish("Cleaning the tables and initializing the words graphs...");
        this.mainWindow.cleanTheTableModels();
        this.mainWindow.charts.initializeCountWordsXYSeriesChart(this.pnlWordsCount, "Word Count", "count", "word", 0, "words");
        
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Requesting Json from URL...");
        //WE CAN USE THIS DIRECT CALL WITHOUT REFERENCE BECAUSE THE FUNCTION getJsonWithHTTPRequest IS STATIC
        this.responce = HTTPRequests.getJsonWithHTTPRequest(this.fromTime, this.toTime, this.keyword, this.decode, this.url);
        System.out.println(this.fromTime);
        
        progress += 20;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Parsing Json, populating the tables building initial ArrayList with words...");
        JsonParserT jsonparsert = new JsonParserT(this.mainWindow.tableModelTitles);
        ArrayList<EntryElements> list = jsonparsert.selectListForMainWindow(this.responce);
        
        progress += 20;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Populating TOP TEN words...");
        //POPULATE CONTROLLER ON GUI
        for(int i=0; i<10; i++){
           this.mainWindow.tableModelTopTen.addRow(new Object[] { list.get(i).getKey(), list.get(i).getValue(), Boolean.FALSE });
        }
        
        progress += 20;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Build small lists according to the intervals and counts, order on all of them...");
        //JSON LOGIC BELOW, BUILD SMALL LISTS ACCORDING TO THE INTERVALS AND COUNTS, ORDERS ON ALL OF THEM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d H:m:s z yyyy", Locale.ENGLISH);
        LocalDateTime fromDate_parsed = LocalDateTime.parse(this.fromTime, formatter);
        this.mainWindow.forNaming = fromDate_parsed;
        LocalDateTime toDate_parsed = LocalDateTime.parse(this.toTime, formatter);
        this.mainWindow.interval = this.interval;
        this.mainWindow.setLabaleInformation("VALUES: Start Time = " + fromDate_parsed.toString() + " - End Time = " + toDate_parsed + " - Interval = " + this.interval);
                
        progress += 20;
        setProgress(progress);
        WorkingThread.failIfInterrupted();
        
        publish("Reading, filtering, counting and ordering each interval...");
        //MAIL FUNCTION FOR READING JSON, FILTERING, COUNTING AND ORDERING FOR EACH INTERVAL
        this.mainWindow.listsForPloting_fromListManager = jsonparsert.selectListForMainWindow_forPloting_linkedListOfLinkedLists(fromDate_parsed, toDate_parsed, this.interval, this.responce);
        if(this.mainWindow.listsForPloting_fromListManager == null){
            this.mainWindow.localPlotingPolice_variableToCheckIfListIsFull_ifNullThenDoNotPlotGraph = 0;
            this.mainWindow.setLabaleInformation("The lists come back empty with current search criteria, please modify search and run again! Check interval");
        }else{
            this.mainWindow.localPlotingPolice_variableToCheckIfListIsFull_ifNullThenDoNotPlotGraph = 1;
        }
        //CHECK INTEGRITY AND LISTS
        //listsForPloting_fromListManager.checkIntegrityAndData();

        progress += 10;
        setProgress(progress);
        publish("ALL DONE!");
        
        return 0;
    }
    
    //FUNCTION FOR PRINTING OR DISPLAYING HTE publish() CONTENT ON THE MAIN WINDOW
    @Override
    protected void process(final List<String> chunks){
        for(final String string : chunks){
            this.mainWindow.setLabaleInformation(string);
        }
    }
    
    //FUNCTION TO THROUGH EXCEPTION IF THE THREAD IS INTERUPTED
    private static void failIfInterrupted() throws InterruptedException{
        if(Thread.currentThread().isInterrupted()){
            throw new InterruptedException("Interrupted while working with request!");
        }
    }
    
}
