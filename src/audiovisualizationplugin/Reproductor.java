package audiovisualizationplugin;

import java.awt.BorderLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javax.swing.JPanel;

public class Reproductor extends JPanel{
    
    private File MEDIA_URL;
    private Media media;
    private MediaPlayer mediaPlayer;
    MediaView mediaView;
    public long end;
    long seekSlider=0;
    int cambio = 0;
     
    private void initFxLater(JFXPanel panel){
        Group root = new Group();
         
        try {
            // create media player
            media = new Media(MEDIA_URL.toURI().toURL().toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(Reproductor.class.getName()).log(Level.SEVERE, null, ex);
        }
        mediaPlayer = new MediaPlayer(media);
        //mediaPlayer.setRate(0.9);
        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                end=(long)media.getDuration().toMillis();
            }
        });
        mediaView = new MediaView(mediaPlayer);
        Scene scene = new Scene(root, mediaView.getFitWidth(), mediaView.getFitHeight());
        ((Group)scene.getRoot()).getChildren().add(mediaView);
         
        panel.setScene(scene);
    }
    
    
    public Reproductor(File file){
        
        MEDIA_URL = file;
        final JFXPanel jFXPanel = new JFXPanel();
        initFxLater(jFXPanel);     
        add(jFXPanel);
    }
 
    public JPanel getPanel() {
        return this;
    }
    
    public void play(){
        mediaPlayer.play();
    }
    public void stop(){
        mediaPlayer.stop();
    }
    public void pause(){
        mediaPlayer.pause();
    }
    public void current(long sw){
        new Thread(new Runnable() {

            @Override
            public void run() {
                    mediaPlayer.seek(Duration.millis(sw)); 
            }
        }).start();
    } 
    
    public long time(){
        return (long) mediaPlayer.getCurrentTime().toMillis();
    }
    
    public long duration(){
        return (long)mediaPlayer.getTotalDuration().toMillis();
    }
    
}
