package com.modsen.passengerservice.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;
import java.math.BigDecimal;

@Document(collection = "bank-cards")
@Data
@Accessors(chain = true)
public class BankCard implements Serializable {

  private static final long serialVersionUID = -5846079447491163597L;

  @Id
  private String id;
  private ObjectId passengerId;
  @Indexed(unique = true)
  private String cardNumber;
  private BigDecimal balance;
}
