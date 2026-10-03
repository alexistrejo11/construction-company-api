package io.github.alexisTrejo11.construction.company.modules.project.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiteLocation {
  @Column(name = "address_line")
  private String addressLine;

  private String city;
  private String state;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  private String country;

  @Column(precision = 10, scale = 8)
  private BigDecimal latitude;

  @Column(precision = 11, scale = 8)
  private BigDecimal longitude;
}
