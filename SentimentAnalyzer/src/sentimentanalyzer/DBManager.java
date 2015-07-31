/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sentimentanalyzer;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import java.io.FileReader;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;


import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Nikolin
 */
public class DBManager {
    
    //CHANGE THIS ACCORDING TO DATABASE SETTINGS AND DATA
    private final String dbURL = "jdbc:mysql://localhost:3306/rss_crawl";
    private final String username = "root";
    private final String password = "";
    
    private static final String filePath = "twitter.json";
    
    public Connection databaseConnection = null;
    public Statement preparedStatmentToExecute = null;
    public ResultSet resultSet = null;
    
    public ArrayList<String> llTitlesWords;
    public ArrayList<String> listToHoldCounted;
    
    public ArrayList<EntryElements> storeCountedWords;
    
    public Stopwords stopwords;
    
    
    public int sourceLanguage = 0;
    
    public Date from;
    public Date to;
    
    public DBManager(int variabelGjuhe, Date from, Date to){
        //DBManager class constructor
        
        //Initialisation of LinkedLists
        this.llTitlesWords = new ArrayList<>();
        this.listToHoldCounted = new ArrayList<>();
        this.storeCountedWords = new ArrayList<>();
        
        this.stopwords = new Stopwords();
        
        
        this.sourceLanguage = variabelGjuhe;
        
        this.from = from;
        this.to = to;
        
    }
    
    //This is a function that make the connection with the DB and executes the queries.
    public void workWithDatabase(String query){
        try{
            this.databaseConnection = DriverManager.getConnection(dbURL, username, password);
            this.preparedStatmentToExecute = this.databaseConnection.prepareStatement(query);
            this.resultSet = this.preparedStatmentToExecute.executeQuery(query);
        }catch(SQLException ex){
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("\n\n" + ex.getMessage() + "\n\n");
        }finally{
           //this.databaseConnection.close();
        }
    }
    
    
    
    public void closeDatabaseConnection(){
        try{
            this.databaseConnection.close();
        }catch(SQLException ex){
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("\n\n" + ex.getMessage() + "\n\n");
        }finally{
           //this.databaseConnection.close();
        }
    }
    
    //FIND MINIMUM
    public Timestamp findMINTimestamp(){
        String testQuery = "";
        
        if(this.sourceLanguage == 0){
            testQuery = "SELECT MIN(FirstAdded), sources.LANG From TITLES join sources on titles.source=sources.id where LANG=\"0\" ";
        }else if(this.sourceLanguage == 1){
            testQuery = "SELECT MIN(FirstAdded), sources.LANG From TITLES join sources on titles.source=sources.id where LANG=\"1\" ";
        }else if(this.sourceLanguage == 2){
            testQuery = "SELECT MIN(FirstAdded) FROM TITLES";
        }else{
            
        }
        
        this.workWithDatabase(testQuery);
        
        Timestamp minTimestamp = new Timestamp(0);
        
        try{
            if(this.resultSet.next()){
                minTimestamp = this.resultSet.getTimestamp(1);
                System.out.println("MIN(FirstAdded): " + minTimestamp.toString());
            }
        }catch(SQLException ex){
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("\n\n" + ex.getMessage() + "\n\n");
        }
        
        return minTimestamp;
    }
    
	
    //FIND MAXIMUM
    public Timestamp findMAXTimestamp(){
        String testQuery ="";
        
        if(this.sourceLanguage == 0){
            testQuery = "Select MAX(FirstAdded), sources.LANG From Titles join sources on titles.source=sources.id where LANG=\"0\"";
        }else if(this.sourceLanguage == 1){
            testQuery = "SELECT MAX(FirstAdded), sources.LANG From Titles join sources on titles.source=sources.id where LANG=\"1\"";
        }else if(this.sourceLanguage == 2){
            testQuery = "SELECT MAX(FirstAdded) FROM TITLES";
            
        }else{
        }
        this.workWithDatabase(testQuery);
        
        Timestamp maxTimestamp = new Timestamp(0);
        
        try{
            
            if(this.resultSet.next()){
                maxTimestamp = this.resultSet.getTimestamp(1);
                System.out.println("MAX(FirstAdded): " + maxTimestamp.toString());
            }
        }catch(SQLException ex){
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("\n\n" + ex.getMessage() + "\n\n");
        }
        
        return maxTimestamp;
    }
    
    //CLEAN ALL LISTS BEFORE USING THEM IN ORDER NOT TO MESS ELEMENTS
    public void prepareLinkedListsForOperation_cleanRestore(){
        this.llTitlesWords.clear();
        this.listToHoldCounted.clear();
        
        this.storeCountedWords.clear();
    }
    
    //ADD 6 HOURS TO CURRENT TIME
    public Timestamp add6Hours(Timestamp time){
        time.setTime(time.getTime() + (((60 * 60) * 12) * 1000));
        return time;
    }
    
    
    //FUNCTION TO SELECT QUERY AND LOOP OVER RESULTS
    public void selectAndLoopOverResults(Timestamp minTime, Timestamp upToTime){
        this.prepareLinkedListsForOperation_cleanRestore();
        String testQuery ="";
        
        if(this.sourceLanguage == 0){
            testQuery = "SELECT Title, sources.LANG FROM titles join sources on titles.source=sources.id WHERE FirstAdded >= '" + minTime.toString() + "' AND FirstAdded <= '" + upToTime.toString() + "' AND LANG=\"0\"";
        }else if(this.sourceLanguage == 1){
            testQuery = "SELECT Title, sources.LANG FROM titles join sources on titles.source=sources.id WHERE FirstAdded >= '" + minTime.toString() + "' AND FirstAdded <= '" + upToTime.toString() + "' AND LANG= \"1\"";
        }else if(this.sourceLanguage == 2){
            testQuery = "SELECT Title FROM titles WHERE FirstAdded >= '" + minTime.toString() + "' AND FirstAdded <= '" + upToTime.toString() + "'";
        
        }else{
        }
            System.out.println("DB QUERY: " + testQuery);
        
        this.workWithDatabase(testQuery);
        
        try{
            while(this.resultSet.next()){
                //CLEAN THE ENTIRE  STRING
                String holder = this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(this.resultSet.getString(1));
                //System.out.println(this.resultSet.getString(1));
                
                //SPLIT IT ACCFORDING TO SPACES AND USE INDIVIDUAL WORDS
                String[] individualWordsInsideTitle = holder.split(" ");
                
                for(int i=0; i<individualWordsInsideTitle.length; i++){
                    this.llTitlesWords.add(this.stopwords.cleanSpecialCharacters(individualWordsInsideTitle[i]));
                    
                    //EXECUTE BELOW TWO LINES TO SEE HOW THE WORDS CHANGE
                    //System.out.println("\t\t\t B:" + individualWordsInsideTitle[i]);
                    //System.out.println("\t\t\t A:" + stopwords.cleanSpecialCharacters(individualWordsInsideTitle[i]));
                }
            }
        }catch(SQLException ex){
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("\n\n" + ex.getMessage() + "\n\n");
        }
        
        this.closeDatabaseConnection();
    }
    
    public void selectAndLoopOverResults_JSON(){
        try{
            FileReader reader = new FileReader(filePath);

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);


            JSONArray text = (JSONArray) jsonObject.get("tweetsFeedback");

            Iterator i = text.iterator();
            int count = 0;

            while (i.hasNext()) {
                count++;
                JSONObject innerObj = (JSONObject) i.next();
                JSONObject user_id = (JSONObject) innerObj.get("user");

                System.out.println( count + " Content: " +  innerObj.get("text"));
                System.out.println("Time of retrieval" + innerObj.get("created_at"));
                System.out.println("The id of the user: " + user_id.get("id"));
                
                String holder = this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(innerObj.get("text").toString());
                String[] individualWordsInsideTitle = holder.split(" ");
                
                for(int j=0; j<individualWordsInsideTitle.length; j++){
                    this.llTitlesWords.add(this.stopwords.cleanSpecialCharacters(individualWordsInsideTitle[j]));
                }
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }
    
    //THIS FUNCTION LOOPS AND CHECK EVERY 6 HOURS
    public void selectEvery6HoursPrintAndStoreResults(){
        
        //Here we are using the class that create text writer, and the second row is initialized the begining, so create an empty file when we will write
        TextFileWriter txtWriter = new TextFileWriter();
        txtWriter.createTextFile();
        
        
        Timestamp minTime = this.findMINTimestamp();
        Timestamp minTime_firstHolder = this.findMINTimestamp();
        Timestamp upToTime = add6Hours(minTime_firstHolder);
        Timestamp maxTime = this.findMAXTimestamp();
        
        while(upToTime.compareTo(maxTime) < 0){
            //this.selectAndLoopOverResults(minTime, upToTime);
            this.selectAndLoopOverResults_JSON();
            
            this.llTitlesWords = stopwords.eraseStopwordsFromList(this.llTitlesWords);
            this.countOccouranceOfEachWordInListAndPopulateNewLists_forUnsortedListsPopulation(this.llTitlesWords);
            
            Collections.sort(this.storeCountedWords);
            this.printTop_fromInbuildMergeSort(11, minTime, upToTime, txtWriter);
            
            upToTime = add6Hours(upToTime);
        }
        
        //CLOSE WRITER AFTER FINISHING PRINTING AND WRITING TO TEXT FILE
        txtWriter.closePrintLine_PrintWriter();
        
    }
    
    public ArrayList<EntryElements> selectListForMainWindow(){
        
        this.storeCountedWords.clear();
        
        Timestamp minTime = this.findMINTimestamp();
        Timestamp minTime_firstHolder = this.findMINTimestamp();
        Timestamp upToTime = add6Hours(minTime_firstHolder);
        Timestamp maxTime = this.findMAXTimestamp();
        
        while(upToTime.compareTo(maxTime) < 0){
            this.selectAndLoopOverResults(minTime, upToTime);
            
            this.llTitlesWords = stopwords.eraseStopwordsFromList(this.llTitlesWords);
            this.countOccouranceOfEachWordInListAndPopulateNewLists_forUnsortedListsPopulation(this.llTitlesWords);
            
            Collections.sort(this.storeCountedWords);
            
            upToTime = add6Hours(upToTime);
        }
        
        return this.storeCountedWords;
    }
    
    //CHECK IF WORD IS PRESENT OF THE HOLD LIST, IF YES THEN IGNORE ELSE COUNT AND STORE BOTH RESULTS ON THE ENTRYELEMENTS LINKEDLIST
    public void countOccouranceOfEachWordInListAndPopulateNewLists_forUnsortedListsPopulation(ArrayList<String> words){
        for(int i=0; i<words.size(); i++){
            if(!this.listToHoldCounted.contains(words.get(i))){
                int count = Collections.frequency(words, words.get(i));
                this.listToHoldCounted.add(words.get(i));
                
                EntryElements entry = new EntryElements(words.get(i), count);
                this.storeCountedWords.add(entry);
            }
        }
       
        //TO TEST COUNTING RESULTS AND FILTERIN PRINT
        //System.out.println(words.size());
        //System.out.println(this.listToHoldCounted.size());
        //System.out.println(this.words_unsortedListNeededForMergeSort.size());
        //System.out.println(this.counters_unsortedListNeededForMergeSort.size());
    }
    
    public void customFrequency(ArrayList<String> words){		    
        HashMap<String, Integer > hm = new HashMap<>();		
        		
        for(int i=0; i<words.size(); i++){		
            String key = words.get(i);		
            			
            if(hm.containsKey(key)){		
                hm.put(key, hm.get(key) + 1);		
            }else{		
                hm.put(key, 1);		
            }		
        }		
        		
        for (String key : hm.keySet()){		
            EntryElements entry = new EntryElements(key, hm.get(key));		
            this.storeCountedWords.add(entry);		
        }
    } 
    
    public void printLLTitlesWords(){
        for(int i=0; i<this.llTitlesWords.size(); i++){
            System.out.println(this.llTitlesWords.get(i));
        }
    }
    
    //RINT FUNCTION FOR AFTER MERGE SORT APPLICATION, CUSTOM SELECT NUMBER OF ELEMENTS TO PRINT
    public void printTop_fromInbuildMergeSort(int elementsToPrint, Timestamp minTime, Timestamp maxTime, TextFileWriter txtWriter){
        
        
        System.out.println("============================");
        System.out.println("TOP " + elementsToPrint + " =====================");

        System.out.println("FROM: " + minTime.toString());

        System.out.println("TO: " + maxTime.toString());

        System.out.println("============================");

        
        txtWriter.writeToTextFile_printLineIsOpen("-- FROM: " + minTime.toString() + " TO: " + maxTime.toString());
        
        if(elementsToPrint > this.storeCountedWords.size()){
            elementsToPrint = this.storeCountedWords.size();
        }
        
        for(int i=1; i<elementsToPrint; i++){
            String lineToShow = "\"" + this.storeCountedWords.get(i).getKey() + "\"" + "\t" + this.storeCountedWords.get(i).getValue();

            String lineToWrite = "\"" + this.storeCountedWords.get(i).getKey() + "\"" + "\t" + this.storeCountedWords.get(i).getValue();
            
            //WRITE TO TEXT FILE
    
            txtWriter.writeToTextFile_printLineIsOpen(lineToWrite);
            System.out.println(lineToShow);
      
        }
        txtWriter.writeToTextFile_printLineIsOpen("-----------------------------------------");
        
        
    }
}