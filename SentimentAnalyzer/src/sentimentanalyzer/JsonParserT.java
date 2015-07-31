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
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Locale;
import javax.swing.table.DefaultTableModel;

public class JsonParserT {
    private static final String filePath = "twitter.json";
    
    public ArrayList<String> llTitlesWords;
    public ArrayList<String> listToHoldCounted;
    
    public ArrayList<EntryElements> storeCountedWords;
    
    public ArrayList<EntryElementsDate> wordsAndCreatedDates;
    public EntryElementsManager storeCountedWords_forPloting;
    
    public Stopwords stopwords;
    
    private DefaultTableModel tableForPrint_model;
    
    
    public JsonParserT(DefaultTableModel tableForPrint_model){
        this.llTitlesWords = new ArrayList<>();
        this.listToHoldCounted = new ArrayList<>();
        
        this.storeCountedWords = new ArrayList<>();
        this.wordsAndCreatedDates = new ArrayList<>();
        this.storeCountedWords_forPloting = new EntryElementsManager();
        
        this.stopwords = new Stopwords();
        
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

            while (i.hasNext()) {
                count++;
                JSONObject innerObj = (JSONObject) i.next();
                JSONObject user_id = (JSONObject) innerObj.get("user");

                System.out.println( count + " Content: " +  innerObj.get("text"));
                
                System.out.println("Time of retrieval " + innerObj.get("created_at"));
                //System.out.println("The id of the user: " + user_id.get("id"));
                
                String holder = this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(innerObj.get("text").toString());
                String holderTime = this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(innerObj.get("created_at").toString());
                
                System.out.println(holder);
                System.out.println("    -" + holderTime);
                
                String[] individualWordsInsideTitle = holder.split(" ");
                
                for(int j=0; j<individualWordsInsideTitle.length; j++){
                    this.llTitlesWords.add(this.stopwords.cleanSpecialCharacters(individualWordsInsideTitle[j]));

                    //EXECUTE BELOW TWO LINES TO SEE HOW THE WORDS CHANGE
                    //System.out.println("\t\t\t B:" + individualWordsInsideTitle[j]);
                    //System.out.println("\t\t\t A:" + stopwords.cleanSpecialCharacters(individualWordsInsideTitle[j]));
                }
                
                this.tableForPrint_model.addRow(new Object[] { holderTime, holder });
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());        
        }
    }
    
    public void selectEvery6HoursPrintAndStoreResults(String jsonRequest){
        this.selectAndLoopOverResults_JSON(jsonRequest);

        this.llTitlesWords = stopwords.eraseStopwordsFromList(this.llTitlesWords);
        this.countOccouranceOfEachWordInListAndPopulateNewLists_forUnsortedListsPopulation(this.llTitlesWords);

        Collections.sort(this.storeCountedWords);
        this.printTop_fromInbuildMergeSort(10);
    }
    
    public ArrayList<EntryElements> selectListForMainWindow(String jsonRequest){
        this.storeCountedWords.clear();

        this.selectAndLoopOverResults_JSON(jsonRequest);

        this.llTitlesWords = stopwords.eraseStopwordsFromList(this.llTitlesWords);
        this.countOccouranceOfEachWordInListAndPopulateNewLists_forUnsortedListsPopulation(this.llTitlesWords);

        Collections.sort(this.storeCountedWords);

        return this.storeCountedWords;
    }
    
    
    public Boolean selectAndLoopOverResults_JSON_withDateTimes(LocalDateTime from, LocalDateTime to, int interval, String jsonRequest){
        try{
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonRequest);
            JSONArray text = (JSONArray) jsonObject.get("tweetsFeedback");

            Iterator i = text.iterator();
            int count = 0;

            this.wordsAndCreatedDates.clear();
            
            while (i.hasNext()) {
                
                try{
                    count++;
                    JSONObject innerObj = (JSONObject) i.next();

                    String holder = this.stopwords.cleanSpecialCharacters_onFullStringBeforeSplitting(innerObj.get("text").toString());
                    //System.out.println(" L: -" + holder);
                    String[] individualWordsInsideTitle = holder.split(" ");
                    String createdAt = innerObj.get("created_at").toString();

                    //SPECIAL CASES WERE DATE IS NOT PARSED OK FROM JSON -- TEAM IN ITALY SHOULD CHECK THE JSON, IS NOT CORRECT SOME TIMES
                    createdAt = createdAt.replace("+0000 ", "");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d H:m:s yyyy", Locale.ENGLISH);
                    LocalDateTime date = LocalDateTime.parse(createdAt, formatter); //2015-03-21T10:42:05 -- i shton nje T ne mes

                    for(int j=0; j<individualWordsInsideTitle.length; j++){
                        EntryElementsDate element = new EntryElementsDate(this.stopwords.cleanSpecialCharacters(individualWordsInsideTitle[j]), date);
                        //System.out.println(" R: -" + element.word + "- D: " + element.date.toString());
                        this.wordsAndCreatedDates.add(element);
                    }

                    //System.out.println(" S: -" + this.wordsAndCreatedDates.size());
                }catch(Exception e){
                    System.out.println(e.getMessage() + " ERROR CAUGHT AT: selectAndLoopOverResults_JSON_withDateTimes() - inside WHILE LOOP, line 150, JSON RECORD IS NOT ABLE TO PARSE, JsonParserT, lines 164 - 173");
                }
            }
            
            this.storeCountedWords_forPloting = new EntryElementsManager(interval, from, to, this.wordsAndCreatedDates);
            
            return true; 
        }catch (Exception ex){
            System.out.println(ex.getMessage() + " ERROR CAUGHT AT: selectAndLoopOverResults_JSON_withDateTimes(), JsonParserT, lines 164 - 173");    
            return false;
        }
    }
    
    //PREPARES ALL THE LISTS BASED ON THE INTERVAL ARGUMENT
    //MIGHT TAKE A WHILE TO RUN (on excecution)
    public EntryElementsManager selectListForMainWindow_forPloting_linkedListOfLinkedLists(LocalDateTime from, LocalDateTime to, int interval, String jsonRequest){

        Boolean run = this.selectAndLoopOverResults_JSON_withDateTimes(from, to, interval, jsonRequest);
        
        if(run){
            this.storeCountedWords_forPloting.prepareLists();
            this.storeCountedWords_forPloting.populateMyData();

            //BELOW LOGICS ARE INTEGRATED INSIDE EACH EntryElementsHolder Object, EACH ONE COUNTS ITS OWN WORDS, MAIN FUNCTION CALLED FROM EntryElementsManager THAT LOOPS
            //this.countOccouranceOfEachWordInListAndPopulateNewLists_forUnsortedListsPopulation(this.llTitlesWords);
            //Collections.sort(this.storeCountedWords_forPloting);

            this.storeCountedWords_forPloting.countAllWordsInTheLists();
            //this.storeCountedWords_forPloting.sortAllWordsInTheLists_afterCounted();

            return this.storeCountedWords_forPloting;
        }
        
        return null;
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
    
    
    
            
            
        

