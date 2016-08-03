import io.mindmaps.core.dao.MindmapsGraph;
import io.mindmaps.core.model.Instance;
import io.mindmaps.core.model.Type;
import org.apache.tinkerpop.gremlin.process.computer.KeyValue;
import org.apache.tinkerpop.gremlin.process.computer.MapReduce;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;

import java.util.Iterator;
import java.util.Set;

/**
 * extra methods on core - everything done via configs
 */
public class NoCoreAPI {
    static TraversalSource getTraversalSource() {return null;}
    static MindmapsGraph getSubGraph(Set<Type> set) {return null;}
}

/**
 * new computer class
 */
class Computer {

    /**
     * constructor creates the computer from the mindmaps graph, and an optional set of types to specify a subgraph
     */
    public Computer (MindmapsGraph graph, Set<Type> types) {
        NoCoreAPI.getTraversalSource();
    }

    /**
     * execute a program and process results using result program
     */
    public MindmapsResult execute(ConceptProgram program, ResultProgram resultProgram) {
        return null;
    }

}

/**
 * wrapper for a tinkerpop vertex program
 */
class ConceptProgram {
    public void execute(Instance instance, MindmapsMessenger mindmapsMessenger, MindmapsMemory mindmapsMemory) {}
}

/**
 * The result of the computation
 */
class MindmapsResult {
    /**
     * get the result and its type is set using generics
     */
    public void get() {};
}

/**
 * a map reduce program to return either a map of results or a single aggregated results
 */
class ResultProgram {
    /**
     * the map stage - emits key value pairs
     */
    public void map(Instance instance, MapEmitter mapEmitter) {}

    /**
     * the reduce stage - emits reduced key value pairs to be emitted
     */
    public void reduce(String mapKey, Iterator mapValues, ReduceEmitter reduceEmitter) {}

    /**
     * same signature as reduce but performed on a worker before starting the reduce phase
     */
    public void combine(String mapKey, Iterator mapValues, ReduceEmitter reduceEmitter) {}

    /**
     * process the result of the reduce phase into an object to be returned
     */
    public void generateFinalResult(Iterator<KeyValue<String, String>> keyValues) {}

    /**
     * add the object to global memory if necessary
     */
    public void addResultToMemory(MindmapsMemory memory, Iterator<KeyValue<String, String>> keyValues) {}
}

/**
 * The incoming / outgoing messages - don't need to change anything
 */
class MindmapsMessenger {}

/**
 * Setters and getters for global memory - don't need to change anything
 */
class MindmapsMemory {}

/**
 * The set of key value pairs to map across the cluster - don't need to change anything
 */
class MapEmitter {}

/**
 * The set of key value pairs that result from the reduce - don't need to change anything
 */
class ReduceEmitter {}