package com.vmax.vmax_multi_source;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_ANY;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.json.JSONArray;
import org.json.JSONObject;

// JSON format mirrors vmax_core's JsonInterface / JsonNodeType:
//   node:   {"type": "UNBOUND"} | {"type": "URI", "uri": "..."} | {"type": "LITERAL", "data_type": "<xsd-uri>", "data": ...}
//   triple: {"subject": <node>, "predicate": <node>, "object": <node>}
//   list:   JSONArray of triple-JSONObjects, empty = []
public class TripleConverter {

    // key constants (matching vmax_core JsonInterface)
    static final String KEY_TYPE      = "type";
    static final String KEY_URI       = "uri";
    static final String KEY_DATA_TYPE = "data_type";
    static final String KEY_DATA      = "data";
    static final String TYPE_UNBOUND  = "UNBOUND";
    static final String TYPE_URI      = "URI";
    static final String TYPE_LITERAL  = "LITERAL";
    static final String[] NODE_KEYS   = {"subject", "predicate", "object"};

    // ----------------------------------------------------------------
    // Triple  ->  JSONObject  (for outgoing requests)
    // ----------------------------------------------------------------

    public static JSONObject tripleToJson(Triple triple) {
        JSONObject tripleAsJson = new JSONObject();
        tripleAsJson.put(NODE_KEYS[0], nodeToJson(triple.getSubject()));
        tripleAsJson.put(NODE_KEYS[1], nodeToJson(triple.getPredicate()));
        tripleAsJson.put(NODE_KEYS[2], nodeToJson(triple.getObject()));
        return tripleAsJson;
    }

    private static JSONObject nodeToJson(Node node) {
        JSONObject nodeAsJson = new JSONObject();

        if (node.getClass() == Node_ANY.class) {
            nodeAsJson.put(KEY_TYPE, TYPE_UNBOUND);
        } else if (node.isURI()) {
            nodeAsJson.put(KEY_TYPE, TYPE_URI);
            nodeAsJson.put(KEY_URI, node.getURI());
        } else if (node.isLiteral()) {
            if (node.getLiteralDatatype() == null) { return null; }
            nodeAsJson.put(KEY_TYPE, TYPE_LITERAL);
            nodeAsJson.put(KEY_DATA_TYPE, node.getLiteralDatatype().getURI());
            nodeAsJson.put(KEY_DATA, node.getLiteralValue());
        } else {
            return null;
        }

        return nodeAsJson;
    }

    // ----------------------------------------------------------------
    // JSONArray  ->  List<Triple>  (for incoming responses)
    // ----------------------------------------------------------------

    public static List<Triple> jsonToTripleList(JSONArray tripleListAsJsonArray) {
        List<Triple> returnList = new ArrayList<Triple>();
        if (tripleListAsJsonArray.isEmpty()) { return returnList; }
        for (int i = 0; i < tripleListAsJsonArray.length(); i++) {
            Object item = tripleListAsJsonArray.get(i);
            if (item instanceof JSONObject) {
                Triple triple = jsonToTriple((JSONObject) item);
                if (triple != null) { returnList.add(triple); }
            }
        }
        return returnList;
    }

    public static ExtendedIterator<Triple> jsonToTripleIterator(JSONArray tripleListAsJsonArray) {
        return WrappedIterator.create(jsonToTripleList(tripleListAsJsonArray).iterator());
    }

    private static Triple jsonToTriple(JSONObject tripleAsJson) {
        try {
            Node subject   = jsonToNode(tripleAsJson.getJSONObject(NODE_KEYS[0]));
            Node predicate = jsonToNode(tripleAsJson.getJSONObject(NODE_KEYS[1]));
            Node object    = jsonToNode(tripleAsJson.getJSONObject(NODE_KEYS[2]));
            if (subject == null || predicate == null || object == null) { return null; }
            return Triple.create(subject, predicate, object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Node jsonToNode(JSONObject nodeAsJson) {
        String type = nodeAsJson.optString(KEY_TYPE, null);
        if (type == null) { return null; }

        switch (type) {
            case TYPE_UNBOUND:
                return Node.ANY;
            case TYPE_URI:
                return NodeFactory.createURI(nodeAsJson.getString(KEY_URI));
            case TYPE_LITERAL: {
                String dataTypeUri = nodeAsJson.getString(KEY_DATA_TYPE);
                Object data        = nodeAsJson.get(KEY_DATA);
                if (dataTypeUri.equals(XSDDatatype.XSDstring.getURI())) {
                    return NodeFactory.createLiteralByValue(data.toString(), XSDDatatype.XSDstring);
                } else if (dataTypeUri.equals(XSDDatatype.XSDdouble.getURI())) {
                    return NodeFactory.createLiteralByValue(((Number) data).doubleValue(), XSDDatatype.XSDdouble);
                } else if (dataTypeUri.equals(XSDDatatype.XSDinteger.getURI())) {
                    return NodeFactory.createLiteralByValue(((Number) data).intValue(), XSDDatatype.XSDinteger);
                } else if (dataTypeUri.equals(XSDDatatype.XSDboolean.getURI())) {
                    return NodeFactory.createLiteralByValue(data, XSDDatatype.XSDboolean);
                } else {
                    return null;
                }
            }
            default:
                return null;
        }
    }
}
