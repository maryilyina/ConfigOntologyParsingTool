import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;

import java.util.ArrayList;

/**
 * Created by Mary on 21.02.2017.
 */
public class CtrlSystDevType {
    private static String ONTOLOGICAL_NAME = "CtrlSystDevType";

    private String name;
    private ArrayList<CtrlSystChanGroup> chanGroupList;
    private ArrayList<CtrlSystChannel> channelList;

    public CtrlSystDevType(String name, ArrayList<CtrlSystChanGroup> chanGroups){
        this.name = name;
        chanGroupList = chanGroups;
        channelList = new ArrayList<>();
    }
    public CtrlSystChanGroup findGroupForChannel(int channelNumber){
        int index = 0;
        int lastIndex = 0;
        CtrlSystChanGroup group = null;
        for (int i = 0; i < chanGroupList.size(); i++) {
            group = chanGroupList.get(i);
            index += group.getSize();
            if (channelNumber >= lastIndex && channelNumber < index)
                break;
        }
        return group;
    }

    public void addToOntology(OntModel ontModel, String baseUri){
        OntClass devTypeClass = ontModel.getOntClass(baseUri + ONTOLOGICAL_NAME);
        Individual newDevType = devTypeClass.createIndividual(baseUri + name);

        Property hasName = ontModel.getProperty(baseUri + "hasName");
        newDevType.addProperty(hasName, name);

        Property hasChannel = ontModel.getProperty(baseUri + "hasChannel");
        channelList.forEach(chan ->
                newDevType.addProperty(hasChannel,
                chan.addToOntology(ontModel, baseUri, name)));
    }

    public void addChannel(String channelName, int channelNumber){
        CtrlSystChanGroup chanGroup = findGroupForChannel(channelNumber);
        channelList.add(new CtrlSystChannel(channelName, channelNumber, chanGroup.getDatatype(), chanGroup.getDirection()));
    }

    public void addChanGroup(CtrlSystChanGroup ctrlSystChanGroup) {
        chanGroupList.add(ctrlSystChanGroup);
    }

    public String serialize() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("devtype ").append(name).append("\n")
                .append("\t").append("channels:").append("\n");
        channelList.forEach(channel -> stringBuilder.append("\t\t").append(channel.serialize()).append("\n"));
        return stringBuilder.toString();
    }

    public void addMatchingChannel(String channelName, String oldChannelName) {
        Integer matchingNumber = null;
        for (int i = 0; i < channelList.size(); i++) {
            CtrlSystChannel channel = channelList.get(i);
            if (channel.getName().equals(oldChannelName)){
                matchingNumber = channel.getNumber();
                break;
            }
        }
        if (matchingNumber != null) {
            CtrlSystChanGroup chanGroup = findGroupForChannel(matchingNumber);
            channelList.add(new CtrlSystChannel(channelName, oldChannelName, chanGroup.getDatatype(), chanGroup.getDirection()));
        }}

    public String getName() {
        return name;
    }

    public ArrayList<CtrlSystChannel> getChannelList() {
        return channelList;
    }
}
