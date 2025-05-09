package com.example.carmmunity_capstone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Main {
    static class Node implements Comparable<Node> {
        int vertex, remainFree;
        long weight;
        HashSet<Integer> visited;


        public Node(int vertex, long weight, int remainFree, HashSet<Integer> visited) {
            this.vertex = vertex;
            this.weight = weight;
            this.remainFree = remainFree;
            this.visited = visited;
        }

        @Override
        public int compareTo(Node o) {
            return Long.compare(this.weight, o.weight);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        // 입력
        String[] input = br.readLine().split(" ");
        int N = Integer.parseInt(input[0]); // 정점 개수(1 ~ N)
        int P = Integer.parseInt(input[1]); // 간선 개수 P
        int K = Integer.parseInt(input[2]); // 공짜 제공 케이블 개수


        // P개의 간선
        ArrayList<ArrayList<Node>> graph = new ArrayList<>();
        for (int i = 0; i <= N; i++) graph.add(new ArrayList<>());


        for (int i = 0; i < P; i++) {
            String[] connection = br.readLine().split(" ");
            int v1 = Integer.parseInt(connection[0]);
            int v2 = Integer.parseInt(connection[1]);
            int w = Integer.parseInt(connection[2]);
            graph.get(v1).add(new Node(v2, w, 0, new HashSet<>()));
            graph.get(v2).add(new Node(v1, w, 0, new HashSet<>()));
        }


        System.out.print(dijkstra(graph, N, K));
    }

    // start는 N, free는 제공해주는 공짜 선 개수
    public static long dijkstra(ArrayList<ArrayList<Node>> graph, int start, int free) {
        // dist 배열 초기화
        int N = graph.size();
        long[] dist = new long[N + 1];
        Arrays.fill(dist, Long.MAX_VALUE);
        dist[start] = 0;

        // pq 초기화
        PriorityQueue<Node> pq = new PriorityQueue<>();
        HashSet<Integer> visited = new HashSet<>();
        visited.add(start);
        pq.add(new Node(start, 0, free, visited));


        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int currVertex = curr.vertex;
            long currWeight = curr.weight;
            int remainFree = curr.remainFree;
            HashSet<Integer> currVisited = curr.visited;


            // 최적의 경로 아님
            if (currWeight > dist[currVertex]) continue;


            // 인접 노드 방문
            for (Node neighbor : graph.get(currVertex)) {
                int next = neighbor.vertex;
                if (currVisited.contains(next)) continue;    // 방문한 곳 다시 안감
                long nextWeight = Long.max(currWeight, neighbor.weight);  // 기존 가중치랑 비교해서 더 큰 값

                // 남은 공짜로 제공 가능한 선 개수 남아있으면 가중치 0
                HashSet<Integer> nextVisited = new HashSet<>(currVisited);
                if (remainFree > 0) {
                    nextWeight = 0; // 가중치 0
                    dist[next] = nextWeight;
                    nextVisited.add(next);
                    pq.add(new Node(next, nextWeight, remainFree - 1, nextVisited));
                }
                else if (remainFree == 0) {
                    dist[next] = nextWeight;
                    nextVisited.add(next);
                    pq.add(new Node(next, nextWeight, 0, nextVisited));
                }
            }
        }


        return (dist[1] == Long.MAX_VALUE) ? -1 : dist[1];
    }
}











