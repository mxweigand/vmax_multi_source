package com.vmax.vmax_multi_source;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.graph.GraphFactory;
import org.json.JSONArray;

public class VmaxPlugin {

    private String identifier; 
    private Integer port;
    private RestClient restClient;
    private Graph tboxGraph;
    private Graph aboxGraph;
    private Graph unionGraph;

    /**
     * constructor
     * @param identifier
     * @param port
     */
    public VmaxPlugin(String identifier, Integer port) {
        // set vars 
        this.port = port;
        this.identifier = identifier; 
        this.restClient = new RestClient(port);
        // generate tbox and abox graphs
        generateTboxGraph();
        this.aboxGraph = new VirtualAboxGraph(restClient);
        // create union of both graphs
        this.unionGraph = new Union(tboxGraph, aboxGraph);
    }

    /**
     * generate tbox as materialized Graph
     */
    private void generateTboxGraph() {

        // create an empty graph
        tboxGraph = GraphFactory.createDefaultGraph();
        
        // get list of triples from rest client and convert to triple list
        // if /tbox is not available yet, log and continue with empty tbox
        try {
            JSONArray tboxTriplesJson = restClient.getTboxTriples();
            List<Triple> tboxTripleList = TripleConverter.jsonToTripleList(tboxTriplesJson);
            for (Triple tboxTriple: tboxTripleList) {
                tboxGraph.add(tboxTriple);
            }
        } catch (Exception e) {
            System.out.println("[VmaxPlugin] TBox not available for '" + identifier + "' (port " + port + ") - continuing with empty TBox. Reason: " + e.getMessage());
        }

    }

    /**
     * getter for union Graph (tbox graph + virtual abox graph)
     * @return
     */
    public Graph getUnionGraph() {
        return unionGraph;
    }

    /**
     * getter for port
     * @return
     */
    public Integer getPort() {
        return port;
    }

    /**
     * getter for identifier
     * @return
     */
    public String getIdentifier() {
        return identifier;
    }

}
