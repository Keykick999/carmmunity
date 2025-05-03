package com.example.carmmunity_capstone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;


public class Main {
    static class Node implements Comparable<Node> {
        int vertex, weight;
        boolean visited;

        public Node(int vertex, int weight, boolean visited) {
            this.vertex = vertex;
            this.weight = weight;
            this.visited = visited;
        }

        @Override
        public int compareTo(Node o) {
            return this.weight - o.weight;
        }
    }


    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder buffer = new StringBuilder();


        // 입력
        int T = Integer.parseInt(br.readLine());


        for (int p = 0; p < T; p++) {
            String[] input1 = br.readLine().split(" ");
            int n = Integer.parseInt(input1[0]);    // 정점 개수
            int m = Integer.parseInt(input1[1]);    // 간선 개수
            int t = Integer.parseInt(input1[2]);    // 목적지 후보 개수
            String[] input2 = br.readLine().split(" ");
            int start = Integer.parseInt(input2[0]);    // 출발지
            int g = Integer.parseInt(input2[1]);    // 필수 방문 간선 출발점
            int h = Integer.parseInt(input2[2]);    // 필수 방문 간선 도착점

            // 간선 초기화
            ArrayList<ArrayList<Node>> adjList = new ArrayList<>();
            for (int i = 0; i <= n; i++) adjList.add(new ArrayList<>());


            // m개의 간선 입력
            for (int i = 0; i < m; i++) {
                String[] connection = br.readLine().split(" ");
                int v1 = Integer.parseInt(connection[0]);
                int v2 = Integer.parseInt(connection[1]);
                int w = Integer.parseInt(connection[2]);
                adjList.get(v1).add(new Node(v2,w,false));
                adjList.get(v2).add(new Node(v1,w,false));
            }


            // t개의 목적지 후보들 입력
            HashSet<Integer> possibleObjectives = new HashSet<>();
            for (int i = 0; i < t; i++) possibleObjectives.add(Integer.parseInt(br.readLine()));


            // 다익스트라
            ArrayList<Integer> resultList = dijkstra(adjList, start, possibleObjectives, g, h);
            for (Integer i : resultList) {
                buffer.append(i + " ");
            }
            buffer.append("\n");
        }

        System.out.print(buffer);
    }


    // 다익스트라
    public static ArrayList<Integer> dijkstra(ArrayList<ArrayList<Node>> adjList, int start, HashSet<Integer> possibleObjectives, int g, int h) {
        // dist 배열 초기화
        int[][] dist = new int[adjList.size()][2];
        for (int i = 1; i < dist.length; i++) {
            dist[i][0] = Integer.MAX_VALUE; // g->h 방문x시의 가중치
            dist[i][1] = Integer.MAX_VALUE; // g->h 방문시의 가중치
        }
        dist[start][0] = 0;
        dist[start][1] = 0;

        // pq 초기화
        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(start, 0, false));


        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int currVertex = curr.vertex;
            int currWeight = curr.weight;
            boolean visited = curr.visited;


            // 최단 경로 아니면 continue
            if (!visited && dist[currVertex][0] < currWeight) continue;
            if (visited && dist[currVertex][1] < currWeight) continue;


            // 이웃 노드 방문
            for (Node neighbor : adjList.get(currVertex)) {
                int next = neighbor.vertex;
                int weight = neighbor.weight;
                boolean nextVisited = visited;

                // 조건 만족하는 경우(g -> h or h -> g) visited는 true
                if ((currVertex == g && next == h) || (currVertex == h && next == g)) {
                    nextVisited = true;
                }

                // 가중치 낮거나 같은데 조건 만족하는 경우만 방문
                if (!nextVisited && (currWeight + weight < dist[next][0])) {
                    dist[next][0] = weight + currWeight;
                    pq.add(new Node(next, dist[next][0], nextVisited));
                }
                else if (nextVisited && (currWeight + weight < dist[next][1])) {
                    dist[next][1] = weight + currWeight;
                    pq.add(new Node(next, dist[next][1], nextVisited));
                }
            }
        }


        // 목적지들중 불가능한 목적지(방문 여부 0인)는 제외
        ArrayList<Integer> resultList = new ArrayList<>();
        for (Integer possibleObjective : possibleObjectives) {
            if (dist[possibleObjective][1] != Integer.MAX_VALUE && dist[possibleObjective][1] <= dist[possibleObjective][0]) {
                resultList.add(possibleObjective);
            }
        }
        Collections.sort(resultList);
        return resultList;
    }
}