package com.example.carmmunity_capstone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class Main {
    static class Node implements Comparable<Node> {
        int vertex, weight;

        public Node(int vertex, int weight) {
            this.vertex = vertex;
            this.weight = weight;
        }

        @Override
        public int compareTo(Node o) {
            return this.weight - o.weight;
        }
    }


    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // 입력
        int N = Integer.parseInt(br.readLine());    // 정점 개수 N
        String[] friends = br.readLine().split(" ");    // 친구들 위치 입력 값
        int[] friendPos = new int[3];   // 친구들 위치
        for (int i = 0; i < 3; i++) {
            friendPos[i] = Integer.parseInt(friends[i]);
        }
        int M = Integer.parseInt(br.readLine());    // 간선 개수 M
        ArrayList<ArrayList<Node>> adjList = new ArrayList<>(); // 인접 행렬

        // 인접 행렬 초기화
        for (int i = 0; i <= N; i++) adjList.add(new ArrayList<>());

        for (int i = 0; i < M; i++) {
            String[] connection = br.readLine().split(" ");
            int v1 = Integer.parseInt(connection[0]);
            int v2 = Integer.parseInt(connection[1]);
            int w = Integer.parseInt(connection[2]);

            // 양방향 간선
            adjList.get(v1).add(new Node(v2, w));
            adjList.get(v2).add(new Node(v1, w));
        }

        int[] resultDist = new int[N + 1];
        Arrays.fill(resultDist, Integer.MAX_VALUE);
        for (int i = 0; i < 3; i++) {
            int[] dist = dijkstra(friendPos[i], adjList, N);
            // 기존의 값과 새로운 값 중 더 작은 값으로 갱신
            for (int j = 1; j <= N; j++) {
                resultDist[j] = Integer.min(dist[j], resultDist[j]);
            }
        }

        int max = Integer.MIN_VALUE;
        int result = 1;
        for (int i = 1; i <= N; i++) {
            if (resultDist[i] != Integer.MAX_VALUE && resultDist[i] > max) {
                max = resultDist[i];
                result = i;
            }
        }

        System.out.print(result);
    }


    public static int[] dijkstra(int start, ArrayList<ArrayList<Node>> adjList, int N) {
        // dist 배열 초기화
        int[] dist = new int[N + 1];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;

        // pq 초기화
        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(start, 0));


        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int currVertex = curr.vertex;
            int currWeight = curr.weight;


            // 최적의 경로가 아님
            if (currWeight > dist[currVertex]) continue;


            // 인접 노드 방문
            for (Node neighbor : adjList.get(currVertex)) {
                int next = neighbor.vertex;
                int weight = neighbor.weight;

                if (dist[next] > currWeight + weight) {
                    dist[next] = currWeight + weight;
                    pq.add(new Node(next, dist[next]));
                }
            }
        }

        return dist;
    }
}










