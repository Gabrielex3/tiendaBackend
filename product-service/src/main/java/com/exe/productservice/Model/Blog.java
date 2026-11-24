package com.exe.productservice.Model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity @Table(name="blogs") @Data
public class Blog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=255) private String title;
 @Column(nullable=false,length=1000) private String summary;
 @Column(nullable=false,length=2000) private String image;
 @Column(nullable=false,columnDefinition="TEXT") private String content;
 @Column(nullable=false) private LocalDate date;
}
