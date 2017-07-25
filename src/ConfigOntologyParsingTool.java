import com.hp.hpl.jena.ontology.OntModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * ConfigOntologyParsingTool parses configuration .lst file for CX control system and creates thr ontology model
 *
 * Created by Mary on 21.02.2017.
 */
public class ConfigOntologyParsingTool {
    private static String baseOntologyUri;
    private OntModel ontologyModel;
    private Path ontologyFilePath;
    private HashMap<String, CtrlSystDevType> ontologyDevTypes;

    public ConfigOntologyParsingTool(String input_ontology, OntModel model, String baseOntologyUri) {
        this.ontologyModel = model;
        this.baseOntologyUri = baseOntologyUri;
        ontologyFilePath = Paths.get(input_ontology);
        ontologyDevTypes = new HashMap<>();
    }

    /**
     * Reads the given file by the tags "dev", "devtype", "cpoint"
     * @throws IOException if cannot read file by the given path
     */
    public void parse() throws IOException {
        List<String> lines = Files.readAllLines(ontologyFilePath);

        Iterator<String> iter = lines.iterator();
        while (iter.hasNext()) {
            String line = iter.next();
            line = clearLineSpaces(line);

            /*   parse devtype   */
            if (line.startsWith("devtype")) {
                String[] chulks = line.split(" ");
                createDevType(chulks[1], chulks[2], getBodyFrom(line, iter));
            }
            /*   parse device   */
            else if (line.startsWith("dev")){
                createDevice(line);
            }
            /*   parse cpoint   */
            if (line.startsWith("cpoint")) {
                if (!line.contains("{")) {
                    createCtrlSystLink(null, line.replace("cpoint ", ""));
                }
                else {
                    String basename = line.split(" ")[1];   // name of base cpoint
                    String[] cpoint_lines = getBodyFrom(line, iter).split("\n");
                    for (int i = 0; i < cpoint_lines.length; i++) {
                        if (!cpoint_lines[i].equals("")) {
                            createCtrlSystLink(basename, cpoint_lines[i]);
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes extra spaces from the line
     * @param line
     * @return cleared line
     */
    private String clearLineSpaces(String line) {
        return line.trim().replace("\t", " ").replace("     ", " ").replace("    ", " ").replace("   ", " ").replace("  ", " ");
    }


    /**
     * Cuts the body of the text bounded in figure brackets "{...}", missing the comments defined by "#"
     * @param line - line to start searching with
     * @param iter - iterator
     * @return body
     */
    private String getBodyFrom(String line, Iterator<String> iter) {
        StringBuilder body = new StringBuilder();
        if (line.endsWith("{")) {
            line = iter.next();
            while (line != null && !line.endsWith("}")) {
                if (!line.startsWith("#"))
                    body.append(clearLineSpaces(line)).append("\n");
                line = iter.next();
            }
        }
        return body.toString();
    }

    /**
     * Creates CtrlSystDevice object with parsed parameters and adds it to the ontology
     * @param parameters - device parameters
     */
    private void createDevice(String parameters) {
        String[] chulks = parameters.split(" ");
        String name = chulks[1];

        String devTypeName, driverName = null, busInfo = null, auxInfo = null;
        ArrayList<CtrlSystChanGroup> chanGroups = null;

        if (!chulks[2].contains("/"))
            devTypeName = chulks[2];
        else {
            int regex_index = chulks[2].indexOf("/");
            devTypeName = chulks[2].substring(0, regex_index);
            driverName = chulks[2].substring(regex_index + 1);
        }

        if (!chulks[3].equals("~")) {
            chanGroups = parseChanGroups(chulks[3]);
        }

        if (chulks.length >= 5)
            busInfo = chulks[4];

        if (chulks.length >= 6)
            auxInfo = parameters.substring(parameters.indexOf(chulks[4]) + chulks[4].length() + 1);

        CtrlSystDevice device = new CtrlSystDevice(name, ontologyDevTypes.get(devTypeName), chanGroups, driverName, busInfo, auxInfo);
        System.out.println(device.serialize());
        device.addToOntology(ontologyModel, baseOntologyUri);

    }

    /**
     * Creates CtrlSystDevType object with parsed parameters and adds it to the ontology
     * @param name - object name
     * @param changroups - channel group info
     * @param body - body with extra devtype parameters
     */
    private void createDevType(String name, String changroups, String body) {
        CtrlSystDevType devType = new CtrlSystDevType(name, parseChanGroups(changroups));

        //parse channels
        String[] lines = body.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String channels[] = lines[i].split(" ");
            Integer number;
            if (channels.length > 0 && !channels[0].equals("")) {
                try {
                    number = Integer.valueOf(channels[1].equals("") ? channels[2] : channels[1]);
                    //raw<0-7>
                    if (channels[0].contains("<") && channels[0].endsWith(">")) {
                        // raw
                        String prefix = channels[0].substring(0, channels[0].indexOf("<"));

                        //[0,7]
                        String[] subnumbers = channels[0].substring(channels[0].indexOf("<") + 1, channels[0].length() - 1).split("-");
                        Integer start = Integer.valueOf(subnumbers[0]);
                        Integer end = Integer.valueOf(subnumbers[1]);

                        if (start != null && end != null)
                            for (int k = start; k <= start + end; k++)
                                devType.addChannel(prefix + String.valueOf(k), number + k);
                    } else
                        devType.addChannel(channels[0], number);
                } catch (NumberFormatException nf_exc) {
                    // adc_t    adc12
                    devType.addMatchingChannel(channels[0], channels[1]);
                }
            }
        }

        System.out.println(devType.serialize());
        devType.addToOntology(ontologyModel, baseOntologyUri);
        ontologyDevTypes.put(devType.getName(), devType);
    }

    /**
     * Parses a list of channel groups of device
     * @param line - line that contains channel group descriptions
     * @return channel groups list
     */
    private ArrayList<CtrlSystChanGroup> parseChanGroups(String line) {
        String[] groups = line.split(",");
        ArrayList<CtrlSystChanGroup> changroups = new ArrayList<>();
        for (int i = 0; i < groups.length; i++) {
            int length = groups[i].getBytes().length;
            String size = groups[i].substring(1, length - 1);
            changroups.add(new CtrlSystChanGroup(groups[i].charAt(0), Integer.valueOf(size), groups[i].charAt(length - 1)));
        }
        return changroups;
    }


    /**
     * Creates CtrlSystemLink object with parsed parameters and adds it to the ontology
     * @param basename - parent cpoint name(if exists)
     * @param line - line with other parameters
     */
    private void createCtrlSystLink(String basename, String line) {
        String[] chulks = line.split(" ");

        if (chulks.length > 2 && chulks[2].startsWith("r:"))
            chulks[2] = chulks[2].substring(2);
        CtrlSystLink clink = new CtrlSystLink(
                basename == null ? chulks[0] : basename.concat("." + chulks[0]),    // name
                chulks[1],                                                          // endpoint
                chulks.length > 2 ? Double.valueOf(chulks[2]) : 1,                  // mult coefficient
                chulks.length > 3 && !chulks[3].startsWith("#") ? Double.valueOf(chulks[3]) : 0); // zero offset

        System.out.println(clink.serialize());
        clink.addToOntology(ontologyModel, baseOntologyUri);
    }

}
