package io;

import model.Graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GraphLoader {

    public static Graph loadFromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;

            // lire type
            do {
                line = br.readLine();
                if (line == null) throw new IOException("Fichier invalide");
                line = line.trim();
            } while (line.isEmpty() || line.startsWith("#"));

            boolean directed = line.equalsIgnoreCase("DIRECTED");
            Graph g = new Graph(directed);

            // lire n, m
            do {
                line = br.readLine();
                if (line == null) throw new IOException("Fichier invalide");
                line = line.trim();
            } while (line.isEmpty() || line.startsWith("#"));
            String[] parts = line.split("\\s+");
            int n = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);

            // lire sommets
            for (int i = 0; i < n; i++) {
                line = br.readLine();
                if (line == null) throw new IOException("Fichier invalide (sommets)");
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    i--;
                    continue;
                }
                String[] p = line.split("\\s+", 2);
                String id = p[0];
                String label = (p.length > 1 ? p[1] : id);
                g.addVertex(id, label);
            }

            // lire arêtes/arcs
            for (int i = 0; i < m; i++) {
                line = br.readLine();
                if (line == null) throw new IOException("Fichier invalide (arêtes)");
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    i--;
                    continue;
                }
                String[] p = line.split("\\s+");
                if (p.length < 3) throw new IOException("Ligne arête invalide : " + line);
                String from = p[0];
                String to = p[1];
                double w = Double.parseDouble(p[2]);
                g.addEdge(from, to, w);
            }

            return g;
        }
    }
}
