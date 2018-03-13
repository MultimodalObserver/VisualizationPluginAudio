package audiovisualizationplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.scene.media.MediaPlayer.Status.STOPPED;
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
    private Reproductor r;
    private boolean isSync;
    

    private static final Logger logger = Logger.getLogger(AudioPlayer.class.getName());

    public AudioPlayer(File file,String id) {
            r = new Reproductor(file);
            ap = new Panel(file,r);
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
                    DockableElement e = new DockableElement(id);
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
            r.pause();
        }
    }

    @Override
    public void seek(long desiredMillis) {
        if(desiredMillis>=start && desiredMillis<=end){            
            r.current(desiredMillis-start);
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
        if(millis>=start && millis <end){
            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ap.play(millis-start,end-start);
                        if(isSync){
                            if(start-millis==0 ||r.getStatus()!=STOPPED){
                                r.play(millis-start,end-start,isSync);                                
                            }
                        }
                        if(!isSync && !isPlaying){
                            r.play(millis-start, end-start,isSync);
                        }
                        isPlaying=true;                    
                    }
                }).start();
        }
        else{
            r.stop();
            isPlaying=false;
        }
    }

    @Override
    public void stop() {
        if(isPlaying){
            ap.stop();
            r.stop();
            isPlaying=false;
        }
    }    

    @Override
    public void sync(boolean bln) {
        isSync=bln;
    }
}
