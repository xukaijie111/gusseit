package com.guseeit.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GuessRequest {

    @NotNull
    private Long imageId;

    @NotNull
    private Integer dynastyId;

    @NotNull
    @Min(-90)
    @Max(90)
    private Double latitude;

    @NotNull
    @Min(-180)
    @Max(180)
    private Double longitude;

    private String token;

    public Long getImageId() { return imageId; }
    public void setImageId(Long imageId) { this.imageId = imageId; }
    public Integer getDynastyId() { return dynastyId; }
    public void setDynastyId(Integer dynastyId) { this.dynastyId = dynastyId; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
