import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by Mary on 20.02.2017.
 */
public class PopulatingConfigOntology {
    private static String configFile = "config.lst";
    private static String metamodelOntologyFile = "CtrlSystemMetamodel.owl";
    private static String outputOntologyFile = "CtrlSystemOntology.owl";
    private static String inferredOntologyFile = "CtrlSystemOntology_inferred.owl";

    private static  String baseOntologyUri = "http://www.semanticweb.org/mary/ontologies/2017/0/control-system-ontology/";

    public static void main(String[] args) {
        //OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
        model.setDerivationLogging(true);

        InputStream in = FileManager.get().open(metamodelOntologyFile);
        model.read(in, baseOntologyUri);

        ConfigOntologyParsingTool parser = new ConfigOntologyParsingTool(configFile, model, baseOntologyUri);
        try {
            parser.parse();


            ((PelletInfGraph) model.getGraph()).classify();

            if (model.validate().isValid()) {
                System.out.println("Valid ontology");
                model.write( new FileOutputStream(outputOntologyFile), "RDF/XML", null);
                System.out.println("Ontology model loaded to " + outputOntologyFile);
            }


            /*StmtIterator stmtIterator = inferredModel.listStatements();
            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();
                System.out.println(statement.getSubject() + "	" + statement.getPredicate() + " : " + statement.getObject());
            }*/

           /* OntModel inferredModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF, infModel);
            printIndividuals(infModel,"gid25_group0", "NONE");*/

            //inferredModel.write( new FileOutputStream(inferredOntologyFile), "RDF/XML", null);
            //System.out.println("Inferred ontology model loaded to " + inferredOntologyFile);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
