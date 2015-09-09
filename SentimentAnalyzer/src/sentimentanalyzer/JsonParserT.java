/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sentimentanalyzer;

/**
 *
 * @author Nikolin
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParserT {
    private static final String filePath = "twitter.json";
    
    public ArrayList<String> llTitlesWords;
    public ArrayList<String> listToHoldCounted;
    
    public ArrayList<EntryElements> storeCountedWords;
    
    public ArrayList<EntryElementsDate> wordsAndCreatedDates;
    public EntryElementsManager storeCountedWords_forPloting;
    
    public Stopwords stopwords;
    
    public LinkedList<Tweet> tweets;
    
    private DefaultTableModel tableForPrint_model;
    
    
    public JsonParserT(DefaultTableModel tableForPrint_model){
        this.llTitlesWords = new ArrayList<>();
        this.listToHoldCounted = new ArrayList<>();
        
        this.storeCountedWords = new ArrayList<>();
        this.wordsAndCreatedDates = new ArrayList<>();
        this.storeCountedWords_forPloting = new EntryElementsManager();
        
        this.stopwords = new Stopwords();
        
        tweets = new LinkedList<Tweet>();
        
        this.tableForPrint_model = tableForPrint_model;
    }

    //Clean all list before using
    public void prepareLinkedListsForOperation_cleanRestore(){
        this.llTitlesWords.clear();
        this.listToHoldCounted.clear();
    }   
    
    //Function to loop over the results;
    public void selectAndLoopResults(){
        this.prepareLinkedListsForOperation_cleanRestore();
    }
        
    //JSON WITH LOCAL FILE
    public void selectAndLoopOverResults_JSON(String jsonRequest){
        try{
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonRequest);

            JSONArray text = (JSONArray) jsonObject.get("tweetsFeedback");

            Iterator i = text.iterator();
            int count = 0;
            
            this.tweets.clear();
            System.out.println("tetete");
            while (i.hasNext()) {
                
                Tweet tweet = new Tweet();
                
                count++;
                JSONObject innerObj = (JSONObject) i.next();
                JSONObject user_id = (JSONObject) innerObj.get("user");

               // System.out.println( count + " Content: " +  innerObj.get("text"));
                
                //System.out.println("Time of retrieval " + innerObj.get("created_at"));
                String createdAt = innerObj.get("created_at").toString();

                //SPECIAL CASES WERE DATE IS NOT PARSED OK FROM JSON -- TEAM IN ITALY SHOULD CHECK THE JSON, IS NOT CORRECT SOME TIMES
                createdAt = createdAt.replace("+0000 ", "");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d H:m:s yyyy", Locale.ENGLISH);
                LocalDateTime date = LocalDateTime.parse(createdAt, formatter); //2015-03-21T10:42:05 -- i shton nje T ne mes

                tweet.createdAt = date;
                
                
                //System.out.println("The id of the user: " + user_id.get("id"));
                
                String holder = this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(innerObj.get("text").toString());
                String holderTime = this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(innerObj.get("created_at").toString());
                
               // System.out.println(holder);
                //System.out.println("    -" + holderTime);
                
                String[] individualWordsInsideTitle = holder.split(" ");
                
                for(int j=0; j<individualWordsInsideTitle.length; j++){
                    String output = this.stopwords.cleanSpecialCharacters(individualWordsInsideTitle[j]);
                    this.llTitlesWords.add(output);
                    tweet.words.add(output);
                }
                
                for(int j=0; j<individualWordsInsideTitle.length; j++){
                }
                this.tweets.add(tweet);
                
                this.tableForPrint_model.addRow(new Object[] { holderTime, holder, ClassifierWrapperT.ScoreResult() });
            }
        }catch (ParseException ex){
            System.out.println(ex.getMessage());        
        }
    }
    
    public EntryElementsManager selectAndLoopOverResults_JSON_withDateTimes
        (LocalDateTime from, LocalDateTime to, int interval, String jsonRequest){
        return new EntryElementsManager(interval, from, to, this.tweets);
    }
    
    public void selectEvery6HoursPrintAndStoreResults(String jsonRequest){
        this.selectAndLoopOverResults_JSON(jsonRequest);

        this.llTitlesWords = stopwords.eraseStopwordsFromList(this.llTitlesWords);
        this.customFrequency(this.llTitlesWords);

        Collections.sort(this.storeCountedWords);
        this.printTop_fromInbuildMergeSort(10);
    }
    
    public ArrayList<EntryElements> selectListForMainWindow(String jsonRequest){
        this.storeCountedWords.clear();

        this.selectAndLoopOverResults_JSON(jsonRequest);

        this.llTitlesWords = stopwords.eraseStopwordsFromList(this.llTitlesWords);
        this.customFrequency(this.llTitlesWords);

        Collections.sort(this.storeCountedWords);

        return this.storeCountedWords;
    }
    
 
    
    //PREPARES ALL THE LISTS BASED ON THE INTERVAL ARGUMENT
    //MIGHT TAKE A WHILE TO RUN (on excecution)
    public EntryElementsManager selectListForMainWindow_forPloting_linkedListOfLinkedLists(LocalDateTime from, LocalDateTime to, int interval, String jsonRequest){

        this.storeCountedWords_forPloting = selectAndLoopOverResults_JSON_withDateTimes(from, to, interval, jsonRequest);
        return this.storeCountedWords_forPloting;
        
    }
    
    public void countOccouranceOfEachWordInListAndPopulateNewLists_forUnsortedListsPopulation(ArrayList<String> words){
        for(int i=0; i<words.size(); i++){
            if(!this.listToHoldCounted.contains(words.get(i))){
                int count = Collections.frequency(words, words.get(i));
                this.listToHoldCounted.add(words.get(i));
                
                EntryElements entry = new EntryElements(words.get(i), count);
                this.storeCountedWords.add(entry);
            }
        }
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
    
    public void printTop_fromInbuildMergeSort(int elementsToPrint){
        System.out.println("-============================");
        System.out.println("-TOP " + elementsToPrint + " =====================");
        
        System.out.println("-============================");
        
        if(elementsToPrint > this.storeCountedWords.size()){
            elementsToPrint = this.storeCountedWords.size();
        }
        
        for(int i=0; i<elementsToPrint; i++){
            String lineToShow = "\"" + this.storeCountedWords.get(i).getKey() + "\"" + "\t" + this.storeCountedWords.get(i).getValue();
            System.out.println(lineToShow);
        }
    }
    
}
    
    
    
            
            
        

