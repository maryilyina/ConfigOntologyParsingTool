import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Mary on 21.02.2017.
 */
public class CtrlSystChanGroup {
    private int size;
    private char datatype;
    private char direction;

    public CtrlSystChanGroup(char direction, Integer size, char datatype) {
        this.size = size;
        this.datatype = datatype;
        this.direction = direction;
    }

    public int getSize() {
        return size;
    }

    public char getDatatype() {
        return datatype;
    }

    public char getDirection() {
        return direction;
    }

}
