package sample;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class Controller {
    private final int LINE = 0; // status corresponding to draw line mode
    private final int RECTANGLE = 1; // status corresponding to draw rectangle mode
    private final int SELECTANDMOVE = 2; // statuc corresponding to select/move mode
    private final int ELLIPSE = 3; // status corresponding to draw ellipse mode

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
    ToggleGroup group = new ToggleGroup(); // group all radioButtons
    private ArrayList<Line> lines; // list of lines on the draw area
    private ArrayList<Rectangle> rectangles; // list of rectangles on draw area
    private ArrayList<Ellipse> ellipses; // list of ellipses on draw area
    private int status; // status corresponding to the radio button of group selected
    private double lastClickedX; // last clicked point x in select/move mode usefull to delete an object or clone it
    private double lastClickedY; // last clicked point y in select/move mode usefull to delete an object or clone it
    double orgSceneX;
    double orgSceneY;

    // event when an object is moved
    private EventHandler<MouseEvent> catchEvent = (t) -> {
        orgSceneX = t.getSceneX();
        orgSceneY = t.getSceneY();
        if (t.getSource() instanceof Ellipse) {
            Ellipse tmp = (Ellipse) (t.getSource());
            tmp.toFront();
        }
        else if (t.getSource() instanceof Rectangle) {
            Rectangle tmp = (Rectangle) (t.getSource());
            tmp.toFront();
        }
        else {
            Line tmp = (Line) (t.getSource());
            tmp.toFront();
        }
    };

    // event when an object is moved
    private EventHandler<MouseEvent> releaseEvent = (t) -> {
        double offsetX = t.getSceneX() - orgSceneX;
        double offsetY = t.getSceneY() - orgSceneY;
        if (t.getSource() instanceof Ellipse) {
            Ellipse tmp = (Ellipse) (t.getSource());
            tmp.setCenterX(tmp.getCenterX() + offsetX);
            tmp.setCenterY(tmp.getCenterY() + offsetY);
        }
        else if (t.getSource() instanceof Rectangle) {
            Rectangle tmp = (Rectangle) (t.getSource());
            tmp.setX(tmp.getX() + offsetX);
            tmp.setY(tmp.getY() + offsetY);
        }
        else {
            Line tmp = (Line) (t.getSource());
            tmp.setStartX(tmp.getStartX() + offsetX);
            tmp.setStartY(tmp.getStartY() + offsetY);
            tmp.setEndX(tmp.getEndX() + offsetX);
            tmp.setEndY(tmp.getEndY() + offsetY);
        }
        orgSceneX = t.getSceneX();
        orgSceneY = t.getSceneY();
    };

    // allow drag and drop on objects when clicking on select/move mode
    private EventHandler<MouseEvent> enableDragAndDropEvent = (t) -> {
        enableDragAndDrop();
    };

    // disable drag and drop on objects when clicking on not select/move mode
    private EventHandler<MouseEvent> disableDragAndDropEvent = (t) -> {
        disableDragAndDrop();
    };

    @FXML
    private void initialize() {
        line.addEventHandler(MouseEvent.MOUSE_CLICKED, disableDragAndDropEvent); // if we are not in select/move mode we don't want to move an object
        selectAndMove.addEventHandler(MouseEvent.MOUSE_CLICKED, enableDragAndDropEvent); // we are in select/move so we can move an object
        rectangle.addEventHandler(MouseEvent.MOUSE_CLICKED, disableDragAndDropEvent); // if we are not in select/move mode we don't want to move an object
        ellipse.addEventHandler(MouseEvent.MOUSE_CLICKED, disableDragAndDropEvent); // if we are not in select/move mode we don't want to move an object
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
                        drawEllipse(event.getX(), event.getY(), 75, 40, colorPicker.getValue());
                        break;
                    case RECTANGLE:
                        drawRectangle(event.getX(), event.getY(), 150, 100, colorPicker.getValue());
                        break;
                    case LINE:
                        drawLine(event.getX(), event.getY(), 200, colorPicker.getValue());
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

        clone.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                duplicate();
            }
        });
    }

    // draw a line
    private void drawLine(double x, double y, double width, Paint p) {
        Line l = new Line(x, y, x+width, y);
        l.setStrokeWidth(10);
        l.setStroke(p);
        l.setFill(p);
        drawArea.getChildren().addAll(l);
        lines.add(l);
    }

    // draw a rectangle
    private void drawRectangle(double x, double y, double width, double height, Paint p) {
        Rectangle r = new Rectangle(x, y, width, height);
        r.setFill(p);
        r.setStroke(p);
        drawArea.getChildren().addAll(r);
        rectangles.add(r);
    }

    // draw an ellipse
    private void drawEllipse(double x, double y, double radiusX, double radiusY, Paint p) {
        Ellipse e = new Ellipse(x, y, radiusX, radiusY);
        e.setFill(p);
        e.setStroke(p);
        drawArea.getChildren().addAll(e);
        ellipses.add(e);
    }

    // remember last clicked point when we are in select and move mode to know what object to delete or clone
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

    // clone selected object
    private boolean duplicate() {
        if (lastClickedX != -1 && lastClickedY != -1) {
            for (Line l : lines) {
                if (lastClickedX >= l.getStartX() && lastClickedX <= l.getEndX() && ((lastClickedY >= l.getStartY() && lastClickedY <= l.getStartY() + l.getStrokeWidth() / 2) || (lastClickedY < l.getStartY() && lastClickedY >= l.getStartY() - l.getStrokeWidth() / 2))) {
                    drawLine(lastClickedX, lastClickedY, 200, l.getFill());
                    enableDragAndDrop(lines.get(lines.size()-1));
                    lastClickedX = -1;
                    lastClickedY = -1;
                    return true;
                }
            }
            for (Rectangle r : rectangles) {
                if (lastClickedX >= r.getX() && lastClickedX <= r.getX() + r.getWidth() && lastClickedY >= r.getY() && lastClickedY <= r.getY() + r.getHeight()) {
                    drawRectangle(lastClickedX, lastClickedY, 150, 100, r.getFill());
                    enableDragAndDrop(rectangles.get(rectangles.size()-1));
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
                    drawEllipse(lastClickedX, lastClickedY, 75, 40, e.getFill());
                    enableDragAndDrop(ellipses.get(ellipses.size()-1));
                    lastClickedX = -1;
                    lastClickedY = -1;
                    return true;
                }
            }
        }
        return false;
    }

    // disable buttons if we are not in select/move mode
    private void disableButtons() {
        delete.setDisable(true);
        clone.setDisable(true);
        lastClickedX = -1;
        lastClickedY = -1;
    }

    // enable buttons if we are not in select/move mode
    private void enableButtons() {
        delete.setDisable(false);
        clone.setDisable(false);
    }

    private void enableDragAndDrop(Shape s) {
        s.setOnMousePressed(catchEvent);
        s.setOnMouseDragged(releaseEvent);
    }

    private void disableDragAndDrop(Shape s) {
        s.removeEventFilter(MouseEvent.MOUSE_PRESSED, catchEvent);
        s.removeEventFilter(MouseEvent.MOUSE_DRAGGED, releaseEvent);
    }

    // enable drag and drop if are not in select/move mode
    private void enableDragAndDrop() {
        for (Line l : lines)
            enableDragAndDrop(l);
        for (Rectangle r : rectangles)
            enableDragAndDrop(r);
        for (Ellipse e : ellipses)
            enableDragAndDrop(e);
    }

    // disable drag and drop if we are not in select/move mode
    private void disableDragAndDrop() {
        for (Line l : lines)
            disableDragAndDrop(l);
        for (Rectangle r : rectangles)
            disableDragAndDrop(r);
        for (Ellipse e : ellipses)
            disableDragAndDrop(e);
    }
}
