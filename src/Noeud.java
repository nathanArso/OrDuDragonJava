import javafx.scene.shape.Circle;

/**
 * Created by 201453471 on 2016-05-06.
 */
public class Noeud extends Circle {

    int x;
    int y;

    public Noeud(){
        super();
    }

    public Noeud(int x, int y, int radius){
        super(x,y,radius);
        this.x = x;
        this.y = y;
    }

}
