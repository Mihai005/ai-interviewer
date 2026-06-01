package com.example.interviewer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryData {
    private String overview;
    private String sentiment;
    private Double sentimentScore;
    private List<String> themes;
    private List<String> keyPoints;
    private List<String> keywords;
}
