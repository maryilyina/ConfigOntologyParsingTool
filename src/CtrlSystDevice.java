import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.apache.jena.atlas.iterator.Iter;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Mary on 03.03.2017.
 */
public class CtrlSystDevice {
    private static String ONTOLOGICAL_NAME_DEVICE = "CtrlSystDevice";

    private String name;
    private String devType;
    private String driverName;
    private String busInfo;
    private String auxInfo;

    private ArrayList<CtrlSystChanGroup> chanGroupList;
    private ArrayList<CtrlSystChannel> channelList;

    public CtrlSystDevice(String name, CtrlSystDevType devType, ArrayList<CtrlSystChanGroup> chanGroups, String driverName, String busInfo, String auxInfo){
        this.name = name;
        this.driverName = driverName;
        this.busInfo = busInfo;
        this.auxInfo = auxInfo;
        this.chanGroupList = chanGroups;

        if (devType != null) {
            this.devType = devType.getName();
            this.channelList = devType.getChannelList();
        }
    }

    public void addToOntology(OntModel ontModel, String baseUri) {
        OntClass deviceClass = ontModel.getOntClass(baseUri + ONTOLOGICAL_NAME_DEVICE);
        Individual newDevice = deviceClass.createIndividual(baseUri + name);

        Property hasName = ontModel.getProperty(baseUri + "hasName");
        newDevice.addProperty(hasName, name);

        Property hasBusInfo = ontModel.getProperty(baseUri + "hasBusInfo");
        newDevice.addProperty(hasBusInfo, busInfo);

        Property hasAuxInfo = ontModel.getProperty(baseUri + "hasAuxInfo");
        newDevice.addProperty(hasAuxInfo, auxInfo);

        if (driverName != null) {
            Property hasDriverName = ontModel.getProperty(baseUri + "hasDriverName");
            newDevice.addProperty(hasDriverName, driverName);
        }

        if (devType != null) {
            Property hasDevType = ontModel.getProperty(baseUri + "hasDevType");
            newDevice.addProperty(hasDevType, ontModel.getIndividual(baseUri + devType));
        }
        else if (chanGroupList != null) {
            channelList = new ArrayList<>();
            int seqNumber = 0;
            for (int i = 0; i < chanGroupList.size(); i++ ){
                CtrlSystChanGroup group = chanGroupList.get(i);
                for (int k = 0; k < group.getSize(); k++) {
                    channelList.add(new CtrlSystChannel(String.valueOf(seqNumber), seqNumber, group.getDatatype(), group.getDirection()));
                    seqNumber++;
                }
            }
        }

        if (channelList != null) {
            Property hasChannel = ontModel.getProperty(baseUri + "hasChannel");
            channelList.forEach(chan ->
                    newDevice.addProperty(hasChannel,
                            chan.addToOntology(ontModel, baseUri, name)));
        }

    }


    public String serialize() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("device ").append(name).append("\n")
                .append("\t").append("type: ").append(devType).append("\n")
                .append("\t").append("bus: ").append(busInfo).append("\n")
                .append("\t").append("aux: ").append(auxInfo).append("\n");
        if (driverName != null)
            stringBuilder.append("\t").append("driver: ").append(driverName).append("\n");
        return stringBuilder.toString();
    }
}
