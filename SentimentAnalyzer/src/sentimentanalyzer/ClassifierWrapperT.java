/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sentimentanalyzer;

import java.util.ArrayList;
import java.util.List;

import org.python.util.PythonInterpreter;
import org.python.core.*;


/**
 *
 * @author Nikolin
 */
public class ClassifierWrapperT {
    
    PySystemState sys;
    PythonInterpreter pi;
    
    public static final String modulename = "unsupervised_sentix_classifier";
    public static final String objectname = "SentixClassifier";
    public static final String sentixdicname = "sentix3.csv";
    public static final String amplifiersname = "amplifiers.txt";
    public static final String decreasersname = "decreasers.txt";
    
    
    public ClassifierWrapperT(){
        
        sys = Py.getSystemState();
        sys.path.append(new PyString("C:\\jython2.7.0\\Lib\\site-packages\\pattern-2.6-py2.7.egg"));
        pi = new PythonInterpreter(null, sys);
        pi.exec("import "+modulename);
        pi.exec("S="+modulename+"."+objectname+"('"+sentixdicname+"','"+amplifiersname+"','"+decreasersname+"')");
	
    }
    
    public double classify(String s) {
        
        pi.set("t", new PyString(s));
        pi.exec("score = S.cleanandscore_with_modifiers(t)");
	PyFloat sentimentscore = (PyFloat)pi.get("score");
        
        return sentimentscore.asDouble();
    }
    
    public List<Double> classify(List<String> l) {
	List<Double> results = new ArrayList<Double>();
		
	// XXX inefficient method: for each string, invoke interpreter		
	for (String s : l) {
		pi.set("t",new PyString(s));
		pi.exec("score = S.cleanandscore_with_modifiers(t)");
		PyFloat sentimentscore = (PyFloat)pi.get("score");
		results.add(sentimentscore.asDouble());
	}
		
	return results;
    } 
    
    public List<Double> classify1(List<String> l) {
	PyList pl = new PyList(l);
	pi.set("tl",pl);
		
	pi.exec("scores = S.cleanandscore_with_modifiers_batch(tl)");
	PyList sentscores = (PyList)pi.get("scores");
		
	List<Double> results = new ArrayList<Double>();
		
	for (Object a : sentscores) {
		Double f = (Double)a;
		results.add(f);
	}
		
	return results;
    }
    
    
    public static double ScoreResult(){
        
        long startTime, endTime;
        int iterations = 10000;
        
        /*
	* Stats
	* 2000 texts : 40 sec
	* 10000 texts: 50 sec
	* Preload time: ca. 30-35 sec
	*/
        
        String teststring = "troppo bello";
        
        /************* START TIMING **************/
        startTime = System.nanoTime();
        
        ClassifierWrapper c = new ClassifierWrapper();
        
 
// You can test the Sentiment Score here...  
//      String s = "troppo bello";
//      double score = c.classify(s);
//      System.out.println("text:\t"+s+"\nscore:\t"+score);
        
        ArrayList<String> a = new ArrayList<String>();
//        a.add("troppo bello");
//        a.add("troppo brutto");
        for (int i=0; i<iterations; i++) a.add(teststring);
        
        List<Double> scores = c.classify1(a);

        
//        for(int i=0; i<a.size(); i++) {
//            System.out.println("Text:\t"+a.get(i)+"\tScore:\t"+scores.get(i));
//        }
       
        endTime = System.nanoTime();
        System.out.println((double)(endTime-startTime)/1000000000);
        /*********** END TIMING ************/
        
        return ((double)(endTime-startTime)/1000000000);
        
    }
    
    
    
}
