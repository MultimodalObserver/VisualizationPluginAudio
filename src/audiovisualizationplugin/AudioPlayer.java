package audiovisualizationplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import mo.core.ui.dockables.DockableElement;
import mo.core.ui.dockables.DockablesRegistry;
import mo.visualization.Playable;

public class AudioPlayer implements Playable{

    private long start;
    private long end;
    private boolean isPlaying=false;
    private boolean isPaused=true;
    private Panel ap;
    private String path;
    

    private static final Logger logger = Logger.getLogger(AudioPlayer.class.getName());

    public AudioPlayer(File file) {
        ap = new Panel(file);
            path = file.getAbsolutePath();
            String path2 =  path.substring(0,path.lastIndexOf(".")) + "-temp.txt";
            String cadena;
            FileReader f;
            try {
                f = new FileReader(path2);
                BufferedReader b = new BufferedReader(f);
                try {
                    if((cadena=b.readLine())!=null){
                        start = Long.parseLong(cadena);
                    }if((cadena=b.readLine())!=null){
                        end = Long.parseLong(cadena);
                    }  
                    b.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } catch (FileNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
            }          
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    DockableElement e = new DockableElement();
                    e.add(ap);
                    DockablesRegistry.getInstance().addAppWideDockable(e);
                }
            }); 
    }
        
    
    @Override
    public void pause() {
        if(isPlaying){
            ap.pause();
            isPlaying=false;
            isPaused=true;
        }
    }

    @Override
    public void seek(long desiredMillis) {
        if(desiredMillis>=start && desiredMillis<=end){            
            ap.current(desiredMillis-start);
        }
    }
    
    @Override
    public long getStart() {        
        return start;
    }

    @Override
    public long getEnd() {
        return end;
    } 

    @Override
    public void play(long millis) {
        if(millis>=start && millis <=end){
            if(!isPlaying){
                ap.play();
                if(!isPaused){
                    ap.stop();
                    seek(start);
                    ap.play();
                    seek(start);
                    ap.pause();
                    seek(start);
                    ap.play();
                    seek(start);
                }
                else{
                    isPaused=false;
                }
                isPlaying=true;
            }
        }
    }

    @Override
    public void stop() {
        if(isPlaying){
            ap.stop();
            isPlaying=false;
        }
    }    
}
