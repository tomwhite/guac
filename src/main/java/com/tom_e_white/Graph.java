package com.tom_e_white;

import com.google.common.base.Function;
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
    Collection<List<Character>> permutations = Collections2.permutations(ImmutableList.of('A', 'C', 'G', 'T'));
    System.out.println("Num perms: " + Iterables.size(permutations));
    final List<Character> perm = Iterables.get(permutations, 7); // TODO: look through all of them!

    // map from single letter amino acid code to codons
    Multimap<Character, String> gc = theGeneticCode();
    System.out.println(gc);

    // turn codons into points
    Multimap<Character, Point> aminoToPoints = Multimaps.transformValues(gc, new
        Function<String, Point>() {
      public Point apply(String code) {
        return new Point(lookup(code.charAt(0), perm), lookup(code.charAt(1), perm), lookup
            (code.charAt(2), perm));
      }
    });
    System.out.println(aminoToPoints);

    // turn points for each amino acid into pairs
    Map<Character, Iterable<Set<Point>>> aminoToPairs = Maps.transformValues(aminoToPoints.asMap(), new Function<Collection<Point>, Iterable<Set<Point>>>() {
      public Iterable<Set<Point>> apply(Collection<Point> points) {
        return pairs(points);
      }
    });
    System.out.println(aminoToPairs);

    // determine if points within each pair are connected, or not, based on distance;
    // and build a graph
    Map<Character, UndirectedGraph<Point, DefaultEdge>> aminoToGraph =
        Maps.transformValues(aminoToPairs, new Function<Iterable<Set<Point>>,
            UndirectedGraph<Point, DefaultEdge>>() {
      public UndirectedGraph<Point, DefaultEdge> apply(Iterable<Set<Point>> sets) {
        UndirectedGraph<Point, DefaultEdge> g =
            new SimpleGraph<Point, DefaultEdge>(DefaultEdge.class);
        for (Set<Point> pair : sets) {
          Point p1 = Iterables.get(pair, 0);
          Point p2 = Iterables.get(pair, 1);
          g.addVertex(p1);
          g.addVertex(p2);
          if (p1.distance(p2) <= 1) {
            g.addEdge(p1, p2);
          }
        }
        return g;
      }
    });
    System.out.println(aminoToGraph);

    // find number of connected components
    Map<Character, Integer> aminoToNumComponents = Maps.transformValues(aminoToGraph,
        new Function<UndirectedGraph<Point, DefaultEdge>, Integer>() {
      public Integer apply(UndirectedGraph<Point, DefaultEdge> g) {
        return new ConnectivityInspector<Point, DefaultEdge>(g).connectedSets().size();
      }
    });
    System.out.println(aminoToNumComponents);

  }

  private static int lookup(char code, List<Character> perm) {
    return perm.indexOf(code);
  }

  private static Multimap<Character, String> theGeneticCode() {
    Multimap<Character, String> mm = LinkedListMultimap.create();
    mm.putAll('A', ImmutableList.of("GCA", "GCC", "GCG", "GCT"));
    mm.putAll('B', ImmutableList.of("AAC", "AAT", "GAC", "GAT"));
    mm.putAll('S', ImmutableList.of("AGC", "AGT", "TCA", "TCC", "TCG", "TCT"));
    return mm;
  }

  private static <T> Iterable<Set<T>> pairs(Iterable<T> collection) {
    Set<Set<T>> p = new LinkedHashSet<Set<T>>();
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
      // this is *maximum norm*
      return Math.max(Math.max(Math.abs(x - other.x), Math.abs(y - other.y)), Math.abs
          (z - other.z));
    }
  }
}
