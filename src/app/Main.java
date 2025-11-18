package app;

import algo.GraphAlgorithms;
import io.GraphLoader;
import model.Graph;
import model.Vertex;
import service.CollectionPlanner;
import service.SectorPlanner;

import java.io.IOException;
import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== Application de planification des tournées de collecte de déchets ===");

        // Par défaut, on démarre avec un petit territoire de démo
        Graph territoire = buildDemoTerritoire();

        while (true) {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("Territoire courant : " + (territoire.isDirected() ? "orienté" : "non orienté"));
            System.out.println();
            System.out.println("1. Visualiser le territoire (graphe)");
            System.out.println("2. ENTREPRISE : Tournée d'encombrants (centre -> foyer -> centre)");
            System.out.println("3. ENTREPRISE : Tournée de collecte des points de collecte (colonnes, bacs...)");
            System.out.println("4. ENTREPRISE : Tournée de collecte \"toutes les rues\" (cycle eulérien, si possible)");
            System.out.println("5. COLLECTIVITE : Planifier les jours de collecte par secteur (coloration)");
            System.out.println("6. COLLECTIVITE : Charger un territoire depuis un fichier texte");
            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    afficherTerritoire(territoire);
                    break;
                case "2":
                    runTourneeEncombrants(territoire);
                    break;
                case "3":
                    runTourneePointsCollecte(territoire);
                    break;
                case "4":
                    runTourneeToutesRues(territoire);
                    break;
                case "5":
                    runPlanificationJours(territoire);
                    break;
                case "6":
                    territoire = chargerTerritoireDepuisFichier();
                    break;
                case "0":
                    System.out.println("Fin de l'application. Merci.");
                    return;
                default:
                    System.out.println("Choix invalide, veuillez recommencer.");
            }
        }
    }

    /**
     * Territoire de démo : quelques rues entre 4 points.
     * Tu peux utiliser un fichier externe à la place (option 6).
     */
    private static Graph buildDemoTerritoire() {
        Graph g = new Graph(false); // non orienté, comme un réseau de rues
        // Sommets = intersections / points importants
        // Arêtes = tronçons de rue, poids = distance / temps
        g.addEdge("DEPOT", "A", 2.0);
        g.addEdge("A", "B", 3.0);
        g.addEdge("B", "C", 4.0);
        g.addEdge("DEPOT", "C", 5.0);
        g.addEdge("A", "C", 1.5);
        return g;
    }

    /* ====================== OPTION 1 : visualiser ====================== */

    private static void afficherTerritoire(Graph g) {
        System.out.println("\n--- Territoire de collecte (graphe) ---");
        System.out.println(g);
        System.out.println("Nombre de sommets (points / secteurs / intersections) : " + g.getVertices().size());
        System.out.println("Nombre d'arêtes (tronçons de rue / connexions) : " + g.getEdges().size());
    }

    /* ====================== OPTION 2 : tournée d'encombrants ====================== */

    private static void runTourneeEncombrants(Graph g) {
        System.out.println("\n--- Tournée d'encombrants ---");
        List<Vertex> sommets = new ArrayList<>(g.getVertices());
        if (sommets.isEmpty()) {
            System.out.println("Le territoire est vide.");
            return;
        }

        System.out.println("Points disponibles sur le territoire :");
        for (int i = 0; i < sommets.size(); i++) {
            System.out.println(i + " : " + sommets.get(i));
        }

        System.out.print("Index du centre de traitement (dépôt du camion) : ");
        int ci = lireIndex(sommets.size());
        System.out.print("Index du foyer à desservir (encombrants) : ");
        int pi = lireIndex(sommets.size());

        Vertex centre = sommets.get(ci);
        Vertex foyer = sommets.get(pi);

        List<Vertex> tour = CollectionPlanner.itineraireEncombrants(g, centre, foyer);
        double dist = CollectionPlanner.longueurTour(g, tour);

        System.out.println("\nItinéraire proposé pour la tournée d'encombrants :");
        System.out.println("  " + tour);
        System.out.println("Distance totale approximative : " + dist);
        System.out.println("(Modélise : plus courts chemins centre -> foyer -> centre)");
    }

    /* ====================== OPTION 3 : tournée des points de collecte ====================== */

    private static void runTourneePointsCollecte(Graph g) {
        System.out.println("\n--- Tournée de collecte des points de collecte (colonnes, bacs...) ---");
        List<Vertex> sommets = new ArrayList<>(g.getVertices());
        if (sommets.isEmpty()) {
            System.out.println("Le territoire est vide.");
            return;
        }

        System.out.println("Points disponibles (intersections, secteurs, points de collecte) :");
        for (int i = 0; i < sommets.size(); i++) {
            System.out.println(i + " : " + sommets.get(i));
        }

        System.out.print("Index du dépôt / centre de traitement : ");
        int ci = lireIndex(sommets.size());
        Vertex depot = sommets.get(ci);

        System.out.println("Entrez les index des points de collecte à visiter (séparés par des espaces) : ");
        String line = scanner.nextLine().trim();
        String[] parts = line.split("\\s+");
        List<Vertex> points = new ArrayList<>();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            int idx = Integer.parseInt(p);
            if (idx < 0 || idx >= sommets.size()) {
                System.out.println("Index ignoré (hors limites) : " + idx);
                continue;
            }
            points.add(sommets.get(idx));
        }

        if (points.isEmpty()) {
            System.out.println("Aucun point de collecte sélectionné.");
            return;
        }

        List<Vertex> tour = CollectionPlanner.tourEncombrantsMulti(g, depot, points);
        double dist = CollectionPlanner.longueurTour(g, tour);

        System.out.println("\nTournée de collecte proposée (heuristique du plus proche voisin) :");
        System.out.println("  " + tour);
        System.out.println("Distance totale approximative : " + dist);
        System.out.println("(Modélise : tournée d'un camion qui visite tous les points de collecte puis revient au dépôt)");
    }

    /* ====================== OPTION 4 : tournée \"toutes les rues\" (eulérien) ====================== */

    private static void runTourneeToutesRues(Graph g) {
        System.out.println("\n--- Tournée de collecte \"toutes les rues\" ---");
        System.out.println("On cherche une tournée qui passe au moins une fois sur chaque tronçon de rue.");
        System.out.println("Mathématiquement : cycle eulérien (si tous les degrés sont pairs).");

        try {
            List<Vertex> cycle = GraphAlgorithms.findEulerianCycle(g);
            System.out.println("\nLe territoire est eulérien : une tournée idéale existe.");
            System.out.println("Tournée (cycle eulérien) :");
            System.out.println("  " + cycle);
            System.out.println("(Le camion passe sur chaque tronçon exactement une fois, en revenant au point de départ.)");
        } catch (IllegalArgumentException e) {
            System.out.println("Le territoire n'est pas eulérien : tous les tronçons ne peuvent pas être parcourus une seule fois.");
            System.out.println("Dans le rapport, vous pourrez expliquer les cas 2 sommets impairs et cas général (problème du postier chinois).");
        }
    }

    /* ====================== OPTION 5 : planification des jours (coloration) ====================== */

    private static void runPlanificationJours(Graph g) {
        System.out.println("\n--- Planification des jours de collecte par secteur ---");
        if (g.isDirected()) {
            System.out.println("ATTENTION : la planification attend un graphe non orienté (secteurs voisins).");
            return;
        }

        Map<Vertex, Integer> jours = SectorPlanner.planificationJoursParColoration(g);

        System.out.println("Proposition de planification (coloration gloutonne) :");
        for (Map.Entry<Vertex, Integer> e : jours.entrySet()) {
            Vertex secteur = e.getKey();
            int jour = e.getValue();
            System.out.println("  " + secteur + " -> jour " + jour);
        }

        System.out.println("\nInterprétation :");
        System.out.println("- Chaque sommet = un secteur de collecte.");
        System.out.println("- Une arête entre deux secteurs signifie qu'ils sont voisins.");
        System.out.println("- Deux secteurs voisins n'ont pas le même jour (pas de conflit de ressources).");
    }

    /* ====================== OPTION 6 : charger territoire ====================== */

    private static Graph chargerTerritoireDepuisFichier() {
        System.out.println("\n--- Chargement d'un territoire depuis un fichier texte ---");
        System.out.println("Format attendu :");
        System.out.println("  1ère ligne : UNDIRECTED ou DIRECTED");
        System.out.println("  2ème ligne : nbSommets nbAretes");
        System.out.println("  puis nbSommets lignes : id label");
        System.out.println("  puis nbAretes lignes : idFrom idTo poids");
        System.out.print("Chemin du fichier (ex : resources/demo_small.txt) : ");

        String path = scanner.nextLine().trim();
        try {
            Graph g = GraphLoader.loadFromFile(path);
            System.out.println("Territoire chargé avec succès.");
            System.out.println("Nombre de sommets : " + g.getVertices().size());
            System.out.println("Nombre d'arêtes : " + g.getEdges().size());
            return g;
        } catch (IOException e) {
            System.out.println("Erreur de chargement : " + e.getMessage());
            System.out.println("On conserve le territoire précédent.");
            return buildDemoTerritoire();
        }
    }

    /* ==================aaaa==== utilitaire ====================== */

    private static int lireIndex(int max) {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                int idx = Integer.parseInt(line);
                if (idx < 0 || idx >= max) {
                    System.out.print("Index invalide, recommencez : ");
                    continue;
                }
                return idx;
            } catch (NumberFormatException e) {
                System.out.print("Entrée non valide, recommencez : ");
            }
        }
    }
}
