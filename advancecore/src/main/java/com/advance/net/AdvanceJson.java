package com.advance.net;

import com.advance.utils.LogUtil;

import org.json.JSONArray;

import java.util.ArrayList;

public class AdvanceJson {


    /**
     * 将并行优先级进行重新组装成二维数组，并进行升序排序
     *
     * @param jsonArray
     * @return
     */
    public static ArrayList<ArrayList<Integer>> convertJsonToGroup(JSONArray jsonArray) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        try {
            if (jsonArray == null) {
                return result;
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONArray one = jsonArray.optJSONArray(i);
                    ArrayList<Integer> item = new ArrayList<>();

                    for (int j = 0; j < one.length(); j++) {
                        int priority = one.optInt(j, -1);
                        item.add(priority);
                    }
                    bubbleSort(item);
                    result.add(item);
                }
                bubbleSortGroup(result);

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        LogUtil.high("[paraGroup] convertJsonToGroup result = " + result.toString());
        return result;
    }


    public static void bubbleSortGroup(ArrayList<ArrayList<Integer>> numbers) {
        ArrayList<Integer> temp;
        int size = numbers.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                if (numbers.get(j).get(0) > numbers.get(j + 1).get(0))  //交换两数位置
                {
                    temp = numbers.get(j);
                    numbers.set(j, numbers.get(j + 1));
                    numbers.set(j + 1, temp);
                }
            }
        }
    }

    public static void bubbleSort(ArrayList<Integer> numbers) {
        int temp;
        int size = numbers.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                if (numbers.get(j) > numbers.get(j + 1))  //交换两数位置
                {
                    temp = numbers.get(j);
                    numbers.set(j, numbers.get(j + 1));
                    numbers.set(j + 1, temp);
                }
            }
        }
    }

    public static ArrayList<Integer> convertJsonArrayToList(JSONArray jsonArray) {
        ArrayList<Integer> result = new ArrayList<>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    int priority = jsonArray.optInt(i);
                    result.add(priority);
                }
                //重排序
                bubbleSort(result);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        LogUtil.high("[bidGroup] convertJsonArrayToList result = " + result.toString());
        return result;
    }


    public static ArrayList<String> convertJsonArrayToArrayList(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        } else {
            ArrayList<String> arrayList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                String s = jsonArray.optString(i);
                if (s != null) {
                    arrayList.add(jsonArray.optString(i));
                }
            }
            return arrayList;
        }
    }
}
