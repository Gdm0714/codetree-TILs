import java.io.*;
import java.util.*;

public class Main {
    // 상수 정의
    static final int MAX = 20 + 5;

    // 평면 정의
    static final int EAST = 0;
    static final int WEST = 1;
    static final int SOUTH = 2;
    static final int NORTH = 3;
    static final int TOP = 4;
    static final int BOTTOM = 5;

    // 맵 요소 정의
    static final int EMPTY = 0;
    static final int WALL = 1;
    static final int TIME_MACHINE = 2;
    static final int CUBE = 3;
    static final int EXIT = 4;

    // 방향 정의
    static final int RIGHT = 0;
    static final int LEFT = 1;
    static final int DOWN = 2;
    static final int UP = 3;

    static int T;

    static int N, M, F;
    static int[][][] MAP = new int[6][MAX][MAX];
    static int[][][] visit = new int[6][MAX][MAX];

    static int[][][] TIME_WALL = new int[6][MAX][MAX];

    // 2차원 좌표 클래스
    static class RC {
        int r;
        int c;

        RC(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    static RC start, end;

    // 3차원 좌표 클래스 (평면 + 2차원 좌표)
    static class PRC {
        int p; // plane
        int r;
        int c;

        PRC(int p, int r, int c) {
            this.p = p;
            this.r = r;
            this.c = c;
        }
    }

    static PRC[][] queue = new PRC[MAX * MAX * 6][1];
    static int rp, wp;

    static PRC[][][][] next = new PRC[6][MAX][MAX][4];

    // 시간 이상 현상 정보 클래스
    static class TIME_INFO {
        int p;
        int r;
        int c;
        int d;
        int v;

        TIME_INFO(int p, int r, int c, int d, int v) {
            this.p = p;
            this.r = r;
            this.c = c;
            this.d = d;
            this.v = v;
        }
    }

    static TIME_INFO[] timeInfo = new TIME_INFO[10 + 3];

    // →, ←, ↓, ↑ 방향 배열
    static int[] dr = {0, 0, 1, -1};
    static int[] dc = {1, -1, 0, 0};

    static void input() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        F = Integer.parseInt(st.nextToken());

        // 초기화
        for (int i = 0; i < 6; i++)
            for (int r = 0; r < MAX; r++)
                for (int c = 0; c < MAX; c++)
                    MAP[i][r][c] = visit[i][r][c] = TIME_WALL[i][r][c] = 0;

        // 미지의 공간 평면도 입력
        for (int r = 1; r <= N; r++) {
            st = new StringTokenizer(br.readLine());
            for (int c = 1; c <= N; c++) {
                MAP[BOTTOM][r][c] = Integer.parseInt(st.nextToken());

                if (MAP[BOTTOM][r][c] == EXIT) {
                    end = new RC(r, c);
                }
            }
        }

        // 시간의 벽 각 면 입력
        for (int i = 0; i < 5; i++) {
            for (int r = 1; r <= M; r++) {
                st = new StringTokenizer(br.readLine());
                for (int c = 1; c <= M; c++) {
                    MAP[i][r][c] = Integer.parseInt(st.nextToken());

                    if (i == TOP && MAP[TOP][r][c] == TIME_MACHINE) {
                        start = new RC(r, c);
                    }
                }
            }
        }

        // 시간 이상 현상 정보 입력
        for (int f = 0; f < F; f++) {
            st = new StringTokenizer(br.readLine());
            int r = Integer.parseInt(st.nextToken());
            int c = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());
            int v = Integer.parseInt(st.nextToken());

            timeInfo[f] = new TIME_INFO(BOTTOM, r + 1, c + 1, d, v);
        }
    }

    static void printMap(int[][] map, int size) {
        for (int r = 1; r <= size; r++) {
            for (int c = 1; c <= size; c++) {
                System.out.print(map[r][c] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    static void printMapAll(int[][][] map) {
        System.out.println("BOTTOM");
        printMap(map[BOTTOM], N);

        System.out.println("EAST");
        printMap(map[EAST], M);

        System.out.println("WEST");
        printMap(map[WEST], M);

        System.out.println("SOUTH");
        printMap(map[SOUTH], M);

        System.out.println("NORTH");
        printMap(map[NORTH], M);

        System.out.println("TOP");
        printMap(map[TOP], M);
    }

    static void preprocess() {
        for (int i = 0; i < 6; i++)
            for (int r = 0; r < MAX; r++)
                for (int c = 0; c < MAX; c++)
                    for (int d = 0; d < 4; d++)
                        next[i][r][c][d] = new PRC(0, 0, 0);

        for (int i = 1; i <= M; i++) {
            // TOP (1, i)에서 ↑ 가면 NORTH (1, M + 1 - i)
            next[TOP][1][i][UP] = new PRC(NORTH, 1, M + 1 - i);

            // NORTH (1, i)에서 ↑ 가면 TOP (1, M + 1 - i)
            next[NORTH][1][i][UP] = new PRC(TOP, 1, M + 1 - i);

            // ------------------------------------- // 

            // TOP (M, i)에서 ↓ 가면 SOUTH (1, i)
            next[TOP][M][i][DOWN] = new PRC(SOUTH, 1, i);

            // SOUTH (1, i)에서 ↑ 가면 TOP (M, i)
            next[SOUTH][1][i][UP] = new PRC(TOP, M, i);

            // ------------------------------------- // 

            // TOP (i, M)에서 → 가면 EAST (1, M + 1 - i)
            next[TOP][i][M][RIGHT] = new PRC(EAST, 1, M + 1 - i);

            // EAST (1, i)에서 ↑ 가면 TOP (M + 1 - i, M)
            next[EAST][1][i][UP] = new PRC(TOP, M + 1 - i, M);

            // ------------------------------------- // 

            // TOP (i, 1)에서 ← 가면 WEST (1, i)
            next[TOP][i][1][LEFT] = new PRC(WEST, 1, i);
            
            // WEST (1, i)에서 ↑ 가면 TOP (i, 1)
            next[WEST][1][i][UP] = new PRC(TOP, i, 1);
        }

        // →
        for (int i = 1; i <= M; i++) {
            // SOUTH (i, M)에서 → 가면 EAST (i, 1)
            next[SOUTH][i][M][RIGHT] = new PRC(EAST, i, 1);

            // EAST (i, M)에서 → 가면 NORTH (i, 1)
            next[EAST][i][M][RIGHT] = new PRC(NORTH, i, 1);

            // NORTH (i, M)에서 → 가면 WEST (i, 1)
            next[NORTH][i][M][RIGHT] = new PRC(WEST, i, 1);

            // WEST (i, M)에서 → 가면 SOUTH (i, 1)
            next[WEST][i][M][RIGHT] = new PRC(SOUTH, i, 1);
        }

        // ←
        for (int i = 1; i <= M; i++) {
            // SOUTH (i, 1)에서 ← 가면 WEST (i, M)
            next[SOUTH][i][1][LEFT] = new PRC(WEST, i, M);

            // WEST (i, 1)에서 ← 가면 NORTH (i, M)
            next[WEST][i][1][LEFT] = new PRC(NORTH, i, M);

            // NORTH (i, 1)에서 ← 가면 EAST (i, M)
            next[NORTH][i][1][LEFT] = new PRC(EAST, i, M);

            // EAST (i, 1)에서 ← 가면 SOUTH (i, M)
            next[EAST][i][1][LEFT] = new PRC(SOUTH, i, M);
        }
        
        int index;

        // BOTTOM, WEST    
        index = 1;
        for (int r = 1; r <= N; r++) {
            for (int c = 1; c <= N; c++) {
                if (MAP[BOTTOM][r][c] == CUBE) {
                    // BOTTOM → WEST
                    next[BOTTOM][r][c - 1][RIGHT] = new PRC(WEST, M, index);

                    // BOTTOM ← WEST = (↓)
                    next[WEST][M][index][DOWN] = new PRC(BOTTOM, r, c - 1);

                    index++;
                    break;
                }
            }
        }

        // BOTTOM, EAST
        index = M;
        for (int r = 1; r <= N; r++) {
            for (int c = N; c >= 1; c--) {            
                if (MAP[BOTTOM][r][c] == CUBE) {
                    // EAST ← BOTTOM
                    next[BOTTOM][r][c + 1][LEFT] = new PRC(EAST, M, index);

                    // EAST → BOTTOM = (↓)
                    next[EAST][M][index][DOWN] = new PRC(BOTTOM, r, c + 1);

                    index--;
                    break;
                }
            }
        }

        // BOTTOM, NORTH
        index = M;
        for (int c = 1; c <= N; c++) {
            for (int r = 1; r <= N; r++) {
                if (MAP[BOTTOM][r][c] == CUBE) {
                    // BOTTOM ↓ NORTH = ↓
                    next[BOTTOM][r - 1][c][DOWN] = new PRC(NORTH, M, index);

                    // BOTTOM ↑ NORTH = ↓
                    next[NORTH][M][index][DOWN] = new PRC(BOTTOM, r - 1, c);

                    index--;
                    break;
                }
            }
        }

        // BOTTOM, SOUTH
        index = 1;
        for (int c = 1; c <= N; c++) {
            for (int r = N; r >= 1; r--) {
                if (MAP[BOTTOM][r][c] == CUBE) {
                    // SOUTH ↑ BOTTOM = ↑
                    next[BOTTOM][r + 1][c][UP] = new PRC(SOUTH, M, index);

                    // SOUTH ↓ BOTTOM = ↓
                    next[SOUTH][M][index][DOWN] = new PRC(BOTTOM, r + 1, c);

                    index++;
                    break;
                }
            }
        }
    }

    static void makeTime() {
        for (int f = 0; f < F; f++) {
            int p, r, c, d, v;
            
            p = timeInfo[f].p;
            r = timeInfo[f].r;
            c = timeInfo[f].c;
            d = timeInfo[f].d;
            v = timeInfo[f].v;
        
            TIME_WALL[p][r][c] = 1;

            while (true) {            
                int np, nr, nc;

                np = p;
                nr = r + dr[d];
                nc = c + dc[d];

                if (p == BOTTOM && MAP[BOTTOM][nr][nc] == CUBE) {
                    np = next[BOTTOM][r][c][d].p;
                    nr = next[BOTTOM][r][c][d].r;
                    nc = next[BOTTOM][r][c][d].c;
                } else if ((p != BOTTOM) && (nr < 1 || nc < 1 || nr > M || nc > M)) {
                    np = next[p][r][c][d].p;
                    nr = next[p][r][c][d].r;
                    nc = next[p][r][c][d].c;                
                }

                if ((np == BOTTOM) && (nr < 1 || nc < 1 || nr > N || nc > N)) break;
                if (MAP[np][nr][nc] == WALL) break;
                if (nr == end.r && nc == end.c) break;

                TIME_WALL[np][nr][nc] = TIME_WALL[p][r][c] + v;

                p = np;
                r = nr;
                c = nc;
            }
        }
    }

    static int BFS(int r, int c) {
        rp = wp = 0;

        visit[TOP][r][c] = 1;

        queue[wp] = new PRC[1];
        queue[wp][0] = new PRC(TOP, r, c);
        wp++;

        while (rp < wp) {
            PRC out = queue[rp][0];
            rp++;

            if (out.p == BOTTOM && out.r == end.r && out.c == end.c)
                return visit[BOTTOM][out.r][out.c] - 1;

            for (int i = 0; i < 4; i++) {
                int np, nr, nc;

                np = out.p;
                nr = out.r + dr[i];
                nc = out.c + dc[i];

                if (out.p == BOTTOM && MAP[BOTTOM][nr][nc] == CUBE) {
                    np = next[BOTTOM][out.r][out.c][i].p;
                    nr = next[BOTTOM][out.r][out.c][i].r;
                    nc = next[BOTTOM][out.r][out.c][i].c;
                } else if ((out.p != BOTTOM) && (nr < 1 || nc < 1 || nr > M || nc > M)) {
                    np = next[out.p][out.r][out.c][i].p;
                    nr = next[out.p][out.r][out.c][i].r;
                    nc = next[out.p][out.r][out.c][i].c;
                }

                if ((np == BOTTOM) && (nr < 1 || nc < 1 || nr > N || nc > N)) continue;          

                if (MAP[np][nr][nc] == WALL || visit[np][nr][nc] != 0) continue;
                
                if (TIME_WALL[np][nr][nc] != 0 
                    && visit[out.p][out.r][out.c] + 1 >= TIME_WALL[np][nr][nc]) continue;
                
                queue[wp] = new PRC[1];
                queue[wp][0] = new PRC(np, nr, nc);
                wp++;

                visit[np][nr][nc] = visit[out.p][out.r][out.c] + 1;
            }
        }

        return -1;
    }

    public static void main(String[] args) throws IOException {
        T = 1;
        for (int tc = 1; tc <= T; tc++) {
            input();
            preprocess();
            makeTime();
            System.out.println(BFS(start.r, start.c));
        }
    }
}