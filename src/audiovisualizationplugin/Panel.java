package audiovisualizationplugin;

import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class Panel extends AnchorPane {

    private static List<Double> datosVoz;
    private int intervalo;

    private double escalaX = 0.008;
    private double escalaY = 0.0141;

    private static final int x0 = 15;

    private int y0;

    private int xp;

    private boolean isPlaying = false;

    public int fin;

    private AudioInputStream flujoEntradaAudio;

    private Reproductor r;

    private Button AmplitudU = new Button("+");
    private Button AmplitudD = new Button("-");
    private Button LongitudU = new Button("+");
    private Button LongitudD = new Button("-");

    private Canvas drawingCanvas;
    private ScrollPane scrollPane;

    public Panel(File f, Reproductor rep) {
        this.setStyle("-fx-background-color: black;");

        scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: black; -fx-control-inner-background: black;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        drawingCanvas = new Canvas(1200, 400);
        drawingCanvas.setStyle("-fx-background-color: black;");
        scrollPane.setContent(drawingCanvas);

        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            drawingCanvas.setHeight(newBounds.getHeight());
            draw();
        });

        widthProperty().addListener((obs, oldWidth, newWidth) -> {
            scrollPane.setPrefWidth(newWidth.doubleValue());
            draw();
        });
        heightProperty().addListener((obs, oldHeight, newHeight) -> {
            scrollPane.setPrefHeight(newHeight.doubleValue());
            draw();
        });

        getChildren().add(scrollPane);

        r = rep;
        getChildren().add(r.getView());

        GridPane controls = new GridPane();
        controls.setHgap(5);
        controls.setVgap(5);
        controls.add(AmplitudU, 1, 0);
        controls.add(AmplitudD, 1, 2);
        controls.add(LongitudU, 2, 1);
        controls.add(LongitudD, 0, 1);

        AnchorPane.setTopAnchor(controls, 10.0);
        AnchorPane.setLeftAnchor(controls, 10.0);
        getChildren().add(controls);

        setupAudioData(f);

        AmplitudU.setStyle("-fx-background-color: #b4eda6; -fx-text-fill: black;");
        AmplitudD.setStyle("-fx-background-color: ea908a; -fx-text-fill: black;");
        LongitudU.setStyle("-fx-background-color: #b4eda6; -fx-text-fill: black;");
        LongitudD.setStyle("-fx-background-color: ea908a; -fx-text-fill: black;");

        AmplitudU.setOnAction(e -> {
            setUEscalaY();
            draw();
        });
        AmplitudD.setOnAction(e -> {
            setDEscalaY();
            draw();
        });
        LongitudU.setOnAction(e -> {
            setUEscalaX();
            draw();
        });
        LongitudD.setOnAction(e -> {
            setDEscalaX();
            draw();
        });

        MediaPlayer mp = r.getMediaPlayer();
        mp.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            if (isPlaying) {
                long mediaTimeMs = (long) newVal.toMillis();
                long totalMs = r.end;

                if (totalMs > 0) {
                    xp = (int) (x0 + mediaTimeMs * datosVoz.size() * escalaX / totalMs);
                }

                if (xp >= fin) {
                    stop();
                } else {
                    draw();
                    centerProgressLineInView();
                }
            }
        });

        javafx.application.Platform.runLater(() -> {
            draw();
            centerProgressLineInView();
        });
    }

    private void setupAudioData(File f) {
        try {
            flujoEntradaAudio = AudioSystem.getAudioInputStream(f);
            int bytesPorFrame = flujoEntradaAudio.getFormat().getFrameSize();
            int longitudArchivoBytes = bytesPorFrame * (int) flujoEntradaAudio.getFrameLength();
            Datos datos = new Datos(longitudArchivoBytes, flujoEntradaAudio.getFormat().isBigEndian());
            byte[] datosTemporal = new byte[longitudArchivoBytes];
            flujoEntradaAudio.read(datosTemporal);
            datos.llenarByte(datosTemporal);
            datosVoz = datos.convertirByteADouble(longitudArchivoBytes / bytesPorFrame);
            intervalo = 1;
        } catch (UnsupportedAudioFileException | IOException ex) {
            Logger.getLogger(Panel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void draw() {
        GraphicsContext gc = drawingCanvas.getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, drawingCanvas.getWidth(), drawingCanvas.getHeight());

        y0 = (int) (drawingCanvas.getHeight() / 2);

        int expectedWidth = (int) (datosVoz.size() * escalaX);
        if (expectedWidth < 1) {
            expectedWidth = 1;
        }

        double viewportW = scrollPane.getViewportBounds().getWidth();
        if (Double.isNaN(viewportW)) {
            viewportW = drawingCanvas.getWidth();
        }

        double newCanvasWidth = Math.max(expectedWidth, viewportW);
        drawingCanvas.setWidth(newCanvasWidth);

        graficarEscpectrograma(gc);
        graficarEjes(gc);
        graficarProgreso(gc);
    }

    private void graficarEjes(GraphicsContext gc) {
        gc.setStroke(Color.WHITE);
        linea(0, 0, (int) drawingCanvas.getWidth(), 0, gc);
        linea(0, y0, 0, -y0, gc);
    }

    private void graficarProgreso(GraphicsContext gc) {
        gc.setFill(Color.rgb(255, 117, 20, 0.5));
        gc.fillRect(xp, y0 - (drawingCanvas.getHeight() / 2),
                10, drawingCanvas.getHeight());
    }

    private void graficarEscpectrograma(GraphicsContext gc) {
        gc.setStroke(Color.GREEN);
        int length = datosVoz.size();
        int[] puntoP = new int[2];
        int i, xi = 0, xf = 0, yi = 0, yf = 0;

        for (i = 0; i < length - intervalo; i += intervalo) {
            puntoP[0] = (int) (i * escalaX);
            puntoP[1] = (int) (datosVoz.get(i) * escalaY);
            xi = puntoP[0];
            yi = puntoP[1];

            puntoP[0] = (int) ((i + intervalo) * escalaX);
            puntoP[1] = (int) (datosVoz.get(i + intervalo) * escalaY);
            xf = puntoP[0];
            yf = puntoP[1];

            linea(xi, yi, xf, yf, gc);
        }
        fin = xf;
    }

    private void linea(double x1, double y1, double x2, double y2, GraphicsContext gc) {
        gc.strokeLine(x1 + x0, y0 - y1, x2 + x0, y0 - y2);
    }

    private void centerProgressLineInView() {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        if (Double.isNaN(viewportWidth) || viewportWidth <= 0) {
            return;
        }

        double halfView = viewportWidth / 2.0;
        double desiredCenter = xp;
        double minScrollX = desiredCenter - halfView;
        if (minScrollX < 0) {
            minScrollX = 0;
        }

        double maxScrollX = drawingCanvas.getWidth() - viewportWidth;
        if (maxScrollX < 0) {
            maxScrollX = 0;
        }
        if (minScrollX > maxScrollX) {
            minScrollX = maxScrollX;
        }

        double scrollRatio = (minScrollX) / (drawingCanvas.getWidth() - viewportWidth);
        scrollPane.setHvalue(scrollRatio);
    }

    public void setDEscalaX() {
        if (escalaX < 1) {
            escalaX /= 1.2;
        }
    }

    public void setUEscalaX() {
        if (escalaX > 0) {
            escalaX *= 1.2;
        }
    }

    public void setDEscalaY() {
        if (escalaY < 1) {
            escalaY /= 1.2;
        }
    }

    public void setUEscalaY() {
        if (escalaY > 0) {
            escalaY *= 1.2;
        }
    }

    public void play(long millis, long duration) {
        xp = (int) (x0 + millis * datosVoz.size() * escalaX / duration);

        draw();
        centerProgressLineInView();

        r.getMediaPlayer().seek(javafx.util.Duration.millis(millis));

        javafx.application.Platform.runLater(() -> {
            r.getMediaPlayer().play();
            isPlaying = true;
        });
    }

    public void pause() {
        isPlaying = false;
        r.getMediaPlayer().pause();
    }

    public void stop() {
        isPlaying = false;
        xp = x0;
        r.getMediaPlayer().stop();
        draw();
        centerProgressLineInView();
    }

    public void syncWithAudio(long currentMillis, long duration) {
        double factor = datosVoz.size() * escalaX / duration;
        xp = (int) (x0 + currentMillis * factor);
        draw();
        centerProgressLineInView();
    }
}
