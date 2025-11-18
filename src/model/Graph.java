package model;

import java.util.*;

public class Graph {
    private final boolean directed;
    private final Map<String, Vertex> vertices = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Vertex, List<Edge>> adjacency = new HashMap<>();

    public Graph(boolean directed) {
        this.directed = directed;
    }

    public boolean isDirected() {
        return directed;
    }

    public Collection<Vertex> getVertices() {
        return vertices.values();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Vertex addVertex(String id) {
        return addVertex(id, id);
    }

    public Vertex addVertex(String id, String label) {
        Vertex v = vertices.get(id);
        if (v == null) {
            v = new Vertex(id, label);
            vertices.put(id, v);
            adjacency.put(v, new ArrayList<>());
        }
        return v;
    }

    public Vertex getVertex(String id) {
        return vertices.get(id);
    }

    public Edge addEdge(String fromId, String toId, double weight) {
        Vertex from = addVertex(fromId);
        Vertex to = addVertex(toId);
        Edge e = new Edge(from, to, weight, directed);
        edges.add(e);
        adjacency.get(from).add(e);
        if (!directed) {
            // on ajoute aussi l'arête dans l'autre sens dans l'adjacence
            Edge e2 = new Edge(to, from, weight, directed);
            adjacency.get(to).add(e2);
        }
        return e;
    }

    public List<Edge> getOutgoingEdges(Vertex v) {
        return adjacency.getOrDefault(v, Collections.emptyList());
    }

    public int getDegree(Vertex v) {
        if (directed) {
            throw new UnsupportedOperationException("Use in/out degree for directed graph");
        }
        return adjacency.getOrDefault(v, Collections.emptyList()).size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Graph(")
                .append(directed ? "directed" : "undirected")
                .append(")\nVertices:\n");
        for (Vertex v : getVertices()) {
            sb.append("  ").append(v).append("\n");
        }
        sb.append("Edges:\n");
        for (Edge e : edges) {
            sb.append("  ").append(e).append("\n");
        }
        return sb.toString();
    }
}
