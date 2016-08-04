import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.process.computer.KeyValue;
import org.apache.tinkerpop.gremlin.process.computer.MapReduce;
import org.apache.tinkerpop.gremlin.process.computer.Memory;
import org.apache.tinkerpop.gremlin.process.computer.clustering.peerpressure.PeerPressureVertexProgram;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */

public class CountMapReduce implements MapReduce<Serializable, Long, Serializable, Long, Map<Serializable, Long>> {

    public static final String COUNT_MEMORY_KEY = "analytics.countMapReduce.memoryKey";
    public static final String DEFAULT_KEY = "count";

    private String memoryKey = DEFAULT_KEY;

    public CountMapReduce() {
    }

    public CountMapReduce(final String memoryKey) {
        this.memoryKey = memoryKey;
    }

    @Override
    public void storeState(final Configuration configuration) {
        configuration.setProperty(COUNT_MEMORY_KEY, this.memoryKey);
        configuration.setProperty(MAP_REDUCE, CountMapReduce.class.getName());
    }

    @Override
    public void loadState(final Graph graph, final Configuration configuration) {
        this.memoryKey = configuration.getString(COUNT_MEMORY_KEY, DEFAULT_KEY);
    }

    @Override
    public boolean doStage(final MapReduce.Stage stage) {
        return true;
    }

    @Override
    public void map(final Vertex vertex, final MapReduce.MapEmitter<Serializable, Long> emitter) {
        emitter.emit(this.memoryKey, 1l);
    }

    @Override
    public void combine(final Serializable key, final Iterator<Long> values, final MapReduce.ReduceEmitter<Serializable, Long> emitter) {
        this.reduce(key, values, emitter);
    }

    @Override
    public void reduce(final Serializable key, final Iterator<Long> values, final MapReduce.ReduceEmitter<Serializable, Long> emitter) {
        long count = 0l;
        while (values.hasNext()) {
            count = count + values.next();
        }
        emitter.emit(key, count);
    }

    @Override
    public Map<Serializable, Long> generateFinalResult(final Iterator<KeyValue<Serializable, Long>> keyValues) {
        final Map<Serializable, Long> count = new HashMap<>();
        keyValues.forEachRemaining(pair -> count.put(pair.getKey(), pair.getValue()));
        return count;
    }

    @Override
    public String getMemoryKey() {
        return this.memoryKey;
    }

    @Override
    public String toString() {
        return StringFactory.mapReduceString(this, this.memoryKey);
    }

    @Override
    public MapReduce<Serializable, Long, Serializable, Long, Map<Serializable, Long>> clone() {
        return null;
    }
}
