package com.example.carmmunity_capstone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.PriorityQueue;


public class Main {
    static class Node implements Comparable<Node> {
        int vertex;
        long transferTime, spentTime;

        public Node(int vertex, long transferTime, long spentTime) {
            this.vertex = vertex;
            this.transferTime = transferTime;
            this.spentTime = spentTime;
        }

        @Override
        public int compareTo(Node o) {
            int comp = Long.compare(this.transferTime, o.transferTime);
            if (comp != 0) return comp;
            return Long.compare(this.spentTime, o.spentTime);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        // 입력
        String[] NM = br.readLine().split(" ");
        int N = Integer.parseInt(NM[0]);    // 정점 개수 N
        int objective = Integer.parseInt(NM[1]);    // 도착지 번호


        // N개의 회사 정보
        String[] trainType = new String[N];
        for (int i = 0; i < N; i++) trainType[i] = br.readLine();

        // 인접 리스트 입력
        ArrayList<ArrayList<Node>> adjList = new ArrayList<>();
        for (int i = 0; i < N; i++) adjList.add(new ArrayList<>());
        for (int y = 0; y < N; y++) {
            String[] input = br.readLine().split(" ");
            for (int x = 0; x < N; x++) {
                int weight = Integer.parseInt(input[x]);
                if (weight > 0) {
                    int transferTime = 0;   // 환승 횟수 계산
                    if (!trainType[y].equals(trainType[x])) transferTime = 1;
                    adjList.get(y).add(new Node(x, transferTime, weight));
                }
            }
        }


        // 출발지를 0으로 설정하여 다익스트라 실행
        long[] result = dijkstra(adjList, 0, objective);
        System.out.print(result[0] + " " + result[1]);
    }

    public static long[] dijkstra(ArrayList<ArrayList<Node>> adjList, int start, int objective) {
        // dist 배열 초기화
        int N = adjList.size();
        long[][] dist = new long[N][2]; // (환승횟수, 소요시간)
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < 2; j++) {
                dist[i][j] = Long.MAX_VALUE;
            }
        }
        dist[start][0] = 0;
        dist[start][1] = 0;

        // pq 초기화
        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(start, 0, 0));


        while (!pq.isEmpty()) {
            Node curr = pq.poll();
            int currVertex = curr.vertex;
            long currTransferTime = curr.transferTime;
            long currSpentTime = curr.spentTime;


            // 최적의 경로가 아니면(환승횟수가 더 크거나 혹은 같은데 소요 시간 더 큼)
            if (currTransferTime > dist[currVertex][0]) continue;
            if ((currTransferTime == dist[currVertex][0]) && (currSpentTime > dist[currVertex][1])) continue;


            // 인접 노드 방문
            for (Node neighbor : adjList.get(currVertex)) {
                int next = neighbor.vertex;
                long transferTime = neighbor.transferTime;
                long spentTime = neighbor.spentTime;

                // 환승횟수 적거나 혹은 같은데 소요 시간 적으면 방문
                if (currTransferTime + transferTime < dist[next][0]) {
                    dist[next][0] = currTransferTime + transferTime;
                    dist[next][1] = currSpentTime + spentTime;
                    pq.add(new Node(next, dist[next][0], dist[next][1]));
                }
                else if ((currTransferTime + transferTime == dist[next][0]) && (currSpentTime + spentTime < dist[next][1])) {
                    dist[next][0] = currTransferTime + transferTime;
                    dist[next][1] = currSpentTime + spentTime;
                    pq.add(new Node(next, dist[next][0], dist[next][1]));
                }
            }
        }

        return new long[]{dist[objective][0], dist[objective][1]};
    }
}