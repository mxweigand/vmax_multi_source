package com.vmax.vmax_multi_source;

import org.apache.jena.fuseki.system.FusekiLogging;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.sparql.core.DatasetGraphFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class VmaxMultiSource {

    private Graph graph;
    private Integer mainPort;
    private List<VmaxPlugin> vmaxPlugins = new ArrayList<VmaxPlugin>();

    public VmaxMultiSource() {

        // read config file
        try { readConfigFile(); } 
        catch (Exception e) { e.printStackTrace(); return; }

    }

    public void run() {

        try { generateGraph(); }
        catch (Exception e) { e.printStackTrace(); return; }

        try { startServer(); }
        catch (Exception e) { e.printStackTrace(); return; }

    }

    private void readConfigFile() throws Exception {

        // read file
        JSONObject configJson = null;
        try {
            File configFile = new File("config.json");
            byte[] bytes = Files.readAllBytes(configFile.toPath());
            String configString = new String (bytes);
            configJson = new JSONObject(configString);
        } catch (Exception e) {
            throw new Exception("error trying to access config.json", e);
        }

        // assign to variables
        try {
            mainPort = configJson.getInt("main-port");
            JSONArray plugins = configJson.getJSONArray("plugins");
            for (int i = 0; i < plugins.length(); i++) {
                JSONObject plugin = plugins.getJSONObject(i);
                String pluginName = plugin.getString("plugin-name");
                Integer pluginPort = plugin.getInt("plugin-port");
                if (pluginName != null && pluginPort != null) {
                    vmaxPlugins.add(new VmaxPlugin(pluginName, pluginPort));
                }
            }
        } catch (Exception e) {
            throw new Exception("error trying to read fields of config.json", e);
        }

        // print
        System.out.println("+++ config +++\n" + configJson.toString(2) + "\n+++ config +++\n");

    }

    private void generateGraph() {
        List<Graph> graphList = new ArrayList<Graph>();
        for (VmaxPlugin vmaxPlugin : vmaxPlugins) {
            graphList.add(vmaxPlugin.getUnionGraph());
        }
        graph = new MultiUnion(graphList.iterator());
    }

    private void startServer() throws Exception {
	
        try {
            // set logging
            FusekiLogging.setLogging();
            // create and build server     
			FusekiServer server = FusekiServer
					.create()
					.port(mainPort)
					.loopback(true)
					.verbose(false)
					.enablePing(true)
					.add("/data", DatasetGraphFactory.wrap(graph))
					.build();
            // start server
			server.start();	
		} catch (Exception e) {
			throw new Exception("error trying to start the server", e);	
		}
		
	}

}