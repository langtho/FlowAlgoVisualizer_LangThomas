package main.graphStruct;

public class Edge {

    public int dest_node;
    public int source_node;
    public int capacity;
    public int flow;
    public int cost;
    public int reverse;
    public boolean isExploring = false;
    public boolean isPath = false;
    public boolean isMinCut=false;
    public int reducedCost = 0;

    public Edge(int dn,int sn, int cap,int cst, int rev){
        dest_node=dn;
        capacity=cap;
        flow=0;
        cost=cst;
        reverse=rev;
        source_node=sn;
    }

    public String printEdge(){
        return flow+"/"+capacity+"- c:"+cost+" -> "+dest_node;
    }

}
