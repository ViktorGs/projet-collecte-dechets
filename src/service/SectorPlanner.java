package service;

import algo.GraphAlgorithms;
import model.Graph;
import model.Vertex;

import java.util.HashMap;
import java.util.Map;

public class SectorPlanner {

    public static Map<Vertex, Integer> planificationJoursParColoration(Graph g) {
        return GraphAlgorithms.greedyColoring(g);
    }

    public static boolean verifieCapaciteParJour(Map<Vertex, Integer> jours,
                                                 Map<Vertex, Double> secteursQuantites,
                                                 double capaciteParJour) {
        Map<Integer, Double> quantiteParJour = new HashMap<>();
        for (Map.Entry<Vertex, Integer> e : jours.entrySet()) {
            Vertex s = e.getKey();
            int jour = e.getValue();
            double q = secteursQuantites.getOrDefault(s, 0.0);
            quantiteParJour.put(jour, quantiteParJour.getOrDefault(jour, 0.0) + q);
        }
        for (Map.Entry<Integer, Double> e : quantiteParJour.entrySet()) {
            if (e.getValue() > capaciteParJour) {
                return false;
            }
        }
        return true;
    }
}
