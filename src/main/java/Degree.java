
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.core.implementation.Data;
import io.mindmaps.core.model.Concept;
import io.mindmaps.core.model.ResourceType;
import io.mindmaps.core.model.RoleType;
import io.mindmaps.factory.MindmapsGraphFactory;
import io.mindmaps.factory.MindmapsTinkerGraphFactory;
import io.mindmaps.factory.MindmapsTitanGraphFactory;
import io.mindmaps.graql.api.parser.QueryParser;
import io.mindmaps.graql.api.query.QueryBuilder;
import io.mindmaps.migration.TransactionManager;
import io.mindmaps.migration.csv.CSVDataMigrator;
import io.mindmaps.migration.csv.CSVSchemaMigrator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.tinkerpop.gremlin.process.computer.ComputerResult;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer;

import static io.mindmaps.graql.api.query.QueryBuilder.var;
import static org.junit.Assert.assertNotNull;

/**
 *
 */

public class Degree {
//    private final String graphConfig = "/opt/mindmaps/resources/conf/titan-cassandra-es.properties";
//    private final String graphConfig = "/opt/mindmaps/resources/conf/titan-cassandra-unit-test.properties";
//    private final String graphConfig = "/opt/mindmaps/resources/conf/titan-cassandra-test-cluster.properties";
private final String graphConfig = "/opt/mindmaps/resources/conf/titan-cassandra-test-hadoop-cluster.properties";
    private MindmapsTransaction transaction;
    private MindmapsGraph mindmapsGraph;

    public void main() {
        // Disable horrid cassandra logs
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
//        System.out.println("======" + System.currentTimeMillis() + "start initialise graph");
//        initialiseGraph();
//        System.out.println("======" + System.currentTimeMillis() + "stop initialise graph");
//        System.out.println("======" + System.currentTimeMillis() + "start load pokemon");
//        loadPokemon();
//        System.out.println("======" + System.currentTimeMillis() + "stop load pokemon");
//        System.out.println("======" + System.currentTimeMillis() + "start load icij");
//        loadICIJ();
//        System.out.println("======" + System.currentTimeMillis() + "stop load icij");
        computeDegree();
    }

    private void initialiseGraph() {
        mindmapsGraph = MindmapsTitanGraphFactory.getInstance().newGraph(graphConfig);
//        mindmapsGraph = MindmapsTinkerGraphFactory.getInstance().newGraph();
        transaction = mindmapsGraph.newTransaction();
        transaction.clearGraph();
        try {
            transaction.commit();
        } catch (MindmapsValidationException e) {
            e.printStackTrace();
        }
    }

    private void loadPokemon() {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get("src/main/resources/graql/pokemon.gql"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String query = lines.stream().reduce("", (s1, s2) -> s1 + "\n" + s2);
        QueryParser.create(transaction).parseInsertQuery(query).execute();
        RoleType value = transaction.getRoleType("has-resource-value");
        ResourceType<Long> degree = transaction.putResourceType("degree", Data.LONG).playsRole(value);
        try {
            transaction.commit();
        } catch (MindmapsValidationException e) {
            e.printStackTrace();
        }
    }

    private void loadICIJ() {

        TransactionManager manager = new TransactionManager(mindmapsGraph);
        CSVSchemaMigrator schemamigrator = new CSVSchemaMigrator(manager);
        CSVDataMigrator datamigrator = new CSVDataMigrator(manager);

        // test a entity

        schemamigrator.migrateSchema("entity", createCSV("/opt/mindmaps/icij-data/Entities.csv"));
        datamigrator.migrateData("entity", createCSV("/opt/mindmaps/icij-data/Entities.csv"));

//        // test an address
//        schemamigrator.migrateSchema("address", createCSV("/opt/mindmaps/icij-data/Addresses.csv"));
//        datamigrator.migrateData("address", createCSV("/opt/mindmaps/icij-data/Addresses.csv"));
//
//        // test an officer
//        schemamigrator.migrateSchema("officer", createCSV("/opt/mindmaps/icij-data/Officers.csv"));
//        datamigrator.migrateData("officer", createCSV("/opt/mindmaps/icij-data/Officers.csv"));
//
//        // test an intermediary
//        schemamigrator.migrateSchema("intermediary", createCSV("/opt/mindmaps/icij-data/Intermediaries.csv"));
//        datamigrator.migrateData("intermediary", createCSV("/opt/mindmaps/icij-data/Intermediaries.csv"));

        try {
            transaction.commit();
        } catch (MindmapsValidationException e) {
            e.printStackTrace();
        }
    }

    private CSVParser createCSV(String path) {
        URL dataUrl = null;
        try {
            dataUrl = new File(path).toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        assert dataUrl != null;
        CSVParser parser = null;
        try {
            parser = CSVParser.parse(dataUrl, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parser;
    }

    private void computeDegree() {
        TitanGraph titanGraph = TitanFactory.open(graphConfig);

        // does page rank work
        try {
            System.out.println("======" + System.currentTimeMillis()/1000 + "(secs) start compute degree");
//            ComputerResult result = titanGraph.compute().program(PageRankVertexProgram.build().create(titanGraph)).submit().get();
//            ComputerResult result = titanGraph.compute().program(new DegreeVertexProgram()).submit().get();
            ComputerResult result = titanGraph.compute().workers(10).program(new DegreeVertexProgram()).submit().get();
            System.out.println("======" + System.currentTimeMillis()/1000 + "(secs) end compute degree");
            System.out.println("======"+System.currentTimeMillis()/1000+"(secs) start print degree");
            result.graph().traversal().V().valueMap().forEachRemaining(System.out::println);
            System.out.println("======"+System.currentTimeMillis()/1000+"(secs) stop print degree");
            System.out.println("======" + System.currentTimeMillis()/1000 + "start commit");
            result.graph().tx().commit();
            System.out.println("======" + System.currentTimeMillis()/1000 + "end commit");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
