package autoparts.view;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/**
 * El logo de ORIGEN (el árbol de circuitos con el engranaje) dibujado con
 * figuras de JavaFX y animado.
 *
 * Primero se dibuja el árbol: raíces, tronco y ramas. Después, en un ciclo
 * que se repite, unos puntos de luz blancos (los "nutrientes") suben desde
 * las raíces por el tronco, encienden el engranaje y llegan a la punta de
 * cada rama, que brilla.
 *
 * Se usa en Login.fxml y Register.fxml como cualquier otro nodo:
 * {@code <LogoTree/>}. Es un componente visual reutilizable, igual que
 * ComponentCard.
 *
 * Las coordenadas son las del dibujo original (320 × 360) y se reducen con
 * {@link #SCALE}. Es un Pane de tamaño fijo para que los puntos que se mueven
 * no cambien su tamaño ni hagan saltar la pantalla.
 *
 * @author Jahdiel Torres
 */
public class LogoTree extends Pane {

    /** Tamaño del logo respecto al dibujo original de 320 × 360. */
    private static final double SCALE = 0.9;

    private static final Color LINE = Color.web("#4A6690");
    private static final Color NUTRIENT = Color.WHITE;
    private static final Color GLOW = Color.web("#7DD3FC");
    private static final Color LIT = Color.web("#E0F2FE");
    /** Color del centro del engranaje: el mismo del fondo del panel. */
    private static final Color GEAR_HOLE = Color.web("#13203A");

    // Cada línea del árbol es una lista de puntos x, y. Todas van en el
    // sentido en que suben los nutrientes: de la raíz al tronco y del centro
    // hacia la punta de la rama.
    private static final double[][] ROOTS = {
        {78, 336, 112, 336, 150, 298},
        {112, 352, 150, 314, 150, 298},
        {160, 356, 160, 298},
        {242, 336, 208, 336, 170, 298},
        {208, 352, 170, 314, 170, 298}
    };
    private static final double[][] TRUNK = {
        {150, 298, 150, 180},
        {160, 298, 160, 184},
        {170, 298, 170, 180}
    };
    private static final double[][] BRANCHES = {
        {160, 116, 160, 34},
        {148, 120, 148, 90, 120, 62, 120, 30},
        {172, 120, 172, 90, 200, 62, 200, 30},
        {120, 62, 96, 62, 84, 50},
        {200, 62, 224, 62, 236, 50},
        {137, 128, 106, 97, 82, 97, 62, 77},
        {183, 128, 214, 97, 238, 97, 258, 77},
        {128, 145, 92, 145, 68, 121, 42, 121},
        {192, 145, 228, 145, 252, 121, 278, 121},
        {130, 160, 96, 160, 84, 172, 48, 172},
        {190, 160, 224, 160, 236, 172, 272, 172},
        {140, 176, 116, 200, 82, 200},
        {180, 176, 204, 200, 238, 200}
    };

    /** Retraso de cada nutriente (ms) para que no salgan todos a la vez. */
    private static final int[] ROOT_DELAYS = {0, 180, 60, 240, 120};
    private static final int[] BRANCH_DELAYS = {0, 80, 160, 240, 40, 200, 120, 280, 20, 180, 100, 260, 140};

    private final Group drawing = new Group();
    private final List<Path> rootLines = new ArrayList<>();
    private final List<Path> trunkLines = new ArrayList<>();
    private final List<Path> branchLines = new ArrayList<>();
    private final List<Circle> tips = new ArrayList<>();
    private final Group gear = new Group();
    /** Color del engranaje; se anima cuando le llegan los nutrientes. */
    private final ObjectProperty<Color> gearColor = new SimpleObjectProperty<>(LINE);

    private final Animation intro;
    private final Animation cycle;

    public LogoTree() {
        setPrefSize(320 * SCALE, 360 * SCALE);
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        drawing.getTransforms().add(new Scale(SCALE, SCALE));
        getChildren().add(drawing);

        addLines(ROOTS, rootLines);
        addLines(TRUNK, trunkLines);
        addLines(BRANCHES, branchLines);
        addGear();
        addTips();

        intro = createIntro();
        cycle = createCycle();
        intro.setOnFinished(event -> cycle.play());

        // Solo se anima mientras la pantalla está abierta
        sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene == null) {
                intro.stop();
                cycle.stop();
            }
        });
        intro.play();
    }

    // ---------- Dibujo ----------

    private void addLines(double[][] lines, List<Path> target) {
        for (double[] points : lines) {
            Path line = createPath(points);
            line.setStroke(LINE);
            line.setStrokeWidth(5);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.setStrokeLineJoin(StrokeLineJoin.ROUND);
            target.add(line);
            drawing.getChildren().add(line);
        }
    }

    /** Convierte una lista de puntos x, y en una línea quebrada. */
    private Path createPath(double[] points) {
        Path path = new Path(new MoveTo(points[0], points[1]));
        for (int i = 2; i < points.length; i += 2) {
            path.getElements().add(new LineTo(points[i], points[i + 1]));
        }
        return path;
    }

    /** Engranaje en el centro: 8 dientes alrededor de un anillo. */
    private void addGear() {
        for (int angle = 0; angle < 360; angle += 45) {
            Rectangle tooth = new Rectangle(-5, -33, 10, 11);
            tooth.setArcWidth(4);
            tooth.setArcHeight(4);
            tooth.fillProperty().bind(gearColor);
            tooth.getTransforms().add(new Rotate(angle, 0, 0));
            gear.getChildren().add(tooth);
        }
        Circle ring = new Circle(23, GEAR_HOLE);
        ring.setStrokeWidth(6);
        ring.strokeProperty().bind(gearColor);
        Circle center = new Circle(8, Color.TRANSPARENT);
        center.setStrokeWidth(5);
        center.strokeProperty().bind(gearColor);
        gear.getChildren().addAll(ring, center);

        gear.setTranslateX(160);
        gear.setTranslateY(150);
        gear.setOpacity(0);
        drawing.getChildren().add(gear);
    }

    /** Un punto al final de cada rama; brilla cuando le llega el nutriente. */
    private void addTips() {
        for (double[] points : BRANCHES) {
            Circle tip = new Circle(points[points.length - 2], points[points.length - 1], 5, LINE);
            tip.setOpacity(0);
            tips.add(tip);
            drawing.getChildren().add(tip);
        }
    }

    // ---------- Animación ----------

    /**
     * Al abrir la pantalla: se dibujan las raíces, luego el tronco y luego
     * las ramas, como si el árbol creciera.
     */
    private Animation createIntro() {
        ParallelTransition grow = new ParallelTransition();
        for (Path line : rootLines) {
            grow.getChildren().add(drawLine(line, 200));
        }
        for (Path line : trunkLines) {
            grow.getChildren().add(drawLine(line, 800));
        }
        for (Path line : branchLines) {
            grow.getChildren().add(drawLine(line, 1300));
        }
        grow.getChildren().add(fadeIn(gear, 1100));
        for (Circle tip : tips) {
            grow.getChildren().add(fadeIn(tip, 2000));
        }
        return grow;
    }

    /**
     * Dibuja una línea de a poco: la línea se pinta como un guion tan largo
     * como ella y se va corriendo hasta quedar completa.
     */
    private Animation drawLine(Path line, int delayMillis) {
        double length = lengthOf(line);
        line.getStrokeDashArray().setAll(length, length);
        line.setStrokeDashOffset(length);
        Timeline draw = new Timeline(new KeyFrame(Duration.millis(900),
                new KeyValue(line.strokeDashOffsetProperty(), 0, Interpolator.EASE_OUT)));
        draw.setDelay(Duration.millis(delayMillis));
        draw.setOnFinished(event -> line.getStrokeDashArray().clear());
        return draw;
    }

    /** Largo de una línea quebrada: la suma de sus tramos. */
    private double lengthOf(Path line) {
        double length = 0;
        double lastX = 0;
        double lastY = 0;
        for (var element : line.getElements()) {
            if (element instanceof MoveTo move) {
                lastX = move.getX();
                lastY = move.getY();
            } else if (element instanceof LineTo to) {
                length += Math.hypot(to.getX() - lastX, to.getY() - lastY);
                lastX = to.getX();
                lastY = to.getY();
            }
        }
        return length;
    }

    private Animation fadeIn(Node node, int delayMillis) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), node);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMillis));
        return fade;
    }

    /**
     * El ciclo que se repite: los nutrientes suben por las raíces, después
     * por el tronco, encienden el engranaje y terminan en las ramas.
     */
    private Animation createCycle() {
        ParallelTransition roots = new ParallelTransition();
        for (int i = 0; i < rootLines.size(); i++) {
            roots.getChildren().add(nutrient(rootLines.get(i), 700, ROOT_DELAYS[i], null));
        }
        ParallelTransition trunk = new ParallelTransition();
        for (Path line : trunkLines) {
            trunk.getChildren().add(nutrient(line, 650, 0, null));
        }
        ParallelTransition branches = new ParallelTransition();
        for (int i = 0; i < branchLines.size(); i++) {
            branches.getChildren().add(nutrient(branchLines.get(i), 750, BRANCH_DELAYS[i], tips.get(i)));
        }
        SequentialTransition all = new SequentialTransition(
                roots, trunk, lightGear(), branches, new PauseTransition(Duration.millis(900)));
        all.setCycleCount(Animation.INDEFINITE);
        return all;
    }

    /**
     * Un nutriente: un punto blanco con brillo que recorre una línea. Si la
     * línea termina en una punta de rama, la punta brilla al llegar.
     */
    private Animation nutrient(Path line, int millis, int delayMillis, Circle tip) {
        Circle dot = new Circle(3.5, NUTRIENT);
        dot.setEffect(new DropShadow(10, GLOW));
        dot.setOpacity(0);
        drawing.getChildren().add(dot);

        PathTransition move = new PathTransition(Duration.millis(millis), line, dot);
        move.setInterpolator(Interpolator.LINEAR);

        FadeTransition show = new FadeTransition(Duration.millis(100), dot);
        show.setToValue(1);
        FadeTransition hide = new FadeTransition(Duration.millis(100), dot);
        hide.setToValue(0);
        SequentialTransition visibility = new SequentialTransition(
                show, new PauseTransition(Duration.millis(millis - 200)), hide);

        SequentialTransition trip = new SequentialTransition(
                new PauseTransition(Duration.millis(delayMillis)),
                new ParallelTransition(move, visibility));
        if (tip != null) {
            FillTransition flash = new FillTransition(Duration.millis(250), tip, LINE, LIT);
            flash.setAutoReverse(true);
            flash.setCycleCount(2);
            trip.getChildren().add(flash);
        }
        return trip;
    }

    /** El engranaje se ilumina un momento cuando los nutrientes llegan a él. */
    private Animation lightGear() {
        return new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(gearColor, LINE)),
                new KeyFrame(Duration.millis(180), new KeyValue(gearColor, LIT)),
                new KeyFrame(Duration.millis(450), new KeyValue(gearColor, LINE)));
    }
}
