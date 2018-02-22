package sample;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class Controller {
    private final int LINE = 0;
    private final int RECTANGLE = 1;
    private final int SELECTANDMOVE = 2;
    private final int ELLIPSE = 3;

    @FXML
    private SplitPane splitPane;
    @FXML
    AnchorPane drawArea;
    @FXML
    private RadioButton selectAndMove;
    @FXML
    private RadioButton ellipse;
    @FXML
    private RadioButton rectangle;
    @FXML
    private RadioButton line;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Button delete;
    @FXML
    private Button clone;
    ToggleGroup group = new ToggleGroup();
    private ArrayList<Line> lines;
    private ArrayList<Rectangle> rectangles;
    private ArrayList<Ellipse> ellipses;
    private int status;
    private double lastClickedX;
    private double lastClickedY;

    @FXML
    private void initialize() {
        ////////////////
        //drawArea.getChildren().addAll(createCircle(300, 300, 100, colorPicker.getValue()));



        ///////////////////


        lastClickedX = -1;
        lastClickedY = -1;
        disableButtons();
        lines = new ArrayList<Line>();
        rectangles = new ArrayList<Rectangle>();
        ellipses = new ArrayList<Ellipse>();

        selectAndMove.setToggleGroup(group);
        ellipse.setToggleGroup(group);
        rectangle.setToggleGroup(group);
        line.setToggleGroup(group);

        selectAndMove.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                status = SELECTANDMOVE;
                enableButtons();
            }
        });

        ellipse.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                status = ELLIPSE;
                disableButtons();
            }
        });

        rectangle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                status = RECTANGLE;
                disableButtons();
            }
        });

        line.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                status = LINE;
                disableButtons();
            }
        });

        line.setSelected(true);

        drawArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                switch (status) {
                    case SELECTANDMOVE:
                        setLastClickedPosition(event.getX(), event.getY());
                        break;
                    case ELLIPSE:
                        drawEllipse(event.getX(), event.getY(), 75, 40);
                        break;
                    case RECTANGLE:
                        drawRectangle(event.getX(), event.getY(), 150, 100);
                        break;
                    case LINE:
                        drawLine(event.getX(), event.getY(), 200);
                        break;
                }
            }
        });

        delete.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                delete();
            }
        });
    }

    private void drawLine(double x, double y, double width) {
        Line l = new Line(x, y, x+width, y);
        l.setStrokeWidth(10);
        l.setStroke(colorPicker.getValue());
        drawArea.getChildren().addAll(l);
        lines.add(l);
    }

    private void drawRectangle(double x, double y, double width, double height) {
        Rectangle r = new Rectangle(x, y, width, height);
        r.setFill(colorPicker.getValue());
        r.setStroke(colorPicker.getValue());
        drawArea.getChildren().addAll(r);
        rectangles.add(r);
    }

    private void drawEllipse(double x, double y, double radiusX, double radiusY) {
        Ellipse e = new Ellipse(x, y, radiusX, radiusY);
        e.setFill(colorPicker.getValue());
        e.setStroke(colorPicker.getValue());
        /*e.setCursor(Cursor.HAND);

        e.setOnMousePressed((t) -> {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
            Ellipse tmp = (Ellipse) (t.getSource());
            tmp.toFront();
        });

        e.setOnMouseDragged((t) -> {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;
            Ellipse tmp = (Ellipse) (t.getSource());
            tmp.setCenterX(tmp.getCenterX() + offsetX);
            tmp.setCenterY(tmp.getCenterY() + offsetY);
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
        });*/

        drawArea.getChildren().addAll(e);
        ellipses.add(e);
    }

    private void setLastClickedPosition(double x, double y) {
        lastClickedX = x;
        lastClickedY = y;
    }

    private boolean delete() {
        if (lastClickedX != -1 && lastClickedY != -1) {
            for (Line l : lines) {
                if (lastClickedX >= l.getStartX() && lastClickedX <= l.getEndX() && ((lastClickedY >= l.getStartY() && lastClickedY <= l.getStartY() + l.getStrokeWidth() / 2) || (lastClickedY < l.getStartY() && lastClickedY >= l.getStartY() - l.getStrokeWidth() / 2))) {
                    drawArea.getChildren().remove(l);
                    lines.remove(l);
                    lastClickedX = -1;
                    lastClickedY = -1;
                    return true;
                }
            }
            for (Rectangle r : rectangles) {
                if (lastClickedX >= r.getX() && lastClickedX <= r.getX() + r.getWidth() && lastClickedY >= r.getY() && lastClickedY <= r.getY() + r.getHeight()) {
                    drawArea.getChildren().remove(r);
                    lines.remove(r);
                    lastClickedX = -1;
                    lastClickedY = -1;
                    return true;
                }
            }
            for (Ellipse e : ellipses) {
                double tmpX = lastClickedX - e.getCenterX();
                double tmpY = lastClickedY - e.getCenterY();
                tmpX/=e.getRadiusX();
                tmpY/=e.getRadiusY();
                double r = tmpX * tmpX + tmpY * tmpY;
                if (r < 1.0) {
                    drawArea.getChildren().remove(e);
                    lines.remove(e);
                    lastClickedX = -1;
                    lastClickedY = -1;
                    return true;
                }
            }
        }
        return false;
    }

    private void disableButtons() {
        delete.setDisable(true);
        clone.setDisable(true);
        lastClickedX = -1;
        lastClickedY = -1;
    }

    private void enableButtons() {
        delete.setDisable(false);
        clone.setDisable(false);
    }

    double orgSceneX, orgSceneY;
    private Circle createCircle(double x, double y, double r, Color color) {
        Circle circle = new Circle(x, y, r, color);

        circle.setCursor(Cursor.HAND);

        circle.setOnMousePressed((t) -> {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();

            Circle c = (Circle) (t.getSource());
            c.toFront();
        });
        circle.setOnMouseDragged((t) -> {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;

            Circle c = (Circle) (t.getSource());

            c.setCenterX(c.getCenterX() + offsetX);
            c.setCenterY(c.getCenterY() + offsetY);

            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
        });
        return circle;
    }
}
