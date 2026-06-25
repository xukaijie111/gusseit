package com.guseeit.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

public class GenerateRequest {

    @NotBlank
    private String dynasty;

    @Min(1)
    @Max(20)
    private int count;

    public String getDynasty() { return dynasty; }
    public void setDynasty(String dynasty) { this.dynasty = dynasty; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
}
