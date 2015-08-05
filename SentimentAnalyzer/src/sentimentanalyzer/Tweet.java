/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sentimentanalyzer;

import java.time.LocalDateTime;
import java.util.HashSet;

/**
 *
 * @author Nikolin
 */
public class Tweet {
    
    public LocalDateTime createdAt;
    public HashSet<String> words = new HashSet <String>();
    
    
    
    
}
