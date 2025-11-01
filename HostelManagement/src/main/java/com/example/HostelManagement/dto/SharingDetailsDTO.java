package com.example.HostelManagement.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SharingDetailsDTO {

    private Integer sharingCapacity;
   private BigDecimal sharingFee;
   private String success;

   public SharingDetailsDTO(Integer sharingCapacity,BigDecimal sharingFee,String success)
   {
    this.sharingCapacity = sharingCapacity;
    this.sharingFee = sharingFee;
    this.success = success;
   }
   
}
