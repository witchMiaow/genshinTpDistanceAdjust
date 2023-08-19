package com.miaow;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author miaow
 */
public class Main {
    public static void main(String[] args) {
        File directory = new File(".");
        File[] fileAry = directory.listFiles();
        if (fileAry == null || fileAry.length == 0) {
            System.out.println("当前目录下文件为空.");
            return;
        }

        List<File> fileList = new ArrayList<>();
        for (File file : fileAry) {
            if (!file.isDirectory()) {
                if (file.getName().contains(".json")) {
                    fileList.add(file);
                }
            }
        }
        if (fileList.isEmpty()) {
            System.out.println("当前目录下文件为空.");
            return;
        }
        statistics(fileList);

        System.out.println("是否需要调整文件位置?");
        Scanner scanner = new Scanner(System.in);
        String isAdjust = scanner.nextLine();
        if ("Y".equalsIgnoreCase(isAdjust)) {
            boolean adjust = true;
            while (adjust) {
                int[] indexAry = input();
                File temp1 = fileList.get(indexAry[0]);
                File temp2 = fileList.get(indexAry[1]);
                JSONObject jsonObject1, jsonObject2;
                try {
                    jsonObject1 = JSONObject.parseObject(readJson(temp1));
                    jsonObject2 = JSONObject.parseObject(readJson(temp2));
                    String filename1 = temp1.getName();
                    String filename2 = temp2.getName();

                    JSONArray temp = jsonObject1.getJSONArray("position");
                    jsonObject1.put("position", jsonObject2.getJSONArray("position"));
                    jsonObject2.put("position", temp);

                    BufferedWriter out = new BufferedWriter(new FileWriter(temp1));
                    out.write(JSONObject.toJSONString(jsonObject1));
                    out.close();
                    out = new BufferedWriter(new FileWriter(temp2));
                    out.write(JSONObject.toJSONString(jsonObject2));
                    out.close();
                    System.out.println(filename1 + " 和 " + filename2 + " 交换数据成功");
                } catch (Exception e) {
                    System.out.println("读取文件异常，系统退出");
                    System.exit(0);
                    return;
                }
                statistics(fileList);
                System.out.println("是否需要调整文件位置?");
                isAdjust = scanner.nextLine();
                adjust = "Y".equalsIgnoreCase(isAdjust);
            }
        } else {
            System.out.println("系统3秒后退出");
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
                System.exit(0);
            } catch (InterruptedException e) {
                System.out.println("系统退出异常," + e.getMessage());
                System.exit(1);
            }
        }
    }

    private static void statistics(List<File> fileList) {
        double x = 0;
        double y = 0;
        int count = 0;
        for (File file : fileList) {
            try {
                JSONObject object = JSONObject.parseObject(readJson(file));
                JSONArray array = object.getJSONArray("position");
                if (x == 0 || y == 0) {
                    x = Double.parseDouble(array.get(0).toString());
                    y = Double.parseDouble(array.get(2).toString());
                } else {
                    double nowX = Double.parseDouble(array.get(0).toString());
                    double nowY = Double.parseDouble(array.get(2).toString());
                    double instance = Math.sqrt(Math.pow(nowX - x, 2) + Math.pow(nowY - y, 2));
                    long flag = Math.round(instance);
                    if (flag <= 150) {
                        count++;
                        String str;
                        if (flag < 10) {
                            str = "0000" + flag;
                        } else if (flag < 100) {
                            str = "000" + flag;
                        } else {
                            str = "00" + flag;
                        }
                        System.out.println("距离:" + str + ", 当前文件名:" + file.getName());
                    }
                    x = nowX;
                    y = nowY;
                }
            } catch (Exception e) {
                System.out.println("读取文件异常:" + e.getMessage());
            }
        }
        System.out.println("共有" + count + "个传送文件点位过近.");
    }

    private static String readJson(File file) throws Exception {
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
        int ch;
        StringBuilder sb = new StringBuilder();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        return sb.toString();
    }

    private static int[] input() {
        int[] res = new int[2];
        Scanner scanner = new Scanner(System.in);
        boolean correct = true;
        int fileIndex1 = -1;
        int fileIndex2 = -1;
        while (correct) {
            System.out.println("请输入需要调整的文件位置1");
            String fileIndexStr1 = scanner.nextLine();
            try {
                fileIndex1 = Integer.parseInt(fileIndexStr1);
                res[0] = fileIndex1;
                correct = false;
            } catch (Exception e) {
                System.out.println("输入格式错误，只能输入数字");
            }
        }
        correct = true;
        while (correct) {
            System.out.println("请输入需要调整的文件位置2");
            String fileIndexStr2 = scanner.nextLine();
            try {
                fileIndex2 = Integer.parseInt(fileIndexStr2);
                if (fileIndex2 == fileIndex1) {
                    System.out.println("输入位置不能相同");
                    continue;
                }
                res[1] = fileIndex2;
                correct = false;
            } catch (Exception e) {
                System.out.println("输入格式错误，只能输入数字");
            }
        }
        if (fileIndex1 == -1 || fileIndex2 == -1) {
            System.out.println("未知异常，系统退出");
            System.exit(0);
        }
        return res;
    }
}