package algo;

import model.Edge;
import model.Graph;
import model.Vertex;

import java.util.*;

/**
 * Algorithmes sur les graphes :
 * - BFS
 * - Dijkstra
 * - MST (Prim)
 * - Cycle eulérien (Hierholzer)
 * - TSP heuristique (plus proche voisin)
 * - Coloration gloutonne
 */
public class GraphAlgorithms {

    /* ====================== BFS ====================== */

    public static Map<Vertex, Integer> bfsDistances(Graph g, Vertex source) {
        Map<Vertex, Integer> dist = new HashMap<>();
        Queue<Vertex> queue = new ArrayDeque<>();
        dist.put(source, 0);
        queue.add(source);

        while (!queue.isEmpty()) {
            Vertex u = queue.poll();
            int du = dist.get(u);
            for (Edge e : g.getOutgoingEdges(u)) {
                Vertex v = e.getTo();
                if (!dist.containsKey(v)) {
                    dist.put(v, du + 1);
                    queue.add(v);
                }
            }
        }
        return dist;
    }

    /* ====================== DIJKSTRA ====================== */

    public static class PathResult {
        public final Map<Vertex, Double> dist;
        public final Map<Vertex, Vertex> pred;

        public PathResult(Map<Vertex, Double> dist, Map<Vertex, Vertex> pred) {
            this.dist = dist;
            this.pred = pred;
        }

        public List<Vertex> buildPathTo(Vertex target) {
            List<Vertex> path = new ArrayList<>();
            if (!dist.containsKey(target) || dist.get(target) == Double.POSITIVE_INFINITY) {
                return path; // pas de chemin
            }
            Vertex current = target;
            while (current != null) {
                path.add(current);
                current = pred.get(current);
            }
            Collections.reverse(path);
            return path;
        }
    }

    public static PathResult dijkstra(Graph g, Vertex source) {
        Map<Vertex, Double> dist = new HashMap<>();
        Map<Vertex, Vertex> pred = new HashMap<>();
        for (Vertex v : g.getVertices()) {
            dist.put(v, Double.POSITIVE_INFINITY);
            pred.put(v, null);
        }
        dist.put(source, 0.0);

        PriorityQueue<Vertex> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(source);

        while (!pq.isEmpty()) {
            Vertex u = pq.poll();
            double du = dist.get(u);
            for (Edge e : g.getOutgoingEdges(u)) {
                Vertex v = e.getTo();
                double alt = du + e.getWeight();
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    pred.put(v, u);
                    pq.remove(v);
                    pq.add(v);
                }
            }
        }
        return new PathResult(dist, pred);
    }

    /* ====================== MST (PRIM) ====================== */

    public static Set<Edge> primMST(Graph g) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("MST seulement pour graphe non orienté");
        }
        Set<Edge> mst = new HashSet<>();
        Set<Vertex> inTree = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(Edge::getWeight));

        Iterator<Vertex> it = g.getVertices().iterator();
        if (!it.hasNext()) return mst;
        Vertex start = it.next();
        inTree.add(start);
        pq.addAll(g.getOutgoingEdges(start));

        while (!pq.isEmpty()) {
            Edge e = pq.poll();
            Vertex u = e.getFrom();
            Vertex v = e.getTo();
            if (inTree.contains(v) && inTree.contains(u)) continue;
            mst.add(e);
            Vertex newV = inTree.contains(u) ? v : u;
            inTree.add(newV);
            for (Edge e2 : g.getOutgoingEdges(newV)) {
                Vertex w = e2.getTo();
                if (!inTree.contains(w)) {
                    pq.add(e2);
                }
            }
        }
        return mst;
    }

    /* ====================== EULERIEN ====================== */

    public static boolean isEulerian(Graph g) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("Cette version est pour graphe non orienté");
        }
        for (Vertex v : g.getVertices()) {
            if (g.getDegree(v) % 2 != 0) {
                return false;
            }
        }
        return true; // on suppose connexe sur les sommets de deg > 0
    }

    public static List<Vertex> findEulerianCycle(Graph g) {
        if (!isEulerian(g)) {
            throw new IllegalArgumentException("Le graphe n'est pas eulérien (tous degrés pairs requis)");
        }
        Map<Vertex, Deque<Vertex>> localAdj = new HashMap<>();
        for (Vertex v : g.getVertices()) {
            localAdj.put(v, new ArrayDeque<>());
        }
        for (Edge e : g.getEdges()) {
            localAdj.get(e.getFrom()).add(e.getTo());
        }

        Vertex start = g.getVertices().iterator().next();
        Deque<Vertex> stack = new ArrayDeque<>();
        List<Vertex> circuit = new ArrayList<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            Vertex v = stack.peek();
            Deque<Vertex> neighbors = localAdj.get(v);
            if (neighbors != null && !neighbors.isEmpty()) {
                Vertex u = neighbors.pollFirst();
                stack.push(u);
            } else {
                circuit.add(stack.pop());
            }
        }
        Collections.reverse(circuit);
        return circuit;
    }

    /* ====================== TSP HEURISTIQUE ====================== */

    public static List<Vertex> tspNearestNeighbor(Graph g, Vertex start, List<Vertex> toVisit) {
        List<Vertex> remaining = new ArrayList<>(toVisit);
        List<Vertex> tour = new ArrayList<>();
        Vertex current = start;
        tour.add(current);
        remaining.remove(current);

        while (!remaining.isEmpty()) {
            Vertex best = null;
            double bestDist = Double.POSITIVE_INFINITY;
            PathResult prFromCurrent = dijkstra(g, current);
            for (Vertex v : remaining) {
                double d = prFromCurrent.dist.getOrDefault(v, Double.POSITIVE_INFINITY);
                if (d < bestDist) {
                    bestDist = d;
                    best = v;
                }
            }
            if (best == null) break;
            tour.add(best);
            remaining.remove(best);
            current = best;
        }
        if (!tour.get(tour.size() - 1).equals(start)) {
            tour.add(start);
        }
        return tour;
    }

    /* ====================== COLORATION GLOUTONNE ====================== */

    public static Map<Vertex, Integer> greedyColoring(Graph g) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("Coloration ici pour graphe non orienté");
        }
        Map<Vertex, Integer> color = new HashMap<>();
        for (Vertex v : g.getVertices()) {
            Set<Integer> forbidden = new HashSet<>();
            for (Edge e : g.getOutgoingEdges(v)) {
                Vertex neigh = e.getTo();
                if (color.containsKey(neigh)) {
                    forbidden.add(color.get(neigh));
                }
            }
            int c = 0;
            while (forbidden.contains(c)) c++;
            color.put(v, c);
        }
        return color;
    }
}
