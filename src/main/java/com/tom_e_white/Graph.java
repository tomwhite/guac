package com.tom_e_white;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Graph {

  public static void main(String[] args) {
    Collection<List<Character>> basePermutations =
        Collections2.permutations(ImmutableList.of('A', 'C', 'G', 'T'));

    int minComponents = Integer.MAX_VALUE;
    for (List<Character> bases1 : basePermutations) {
      for (List<Character> bases2 : basePermutations) {
        for (List<Character> bases3 : basePermutations) {
          int numComponents = computeNumberOfComponents(bases1, bases2, bases3);
          minComponents = Math.min(minComponents, numComponents);
        }
      }
    }
    System.out.println(minComponents);
  }

  private static int computeNumberOfComponents(List<Character> bases1,
      List<Character> bases2, List<Character> bases3) {

    // map from single letter amino acid code to codons
    Multimap<Character, String> gc = theGeneticCode();
    //System.out.println(gc);

    // turn codons into points
    Multimap<Character, Point> aminoToPoints =
        Multimaps.transformValues(gc, codon -> codonToPoint(codon, bases1, bases2, bases3));
    //System.out.println(aminoToPoints);

    // determine if points within each pair are connected, or not, based on distance;
    // and build a graph
    Map<Character, UndirectedGraph<Point, DefaultEdge>> aminoToGraph =
        Maps.transformValues(aminoToPoints.asMap(), Graph::makeGraph);
    //System.out.println(aminoToGraph);

    // find number of connected components for each amino acid
    Map<Character, Integer> aminoToNumComponents =
        Maps.transformValues(aminoToGraph, Graph::numComponents);
    //System.out.println(aminoToNumComponents);

    // find total number of connected components
    int totalComponents = aminoToNumComponents.values().stream().mapToInt(i -> i).sum();
    //System.out.println("Total components: " + totalComponents);
    if (totalComponents == 22) {
      if (bases1.equals(bases2) && bases1.equals(bases3)) {
        System.out.println(bases1 + ", " + bases2 + ", " + bases3);
        System.out.println(aminoToNumComponents);
        System.out.println(aminoToPoints.get('S'));
      }
    }
    return totalComponents;
  }

  private static Multimap<Character, String> theGeneticCode() {
    Multimap<Character, String> mm = LinkedListMultimap.create();
    mm.putAll('A', ImmutableList.of("GCA", "GCC", "GCG", "GCT"));
    mm.putAll('C', ImmutableList.of("TGC", "TGT"));
    mm.putAll('D', ImmutableList.of("GAC", "GAT"));
    mm.putAll('E', ImmutableList.of("GAA", "GAG"));
    mm.putAll('F', ImmutableList.of("TTC", "TTT"));
    mm.putAll('G', ImmutableList.of("GGA", "GGC", "GGG", "GGT"));
    mm.putAll('H', ImmutableList.of("CAC", "CAT"));
    mm.putAll('I', ImmutableList.of("ATA", "ATC", "ATT"));
    mm.putAll('K', ImmutableList.of("AAA", "AAG"));
    mm.putAll('L', ImmutableList.of("CTA", "CTC", "CTG", "CTT", "TTA", "TTG"));
    mm.putAll('M', ImmutableList.of("ATG"));
    mm.putAll('N', ImmutableList.of("AAC", "AAT"));
    mm.putAll('P', ImmutableList.of("CCA", "CCC", "CCG", "CCT"));
    mm.putAll('Q', ImmutableList.of("CAA", "CAG"));
    mm.putAll('R', ImmutableList.of("AGA", "AGG", "CGA", "CGC", "CGG", "CGT"));
    mm.putAll('S', ImmutableList.of("AGC", "AGT", "TCA", "TCC", "TCG", "TCT"));
    mm.putAll('T', ImmutableList.of("ACA", "ACC", "ACG", "ACT"));
    mm.putAll('V', ImmutableList.of("GTA", "GTC", "GTG", "GTT"));
    mm.putAll('W', ImmutableList.of("TGG"));
    mm.putAll('Y', ImmutableList.of("TAC", "TAT"));
    mm.putAll('*', ImmutableList.of("TAA", "TAG", "TGA"));
    return mm;
  }

  private static Point codonToPoint(String codon, List<Character> bases1,
      List<Character> bases2, List<Character> bases3) {
    return new Point(lookup(codon.charAt(0), bases1),
        lookup(codon.charAt(1), bases2),
        lookup(codon.charAt(2), bases3));
  }

  private static int lookup(char code, List<Character> perm) {
    return perm.indexOf(code);
  }

  private static <T> Iterable<Set<T>> pairs(Iterable<T> collection) {
    Set<Set<T>> p = new LinkedHashSet<>();
    for (T e1 : collection) {
      for (T e2 : collection) {
        if (e1.equals(e2)) {
          continue;
        }
        Set<T> pair = ImmutableSet.of(e1, e2);
        p.add(pair);
      }
    }
    return p;
  }

  private static UndirectedGraph<Point, DefaultEdge> makeGraph(Iterable<Point> points) {
    UndirectedGraph<Point, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
    for (Point point : points) {
      g.addVertex(point);
    }
    for (Set<Point> pair : pairs(points)) {
      Point p1 = Iterables.get(pair, 0);
      Point p2 = Iterables.get(pair, 1);
      if (p1.distance(p2) <= 1) {
        g.addEdge(p1, p2);
      }
    }
    return g;
  }

  private static Integer numComponents(UndirectedGraph<Point, DefaultEdge> graph) {
    return new ConnectivityInspector<>(graph).connectedSets().size();
  }

  static class Point {
    int x, y, z;

    public Point(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Point point = (Point) o;

      if (x != point.x) return false;
      if (y != point.y) return false;
      return z == point.z;

    }

    @Override
    public int hashCode() {
      int result = x;
      result = 31 * result + y;
      result = 31 * result + z;
      return result;
    }

    @Override
    public String toString() {
      return "(" + x + ", " + y + ", " + z + ")";
    }

    public int distance(Point other) {
      return manhattan(this, other);
    }

    static int maximumNorm(Point p1, Point p2) {
      return Math.max(Math.max(Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y)),
          Math.abs(p1.z - p2.z));
    }

    static int manhattan(Point p1, Point p2) {
      return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) + Math.abs(p1.z - p2.z);
    }
  }
}
