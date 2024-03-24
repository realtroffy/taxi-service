package com.modsen.passengerservice.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.io.Serializable;
import java.util.List;

@Document(collection = "passengers")
@Data
@Accessors(chain = true)
public class Passenger implements Serializable {

  private static final long serialVersionUID = 4603749101621919833L;

  @Id
  private String id;
  @Indexed(unique = true)
  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private Double rating;

  @ReadOnlyProperty
  @DocumentReference(lookup="{'passengerId':?#{#self._id} }")
  private List<BankCard> bankCards;
}
