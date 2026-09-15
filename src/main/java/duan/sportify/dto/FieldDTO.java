package duan.sportify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldDTO {
    private Integer fieldid;
    private String namefield;
    private Double price;
    private String image;
    private String address;
}
