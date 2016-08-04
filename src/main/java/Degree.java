
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.thinkaurelius.titan.graphdb.olap.computer.FulgoraGraphComputer;
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
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph;
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
import java.util.concurrent.Future;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.tinkerpop.gremlin.process.computer.GraphComputer;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankVertexProgram;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalEngine;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.spark.process.computer.SparkGraphComputer;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;

import static io.mindmaps.graql.api.query.QueryBuilder.var;
import static org.junit.Assert.assertNotNull;

/**
 *
 */

public class Degree {
    // a normal titan cluster
    private String titanClusterConfig = "src/main/resources/graphs/titan-cassandra-test-cluster.properties";
    // a hadoop graph with cassandra as input and gryo as output
    private String sparkClusterConfig = "src/main/resources/graphs/titan-cassandra-test-spark.properties";
    private MindmapsTransaction transaction;
    private MindmapsGraph mindmapsGraph;

    public void main() {
        // Disable horrid cassandra logs
        Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
        System.out.println("======" + System.currentTimeMillis() + "start initialise graph");
        initialiseGraph();
        System.out.println("======" + System.currentTimeMillis() + "stop initialise graph");
//        System.out.println("======" + System.currentTimeMillis() + "start load pokemon");
//        loadPokemon();
//        System.out.println("======" + System.currentTimeMillis() + "stop load pokemon");
//        System.out.println("======" + System.currentTimeMillis() + "start load SIMPLE");
//        loadSimple();
//        System.out.println("======" + System.currentTimeMillis() + "stop load SIMPLE");
        System.out.println("======" + System.currentTimeMillis() + "start load icij");
        loadICIJ();
        System.out.println("======" + System.currentTimeMillis() + "stop load icij");

//        computeDegree();

//        strippedDown();

        quickCount();

        cleanGraph();
    }

    private void initialiseGraph() {
        mindmapsGraph = MindmapsTitanGraphFactory.getInstance().newGraph(titanClusterConfig);
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

//        // test a entity
//        schemamigrator.migrateSchema("entity", createCSV("/opt/mindmaps/icij-data/Entities.csv"));
//        datamigrator.migrateData("entity", createCSV("/opt/mindmaps/icij-data/Entities.csv"));

//        // test an address
//        schemamigrator.migrateSchema("address", createCSV("/opt/mindmaps/icij-data/Addresses.csv"));
//        datamigrator.migrateData("address", createCSV("/opt/mindmaps/icij-data/Addresses.csv"));
//
//        // test an officer
//        schemamigrator.migrateSchema("officer", createCSV("/opt/mindmaps/icij-data/Officers.csv"));
//        datamigrator.migrateData("officer", createCSV("/opt/mindmaps/icij-data/Officers.csv"));
//
        // test an intermediary
        schemamigrator.migrateSchema("intermediary", createCSV("/opt/mindmaps/icij-data/Intermediaries.csv"));
        datamigrator.migrateData("intermediary", createCSV("/opt/mindmaps/icij-data/Intermediaries.csv"));

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
//        TitanGraph graph = TitanFactory.open(titanClusterConfig);
        Graph graph = GraphFactory.open(sparkClusterConfig);

        // test vertex program
        try {
            System.out.println("======" + System.currentTimeMillis()/1000 + "(secs) start compute degree");
//            ComputerResult result = graph.compute().program(new DegreeVertexProgram()).submit().get();
            ComputerResult result = graph.compute(SparkGraphComputer.class).program(new DegreeVertexProgram()).submit().get();
            System.out.println("======" + System.currentTimeMillis()/1000 + "(secs) end compute degree");
            System.out.println("======"+System.currentTimeMillis()/1000+"(secs) start print degree");
            result.graph().traversal().V().valueMap().forEachRemaining(System.out::println);
            System.out.println("======"+System.currentTimeMillis()/1000+"(secs) stop print degree");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void loadSimple() {
        TitanGraph titanGraph = TitanFactory.open(titanClusterConfig);

        //add a tunable size graph
        String edgeLabel = "blank";
        int n = 10;
        Vertex superNode = titanGraph.addVertex(T.label, String.valueOf(0));
        for (int i=1;i<n;i++) {
            Vertex currentNode = titanGraph.addVertex(T.label, String.valueOf(i));
            currentNode.addEdge(edgeLabel,superNode);
        }
        titanGraph.tx().commit();
    }

    private void strippedDown() {
        Long count;
        TitanGraph titanGraph = TitanFactory.open(titanClusterConfig);

        //count with titan
        count = titanGraph.traversal().V().count().next();
        System.out.println("The number of vertices in the graph according to gremlin is: "+count);

        // count the graph using titan graph computer
        count = titanGraph.traversal(GraphTraversalSource.computer(FulgoraGraphComputer.class)).V().count().next();
        System.out.println("The number of vertices in the graph according to fulgora is: "+count);

//        // page rank
//        try {
//            ComputerResult result = titanGraph.compute()
//                    .program(PageRankVertexProgram.build().create(titanGraph))
//                    .submit().get();
//            System.out.println("The number of vertices in the graph is: " + result.graph().traversal().V().count().next());
//            result.graph().traversal().V().forEachRemaining(v -> v.values().forEachRemaining(System.out::println));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

//        // degree
//        try {
//            ComputerResult result = titanGraph.compute()
//                    .program(new DegreeVertexProgram())
//                    .submit().get();
//            System.out.println("The number of vertices in the graph is: " + result.graph().traversal().V().count().next());
//            result.graph().traversal().V().forEachRemaining(v -> v.values().forEachRemaining(System.out::println));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

        Graph sparkGraph = GraphFactory.open(sparkClusterConfig);

//        // count
        count = sparkGraph.traversal(GraphTraversalSource.computer(SparkGraphComputer.class)).V().count().next();
        System.out.println("The number of vertices in the graph according to spark is: "+count);

//        // page rank
//        try {
//            ComputerResult result = sparkGraph.compute(SparkGraphComputer.class)
//                    .program(PageRankVertexProgram.build().create(sparkGraph))
//                    .submit().get();
//            System.out.println("The number of vertices in the graph is: " + result.graph().traversal().V().count().next());
//            result.graph().traversal().V().forEachRemaining(v -> v.values().forEachRemaining(System.out::println));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

//        // degree vertex
//        try {
//            ComputerResult result = sparkGraph.compute(SparkGraphComputer.class)
//                    .program(new DegreeVertexProgram())
//                    .submit().get();
//            System.out.println("The number of vertices in the graph is: " + result.graph().traversal().V().count().next());
//            result.graph().traversal().V().forEachRemaining(v -> v.values().forEachRemaining(System.out::println));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

    }

    public void cleanGraph() {
        TitanGraph titanGraph = TitanFactory.open(titanClusterConfig);
        titanGraph.close();
        TitanCleanup.clear(titanGraph);
    }

    public void quickCount() {
        Graph graph = GraphFactory.open(sparkClusterConfig);
        try {
            ComputerResult result = graph.compute(SparkGraphComputer.class).mapReduce(new CountMapReduce()).submit().get();
            System.out.println("The count via map reduce is: "+result.memory().get(CountMapReduce.DEFAULT_KEY));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
