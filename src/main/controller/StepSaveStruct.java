package main.controller;

import main.graphStruct.Graph;

public record StepSaveStruct (
    Graph.GraphState graphFlows,
    String title,
    String subtitle,
    int maxFlow,
    String bottleneckValue,
    String bottleneckEdge,
    String pathStr,
    boolean isMajor,
    boolean isMinCutAvailable
){}
