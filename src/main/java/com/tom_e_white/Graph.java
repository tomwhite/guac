package com.tom_e_white;

import java.util.List;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Graph {

  public static void main(String[] args) {
    UndirectedGraph<String, DefaultEdge> g =
        new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
    String v1 = "v1";
    String v2 = "v2";
    String v3 = "v3";
    String v4 = "v4";

    g.addVertex(v1);
    g.addVertex(v2);
    g.addVertex(v3);
    g.addVertex(v4);

    g.addEdge(v1, v2);
    g.addEdge(v2, v3);
    g.addEdge(v3, v1);

    System.out.println(g);

    ConnectivityInspector connectivityInspector = new ConnectivityInspector(g);
    List connectedSets = connectivityInspector.connectedSets();

    System.out.println(connectedSets);

  }
}
