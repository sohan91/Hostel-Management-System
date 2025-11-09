package com.example.HostelManagement.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoomWithHostlersDto {
    private Integer roomId;
    private String roomNumber;
    private Integer floorNumber;
    private String roomStatus;
    private Integer currentOccupancy;
    private Integer sharingTypeId;
    private String sharingTypeName;
    private Integer sharingCapacity;
    private Integer availableBeds;
    private List<HostlerDto> hostlers;
}