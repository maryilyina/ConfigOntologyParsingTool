import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Mary on 04.03.2017.
 */
public class CtrlSystLink {
    private static String ONTOLOGICAL_NAME_POINT = "CtrlSystPoint";
    private static String ONTOLOGICAL_NAME_LINK = "CtrlSystLink";

    private String name;
    private ArrayList<String> cpointsNames;
    private String endpoint;
    private double coef;
    private double offset;

    public CtrlSystLink(String name, String endpoint, double coef, double offset) {
        this.name = name;
        cpointsNames = parseCpoints(name);
        this.endpoint = endpoint;
        this.coef = coef;
        this.offset = offset;
    }

    private ArrayList<String> parseCpoints(String fullName) {
        ArrayList<String> cpoints = new ArrayList<>();

        String remainder = fullName;
        int regex_index = remainder.indexOf(".");

        while (regex_index != -1){
            cpoints.add(remainder.substring(0, regex_index));
            remainder = remainder.substring(regex_index + 1);
            regex_index = remainder.indexOf(".");
        }
        cpoints.add(remainder);
        return cpoints;
    }

    public CtrlSystLink(String name) {
        this.name = name;
    }

    public void addToOntology(OntModel ontModel, String baseUri){
        Property hasName = ontModel.getProperty(baseUri + "hasName");
        Property hasParentCpoint = ontModel.getProperty(baseUri + "hasParentCpoint");
        Property hasOffset = ontModel.getProperty(baseUri + "hasOffset");
        Property hasCoefficient = ontModel.getProperty(baseUri + "hasCoefficient");
        Property forCtrlPoint = ontModel.getProperty(baseUri + "forCtrlPoint");
        Property correspondsTo = ontModel.getProperty(baseUri + "correspondsTo");

        //create cpoints
        OntClass cpointClass = ontModel.getOntClass(baseUri + ONTOLOGICAL_NAME_POINT);
        Individual current, previous = null;
        Iterator<String> iterator = cpointsNames.iterator();
        StringBuilder point_name = new StringBuilder();

        while (iterator.hasNext()){
            String cur_name = iterator.next();
            point_name.append(cur_name);
            current = cpointClass.createIndividual(baseUri + point_name.toString());
            current.addProperty(hasName, cur_name);

            if (previous != null)
                current.addProperty(hasParentCpoint, previous);
            previous = current;
            point_name.append("/");
        }


        //create ctrlSystLink
        OntClass clinkClass = ontModel.getOntClass(baseUri + ONTOLOGICAL_NAME_LINK);
        Individual newCtrlLink = clinkClass.createIndividual(baseUri + name);

        newCtrlLink.addProperty(forCtrlPoint, previous);
        newCtrlLink.addProperty(hasCoefficient, String.valueOf(coef));
        newCtrlLink.addProperty(hasOffset, String.valueOf(offset));

        Individual endpointInd = ontModel.getIndividual(baseUri + endpoint.replace(".", "/"));
        if (endpointInd != null)
            newCtrlLink.addProperty(correspondsTo, endpointInd);
    }

    public String serialize() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("cpoint ").append(name).append("\n")
                .append("\t").append("endpoint ").append(endpoint).append("\n")
                .append("\t").append("coef ").append(coef).append("\n")
                .append("\t").append("offset ").append(offset).append("\n");
        return stringBuilder.toString();
    }
}
