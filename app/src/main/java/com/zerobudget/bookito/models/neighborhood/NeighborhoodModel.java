package com.zerobudget.bookito.models.neighborhood;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "comune",
        "quartieri"
})
public class NeighborhoodModel {
   @JsonProperty("comune")
   private String comune;

   @JsonProperty("quartieri")
   private List<String> quartieri;

   @Override
   public String toString() {
      return "NeighborhoodModel{" +
              "comune='" + comune + '\'' +
              ", quartieri=" + quartieri +
              '}';
   }
}
