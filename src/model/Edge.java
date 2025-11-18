package model;

public class Edge {
    private final Vertex from;
    private final Vertex to;
    private final double weight;
    private final boolean directed;

    public Edge(Vertex from, Vertex to, double weight, boolean directed) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.directed = directed;
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isDirected() {
        return directed;
    }

    @Override
    public String toString() {
        String arrow = directed ? "->" : "--";
        return from + " " + arrow + " " + to + " [" + weight + "]";
    }
}
