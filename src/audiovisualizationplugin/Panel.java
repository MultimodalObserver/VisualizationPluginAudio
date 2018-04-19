
package audiovisualizationplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Timer;

class Panel extends JPanel {
    private static java.util.List<Double> datosVoz; //vector de amplitud de la señal
    private int intervalo;
    private double escalaX = 0.008;
    private double escalaY = 0.0141;
    private static int x0 = 15;
    private int y0;
    private int xp,xb,sbValue;
    private int w,h;
    private boolean isPlaying = false;
    public int fin;
    JScrollPane scroller;
    Dimension area = new Dimension(0,0);
    JPanel drawingPane;    
    AudioInputStream flujoEntradaAudio;
    Reproductor r;
    
    JButton AmplitudU = new JButton("+");
    JButton AmplitudD = new JButton("-");
    JButton LongitudU = new JButton("+");
    JButton LongitudD = new JButton("-");
   
    
    public Panel(File f, Reproductor rep){
        drawingPane = new DrawingPane();
        drawingPane.setBackground(Color.BLACK);
        int totalFramesLeidos = 0;
        xp=x0-5;
        try {
            flujoEntradaAudio = AudioSystem.getAudioInputStream(f);
            int bytesPorFrame = flujoEntradaAudio.getFormat().getFrameSize();            
            int numBytes = 1024 * bytesPorFrame;
            byte[] audioBytes = new byte[numBytes];
            int longitudArchivoBytes=(int)flujoEntradaAudio.getFormat().getFrameSize()*(int)flujoEntradaAudio.getFrameLength();
            Datos datos = new Datos(longitudArchivoBytes,flujoEntradaAudio.getFormat().isBigEndian());
            byte[] datosTemporal=new byte[longitudArchivoBytes]; 
            
            int numeroBytesLeidos = 0;
            int numeroFramesLeidos = 0;
            int pos=0;
            while ((numeroBytesLeidos = flujoEntradaAudio.read(audioBytes)) != -1) {
                numeroFramesLeidos = numeroBytesLeidos / bytesPorFrame;
                totalFramesLeidos += numeroFramesLeidos;
                System.arraycopy(audioBytes, 0, datosTemporal, pos, numeroBytesLeidos);
                pos=pos+numeroBytesLeidos;
            }
            datos.llenarByte(datosTemporal);
            datosVoz = new ArrayList<Double>();
            datosVoz=datos.convertirByteADouble(longitudArchivoBytes/bytesPorFrame);
            //intervalo = (int)flujoEntradaAudio.getFrameLength()/100;
            intervalo=1;
        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        } 
        scroller = new JScrollPane(drawingPane);
        scroller.setPreferredSize(new Dimension(600,300));
        add(scroller, BorderLayout.CENTER);
        r = rep;
        add(r);
        AmplitudU.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                        setUEscalaY();
                        repaint();
                }
                
            });  
            AmplitudD.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                        setDEscalaY();
                        repaint();
                }
                
            });  
            LongitudU.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                        setUEscalaX();
                        repaint();
                }
                
            }); 
            LongitudD.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                        setDEscalaX();
                        repaint();
                }
                
            }); 
    }
    
    public void graficarEjes(Graphics g) {
        g.setColor(Color.WHITE);
        linea(0,0,w,0,g);
        linea(0,y0,0,-y0,g);
    }
    
    public void graficarProgreso(Graphics g){
        Color barColor = new Color(255,117,20,127);
        g.setColor(barColor);
        g.fillRect(xp, y0-(drawingPane.getHeight()/2),10,drawingPane.getHeight());
    }
    
    
    class DrawingPane extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            w  = getSize().width;
            h  = getSize().height;
            y0 = h/2;
            AjustarScroll();
            graficarEscpectrograma(g);
            graficarEjes(g);
            graficarProgreso(g);
            
            add(AmplitudU);
            add(AmplitudD);
            add(LongitudU);
            add(LongitudD);
            AmplitudU.setLocation(xb+37, y0-87);
            AmplitudD.setLocation(xb+37, y0-33);
            LongitudU.setLocation(xb+60, y0-60);
            LongitudD.setLocation(xb+20, y0-60);
        }
    }
    public void graficarEscpectrograma(Graphics g){
       g.setColor(Color.GREEN);
        int length = datosVoz.size();
        int[] puntoP = new int[2];
        int i, xi=0,xf=0,yi=0,yf=0;
        //System.out.println(length);
        for(i=0; i<length-intervalo; i+=intervalo) {
             puntoP[0]=(int) (i * escalaX);
            puntoP[1]=(int) (datosVoz.get(i) * escalaY);
            //System.out.println(datosVoz.get(i));
            xi = puntoP[0];
            yi = puntoP[1];
            puntoP[0]=(int) ((i + intervalo) * escalaX);
            puntoP[1] = (int) (datosVoz.get(i+intervalo) * escalaY);
            xf = puntoP[0];
            yf = puntoP[1];
            linea(xi, yi, xf, yf, g);
        }
        fin = xf;
        if(xf>w) {
            area.width = xf+100;
            drawingPane.setPreferredSize(area);
            drawingPane.revalidate();
        } 
    }
     public void AjustarScroll(){
        scroller.setPreferredSize(new Dimension(getWidth(),getHeight()));
    }
     public void linea(double x1, double y1, double x2, double y2, Graphics g) {
        g.drawLine((int)Math.round(x1+x0),
                (int)Math.round(y0-y1),
                (int)Math.round(x2+x0),
                (int)Math.round(y0-y2));
    }
     
    public void setDEscalaX(){
        if(escalaX<1){            
            escalaX=escalaX/1.5;
        }
    }
    public void setUEscalaX(){
        if(escalaX>0){
            escalaX=escalaX*1.5;
        }
    }
    
    public void setDEscalaY(){
        if(escalaY<1){            
            escalaY=escalaY/1.5;
        }
    }
    public void setUEscalaY(){
        if(escalaY>0){
            escalaY=escalaY*1.5;
        }
    }
     
     public void play(long millis,long end){
        JScrollBar sb = scroller.getHorizontalScrollBar();
        double factor=(datosVoz.size()*100)/end;
        factor /=100;
        double cont=1;
        while(factor == 0){
            factor = (datosVoz.size()*cont)/end;
            factor=factor/cont;
            cont*=1000;
        }
        while(factor<0){
            cont/=5;
            factor = (datosVoz.size()*cont)/end;
            factor=factor/cont;
        }
        cont=1;
        double tem =((millis+1)*escalaX*factor);
        xp= (int)(x0+tem-5);
        if(xp<0){
            xp*=(-1);
        }
        sb = scroller.getHorizontalScrollBar();
        if(xp<=fin){
            repaint();
            sb.setValue((int)tem-(x0+10));
            if(sb.getValue()!=sbValue){
                xb=xp;
            }
            sbValue=sb.getValue();
        }else{
            xp=x0-5;
            xb=xp;
            repaint();         
            sb.setValue(0);
        }
     }
          
     public void pause(){
         isPlaying=false;
     }
     public void stop(){
         isPlaying=false;
         xp=x0-5;
     }
     
     public long time(){
         return r.time();
     }
     public void current(long sw){
         r.current(sw);
     }
         
}
