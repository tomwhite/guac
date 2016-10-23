package com.tom_e_white;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;

public class Perm {
  public static void main(String[] args) {
    Collection<List<String>> permutations = Collections2.permutations(ImmutableList.of
        ("1", "2", "3", "4"));
    System.out.println(Iterables.toString(permutations));
  }
}
