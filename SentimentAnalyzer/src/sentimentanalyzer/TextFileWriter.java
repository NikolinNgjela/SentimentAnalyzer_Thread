/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sentimentanalyzer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Nikolin
 */
public class TextFileWriter {
    
    //Save the path of the file when it will be written.
    private String pathToFile;
    private boolean appendToFile = false;
    
    private FileWriter write;
    private PrintWriter print_line;
    
    public TextFileWriter(){
        
    }
    
    public void createTextFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        timeStamp = timeStamp.replace(" ", "");
        
        //Set the current Date and the hour like name to the file and the .txt at the end
        this.pathToFile = timeStamp.toString() + ".txt";
        
        try {
            this.write = new FileWriter(this.pathToFile, this.appendToFile);
            this.print_line = new PrintWriter(this.write);
        } catch (IOException ex) {
            System.out.println("\n\n" + ex.getMessage() + "\n\n");
            Logger.getLogger(TextFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writeToTextFile_printLineIsOpen(String lineToWrite){
        this.print_line.println(lineToWrite);
    }
    
    public void closePrintLine_PrintWriter(){
        this.print_line.close();
    }
    
}