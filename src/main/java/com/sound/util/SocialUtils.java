package com.sound.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SocialUtils {

  public static final double DEFAULT_SOCIAL_POWER = 0.4d;
  public static final long FIRST_CLASS_WEIGHT = 1000;
  public static final int FOLLOW_MIN_BORDER = 3;

  public static <T> List<T> combineLogicAndSocial(Map<T, Integer> logicSeq,
      Map<T, Integer> socialSeq, double socialPower) {

    Map<T, Double> tmp = new HashMap<T, Double>();

    for (T t : logicSeq.keySet()) {
      tmp.put(t, logicSeq.get(t) * (1 - socialPower) + socialSeq.get(t) * socialPower);
    }

    return toSeqList(sortMapByValue(tmp, false));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T, K extends Comparable> List<Entry<T, K>> sortMapByValue(Map<T, K> inputMap,
      final boolean asc) {
    List<Entry<T, K>> toSort = new ArrayList<Entry<T, K>>(inputMap.entrySet());

    Collections.sort(toSort, new Comparator<Entry<T, K>>() {
      public int compare(Entry<T, K> o1, Entry<T, K> o2) {
        if (asc)
          return o1.getValue().compareTo(o2.getValue());
        else
          return -o1.getValue().compareTo(o2.getValue());
      }
    });

    return toSort;
  }

  public static <T> Map<T, Integer> toSeqMap(List<Entry<T, Long>> sortMapByValue) {

    Map<T, Integer> result = new HashMap<T, Integer>();

    for (int i = 0; i < sortMapByValue.size(); i++) {
      result.put(sortMapByValue.get(i).getKey(), i);
    }
    return result;
  }

  public static <T, K> List<T> toSeqList(List<Entry<T, K>> sortMapByValue) {

    List<T> result = new ArrayList<T>();

    for (int i = 0; i < sortMapByValue.size(); i++) {
      result.add(sortMapByValue.get(i).getKey());
    }
    return result;
  }

  public static <T> List<T> sliceList(List<T> allResult, Integer pageNum, Integer pageSize) {
    int offset = (pageNum - 1) * pageSize;
    int resultSize = allResult.size();
    if (resultSize > offset) {
      int left = resultSize - offset;
      if (left > pageSize) {
        return allResult.subList(offset, offset + pageNum - 1);
      } else {
        return allResult.subList(offset, resultSize - 1);
      }
    } else {
      return new ArrayList<T>();
    }
  }

}
