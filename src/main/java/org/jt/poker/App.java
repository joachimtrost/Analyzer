package org.jt.poker;

import org.jt.poker.filewatcher.FileWatcher;
import org.jt.poker.filewatcher.FileWatcherReader;

import javax.imageio.IIOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class App {
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        try {
//            URI uri  = new URI("file:///C:/Users/joach/AppData/Local/PokerStars/HandHistory/Beaker103/HH20180923%20Halley%20-%20%240.01-%240.02%20-%20USD%20No%20Limit%20Hold%27em.txt") ;
//            URI uri = new URI("file:///C:/temp/test.txt");
            URI uri = new URI("file:///C:/Users/joach/AppData/Local/PokerStars/HandHistory/Beaker103");

            File file = null  ;
            File actual = new File(uri);
            long mostRecent = 0l ;
            for( File f : actual.listFiles()){
                long lastModified = f.lastModified() ;
//                LOGGER.info(String.format("name %s date %s", f.getName(), lastModified ));
                if(lastModified > mostRecent) {
                    mostRecent = lastModified ;
                    file = f ;
                }
            }
            LOGGER.info(String.format("most recent file name %s date %s", file.getName(), file.lastModified() ));

            FileWatcherReader fwr = new FileWatcherReader(file, 1000);
            BufferedReader br = new BufferedReader(fwr);
            while (true) {
//                LOGGER.info("before readLine");
//                LOGGER.info(br.readLine());
                System.out.println(br.readLine());
//                LOGGER.info("after readLine");
            }
        } catch (java.net.URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "URISyntaxException", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IOException", ex);
        }
    }
}
