import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.dao.MindmapsTransaction;
import io.mindmaps.core.exceptions.MindmapsValidationException;
import io.mindmaps.core.implementation.Data;
import io.mindmaps.core.model.ResourceType;
import io.mindmaps.core.model.RoleType;
import io.mindmaps.factory.MindmapsTitanGraphFactory;
import io.mindmaps.graql.api.parser.QueryParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 */

public class Degree {
    private final String graphConfig = "/opt/mindmaps/resources/conf/titan-cassandra-unit-test.properties";
    private MindmapsTransaction transaction;
    private MindmapsGraph mindmapsGraph;

    public void main() {
        initialiseGraph();
        loadPokemon();
    }

    public void initialiseGraph() {
        mindmapsGraph = MindmapsTitanGraphFactory.getInstance().newGraph(graphConfig);
        transaction = mindmapsGraph.newTransaction();
    }

    public void loadPokemon() {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get("/src/main/resources/graql/pokemon.gql"), StandardCharsets.UTF_8);
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
}
