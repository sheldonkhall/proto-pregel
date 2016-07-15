import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.*;
import org.apache.tinkerpop.gremlin.process.computer.ranking.pagerank.PageRankMessageCombiner;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 *
 */

public class DegreeVertexProgram implements VertexProgram<Double>{

    public static final String DEGREE = "analytics.degreeVertexProgram.degree";

    private MessageScope.Local<Double> countMessageScope = MessageScope.Local.of(__::bothE);

    private static final Set<String> COMPUTE_KEYS = new HashSet<>(Arrays.asList(DEGREE));

    public DegreeVertexProgram() {

    }

    @Override
    public void loadState(final Graph graph, final Configuration configuration) {

    }

    @Override
    public void storeState(final Configuration configuration) {

    }

    @Override
    public GraphComputer.ResultGraph getPreferredResultGraph() {
        return GraphComputer.ResultGraph.NEW;
    }

    @Override
    public GraphComputer.Persist getPreferredPersist() {
        return GraphComputer.Persist.VERTEX_PROPERTIES;
    }

    @Override
    public Set<String> getElementComputeKeys() {
        return COMPUTE_KEYS;
    }

    @Override
    public Optional<MessageCombiner<Double>> getMessageCombiner() {
        return (Optional) PageRankMessageCombiner.instance();
    }

    @Override
    public Set<MessageScope> getMessageScopes(final Memory memory) {
        final Set<MessageScope> set = new HashSet<>();
        set.add(this.countMessageScope);
        return set;
    }

    @Override
    public DegreeVertexProgram clone() {
        try {
            final DegreeVertexProgram clone = (DegreeVertexProgram) super.clone();
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setup(final Memory memory) {

    }

    @Override
    public void execute(final Vertex vertex, Messenger<Double> messenger, final Memory memory) {
        if (memory.isInitialIteration()) {
            messenger.sendMessage(this.countMessageScope, 1.0d);
        } else {
            double edgeCount = IteratorUtils.reduce(messenger.receiveMessages(), 0.0d, (a, b) -> a + b);
            vertex.property(DEGREE, edgeCount);
        }
    }

    @Override
    public boolean terminate(final Memory memory) {
        return !memory.isInitialIteration();
    }

    @Override
    public String toString() {
        return StringFactory.vertexProgramString(this);
    }

}
