package com.example.HostelManagement.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.List;

import com.example.HostelManagement.dao.RoomCardDetailsFetch;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostlerListResponseDto {
    private String status;
    private String message;
    private RoomCardDetailsFetch roomDetails;
    private RoomInfoDto roomInfo;
    private List<HostlerDto> hostlers;
    private Integer totalHostlers;
    private Long timestamp;
    
    // Constructors
    public HostlerListResponseDto(RoomCardDetailsFetch roomDetails, RoomInfoDto roomInfo, List<HostlerDto> hostlers) {
        this.status = "success";
        this.message = "Hostler list fetched successfully";
        this.roomDetails = roomDetails;
        this.roomInfo = roomInfo;
        this.hostlers = hostlers;
        this.totalHostlers = hostlers != null ? hostlers.size() : 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    public HostlerListResponseDto(RoomCardDetailsFetch roomDetails, String message) {
        this.status = "success";
        this.message = message;
        this.roomDetails = roomDetails;
        this.hostlers = List.of();
        this.totalHostlers = 0;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Static factory methods for common responses
    public static HostlerListResponseDto success(RoomCardDetailsFetch roomDetails, RoomInfoDto roomInfo, List<HostlerDto> hostlers) {
        return HostlerListResponseDto.builder()
            .status("success")
            .message("Hostler list fetched successfully")
            .roomDetails(roomDetails)
            .roomInfo(roomInfo)
            .hostlers(hostlers)
            .totalHostlers(hostlers != null ? hostlers.size() : 0)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static HostlerListResponseDto error(RoomCardDetailsFetch roomDetails, String errorMessage) {
        return HostlerListResponseDto.builder()
            .status("error")
            .message(errorMessage)
            .roomDetails(roomDetails)
            .hostlers(List.of())
            .totalHostlers(0)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    // CORRECTED: Use the roomDetails parameter directly
    public static HostlerListResponseDto notFound(RoomCardDetailsFetch roomDetails) {
        return HostlerListResponseDto.builder()
            .status("success")
            .message("Room not found or no hostlers allocated")
            .roomDetails(roomDetails)
            .hostlers(List.of())
            .totalHostlers(0)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    // ADDITIONAL: Method that accepts just roomId for convenience
    public static HostlerListResponseDto notFound(Integer roomId) {
        RoomCardDetailsFetch roomDetails = new RoomCardDetailsFetch(roomId);
        return HostlerListResponseDto.builder()
            .status("success")
            .message("Room not found or no hostlers allocated")
            .roomDetails(roomDetails)
            .hostlers(List.of())
            .totalHostlers(0)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}