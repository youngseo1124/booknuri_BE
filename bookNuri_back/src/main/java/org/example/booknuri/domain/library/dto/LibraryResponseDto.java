package org.example.booknuri.domain.library.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//백 프론트 응답,요청dto+ db구축 api dto 겸용

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryResponseDto {
    private String libCode;
    private String libName;
    private String fullAddress;
    private String tel;
    private String fax;
    private String homepage;
    private String closed;
    private String operatingTime;
    private Double latitude;
    private Double longitude;
    private Integer bookCount;

    private String si;
    private String gu;
}
