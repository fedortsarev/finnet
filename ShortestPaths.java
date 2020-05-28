import java.util.*;
import java.io.*;

public class ShortestPaths {
    public ShortestPaths() {

    }
    public static void main(String[] args) throws IOException {
      new ShortestPaths().solve();
    }

    int N_EDGES = 4685644;
    int N_VERTICES = 1534749;

    public int parseCompanyType(String t) {
      if ("Limited".equals(t)) {
        return 0;
      } else if ("Unknown".equals(t)) {
        return 1;
      } else if ("Actionary".equals(t)) {
        return 2;
      } else if ("NonComm".equals(t)) {
        return 3;
      } else if ("Individual".equals(t)) {
        return 4;
      } else if ("Agri".equals(t)) {
        return 5;
      } else {
        return -1;
      }
    }

    public double mainOkvedSimilarity(String o1, String o2) {
      int n = Math.min(o1.length(), o2.length());
      int i = 0;
      while ((i < n) && (o1.charAt(i) == o2.charAt(i))) {
        i++;
      }
      return (1.0 * i) / n;
    }

    public double companyTypeSimilarity(int ct1, int ct2) {
      if (ct1 == ct2) {
        return 1.0;
      } else {
        return 0.0;
      }
    }

    public double regionCodeSimilarity(int r1, int r2) {
      return Math.abs(r1 - r2) / 100.0;
    }

    public ArrayList<Double> rank(ArrayList<Double> x) {
      int n = x.size();
      ArrayList<Double> res = new ArrayList<Double>();
      for (int i = 0; i < n; i++) {
        double cur = x.get(i);
        int cnt = 0;
        for (int j = 0; j < n; j++) {
          double t = x.get(j);
          if (t <= cur) {
            cnt++;
          }
        }
        res.add((1.0 * cnt - 1) / (n - 1));
      }
      return res;
    }

    public void solve() throws IOException {
      // id_1,id_2,value,n_transactions
      Scanner in = new Scanner(new File("data/edges.csv"));
      in.useLocale(Locale.US);
      System.err.println(in.nextLine());

      int[] id_1 = new int[N_EDGES];
      int[] id_2 = new int[N_EDGES];
      double[] value = new double[N_EDGES];
      double[] n_transactions = new double[N_EDGES];

      for (int i = 0; i < N_EDGES; i++) {
        String[] t = in.nextLine().split(",");
        id_1[i] = Integer.parseInt(t[0]);
        id_2[i] = Integer.parseInt(t[1]);
        value[i] = Double.parseDouble(t[2]);
        n_transactions[i] = Double.parseDouble(t[3]);
      }

      in.close();

      // id,main_okved,region_code,company_type
      in = new Scanner(new File("data/vertices.csv"));
      System.err.println(in.nextLine());

      int[] id = new int[N_VERTICES];
      String[] main_okved = new String[N_VERTICES];
      int[] region_code = new int[N_VERTICES];
      int[] company_type = new int[N_VERTICES];
      // 'Limited' 0
      //, 'Unknown' 1
      // , 'Actionary' 2
      // , 'NonComm' 3
      // , 'Individual' 4
      // , 'Agri' 5

      for (int i = 0; i < N_VERTICES; i++) {
        String[] t = in.nextLine().split(",");
        id[i] = Integer.parseInt(t[0]);
        main_okved[i] = t[1];
        region_code[i] = Integer.parseInt(t[2]);
        company_type[i] = parseCompanyType(t[3]);
      }
      in.close();

      in = new Scanner(new File("data/ids.csv"));
      System.err.println(in.nextLine());
      int[] ids = new int[100];
      for (int i = 0; i < 100; i++) {
        ids[i] = in.nextInt();
      }
      in.close();

      int[] degree = new int[N_VERTICES + 1];
      for (int i = 0; i < N_EDGES; i++) {
          degree[id_1[i]]++;
          degree[id_2[i]]++;
      }


      for (int curId : ids) {
          System.out.println(curId);

          int[][] edges = new int[N_VERTICES + 1][];
          int[] p = new int[N_VERTICES + 1];
          for (int i = 1; i <= N_VERTICES; i++) {
              edges[i] = new int[degree[i]];
          }

          for (int i = 0; i < N_EDGES; i++) {
              int u = id_1[i];
              int v = id_2[i];
              edges[u][p[u]++] = v;
              edges[v][p[v]++] = u;
          }

          boolean[] w = new boolean[N_VERTICES + 1];
          int[] dist = new int[N_VERTICES + 1];
          Arrays.fill(dist, (int) 1e9);
          int[] q = new int[N_VERTICES];
          int head = 0;
          int tail = 0;

          q[tail++] = curId;
          dist[curId] = 0;
          w[curId] = true;
          while (head < tail) {
              int u = q[head++];
              for (int v : edges[u]) {
                  if (!w[v]) {
                      w[v] = true;
                      dist[v] = dist[u] + 1;
                      q[tail++] = v;
                  }
              }
          }

          boolean[] w2 = new boolean[N_VERTICES + 1];
          int[] dist2 = new int[N_VERTICES + 1];

          int cntProcessed = 0;
          for (int dest : edges[curId]) {
              cntProcessed++;
              if (cntProcessed % 10 == 0) {
                  System.out.println(cntProcessed);
              }
              Arrays.fill(dist2, (int) 1e9);
              Arrays.fill(w2, false);
              head = 0;
              tail = 0;
              q[tail++] = curId;
              dist2[curId] = 0;
              w2[curId] = true;
              while (head < tail) {
                  if (w2[dest]) {
                      break;
                  }
                  int u = q[head++];
                  for (int v : edges[u]) {
                      if (u == curId && v == dest) {
                          continue;
                      }
                      if (u == dest && v == curId) {
                          continue;
                      }
                      if (!w2[v]) {
                          w2[v] = true;
                          dist2[v] = dist2[u] + 1;
                          q[tail++] = v;
                      }
                  }
              }
              dist[dest] = dist2[dest];
          }

          PrintWriter out = new PrintWriter(new FileWriter(new File("data/shortest_paths_" + curId + ".csv")));
          out.println("id_1,id_2,shortest_path");

          for (int i = 1; i <= N_VERTICES; i++) {
              out.println(curId + "," + i + "," + dist[i]);
          }
          out.close();


      }


    }

}
