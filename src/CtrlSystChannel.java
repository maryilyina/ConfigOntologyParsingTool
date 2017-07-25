import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * Created by Mary on 21.02.2017.
 */
public class CtrlSystChannel {
    private static String ONTOLOGICAL_NAME = "CtrlSystDevChannel";

    private boolean isAlias;
    private String name;
    private String aliasFor;
    private int number;
    private char datatype;
    private char direction;
    private int limit; //!!!!!!!!!!!!!

    private  CtrlSystChannel(String name, char datatype, char direction) {
        this.name = name;
        this.datatype = datatype;
        this.direction = direction;
    }

    public CtrlSystChannel(String name, int number, char datatype, char direction) {
        this(name, datatype, direction);
        this.isAlias = false;
        this.number = number;
    }

    public CtrlSystChannel(String name, int number, char datatype, char direction, int limit) {
        this(name, datatype, direction);
        this.limit = limit;
    }

    public CtrlSystChannel(String name, String aliasFor, char datatype, char direction) {
        this(name, datatype, direction);
        this.isAlias = true;
        this.aliasFor = aliasFor;
    }

    public String getName(){
        return name;
    }
    public int getNumber() {
        return number;
    }

    public Individual addToOntology(OntModel ontModel, String baseUri, String baseName){
        OntClass devTypeClass = ontModel.getOntClass(baseUri + ONTOLOGICAL_NAME);
        Individual newDevType = devTypeClass.createIndividual(baseUri + baseName + "/" + name);

        Property hasName = ontModel.getProperty(baseUri + "hasName");
        newDevType.addProperty(hasName, name);

        if (!isAlias) {
            Property hasNumber = ontModel.getProperty(baseUri + "hasNumber");
            newDevType.addProperty(hasNumber, String.valueOf(number), XSDDatatype.XSDint);
        } else {
            Property matches = ontModel.getProperty(baseUri + "matches");
            newDevType.addProperty(matches, ontModel.getIndividual(baseUri + baseName + "/" + aliasFor));
        }

        Property hasDataType = ontModel.getProperty(baseUri + "hasDataType");
        newDevType.addProperty(hasDataType, String.valueOf(datatype));

        Property hasDirectionType = ontModel.getProperty(baseUri + "hasDirectionType");
        newDevType.addProperty(hasDirectionType, String.valueOf(direction));

        return newDevType;
    }

    public String serialize() {
        return new StringBuilder()
                .append(name).append(" ").append(isAlias ? aliasFor : number)
                .append(" type: ").append(datatype)
                .append(" direction: ").append(direction).toString();

    }

}
