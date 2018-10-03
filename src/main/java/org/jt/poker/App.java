package org.jt.poker;

import org.jt.poker.filewatcher.FileWatcher;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class App {
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());
    public static void main(String[] args) {
        System.out.println("Hello World!");
        try {
//            URI uri  = new URI("file:///C:/Users/joach/AppData/Local/PokerStars/HandHistory/Beaker103/HH20180923%20Halley%20-%20%240.01-%240.02%20-%20USD%20No%20Limit%20Hold%27em.txt") ;
            URI uri  = new URI("file:///C:/temp/test.txt") ;
            File file = new File(uri) ;
            FileWatcher fw = new FileWatcher(file,100) ;
            fw.run();
        }
        catch(java.net.URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "URISyntaxException",ex) ;
        }


    }
}
