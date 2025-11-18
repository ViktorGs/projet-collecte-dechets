package service;

import algo.GraphAlgorithms;
import model.Graph;
import model.Vertex;

import java.util.ArrayList;
import java.util.List;

public class CollectionPlanner {

    public static List<Vertex> itineraireEncombrants(Graph g, Vertex centre, Vertex particulier) {
        GraphAlgorithms.PathResult fromCentre = GraphAlgorithms.dijkstra(g, centre);
        List<Vertex> aller = fromCentre.buildPathTo(particulier);

        GraphAlgorithms.PathResult fromParticulier = GraphAlgorithms.dijkstra(g, particulier);
        List<Vertex> retour = fromParticulier.buildPathTo(centre);

        List<Vertex> tour = new ArrayList<>(aller);
        if (!retour.isEmpty()) {
            for (int i = 1; i < retour.size(); i++) {
                tour.add(retour.get(i));
            }
        }
        return tour;
    }

    public static List<Vertex> tourEncombrantsMulti(Graph g, Vertex centre, List<Vertex> particuliers) {
        List<Vertex> toVisit = new ArrayList<>(particuliers);
        if (!toVisit.contains(centre)) {
            toVisit.add(centre);
        }
        return GraphAlgorithms.tspNearestNeighbor(g, centre, toVisit);
    }

    public static List<Vertex> cycleEulerienOuNull(Graph g) {
        try {
            return GraphAlgorithms.findEulerianCycle(g);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static double longueurTour(Graph g, List<Vertex> tour) {
        if (tour == null || tour.size() < 2) return 0.0;
        double total = 0.0;
        for (int i = 0; i < tour.size() - 1; i++) {
            Vertex a = tour.get(i);
            Vertex b = tour.get(i + 1);
            GraphAlgorithms.PathResult pr = GraphAlgorithms.dijkstra(g, a);
            Double d = pr.dist.get(b);
            if (d == null || d == Double.POSITIVE_INFINITY) {
                return Double.POSITIVE_INFINITY;
            }
            total += d;
        }
        return total;
    }
}
